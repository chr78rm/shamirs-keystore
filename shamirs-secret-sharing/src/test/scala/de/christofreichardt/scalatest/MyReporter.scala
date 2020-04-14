/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2020, Christof Reichardt
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