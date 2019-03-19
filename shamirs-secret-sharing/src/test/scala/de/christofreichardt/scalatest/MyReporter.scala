package de.christofreichardt.scalatest

import org.scalatest.Reporter
import org.scalatest.events.Event

import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.scala.diagnosis.Tracing

class MyReporter(reporter: Reporter) extends Reporter with Tracing {
  
  override  def apply(event: Event): Unit = {
    reporter.apply(event)
    val tracer = getCurrentTracer
    tracer.out().printfIndentln("event = %s", event)
  }
  
  override def getCurrentTracer(): AbstractTracer = {
    try {
      TracerFactory.getInstance().getTracer("TestTracer")
    }
    catch {
      case ex: TracerFactory.Exception => TracerFactory.getInstance().getDefaultTracer
    }
  }
}