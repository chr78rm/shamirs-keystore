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

import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import de.christofreichardt.scala.diagnosis.Tracing
import javax.json.{Json, JsonArray, JsonObject}

import scala.jdk.CollectionConverters

class SecretMerging(
  val sharePoints: IndexedSeq[(BigInt, BigInt)],
  val prime:       BigInt) extends Tracing {

  val interpolation: NewtonInterpolation = new NewtonInterpolation(sharePoints, prime)
  val coefficients: IndexedSeq[BigInt] = interpolation.computeCoefficients()
  val degree: Int = sharePoints.length - 1
  val newtonPolynomial: NewtonPolynomial = new NewtonPolynomial(degree, sharePoints.take(sharePoints.length - 1).map(p => p._1), coefficients, prime)
  val s: BigInt = newtonPolynomial.evaluateAt(BigInt(0))
  val secretBytes: IndexedSeq[Byte] = bigIntToBytes(s)
  def password: Array[Char] = {
    val byteBuffer = ByteBuffer.wrap(this.secretBytes.toArray)
    val charBuffer = StandardCharsets.UTF_8.newDecoder().decode(byteBuffer)
    java.util.Arrays.copyOf(charBuffer.array(), charBuffer.limit())
  }
}

object SecretMerging {

  def apply(sharePoints: IndexedSeq[(BigInt, BigInt)], prime: BigInt): SecretMerging = new SecretMerging(sharePoints, prime)

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
    val ids = jsonObjects.map(jsonObject => jsonObject.getString("Id"))
    require(ids.forall(id => id == ids.head), "Inconsistent Ids.")
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

  def apply(paths: Array[Path]): SecretMerging = apply(paths.toIterable)

  def apply(slices: JsonArray): SecretMerging = {
    val iter = CollectionConverters.IteratorHasAsScala(slices.iterator()).asScala
    val jsonObjects = iter.map(jsonValue => jsonValue.asJsonObject()).toIndexedSeq
    processSlices(jsonObjects)
  }
}