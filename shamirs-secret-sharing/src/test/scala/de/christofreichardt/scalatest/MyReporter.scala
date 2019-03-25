package de.christofreichardt.scalatest

import org.scalatest.Reporter
import org.scalatest.events.{Event, TestSucceeded}
import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.scala.diagnosis.Tracing

class MyReporter(reporter: Reporter) extends Reporter with Tracing {
  var succeeded: Int = 0;
  
  override  def apply(event: Event): Unit = {
    reporter.apply(event)
    val tracer = getCurrentTracer
    tracer.out().printfIndentln("event = %s", event)
    event match {
      case event: TestSucceeded => succeeded = succeeded + 1
      case _ =>
    }
  }
  
  override def getCurrentTracer(): AbstractTracer = {
    try {
      TracerFactory.getInstance().getTracer("TestTracer")
    }
    catch {
      case ex: TracerFactory.Exception => TracerFactory.getInstance().getDefaultTracer
    }
  }

  override def toString: String = String.format("de.christofreichardt.scalatest.MyReporter@%d", hashCode(): java.lang.Integer)
}