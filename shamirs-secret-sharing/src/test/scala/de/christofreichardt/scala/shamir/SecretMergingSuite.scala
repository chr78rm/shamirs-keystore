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

package de.christofreichardt.scala
package shamir

import java.nio.charset.StandardCharsets

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

  testWithTracing(this, "Exhaustive-Verification-1") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 8
    val THRESHOLD = 4
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("verified = %b", secretSharing.verified: java.lang.Boolean)
    assert(secretSharing.verified)
  }

  testWithTracing(this, "Exhaustive-Verification-2") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val SHARES = 12
    val THRESHOLD = 6
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("verified = %b", secretSharing.verified: java.lang.Boolean)
    assert(secretSharing.verified)
  }

//
// Takes several minutes to complete.
//
//  testWithTracing(this, "Exhaustive-Verification-3") {
//    val tracer = getCurrentTracer()
//    val SECRET_SIZE = 16 // Bytes
//    val SHARES = 16
//    val THRESHOLD = 8
//    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
//    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
//    val secretSharing = new SecretSharing(SHARES, THRESHOLD, secret)
//    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
//    tracer.out().printfIndentln("verified = %b", secretSharing.verified: java.lang.Boolean)
//    assert(secretSharing.verified)
//  }

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