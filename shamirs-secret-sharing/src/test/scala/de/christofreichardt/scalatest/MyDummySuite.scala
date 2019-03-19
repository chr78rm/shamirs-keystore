package de.christofreichardt.scalatest

class MyDummySuite extends MyFunSuite {
  
  testWithTracing(this, "System Properties") {
    val propertyNames = System.getProperties().stringPropertyNames.toArray(new Array[String](0)).sorted
    propertyNames.foreach(propertyName => getCurrentTracer().out().printfIndentln("%s = %s", propertyName, System.getProperty(propertyName)))
  }

}