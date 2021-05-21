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