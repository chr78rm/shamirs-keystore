package de.christofreichardt.scala.shamir

import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import javax.json.{Json, JsonValue}

import scala.jdk.CollectionConverters

class SecretMerging(
  val sharePoints: IndexedSeq[(BigInt, BigInt)],
  val prime:       BigInt) {

  val interpolation: NewtonInterpolation = new NewtonInterpolation(sharePoints, prime)
  val coefficients: IndexedSeq[BigInt] = interpolation.computeCoefficients()
  val degree: Int = sharePoints.length - 1
  val newtonPolynomial: NewtonPolynomial = new NewtonPolynomial(degree, sharePoints.take(sharePoints.length - 1).map(p => p._1), coefficients, prime)
  val s: BigInt = newtonPolynomial.evaluateAt(BigInt(0))
  val secretBytes: IndexedSeq[Byte] = bigIntToBytes(s)
  val password: Array[Char] = new String(secretBytes.toArray, StandardCharsets.UTF_8).toCharArray
}

object SecretMerging {

  def apply(sharePoints: IndexedSeq[(BigInt, BigInt)], prime: BigInt): SecretMerging = new SecretMerging(sharePoints, prime)

  def apply(path: Path): SecretMerging = {
    val in = new FileInputStream(path.toFile)
    val reader = Json.createReader(in)
    val jsonObject = reader.readObject()
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
    val jsonObjects = paths.map(path => Json.createReader(new FileInputStream(path.toFile)).readObject())
    val ids = jsonObjects.map(jsonObject => jsonObject.getString("Id"))
    require(ids.forall(id => id == ids.head))
    val (prime, threshold) = jsonObjects.view.map(jsonObject => (BigInt(jsonObject.getJsonNumber("Prime").bigIntegerValue()), jsonObject.getInt("Threshold"))).head
    val ps = jsonObjects
      .map(jsonObject => jsonObject.getJsonArray("SharePoints"))
      .flatMap(jsonArray => CollectionConverters.IteratorHasAsScala(jsonArray.iterator()).asScala)
      .map(jsonValue => jsonValue.asJsonObject())
      .map(jsonObject => jsonObject.getJsonObject("SharePoint"))
      .map(sp => (BigInt(sp.getJsonNumber("x").bigIntegerValue()), BigInt(sp.getJsonNumber("y").bigIntegerValue())))
      .toIndexedSeq
    require(ps.length >= threshold)
    new SecretMerging(ps.take(threshold), prime)
  }

  def apply(paths: Array[Path]): SecretMerging = apply(paths.toIterable)
}