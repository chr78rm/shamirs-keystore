package de.christofreichardt.scalatest

import java.io.File

import org.scalatest._
import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.scala.diagnosis.Tracing

class MySuites(suites: Suite*) extends Suites(suites: _*) with Tracing with BeforeAndAfterAll with SequentialNestedSuiteExecution {

  override def run(testName: Option[String], args: Args): Status = {
    printf("%s.run%n", this.getClass().getSimpleName())
    TracerFactory.getInstance().readConfiguration(new File("." + File.separator + "config" + File.separator + "tracerfactory-config.xml"))
    val tracer = getCurrentTracer
    tracer.open()
    tracer.initCurrentTracingContext(5, true)
    try {
      withTracer("Status", this, "run(testName: Option[String], args: Args)") {
        val myReporter = new MyReporter(args.reporter)
        val myArgs = new Args(myReporter, args.stopper, args.filter, args.configMap, args.distributor, args.tracker, args.chosenStyles, args.runTestInNewInstance,
          args.distributedTestSorter, args.distributedSuiteSorter)
        tracer.out().printfIndentln("testName = %s", testName)
        tracer.out().printfIndentln("myArgs = %s", myArgs)
        val status = super.run(testName, myArgs)
        tracer.out().printfIndentln("myReporter.succeeded = %d", myReporter.succeeded: java.lang.Integer)
        status
      }
    } finally {
      tracer.close()
    }
  }

  override def beforeAll: Unit = {
    withTracer("Unit", this, "beforeAll") {
      TracerFactory.getInstance().getTracer("TestTracer").open()
    }
  }

  override def afterAll: Unit = {
    printf("%s.afterAll%n", this.getClass().getSimpleName())
    withTracer("Unit", this, "afterAll") {
      TracerFactory.getInstance().getTracer("TestTracer").close()
    }
  }

  override def getCurrentTracer(): AbstractTracer = {
    try {
      TracerFactory.getInstance().getTracer("MySuitesTracer")
    } catch {
      case ex: TracerFactory.Exception => TracerFactory.getInstance().getDefaultTracer
    }
  }

}