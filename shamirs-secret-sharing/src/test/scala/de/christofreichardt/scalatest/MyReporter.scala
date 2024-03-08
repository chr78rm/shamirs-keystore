/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2024, Christof Reichardt
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

import de.christofreichardt.diagnosis.{AbstractTracer, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing
import java.io.PrintStream
import org.scalatest.Reporter
import org.scalatest.events.*
import scala.collection.mutable.ArrayBuffer

class MyReporter(reporter: Reporter) extends Reporter with Tracing {
  val events: ArrayBuffer[Event] = new ArrayBuffer

  override def apply(event: Event): Unit = {
    reporter.apply(event)
    val tracer = getCurrentTracer()
    tracer.out().printfIndentln("event = %s", event)
    this.events.addOne(event)
  }

  def succeeded: Int = {
    this.events.count(event => event match {
      case event: TestSucceeded => true
      case _ => false
    })
  }

  def failed: Int = {
    this.events.count(event => event match {
      case event: TestFailed => true
      case _ => false
    })
  }

  def canceled: Int = {
    this.events.count(event => event match {
      case event: TestCanceled => true
      case _ => false
    })
  }

  def ignored: Int = {
    this.events.count(event => event match {
      case event: TestIgnored => true
      case _ => false
    })
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