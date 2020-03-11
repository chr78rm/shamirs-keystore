package de.christofreichardt.scala.shamir

import java.nio.file.Path
import java.security.SecureRandom
import java.util.UUID

import de.christofreichardt.scala.combinations.BinomialCombinator
import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.scala.utils.{JsonPrettyPrinter, RandomGenerator}
import javax.json.{Json, JsonObject}

class SecretSharing(
                     val shares: Int,
                     val threshold: Int,
                     val secretBytes: IndexedSeq[Byte],
                     val random: SecureRandom)
  extends Tracing {

  def this(secretBytes: IndexedSeq[Byte]) = this(6, 3, secretBytes, new SecureRandom)

  def this(shares: Int, threshold: Int, secretBytes: IndexedSeq[Byte]) = this(shares, threshold, secretBytes, new SecureRandom)

  def this(shares: Int, threshold: Int, secretBytes: Array[Byte]) = this(shares, threshold, secretBytes.toIndexedSeq, new SecureRandom)

  val n: Int = shares
  val k: Int = threshold
  val s: BigInt = bytes2BigInt(secretBytes)
  val randomGenerator: RandomGenerator = new RandomGenerator(random)
  val prime: BigInt = choosePrime
  val polynomial: Polynomial = new Polynomial(chooseCanonicalCoefficients :+ s, prime)
  val sharePoints: IndexedSeq[(BigInt, BigInt)] = computeShares
  val id: String = UUID.randomUUID().toString()
  lazy val sharePointsAsJson: JsonObject = sharePointsAsJson(sharePoints)
  lazy val verified: Boolean = verifyAll

  require(n >= 2 && k >= 2, "We need at least two shares, otherwise we wouldn't need shares at all.")
  require(k <= n, "The threshold must be less than or equal to the number of shares.")

  def choosePrime: BigInt = {
    val BIT_OFFSET = 1
    val bits = s.bitLength + BIT_OFFSET
    BigInt(bits, CERTAINTY, random)
  }

  def chooseCanonicalCoefficients: IndexedSeq[BigInt] = {
    val bits = s.bitLength * 2
    randomGenerator.bigIntStream(bits, prime).take(k - 1).toIndexedSeq
  }

  def computeShares: IndexedSeq[(BigInt, BigInt)] = {
    val bits = s.bitLength * 2
    randomGenerator.bigIntStream(bits, prime)
      .filterNot(x => x == BigInt(0))
      .take(shares)
      .map(x => (x, polynomial.evaluateAt(x)))
      .toIndexedSeq
  }

  def verifyAll: Boolean = {
    val combinator = new BinomialCombinator[Int](IndexedSeq.range(0, n), k)
    combinator.solutions
      .map(combination => {
        val indices = combination.toIndexedSeq
        val selectedPoints = indices.map(index => sharePoints(index))
        val merger = SecretMerging(selectedPoints, prime)
        merger.secretBytes
      })
      .forall(bytes => bytes == secretBytes)
  }

  def sharePointsAsJson(ps: IndexedSeq[(BigInt, BigInt)]): JsonObject = {
    val arrayBuilder = Json.createArrayBuilder()
    ps.foreach(ps => {
      arrayBuilder.add(Json.createObjectBuilder()
        .add("SharePoint", Json.createObjectBuilder()
          .add("x", ps._1.bigInteger)
          .add("y", ps._2.bigInteger)))
    })
    Json.createObjectBuilder()
      .add("Id", id)
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

    def partition(sizes: Iterable[Int], remainingPoints: IndexedSeq[(BigInt, BigInt)], partitions: List[IndexedSeq[(BigInt, BigInt)]]): List[IndexedSeq[(BigInt, BigInt)]] = {
      if (sizes.isEmpty) partitions
      else partition(sizes.tail, remainingPoints.drop(sizes.head), remainingPoints.take(sizes.head) :: partitions)
    }

    partition(sizes, sharePoints, List())
  }

  def savePartition(sizes: Iterable[Int], path: Path): Unit = {
    require(path.getParent.toFile().exists() && path.getParent.toFile().isDirectory())
    val prettyPrinter = new JsonPrettyPrinter
    prettyPrinter.print(path.getParent.resolve(path.getFileName.toString() + ".json").toFile(), sharePointsAsJson)
    val partition = sharePointPartition(sizes)
    partition.map(part => sharePointsAsJson(part))
      .zipWithIndex
      .foreach({
        case (jsonObject, i) => {
          prettyPrinter.print(path.getParent.resolve(path.getFileName.toString() + "-" + i + ".json").toFile(), jsonObject)
        }
      })
  }

  def savePartition(sizes: Array[Int], path: Path): Unit = savePartition(sizes.reverse.toIterable, path)

  override def toString = String.format("SecretSharing[shares=%d, threshold=%d, s=%s, polynomial=%s, sharePoints=(%s)]", shares: Integer, threshold: Integer, s, polynomial, sharePoints.mkString(","))
}