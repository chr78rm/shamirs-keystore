package de.christofreichardt.scala

package object jws {

  import java.io.ByteArrayInputStream
  import java.io.FileInputStream
  import java.nio.charset.StandardCharsets
  import java.nio.file.Path
  import java.util.Base64
  import javax.crypto.Mac
  import javax.crypto.SecretKey
  import javax.json.Json
  import javax.json.JsonStructure

  def parse(path: Path): JsonStructure = {
    val jsonReader = Json.createReader(new FileInputStream(path.toFile()))
    try {
      jsonReader.read()
    } finally {
      jsonReader.close()
    }
  }

  def encodeJson(jsonStructure: JsonStructure): String = Base64.getUrlEncoder.withoutPadding().encodeToString(jsonStructure.toString().getBytes(StandardCharsets.UTF_8))

  def encodeBytes(bytes: Array[Byte]): Array[Byte] = Base64.getUrlEncoder.withoutPadding().encode(bytes)
  
  def decodeBase64Str(input: String): Array[Byte] = Base64.getUrlDecoder.decode(input)

  def decodeJson(value: String): JsonStructure = {
    val decodedBytes = Base64.getUrlDecoder.decode(value)
    val in = new ByteArrayInputStream(decodedBytes)
    val jsonReader = Json.createReader(in)
    try {
      jsonReader.read()
    } finally {
      jsonReader.close()
    }
  }

  def decodeJson(bytes: Array[Byte]): JsonStructure = {
    val decodedBytes = Base64.getUrlDecoder.decode(bytes)
    val in = new ByteArrayInputStream(decodedBytes)
    val jsonReader = Json.createReader(in)
    try {
      jsonReader.read()
    } finally {
      jsonReader.close()
    }
  }

  def hmac(secretKey: SecretKey, signingInput: String): Array[Byte] = {
    val mac = Mac.getInstance(secretKey.getAlgorithm)
    mac.init(secretKey)
    mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8))
  }

}