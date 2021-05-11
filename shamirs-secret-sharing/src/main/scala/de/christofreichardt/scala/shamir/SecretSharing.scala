/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2020, Christof Reichardt
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

import de.christofreichardt.scala.combinations.BinomialCombinator
import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.scala.utils.{JsonPrettyPrinter, RandomGenerator}

import java.nio.file.Path
import java.security.SecureRandom
import java.util.UUID
import javax.json.{Json, JsonArray, JsonObject}
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

  /** an alias for shares */
  val n: Int = shares
  /** an alias for threshold */
  val k: Int = threshold

  require(n >= 2 && k >= 2, "We need at least two shares, otherwise we wouldn't need shares at all.")
  require(k <= n, "The threshold must be less than or equal to the number of shares.")
  require(secretBytes.length >= 2, "Too few secret bytes.")

  /** the secret encoded as BigInt */
  val s: BigInt = bytes2BigInt(secretBytes)
  /** used to compute a LazyList of random BigInt numbers */
  val randomGenerator: RandomGenerator = new RandomGenerator(random)
  /** the modulus */
  val prime: BigInt = choosePrime

  require((BigInt(n)*BigInt(n)) <= prime, "Too much shares for given secret.")
  require(s < prime, "The encoded secret must be strictly smaller than the prime modulus.")

  /** a random polynomial in the canonical form used to compute the shares */
  val polynomial: Polynomial = choosePolynomial(k - 1)
  /** the actual shares */
  val sharePoints: IndexedSeq[(BigInt, BigInt)] = computeShares

  require(polynomial.degree == k - 1)
  require(this.sharePoints.map(point => point._1).distinct.length == n, String.format("%d distinct sharepoints are needed: %s", n, this.sharePoints))

  /** the partition id */
  val id: String = UUID.randomUUID().toString
  /** all shares converted into a JSON object */
  lazy val sharePointsAsJson: JsonObject = sharePointsAsJson(sharePoints)
  /** indicates if the cross checks with all possible and valid combinations of shares have been successful */
  lazy val verified: Boolean = verifyAll

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
   * Verifies that all valid combinations of shares recover the secret bytes.
   *
   * @return indicates the outcome of all possible and valid cross checks
   */
  def verifyAll: Boolean = {
    val combinator = new BinomialCombinator[Int](IndexedSeq.range(0, n), k)
    combinator.solutions
      .map(combination => {
        val indices = combination
        val selectedPoints = indices.map(index => sharePoints(index))
        val merger = SecretMerging(selectedPoints, prime)
        merger.secretBytes
      })
      .forall(bytes => bytes == secretBytes)
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
    require(sizes.forall(s => s <= k), "A partition must not exceed the threshold.")

    @tailrec
    def partition(sizes: Iterable[Int], remainingPoints: IndexedSeq[(BigInt, BigInt)], partitions: List[IndexedSeq[(BigInt, BigInt)]]): List[IndexedSeq[(BigInt, BigInt)]] = {
      if (sizes.isEmpty) partitions
      else partition(sizes.tail, remainingPoints.drop(sizes.head), remainingPoints.take(sizes.head) :: partitions)
    }

    partition(sizes, sharePoints, List())
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
   * Saves the desired partition. For technical reasons this method delivers the slices in reverse order as given by the sizes.
   *
   * @param sizes denotes the partition
   * @param path the path to the partition file
   */
  def savePartition(sizes: Iterable[Int], path: Path): Unit = {
    require(path.getParent.toFile.exists() && path.getParent.toFile.isDirectory)
    val prettyPrinter = new JsonPrettyPrinter
    prettyPrinter.print(path.getParent.resolve(path.getFileName.toString + ".json").toFile, sharePointsAsJson)
    val partition = sharePointPartition(sizes)
    partition.map(part => sharePointsAsJson(part))
      .zipWithIndex
      .foreach({
        case (jsonObject, i) => prettyPrinter.print(path.getParent.resolve(path.getFileName.toString + "-" + i + ".json").toFile, jsonObject)
      })
  }

  /**
   * A convenience method. Saves the partition in the order as given by the sizes.
   *
   * @param sizes denotes the partition
   * @param path the path to the partition file
   */
  def savePartition(sizes: Array[Int], path: Path): Unit = savePartition(sizes.reverse.toIterable, path)

  /**
   * Gives a textual representation of this particular secret sharing sheme.
   * @return the textual representation
   */
  override def toString: String = String.format("SecretSharing[shares=%d, threshold=%d, s=%s, polynomial=%s, sharePoints=(%s)]", shares: Integer, threshold: Integer, s, polynomial, sharePoints.mkString(","))
}