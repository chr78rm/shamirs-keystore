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

import de.christofreichardt.diagnosis.{AbstractTracer, LogLevel, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing
import org.scalatest.{Args, BeforeAndAfter, BeforeAndAfterAll, Status}
import org.scalatest.funsuite.AnyFunSuite


/**
 * @author Christof Reichardt
 */
class MyFunSuite extends AnyFunSuite with Tracing with BeforeAndAfterAll with BeforeAndAfter {

  override def run(testName: Option[String], args: Args): Status = {
    printf("%s.run%n", this.getClass().getSimpleName())
    val tracer = getCurrentTracer()
    tracer.initCurrentTracingContext(15, true)
    withTracer("Status", this, "run(testName: Option[String], args: Args)") {
      tracer.out().printfIndentln("testName = %s", testName)
      tracer.out().printfIndentln("this.suiteName = %s", this.suiteName)
      tracer.out().printfIndentln("args = %s", args)
      super.run(testName, args)
    }
  }

  override def beforeAll(): Unit = {
    val tracer = getCurrentTracer()
    withTracer("Unit", this, "beforeAll()") {
      tracer.logMessage(LogLevel.INFO, String.format("%s started.", this.suiteName), getClass, "beforeAll()")
    }
  }

  override def runTest(testName: String, args: Args): Status = {
    val tracer = getCurrentTracer()
    withTracer("Status", this, "runTest(testName: String, args: Args)") {
      tracer.logMessage(LogLevel.INFO, testName + " started ...", getClass(), "runTest(testName: String, args: Args)")
      tracer.out().printfIndentln("testName = %s", testName)
      //      val myArgs = new Args(new MyReporter(args.reporter), args.stopper, args.filter, args.configMap, args.distributor, args.tracker, args.chosenStyles, args.runTestInNewInstance,
      //          args.distributedTestSorter, args.distributedSuiteSorter)
      tracer.out().printfIndentln("args = %s", args)
      super.runTest(testName, args)
    }
  }

  override def afterAll(): Unit = {
    val tracer = getCurrentTracer()
    withTracer("Unit", this, "afterAll()") {
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

  def testWithTracing[T](callee: Any, testName: String)(block: => T): Unit = {
    test(testName) {
      withTracer("Unit", this, testName) {
        block
      }
    }
  }

  def ignore[T](callee: Any, testName: String)(block: => T): Unit = {
    val tracer = getCurrentTracer()
    ignore(testName) {
      block
    }
  }
}