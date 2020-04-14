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
package jws {

  import java.security.Key
  import javax.crypto.SecretKey
  import javax.crypto.Mac
  import java.nio.charset.StandardCharsets
  import java.security.PublicKey
  import java.security.Signature

  abstract class JWSVerifier(val kid: String) {
    val verifyingKey = retrieveVerificationKey()

    def retrieveVerificationKey(): (String, Key)

    def verify(signingOutput: String): Boolean = {
      val splits = signingOutput.split("\\.").toIndexedSeq
      val header = decodeJson(splits(0)).asJsonObject()
      val payload = decodeJson(splits(1)).asJsonObject()
      val encodedHeader = encodeJson(header)
      val encodedPayload = encodeJson(payload)
      val signingInput = encodedHeader + "." + encodedPayload
      verifyingKey match {
        case (kid, key) => {
          key match {
            case key: SecretKey => {
              val signature = signingInput + "." + new String(encodeBytes(hmac(key, signingInput)), StandardCharsets.UTF_8)
              signature.split("\\.").toIndexedSeq(2) == splits(2)
            }
            case key: PublicKey => {
              val signatureAlgo = Signature.getInstance("SHA512withECDSA")
              signatureAlgo.initVerify(key)
              signatureAlgo.update(signingInput.getBytes(StandardCharsets.UTF_8))
              val rawSignature = decodeBase64Str(splits(2))
              signatureAlgo.verify(rawSignature)
            }
          }
        }
      }
    }
  }
}