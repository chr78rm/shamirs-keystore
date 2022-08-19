/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2022, Christof Reichardt
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

import de.christofreichardt.scala.utils.JsonPrettyPrinter
import de.christofreichardt.scalatest.MyFunSuite
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.{Key, KeyPairGenerator, SecureRandom, Security}
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyGenerator
import javax.json.{Json, JsonStructure}

class JsonWebSignatureSuite extends MyFunSuite {

  val secureRandom = SecureRandom.getInstance("SHA1PRNG")
  secureRandom.setSeed(0L)
  val hmacKeyGenerator = KeyGenerator.getInstance("HmacSHA256")
  hmacKeyGenerator.init(secureRandom)
  val hmacKey = hmacKeyGenerator.generateKey()

  testWithTracing(this, "Encoding-1") {
    val secretShare = parse(Paths.get("json", "jws", "secret-share.json"))
    val encoded = encodeJson(secretShare)
    Files.write(Paths.get("json", "jws", "encoded-secret-share.txt"), encoded.getBytes(StandardCharsets.UTF_8))
    val decoded = decodeJson(Files.readAllBytes(Paths.get("json", "jws", "encoded-secret-share.txt")))
    val prettyPrinter = new JsonPrettyPrinter
    prettyPrinter.print(Paths.get("json", "jws", "decoded-secret-share.json"), decoded)
    assert(Files.readAllBytes(Paths.get("json", "jws", "secret-share.json")).toSeq == Files.readAllBytes(Paths.get("json", "jws", "decoded-secret-share.json")).toSeq)
  }

  testWithTracing(this, "Hmac-1") {
    val tracer = getCurrentTracer()
    val payload = parse(Paths.get("json", "jws", "secret-share.json")).asJsonObject()
    val jsonWebSignature = new JsonWebSignature("Hmac") {
      def retrieveSigningKey(): (String, Key) = (kid, hmacKey)
    }
    val signature = jsonWebSignature.sign(payload)
    tracer.out().printfIndentln("signature = %s", signature)
    val check = jsonWebSignature.verify(signature)
    tracer.out().printfIndentln("check = %s", check: java.lang.Boolean)
    assert(check)
  }
  
  testWithTracing(this, "Hmac-2") {
    val tracer = getCurrentTracer()
    val payload = parse(Paths.get("json", "jws", "secret-share.json")).asJsonObject()
    val jwsSigner = new JWSSigner("kid-for-hmac") {
      def retrieveSigningKey(): (String, Key) = (kid, hmacKey)
    }
    val signature = jwsSigner.sign(payload)
    tracer.out().printfIndentln("signature = %s", signature)
    val jwsVerifier = new JWSVerifier("kid-for-hmac") {
      def retrieveVerificationKey(): (String, Key) = (kid, hmacKey)
    }
    val check = jwsVerifier.verify(signature)
    tracer.out().printfIndentln("check = %s", check: java.lang.Boolean)
    assert(check)
  }
  
  testWithTracing(this, "EC-Keypair-1") {
    val tracer = getCurrentTracer()
    val service = Security.getProviders("AlgorithmParameters.EC")(0).getService("AlgorithmParameters", "EC")
    tracer.out().printfIndentln("service = %s", service)
    tracer.out().printfIndentln("service.getAttribute(\"SupportedCurves\") = %s", service.getAttribute("SupportedCurves"))
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    val algorithmParameterSpec = new ECGenParameterSpec("secp521r1")
    keyPairGenerator.initialize(algorithmParameterSpec, secureRandom)
    val keyPair = keyPairGenerator.generateKeyPair()
    val jwsSigner = new JWSSigner("kid-for-ec") {
      def retrieveSigningKey(): (String, Key) = (kid, keyPair.getPrivate)
    }
    val payload = parse(Paths.get("json", "jws", "secret-share.json")).asJsonObject()
    val signature = jwsSigner.sign(payload)
    tracer.out().printfIndentln("signature = %s", signature)
    val jwsVerifier = new JWSVerifier("kid-for-ec") {
      def retrieveVerificationKey(): (String, Key) = (kid, keyPair.getPublic)
    }
    val check = jwsVerifier.verify(signature)
    tracer.out().printfIndentln("check = %s", check: java.lang.Boolean)
    assert(check)
  }
}