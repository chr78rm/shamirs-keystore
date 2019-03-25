package de.christofreichardt.scalatest

import org.scalatest.Args
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.Status

import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.LogLevel
import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.scala.diagnosis.Tracing
import org.scalatest.Reporter
import org.scalatest.events.Event


/**
 * @author Christof Reichardt
 */
class MyFunSuite extends FunSuite with Tracing with BeforeAndAfterAll with BeforeAndAfter {

  override def run(testName: Option[String], args: Args): Status = {
    printf("%s.run%n", this.getClass().getSimpleName())
    val tracer = getCurrentTracer
    tracer.initCurrentTracingContext(15, true)
    withTracer("Status", this, "run(testName: Option[String], args: Args)") {
      tracer.out().printfIndentln("testName = %s", testName)
      tracer.out().printfIndentln("this.suiteName = %s", this.suiteName)
      tracer.out().printfIndentln("args = %s", args)
      super.run(testName, args)
    }
  }

  override def beforeAll(): Unit = {
    val tracer = getCurrentTracer
    withTracer("Unit", this, "beforeAll()") {
      tracer.logMessage(LogLevel.INFO, String.format("%s started.", this.suiteName), getClass, "beforeAll()")
    }
  }

  override def runTest(testName: String, args: Args): Status = {
    val tracer = getCurrentTracer
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
    val tracer = getCurrentTracer
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
  
  def testWithTracing[T](callee : Any, testName : String)(block : => T) : Unit = {
    test(testName) {
      withTracer("Unit", this, testName) {
        block
      }
    }
  }
  
  def ignore[T](callee : Any, testName : String)(block : => T) : Unit = {
     val tracer = getCurrentTracer
    ignore(testName) {
       block
     }
  }
}