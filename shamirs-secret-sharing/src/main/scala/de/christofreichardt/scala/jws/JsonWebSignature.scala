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

package de.christofreichardt.scala
package jws

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64

import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonStructure
import javax.crypto.SecretKey
import javax.crypto.Mac
import java.security.Key
import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory

abstract class JsonWebSignature(val kid: String) extends Tracing {
  val signingKey = retrieveSigningKey()
  val algoMap = Map("HmacSHA256" -> "HS256", "SHA256withECDSA" -> "ES256")

  def retrieveSigningKey(): (String, Key)

  def buildHeader: JsonObject = {
    withTracer("JsonObject", this, "buildHeader()") {
      val tracer = getCurrentTracer()
      tracer.out().printfIndentln("signingKey = (%s,(%s))", signingKey._1, formatBytes(signingKey._2.getEncoded))
      signingKey match {
        case (kid, key) => {
          Json.createObjectBuilder()
            .add("typ", "json")
            .add("alg", algoMap(key.getAlgorithm))
            .build
        }
      }
    }
  }

  def hmac(secretKey: SecretKey, signingInput: String): Array[Byte] = {
    val mac = Mac.getInstance(secretKey.getAlgorithm)
    mac.init(secretKey)
    mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8))
  }

  def sign(payload: JsonObject): String = {
    withTracer("String", this, "sign(payload: JsonObject)") {
      val tracer = getCurrentTracer()
      tracer.out().printfIndentln("payload = %s", payload)

      val header = buildHeader
      val encodedHeader = encodeJson(header)
      val encodedPayload = encodeJson(payload)
      val signingInput = encodedHeader + "." + encodedPayload

      signingKey match {
        case (kid, key) => {
          key match {
            case key: SecretKey => signingInput + "." + new String(encodeBytes(hmac(key, signingInput)), StandardCharsets.UTF_8)
          }
        }
      }
    }
  }

  def verify(signingOutput: String): Boolean = {
    withTracer("Boolean", this, "verify(signingOutput: String)") {
      val tracer = getCurrentTracer()
      val splits = signingOutput.split("\\.").toIndexedSeq
      tracer.out().printfIndentln("splits = (%s)", splits)
      val header = decodeJson(splits(0)).asJsonObject()
      val payload = decodeJson(splits(1)).asJsonObject()
      val encodedHeader = encodeJson(header)
      val encodedPayload = encodeJson(payload)
      val signingInput = encodedHeader + "." + encodedPayload

      val signature = signingKey match {
        case (kid, key) => {
          key match {
            case key: SecretKey => signingInput + "." + new String(encodeBytes(hmac(key, signingInput)), StandardCharsets.UTF_8)
          }
        }
      }
      tracer.out().printfIndentln("signature = %s", signature)
      signature.split("\\.").toIndexedSeq(2) == splits(2)
    }
  }

//  override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getTracer("TestTracer")
}