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

package de.christofreichardt.scala
package shamir

import de.christofreichardt.scala.combinations.{LazyBinomialCombinator, MetaCombinator}
import de.christofreichardt.scala.utils.{JsonPrettyPrinter, RandomGenerator}
import de.christofreichardt.scalatest.MyFunSuite
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.security.SecureRandom
import jakarta.json.Json
import scala.jdk.CollectionConverters

class SecretMergingSuite extends MyFunSuite {
  val randomGenerator = new RandomGenerator(new SecureRandom)

  /*
   * We define a fixed secret and derive n (= SHARES) shares from it. All these shares are feeded into the merger
   * and the original secret will be recovered again. Note that the computed Newton polynomial will be somewhat overdetermined
   * because we are using more than the minimal required shares.
   */
  testWithTracing(this, "Merging-1") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x12, 0x34, 0x56, 0x78, 0x9A.toByte, 0xBC.toByte, 0xDE.toByte, 0xF0.toByte)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    val SHARES = secretSharing.shares
    val THRESHOLD = secretSharing.threshold
    assert(SHARES == 6 && THRESHOLD == 3)
    val secretMerging = SecretMerging(secretSharing.sharePoints.take(SHARES), secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }

  /*
   * Now we gain a random secret and derive n (= SHARES) shares from it. We take the first shares sufficient to
   * recalculate the secret and feed them into the merger.
   */
  testWithTracing(this, "Merging-2") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 15 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    val SHARES = secretSharing.shares
    val THRESHOLD = secretSharing.threshold
    assert(SHARES == 6 && THRESHOLD == 3)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val secretMerging = SecretMerging(secretSharing.sharePoints.take(THRESHOLD), secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }

  /*
   * Fewer shares than denoted by the threshold aren't sufficient to recompute the secret.
   */
  testWithTracing(this, "Merging-3") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 15 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    val SHARES = secretSharing.shares
    val THRESHOLD = secretSharing.threshold
    assert(SHARES == 6 && THRESHOLD == 3)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val secretMerging = SecretMerging(secretSharing.sharePoints.take(THRESHOLD - 1), secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret != secretMerging.secretBytes)
  }

  /*
   * Now we take k (= THRESHOLD) random shares to recalculate the secret.
   */
  testWithTracing(this, "Merging-4") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val SHARES = 12
    val THRESHOLD = 4
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    assert(secretSharing.n == SHARES && secretSharing.k == THRESHOLD)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val chosenSharePoints = randomGenerator.intStream(SHARES)
      .distinct
      .take(THRESHOLD)
      .map(i => secretSharing.sharePoints(i))
      .toIndexedSeq
    val secretMerging = SecretMerging(chosenSharePoints, secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }

  /*
   * Any (k - 1) random shares aren't sufficent to recalculate the secret
   */
  testWithTracing(this, "Merging-5") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val SHARES = 12
    val THRESHOLD = 4 // = k
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    assert(secretSharing.n == SHARES && secretSharing.k == THRESHOLD)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val chosenSharePoints = randomGenerator.intStream(SHARES)
      .distinct
      .take(THRESHOLD - 1)
      .map(i => secretSharing.sharePoints(i))
      .toIndexedSeq
    val secretMerging = SecretMerging(chosenSharePoints, secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret != secretMerging.secretBytes)
  }

  /*
   * The computed shares are saved into a JSON file and will be recalculated by parsing
   * and evaluating this file.
   */
  testWithTracing(this, "Merging-6") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partitions = secretSharing.sharePointPartition(Seq(4, 2, 2, 1, 1, 1, 1))
    tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    secretSharing.savePartition(Seq(4, 2, 2, 1, 1, 1, 1), Paths.get("json", "partition-2"))
    val secretMerging = SecretMerging(Paths.get("json", "partition-2.json"))
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }

  /*
   * The computed shares are splitted into several JSON files and will be recalculated by 
   * parsing and evaluating a subset from these files.
   */
  testWithTracing(this, "Merging-7") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partitions = secretSharing.sharePointPartition(Seq(4, 2, 2, 1, 1, 1, 1).reverse)
    tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    secretSharing.savePartition(Seq(4, 2, 2, 1, 1, 1, 1).reverse, Paths.get("json", "partition-3"))
    val secretMerging = SecretMerging(IndexedSeq(Paths.get("json", "partition-3-1.json"), Paths.get("json", "partition-3-2.json")))
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }

  testWithTracing(this, "Merging-8") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partition = Seq(4, 2, 2, 1, 1, 1, 1)
    val slices = secretSharing.partitionAsJson(partition.reverse.toArray)
    val prettyPrinter = new JsonPrettyPrinter
    prettyPrinter.trace(tracer, slices)
    val iter = CollectionConverters.IteratorHasAsScala(slices.iterator()).asScala
    assert(
      iter.map(slice => slice.asJsonObject())
        .map(slice => slice.getJsonArray("SharePoints"))
        .zip(partition)
        .forall(zipped => zipped._1.size() == zipped._2)
    )
    assert(
      SecretMerging(Json.createArrayBuilder()
        .add(slices.get(0))
        .build()).secretBytes == secret
    )
    assert(
      SecretMerging(Json.createArrayBuilder()
        .add(slices.get(1))
        .add(slices.get(2))
        .build()).secretBytes == secret
    )
    assert(
      SecretMerging(Json.createArrayBuilder()
        .add(slices.get(1))
        .add(slices.get(3))
        .add(slices.get(4))
        .build()).secretBytes == secret
    )
    assert(
      SecretMerging(Json.createArrayBuilder()
        .add(slices.get(3))
        .add(slices.get(4))
        .add(slices.get(5))
        .add(slices.get(6))
        .build()).secretBytes == secret
    )
  }

  testWithTracing(this, "Inconsistent-Partition-Id") {
    val tracer = getCurrentTracer()
    val caught = intercept[IllegalArgumentException] {
      val secretMerging = SecretMerging(IndexedSeq(Paths.get("json", "partition-4-1.json"), Paths.get("json", "partition-4-2.json")))
      tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  testWithTracing(this, "Exhaustive-Verification-1 (n=8, k=4)") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 8
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("verified = %s", secretSharing.verified)
    assert(secretSharing.verified._1)
    assert(secretSharing.verified._2 == 70) // == '8 choose 4'
  }

  testWithTracing(this, "Exhaustive-Verification-2 (n=12, k=6)") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 6
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("verified = %s", secretSharing.verified)
    assert(secretSharing.verified._1)
    assert(secretSharing.verified._2 == 924) // == '12 choose 6'
  }

  /*
   * Somewhat expensive
   */
  ignore(this, "Exhaustive-Verification-3 (n=16, k=8)") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 16
    val THRESHOLD = 8
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("verified = %s", secretSharing.verified)
    assert(secretSharing.verified._1)
    assert(secretSharing.verified._2 == 12870) // == '16 choose 8'
  }

  /*
   * Merging with one sharepoint less than the required threshold must not produce the
   * original secret at least not with overwhelming probability
   */
  testWithTracing(this, "Exhaustive-Falsification-1") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 8
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val lazyCombinator = new LazyBinomialCombinator(SHARES, THRESHOLD - 1)
    val falsified = lazyCombinator.produceAll
      .map(combination => {
        val indices = combination
        val selectedPoints = indices.map(index => secretSharing.sharePoints(index))
        val merger = SecretMerging(selectedPoints, secretSharing.prime)
        merger.secretBytes
      })
      .forall(mergedBytes => {
        tracer.out().printfIndentln("mergedBytes = (%s)", formatBytes(mergedBytes))
        mergedBytes != secret
      })
    assert(falsified)
  }

  testWithTracing(this, "Exhaustive-Falsification-2") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val metaCombinator = new MetaCombinator(SHARES)
    val solutions = metaCombinator.solutions
    val falsified = solutions.zipWithIndex
      .tail // skips '12 choose 0' -> {}
      .filter(indexedCombinations => {
        val (_, k) = indexedCombinations
        k < THRESHOLD
      })
      .forall(indexedCombinations => {
        val (combinations, k) = indexedCombinations
        tracer.out().printfIndentln("%d choose %d", metaCombinator.n, k)
        combinations.map(combination => {
          tracer.out().printfIndentln("%s", combination.mkString("{", ",", "}"))
          val indices = combination
          val selectedPoints = indices.map(index => secretSharing.sharePoints(index))
          val merger = SecretMerging(selectedPoints, secretSharing.prime)
          merger.secretBytes
        }).forall(bytes => bytes != secret)
      })
    tracer.out().printfIndentln("falsified = %b", falsified)
    assert(falsified)
  }

  testWithTracing(this, "Exhaustive-Falsification-3") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val (falsified, count) = secretSharing.falsified
    tracer.out().printfIndentln("falsified = %b, count = %d", falsified, count)
    assert(falsified)
    assert(count == 298) // '12 choose 1' + '12 choose 2' + '12 choose 3' = 12 + 66 + 220 = 298
  }

  /*
   * This will likely trigger a falsification error. A secret containing only two bytes will result in a similar small finite field. Five repetitions
   * of '16 choose 1' (== 16 combinations) up to '16 choose 7' (== 11440 combinations) are usually sufficient to generate a distribution whose secret is
   * recoverable by pure chance with less than k (== threshold) sharepoints.
   */
  ignore(this, "Exhaustive-Falsification-4") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 2 // Bytes
    val SHARES = 16
    val THRESHOLD = 8
    Range(0, 5).foreach(index => {
      val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
      tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
      val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
      tracer.out().printfIndentln("secretSharing = %s", secretSharing)
      tracer.out().printfIndentln("verified = %s", secretSharing.verified)
      tracer.out().printfIndentln("falsified = %s", secretSharing.falsified)
    })
  }

  testWithTracing(this, "Exhaustive-Certification-1") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("certified = %s", secretSharing.certified)
    assert(secretSharing.certified.falsified == 298) // '12 choose 1' + '12 choose 2' + '12 choose 3' = 12 + 66 + 220 = 298
    assert(secretSharing.certified.verified == 495) //'12 choose 4' = 495
  }

  testWithTracing(this, "Slices-Certification-1") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 6
    val THRESHOLD = 3
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partitionInstruction = Seq(3, 1, 1, 1)
    val partition = secretSharing.sharePointPartition(partitionInstruction)
    partition.zipWithIndex.foreach({
      case (slice, index) => tracer.out().printfIndentln("slice[%d] = %s", index, slice.mkString("{", ",", "}"))
    })
    val certificationResult = secretSharing.certifySharePointPartition(partition)
    tracer.out().printfIndentln("Verified combinations = %d, falsified combinations = %d", certificationResult.verified, certificationResult.falsified)
    assert(certificationResult.falsified == 6)
    assert(certificationResult.verified == 9)
  }

  testWithTracing(this, "Slices-Verification-2") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partitionInstruction = Seq(4, 2, 2, 1, 1, 1, 1) // 7 slices
    val partition = secretSharing.sharePointPartition(partitionInstruction)
    partition.zipWithIndex.foreach({
      case (slice, index) => tracer.out().printfIndentln("slice[%d] = %s", index, slice.mkString("{", ",", "}"))
    })
    val certificationResult = secretSharing.certifySharePointPartition(partition)
    tracer.out().printfIndentln("Verified combinations = %d, falsified combinations = %d", certificationResult.verified, certificationResult.falsified)
    val metaCombinator = new MetaCombinator(partitionInstruction.size)
    assert(certificationResult.verified + certificationResult.falsified == 127) // '7 choose 1' + '7 choose 2' + '7 choose 3' + '7 choose 4' + '7 choose 5' + '7 choose 6' + '7 choose 7' == 7 + 21 + 35 +35 + 21 + 7 + 1 == 127
  }

  /*
   * A password containing several characters from the range 0080–00FF (Latin-1 Supplement), in particular
   * some german umlauts. The sharing and merging algorithms are defaulting to UTF-8 when encoding respective
   * decoding the characters.
   */
  testWithTracing(this, "Password-1") {
    val tracer = getCurrentTracer()
    val SHARES = 12
    val THRESHOLD = 4
    val password = "secret-\u00E4\u00F6\u00FC-with-umlauts"
    tracer.out().printfIndentln("password = %1$s, UTF-8(%1$s) = %2$s, UTF-16(%1$s) = %3$s", password, formatBytes(password.getBytes(StandardCharsets.UTF_8)), formatBytes(password.getBytes(StandardCharsets.UTF_16)))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, password)
    assert(secretSharing.n == SHARES && secretSharing.k == THRESHOLD)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val chosenSharePoints = randomGenerator.intStream(SHARES)
      .distinct
      .take(THRESHOLD)
      .map(i => secretSharing.sharePoints(i))
      .toIndexedSeq
    val secretMerging = SecretMerging(chosenSharePoints, secretSharing.prime)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    val recoveredPassword = new String(secretMerging.password)
    tracer.out().printfIndentln("recoveredPassword = %1$s, UTF-8(%1$s) = %2$s, UTF-16(%1$s) = %3$s", recoveredPassword, formatBytes(recoveredPassword.getBytes(StandardCharsets.UTF_8)), formatBytes(password.getBytes(StandardCharsets.UTF_16)))
    assert(password == recoveredPassword)
  }
}