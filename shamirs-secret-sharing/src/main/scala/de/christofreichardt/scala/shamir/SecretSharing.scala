/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2024, Christof Reichardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.christofreichardt.scala.shamir

import de.christofreichardt.scala.combinations.{LazyBinomialCombinator, MetaCombinator}
import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.scala.utils.{JsonPrettyPrinter, RandomGenerator}
import java.nio.file.Path
import java.security.SecureRandom
import java.util.UUID
import jakarta.json.{Json, JsonArray, JsonObject}
import scala.annotation.tailrec

/**
 * A secret sharing sheme.
 *
 * @constructor Creates a new SecretSharing sheme with shares, threshold, secretBytes and a secure random source.
 *
 * @param shares the number of shares
 * @param threshold the number of shares required for the recovery of the secret bytes
 * @param secretBytes the actual secret
 * @param random the secure random source
 */
class SecretSharing(
                     val shares: Int,
                     val threshold: Int,
                     val secretBytes: IndexedSeq[Byte],
                     val random: SecureRandom)
  extends Tracing {

  /**
   * Creates a new SecretSharing sheme with 6 shares, threshold == 3, the given secretBytes and a default secure random source.
   *
   * @param secretBytes the actual secret
   */
  def this(secretBytes: IndexedSeq[Byte]) = this(6, 3, secretBytes, new SecureRandom)

  /**
   * Creates a new SecretSharing sheme with shares, threshold, secretBytes and a default secure random source.
   *
   * @param shares the number of shares
   * @param threshold the number of shares required for the recovery of the secret bytes
   * @param secretBytes the actual secret
   */
  def this(shares: Int, threshold: Int, secretBytes: IndexedSeq[Byte]) = this(shares, threshold, secretBytes, new SecureRandom)

  /**
   * Creates a new SecretSharing sheme with shares, threshold, secretBytes and a default secure random source. This is convenient when calling from Java.
   *
   * @param shares the number of shares
   * @param threshold the number of shares required for the recovery of the secret bytes
   * @param secretBytes the actual secret
   */
  def this(shares: Int, threshold: Int, secretBytes: Array[Byte]) = this(shares, threshold, secretBytes.toIndexedSeq, new SecureRandom)

  /**
   * Creates a new SecretSharing sheme with shares, threshold, password and a default secure random source.
   *
   * @param shares the number of shares
   * @param threshold the number of shares required for the recovery of the secret bytes
   * @param password the actual secret, will be encoded with UTF-8
   */
  def this(shares: Int, threshold: Int, password: CharSequence) = this(shares, threshold, charSequenceToByteArray(password))

  /** An alias for shares */
  val n: Int = shares
  /** An alias for threshold */
  val k: Int = threshold

  require(n >= 2 && k >= 2, "We need at least two shares, otherwise we wouldn't need shares at all.")
  require(k <= n, "The threshold must be less than or equal to the number of shares.")
  require(secretBytes.length >= 2, "Too few secret bytes.")

  /** The secret encoded as non-negative BigInt */
  val s: BigInt = bytes2BigInt(secretBytes)
  /** Used to compute a LazyList of random BigInt numbers */
  val randomGenerator: RandomGenerator = new RandomGenerator(random)
  /** The prime modulus */
  val prime: BigInt = choosePrime

  require((BigInt(n)*BigInt(n)) <= prime, "Too much shares for given secret.")
  require(s < prime, "The encoded secret must be strictly smaller than the prime modulus.")

  /** A random polynomial in the canonical form used to compute the shares */
  val polynomial: Polynomial = choosePolynomial(k - 1)
  /** The actual shares */
  val sharePoints: IndexedSeq[(BigInt, BigInt)] = computeShares

  require(polynomial.degree == k - 1)
  require(this.sharePoints.map(point => point._1).distinct.length == n, String.format("%d distinct sharepoints are needed: %s", n, this.sharePoints))

  /** The partition id */
  val id: String = UUID.randomUUID().toString
  /** All shares converted into a JSON object */
  lazy val sharePointsAsJson: JsonObject = sharePointsAsJson(sharePoints)
  /**
   * Indicates if cross checks with all possible combinations of shares with a sharepoint count that equals the threshold have successfully produced the secret.
   * This is backed by a potentially very expensive operation.
   */
  lazy val verified: (Boolean, Int) = verifyAll
  /**
   * Ensures that all combinations of shares below the threshold have failed to produce the secret. This is backed by a potentially very expensive operation.
   */
  lazy val falsified: (Boolean, Int) = falsifyAll
  /**
   * Indicates that both [[verified]] and [[falsified]] have produced the expected results
   */
  lazy val certified: CertificationResult = {
    assert(verified._1 && falsified._1, "Generic sharepoint certification failed.")
    CertificationResult(falsified._2, verified._2)
  }

  /**
   * Calculates a random prime p with the property s < p.
   *
   * @return a random prime
   */
  def choosePrime: BigInt = {
    val BIT_OFFSET = 1
    val bits = s.bitLength + BIT_OFFSET
    BigInt(bits, CERTAINTY, random)
  }

  /**
   * Calculates a batch of coefficients needed for the polynomial in the canonical form.
   * @return the random coefficients
   */
  def chooseCanonicalCoefficients: IndexedSeq[BigInt] = {
    val bits = s.bitLength * 2
    randomGenerator.bigIntStream(bits, prime).take(k - 1).toIndexedSeq
  }

  /**
   * Chooses a random polynomial with the given degrre in the canonical form.
   *
   * @param degree the degree of the polynomial
   * @return the random polynomial
   */
  @tailrec
  final def choosePolynomial(degree: Int): Polynomial = {
    val candidate: Polynomial = new Polynomial(chooseCanonicalCoefficients :+ s, prime)
    if (candidate.degree == degree) candidate
    else choosePolynomial(degree)
  }

  /**
   * Computes the required number of random and distinct shares.
   *
   * @return the shares
   */
  def computeShares: IndexedSeq[(BigInt, BigInt)] = {
    val bits = s.bitLength * 2
    randomGenerator.bigIntStream(bits, prime)
      .filterNot(x => x == BigInt(0))
      .distinct
      .take(shares)
      .map(x => (x, polynomial.evaluateAt(x)))
      .toIndexedSeq
  }

  /**
   * Verifies that all valid combinations of shares recover the secret bytes. That is all combinations of shares with a sharepoint count
   * that equals the threshold will be considered.
   *
   * @return indicates the outcome of all possible and valid cross checks
   */
  def verifyAll: (Boolean, Int) = {
    val combinator = new LazyBinomialCombinator(this.n, this.k)
    var count = 0
    val verified = combinator.produceAll
      .map(combination => {
        val indices = combination
        val selectedPoints = indices.map(index => sharePoints(index))
        val merger = SecretMerging(selectedPoints, prime)
        count = count + 1
        merger.secretBytes
      })
      .forall(bytes => bytes == secretBytes)
    (verified, count)
  }

  def falsifyAll: (Boolean, Int) = {
    val metaCombinator = new MetaCombinator(this.shares)
    val solutions = metaCombinator.solutions
    var count = 0
    val falsified = solutions.zipWithIndex
      .tail // skips 'n choose 0' -> {}
      .filter(indexedCombinations => {
        val (_, k) = indexedCombinations
        k < this.threshold
      })
      .forall(indexedCombinations => {
        val (combinations, k) = indexedCombinations
        !combinations.map(combination => {
          val indices = combination
          val selectedPoints = indices.map(index => sharePoints(index))
          val merger = SecretMerging(selectedPoints, this.prime)
          count = count + 1
          merger.secretBytes
        }).contains(this.secretBytes)
      })
    (falsified, count)
  }

  /**
   * Translates the given shares into JSON.
   *
   * @param ps the shares
   * @return the JSON containing the shares
   */
  def sharePointsAsJson(ps: IndexedSeq[(BigInt, BigInt)]): JsonObject = {
    val arrayBuilder = Json.createArrayBuilder()
    ps.foreach(ps => {
      arrayBuilder.add(Json.createObjectBuilder()
        .add("SharePoint", Json.createObjectBuilder()
          .add("x", ps._1.bigInteger)
          .add("y", ps._2.bigInteger)))
    })
    Json.createObjectBuilder()
      .add("PartitionId", id)
      .add("Prime", prime.bigInteger)
      .add("Threshold", threshold)
      .add("SharePoints", arrayBuilder.build())
      .build
  }

  /**
    * Partitions the share points into disjunct sequences according to the given sizes.
    *
    * @param sizes denotes the sizes of the desired share point sequences
    * @return a list of share point sequences
    */
  def sharePointPartition(sizes: Iterable[Int]): List[IndexedSeq[(BigInt, BigInt)]] = {
    require(sizes.sum == sharePoints.length, "The sum of the shares of each slice doesn't match the number of overall shares.")
    require(sizes.forall(s => s <= k), "A particular slice must not exceed the threshold.")

    @tailrec
    def partition(sizes: Iterable[Int], remainingPoints: IndexedSeq[(BigInt, BigInt)], partitions: List[IndexedSeq[(BigInt, BigInt)]]): List[IndexedSeq[(BigInt, BigInt)]] = {
      if (sizes.isEmpty) partitions
      else partition(sizes.tail, remainingPoints.drop(sizes.head), remainingPoints.take(sizes.head) :: partitions)
    }

    partition(sizes, sharePoints, List())
  }

  /**
   * A mere data holder for recording the certification (both falsification and verification) results of a sharepoint partition.
   *
   * @param falsified the number of falsified slice combinations with a sharepoint count below the threshold
   * @param verified the number of verified slice combinations with a sharepoint count equal or above the threshold
   */
  case class CertificationResult(falsified: Int, verified: Int) {
    override def toString: String = String.format("(%d falsified, %d verified)", falsified, verified)
  }

  /**
   * Certifies a given sharepoint partition by falsifying all slice combinations with a sharepoint count below the threshold and vice versa by verifying all valid
   * slice combinations with a sharepoint count equal or above the threshold. This is a potentially very expensive calculation (both time and memory at present) since
   * all combinations from 'n choose 1' up to 'n choose n' must be considered whereby n is given by the number of slices within the partition
   *
   * @param partition the given sharepoint partition
   * @return the result of the certification if successful
   */
  def certifySharePointPartition(partition: List[IndexedSeq[(BigInt, BigInt)]]): CertificationResult  = {

    def evaluateSharePointPartition(seqSizePredicate: IndexedSeq[(BigInt, BigInt)] => Boolean,
                                    bytesPredicate: IndexedSeq[Byte] => Boolean): Int = {
      val indexedPartition = partition.toIndexedSeq
      val metaCombinator = new MetaCombinator(indexedPartition.length)
      val validSliceCombinations = metaCombinator.solutions
        .flatten
        .dropWhile(indices => indices.isEmpty)
        .map(indices => indices.flatMap(index => indexedPartition(index)))
        .filter(seqSizePredicate)
      val count = validSliceCombinations.length
      validSliceCombinations.map(sliceCombination => new SecretMerging(sliceCombination, this.prime))
        .map(merger => merger.secretBytes)
        .forall(bytesPredicate)
        .ensuring(verified => verified, "Certification of sharepoint partition failed.")

      count
    }

    val falsifiedCount = evaluateSharePointPartition((points: IndexedSeq[(BigInt, BigInt)]) => points.size < this.threshold, (bytes: IndexedSeq[Byte]) => bytes != this.secretBytes)
    val verifiedCount = evaluateSharePointPartition((points: IndexedSeq[(BigInt, BigInt)]) => points.size >= this.threshold, (bytes: IndexedSeq[Byte]) => bytes == this.secretBytes)
    CertificationResult(falsifiedCount, verifiedCount)
  }

  /**
   * Partitions the shares according to the given sizes and converts the different slices containing the shares into a JSON array containing the slices as JSON objects.
   *
   * @param sizes denotes a partition
   * @return the JSON array of slices containing the shares
   */
  def partitionAsJson(sizes: Array[Int]): JsonArray = {
    val partition = sharePointPartition(sizes)
    val arrayBuilder = Json.createArrayBuilder()
    partition.map(slice => sharePointsAsJson(slice))
      .foreach(slice => arrayBuilder.add(slice))
    arrayBuilder.build()
  }

  /**
   * Saves the desired partition. The partition can optionally be certified by verifying all slice combinations containing share points equal or above the threshold
   * and by falsifying all other (invalid) slice combinations.
   *
   * @param sizes denotes the partition
   * @param path the path to the partition file
   * @param certified indicates if the partition is to be certified
   * @return the optional certification result indicating the number of verified and falsified slice combinations
   */
  def savePartition(sizes: Iterable[Int], path: Path, certified: Boolean = false): Option[CertificationResult] = {
    require(path.getParent.toFile.exists() && path.getParent.toFile.isDirectory)
    val prettyPrinter = new JsonPrettyPrinter
    prettyPrinter.print(path.getParent.resolve(path.getFileName.toString + ".json").toFile, this.sharePointsAsJson)
    val partition = sharePointPartition(sizes)
    val certificationResult = {
      if (certified) Option(certifySharePointPartition(partition))
      else Option.empty
    }
    partition.map(part => sharePointsAsJson(part))
      .zipWithIndex
      .foreach({
        case (jsonObject, i) => prettyPrinter.print(path.getParent.resolve(path.getFileName.toString + "-" + i + ".json").toFile, jsonObject)
      })
    certificationResult
  }

  /**
   * A convenience method. Saves the partition in the order as given by the sizes.
   *
   * @param sizes denotes the partition
   * @param path the path to the partition file
   */
  def savePartition(sizes: Array[Int], path: Path): Option[CertificationResult] = savePartition(sizes.reverse.toSeq, path)

  /**
   * a convenience method. Demands that the desired partition is to be certified before saving, see [[savePartition()]].
   *
   * @param sizes denotes the partition
   * @param path the path to the partition file
   * @return the certification result indicating the number of verified and falsified slice combinations
   */
  def saveCertifiedPartition(sizes: Array[Int], path: Path): CertificationResult = savePartition(sizes.reverse.toSeq, path, true).get

  /**
   * Gives a textual representation of this particular secret sharing sheme.
   * @return the textual representation
   */
  override def toString: String = String.format("SecretSharing[shares=%d, threshold=%d, s=%s, polynomial=%s, sharePoints=(%s)]", shares: Integer, threshold: Integer, s, polynomial, sharePoints.mkString(","))
}