/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2021, Christof Reichardt
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

import de.christofreichardt.scala.diagnosis.Tracing

import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import javax.json.{Json, JsonArray, JsonObject}
import scala.jdk.CollectionConverters

/**
 * Recovers the original secret bytes by combining the given shares.
 *
 * @constructor Creates a immutable `SecretMerging` instance.
 *
 * @param sharePoints the shares
 * @param prime the prime modulus
 */
class SecretMerging(
                     val sharePoints: IndexedSeq[(BigInt, BigInt)],
                     val prime: BigInt) extends Tracing {

  /** Newtons interpolation method */
  val interpolation: NewtonInterpolation = new NewtonInterpolation(sharePoints, prime)
  /** the (recovered) encoded secret */
  val s: BigInt = interpolation.newtonPolynomial.evaluateAt(BigInt(0))
  /** the actual (recovered) secret bytes */
  val secretBytes: IndexedSeq[Byte] = bigIntToBytes(s)

  /**
   * Computes a character sequence from the recovered secret bytes by applying UTF-8 encoding.
   *
   * @return the decoded password
   */
  def password: Array[Char] = {
    val byteBuffer = ByteBuffer.wrap(this.secretBytes.toArray)
    val charBuffer = StandardCharsets.UTF_8.newDecoder().decode(byteBuffer)
    java.util.Arrays.copyOf(charBuffer.array(), charBuffer.limit())
  }
}

/**
 * This object provides some operations to create `SecretMerging` instances.
 */
object SecretMerging {

  /**
   * Directly calls the `SecretMerging` primary constructor.
   *
   * @param sharePoints the shares
   * @param prime the prime modulus
   *
   * @return the immutable `SecretMerging` instance
   */
  def apply(sharePoints: IndexedSeq[(BigInt, BigInt)], prime: BigInt): SecretMerging = new SecretMerging(sharePoints, prime)

  /**
   * Evaluates a JSON file containing shares needed to recover the secret.
   *
   * @param path the path to the JSON file
   * @return the immutable `SecretMerging` instance
   */
  def apply(path: Path): SecretMerging = {
    val jsonObject = {
      val fileIn = new FileInputStream(path.toFile)
      try {
        Json.createReader(fileIn).readObject()
      } finally {
        fileIn.close()
      }
    }
    val prime = jsonObject.getJsonNumber("Prime").bigIntegerValue()
    val threshold = jsonObject.getInt("Threshold")
    val sharePointsAsJson = jsonObject.getJsonArray("SharePoints")
    val ps = CollectionConverters.IteratorHasAsScala(sharePointsAsJson.iterator()).asScala
      .map(sp => sp.asJsonObject().getJsonObject("SharePoint"))
      .map(sp => (BigInt(sp.getJsonNumber("x").bigIntegerValue()), BigInt(sp.getJsonNumber("y").bigIntegerValue())))
      .toIndexedSeq
    require(ps.length >= threshold)
    new SecretMerging(ps.take(threshold), prime)
  }

  /**
   * Combines several JSON files containing shares needed to recover the secret.
   *
   * @param paths the paths to the JSON files
   * @return the immutable `SecretMerging` instance
   */
  def apply(paths: Iterable[Path]): SecretMerging = {
    val jsonObjects = paths.map(
      path => {
        val fileIn = new FileInputStream(path.toFile)
        try {
          Json.createReader(new FileInputStream(path.toFile)).readObject()
        } finally {
          fileIn.close()
        }
      }
    ).toIndexedSeq
    processSlices(jsonObjects)
  }

  private def processSlices(jsonObjects: Seq[JsonObject]): SecretMerging = {
    require(jsonObjects.nonEmpty, "Empty Sequence.")
    require(jsonObjects.forall(jsonObject => jsonObject.containsKey("PartitionId")), "No PartitionId found.")
    val ids = jsonObjects.map(jsonObject => jsonObject.getString("PartitionId"))
    require(ids.forall(id => id == ids.head), "Inconsistent PartitionIds.")
    val (prime, threshold) = jsonObjects.view.map(jsonObject => (BigInt(jsonObject.getJsonNumber("Prime").bigIntegerValue()), jsonObject.getInt("Threshold"))).head
    val ps = jsonObjects
      .map(jsonObject => jsonObject.getJsonArray("SharePoints"))
      .flatMap(jsonArray => CollectionConverters.IteratorHasAsScala(jsonArray.iterator()).asScala)
      .map(jsonValue => jsonValue.asJsonObject())
      .map(jsonObject => jsonObject.getJsonObject("SharePoint"))
      .map(sp => (BigInt(sp.getJsonNumber("x").bigIntegerValue()), BigInt(sp.getJsonNumber("y").bigIntegerValue())))
      .toIndexedSeq
    require(ps.length >= threshold, "Too few sharepoints.")
    new SecretMerging(ps.take(threshold), prime)
  }

  /**
   * Combines several JSON files containing shares needed to recover the secret.
   *
   * @param paths the paths to the JSON files
   * @return the immutable `SecretMerging` instance
   */
  def apply(paths: Array[Path]): SecretMerging = apply(paths.toIterable)

  /**
   * Combines JsonObjects each containing a slice of shares needed to recover the secret.
   *
   * @param slices the JsonArray containing the slices
   * @return the immutable `SecretMerging` instance
   */
  def apply(slices: JsonArray): SecretMerging = {
    val iter = CollectionConverters.IteratorHasAsScala(slices.iterator()).asScala
    val jsonObjects = iter.map(jsonValue => jsonValue.asJsonObject()).toIndexedSeq
    processSlices(jsonObjects)
  }
}