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

package de.christofreichardt.scala.utils

import de.christofreichardt.diagnosis.AbstractTracer
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import javax.json.stream.JsonGenerator
import javax.json.{Json, JsonObject, JsonStructure}

class JsonPrettyPrinter {
  val properties = new java.util.HashMap[String, AnyRef]
  properties.put(JsonGenerator.PRETTY_PRINTING, java.lang.Boolean.TRUE)
  private val writerFactory = Json.createWriterFactory(properties)

  def print(out: OutputStream, jsonObject: JsonObject): Unit = {
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.writeObject(jsonObject)
    } finally {
      writer.close()
    }
  }
  
  def print(file: File, jsonObject: JsonObject): Unit = {
    val out = new FileOutputStream(file)
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.writeObject(jsonObject)
    } finally {
      writer.close()
    }
  }

  def print(path: Path, jsonStruct: JsonStructure): Unit = {
    val out = new FileOutputStream(path.toFile)
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.write(jsonStruct)
    } finally {
      writer.close()
    }
  }

  def trace(tracer: AbstractTracer, jsonStruct: JsonStructure): Unit = {
    val out = new ByteArrayOutputStream()
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    val bytes = {
      try {
        writer.write(jsonStruct)
        out.toByteArray
      } finally {
        writer.close()
      }
     }
    val in = new ByteArrayInputStream(bytes)
    val streamReader = new InputStreamReader(in)
    val bufferedReader = new BufferedReader(streamReader)
    try {
      bufferedReader.lines().forEach((line: String) => tracer.out().printfIndentln(line))
    } finally {
      bufferedReader.close()
    }
    tracer.out().println()
  }
}