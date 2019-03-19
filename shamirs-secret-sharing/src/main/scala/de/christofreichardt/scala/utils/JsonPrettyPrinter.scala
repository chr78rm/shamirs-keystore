package de.christofreichardt.scala.utils

import java.util.HashMap
import javax.json.stream.JsonGenerator
import javax.json.Json
import java.io.OutputStream
import javax.json.JsonObject
import scala.util.Try
import java.nio.charset.StandardCharsets
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import javax.json.JsonStructure

class JsonPrettyPrinter {
  val properties = new HashMap[String, AnyRef]
  properties.put(JsonGenerator.PRETTY_PRINTING, java.lang.Boolean.TRUE)
  val writerFactory = Json.createWriterFactory(properties)

  def print(out: OutputStream, jsonObject: JsonObject): Unit = {
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.writeObject(jsonObject)
    } finally {
      writer.close
    }
  }
  
  def print(file: File, jsonObject: JsonObject): Unit = {
    val out = new FileOutputStream(file)
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.writeObject(jsonObject)
    } finally {
      writer.close
    }
  }

  def print(path: Path, jsonStruct: JsonStructure): Unit = {
    val out = new FileOutputStream(path.toFile())
    val writer = writerFactory.createWriter(out, StandardCharsets.UTF_8)
    try {
      writer.write(jsonStruct)
    } finally {
      writer.close
    }
  }
}