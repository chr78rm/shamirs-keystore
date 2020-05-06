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
import java.nio.file.{Files, Paths}
import java.security.SecureRandom

import de.christofreichardt.scala.utils.RandomGenerator
import de.christofreichardt.scalatest.MyFunSuite

class SecretSharingSuite extends MyFunSuite {
  val randomGenerator = new RandomGenerator(new SecureRandom)

  testWithTracing(this, "Conversion-1") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x12, 0x34, 0x56, 0x78, 0x9A.toByte, 0xBC.toByte, 0xDE.toByte, 0xF0.toByte)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("s = %s, (%s)", secretSharing.s, formatBytes(secretSharing.s.toByteArray))
    val test = bigIntToBytes(secretSharing.s)
    tracer.out().printfIndentln("test = (%s)", formatBytes(test))
    assert(secret == test)
  }

  testWithTracing(this, "Conversion-2") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x12, 0x34, 0x56, 0x78, 0x9A.toByte, 0xBC.toByte, 0xDE.toByte, 0xF0.toByte).reverse.map(i => i.toByte)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("s = %s, (%s)", secretSharing.s, formatBytes(secretSharing.s.toByteArray))
    val test = bigIntToBytes(secretSharing.s)
    tracer.out().printfIndentln("test = (%s)", formatBytes(test))
    assert(secret == test)
  }

  testWithTracing(this, "Conversion-3") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("s = %s, (%s)", secretSharing.s, formatBytes(secretSharing.s.toByteArray))
    val test = bigIntToBytes(secretSharing.s)
    tracer.out().printfIndentln("test = (%s)", formatBytes(test))
    assert(secret == test)
  }

  testWithTracing(this, "Preconditions-1") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    val caught = intercept[IllegalArgumentException] {
      val secretSharing = new SecretSharing(1, 3, secret)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  testWithTracing(this, "Preconditions-2") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    val caught = intercept[IllegalArgumentException] {
      val secretSharing = new SecretSharing(2, 3, secret)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  testWithTracing(this, "Preconditions-3") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    val caught = intercept[IllegalArgumentException] {
      val secretSharing = new SecretSharing(2, 1, secret)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  testWithTracing(this, "Preconditions-4") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 15 // Bytes
    val trials = 100
    val test = LazyList.from(1)
      .map(_ => (this.randomGenerator.intStream(12).head, this.randomGenerator.intStream(12).head))
      .filter(tuple => {
        val n = tuple._1
        val k = tuple._2
        n >= 2 && k >= 2 && k <= n
      })
      .take(trials)
      .map(tuple => {
        val n = tuple._1
        val k = tuple._2
        val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
        val secretSharing = new SecretSharing(n, k, secret)
        tracer.out().printfIndentln("secretSharing = %s", secretSharing)
        (k, secretSharing)
      })
      .forall(tuple => {
        val k = tuple._1
        val secretSharing = tuple._2
        secretSharing.polynomial.degree == k - 1
      })
    assert(test)
  }

  testWithTracing(this, "Sharing-1") {
    val tracer = getCurrentTracer()
    val secret: IndexedSeq[Byte] = IndexedSeq(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    tracer.out().printfIndentln("%d secretSharing.sharePoints = (%s)", secretSharing.sharePoints.length: Integer, secretSharing.sharePoints.mkString(","))
    tracer.out().printfIndentln("secretSharing.sharePointsAsJson = %s", secretSharing.sharePointsAsJson.toString())
    Files.write(Paths.get("json", "shares-1.json"), secretSharing.sharePointsAsJson.toString().getBytes(StandardCharsets.UTF_8))
  }
  
  testWithTracing(this, "Sharing-2") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 15 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    Files.write(Paths.get("json", "shares-2.json"), secretSharing.sharePointsAsJson.toString().getBytes(StandardCharsets.UTF_8))
  }
  
  testWithTracing(this, "Partition-1") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val partitions = secretSharing.sharePointPartition(Seq(2,1,1,1,1))
    tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
  }
  
  testWithTracing(this, "Partition-2") {
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
  }
  
  testWithTracing(this, "Partition-3") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val caught = intercept[IllegalArgumentException] {
      val partitions = secretSharing.sharePointPartition(Seq(2,2,1,1,1))
      tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }
  
  testWithTracing(this, "Partition-4") {
    val tracer = getCurrentTracer()
    val SECRET_SIZE = 16 // Bytes
    val secret: IndexedSeq[Byte] = randomGenerator.byteStream.take(SECRET_SIZE).toIndexedSeq
    tracer.out().printfIndentln("secret = (%s)", formatBytes(secret))
    val secretSharing = new SecretSharing(secret)
    tracer.out().printfIndentln("secretSharing = %s", secretSharing)
    val caught = intercept[IllegalArgumentException] {
      val partitions = secretSharing.sharePointPartition(Seq(4,1,1))
      tracer.out().printfIndentln("partitions = (%s)", partitions.mkString(","))
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }
  
  testWithTracing(this, "Save-1") {
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
    secretSharing.savePartition(Seq(4,2,2,1,1,1,1), Paths.get("json", "partition-1"))
  }
}