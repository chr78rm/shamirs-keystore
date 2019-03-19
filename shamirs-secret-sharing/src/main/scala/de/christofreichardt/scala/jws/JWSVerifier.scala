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