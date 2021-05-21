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
package jws {

  import java.nio.charset.StandardCharsets
  import java.security.Key
  import java.security.PrivateKey

  import de.christofreichardt.diagnosis.AbstractTracer
  import de.christofreichardt.diagnosis.TracerFactory
  import de.christofreichardt.scala.diagnosis.Tracing
  import javax.crypto.SecretKey
  import javax.json.Json
  import javax.json.JsonObject
  import java.security.interfaces.ECPrivateKey
  import java.security.spec.ECFieldFp
  import java.security.Signature

  abstract class JWSSigner(val kid: String) extends Tracing {
    val signingKey = retrieveSigningKey()
    val algoMap = Map("HmacSHA256" -> "HS256", "EC" -> "ES512")
    val header = buildHeader
    val encodedHeader = encodeJson(header)

    def retrieveSigningKey(): (String, Key)

    def buildHeader: JsonObject = {
      withTracer("JsonObject", this, "buildHeader()") {
        val tracer = getCurrentTracer()
        tracer.out().printfIndentln("signingKey = (%s,%s(%s))", signingKey._1, signingKey._2.getAlgorithm, formatBytes(signingKey._2.getEncoded))

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

    def sign(payload: JsonObject): String = {
      withTracer("String", this, "sign(payload: JsonObject)") {
        val tracer = getCurrentTracer()
        tracer.out().printfIndentln("payload = %s", payload)

        def trace(key: PrivateKey): Unit = {
          key match {
            case key: ECPrivateKey => {
              val ecParameterSpec = key.getParams
              val curve = ecParameterSpec.getCurve
              val field = curve.getField
              field match {
                case field: ECFieldFp =>
                  tracer.out().printfIndentln(
                    "ecParameterSpec = ECParameterSpec[curve=EllipticCurve[a=%s, b=%s, field=ECFieldFp[p=%s, size=%s]], cofactor=%d, generator=%s, order=%s]",
                    curve.getA, curve.getB, field.getP, field.getFieldSize: Integer, ecParameterSpec.getCofactor: Integer, ecParameterSpec.getGenerator, ecParameterSpec.getOrder)
              }
            }
          }
        }

        val encodedPayload = encodeJson(payload)
        val signingInput = encodedHeader + "." + encodedPayload
        signingKey match {
          case (kid, key) => {
            key match {
              case key: SecretKey => signingInput + "." + new String(encodeBytes(hmac(key, signingInput)), StandardCharsets.UTF_8)
              case key: PrivateKey => {
                trace(key)
                val signatureAlgo = Signature.getInstance("SHA512withECDSA")
                signatureAlgo.initSign(key)
                signatureAlgo.update(signingInput.getBytes(StandardCharsets.UTF_8))
                signingInput + "." + new String(encodeBytes(signatureAlgo.sign()), StandardCharsets.UTF_8)
              }
            }
          }
        }
      }
    }

//    override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getTracer("TestTracer")
  }
}