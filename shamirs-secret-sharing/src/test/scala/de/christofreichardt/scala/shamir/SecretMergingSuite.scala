package de.christofreichardt.scala
package shamir

import de.christofreichardt.scalatest.MyFunSuite
import de.christofreichardt.scala.utils.RandomGenerator
import java.security.SecureRandom
import java.nio.file.Paths

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
    val partitions = secretSharing.sharePointPartition(Seq(4,2,2,1,1,1,1))
    tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    secretSharing.savePartition(Seq(4,2,2,1,1,1,1), Paths.get("json", "partition-2"))
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
    val partitions = secretSharing.sharePointPartition(Seq(4,2,2,1,1,1,1).reverse)
    tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    secretSharing.savePartition(Seq(4,2,2,1,1,1,1).reverse, Paths.get("json", "partition-3"))
    val secretMerging = SecretMerging(IndexedSeq(Paths.get("json", "partition-3-1.json"), Paths.get("json", "partition-3-2.json")))
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secretMerging.secretBytes))
    assert(secret == secretMerging.secretBytes)
  }
}