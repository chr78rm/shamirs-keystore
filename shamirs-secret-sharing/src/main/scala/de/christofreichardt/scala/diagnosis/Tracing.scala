package de.christofreichardt.scala.diagnosis

import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.diagnosis.AbstractTracer

trait Tracing {
	def withTracer[T](resultTypeAsString: String, callee: AnyRef, methodSignature: String)(block: => T): T = {
	  val tracer = getCurrentTracer()
	  tracer.entry(resultTypeAsString, callee, methodSignature)
    try {
      block
    }
    finally {
      tracer.wayout()
    }
	}
	
	def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer()
}