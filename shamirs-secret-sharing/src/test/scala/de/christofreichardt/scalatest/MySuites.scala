/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2022, Christof Reichardt
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
import java.io.{File, PrintStream}
import java.nio.file.{Files, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant, LocalDateTime, ZoneId}
import org.scalatest.*
import org.scalatest.events.*

class MySuites(suites: Suite*) extends Suites(suites: _*) with Tracing with BeforeAndAfterAll with SequentialNestedSuiteExecution {

  override def run(testName: Option[String], args: Args): Status = {
    printf("%s.run%n", this.getClass.getSimpleName)
    TracerFactory.getInstance().readConfiguration(new File("." + File.separator + "config" + File.separator + "tracerfactory-config.xml"))
    val tracer = getCurrentTracer()
    tracer.open()
    tracer.initCurrentTracingContext(5, true)
    try {
      withTracer("Status", this, "run(testName: Option[String], args: Args)") {
        traceSuites(this)

        val myReporter = new MyReporter(args.reporter)
        val myArgs = new Args(myReporter, args.stopper, args.filter, args.configMap, args.distributor, args.tracker, args.chosenStyles, args.runTestInNewInstance,
          args.distributedTestSorter, args.distributedSuiteSorter)
        tracer.out().printfIndentln("testName = %s", testName)
        tracer.out().printfIndentln("myArgs = %s", myArgs)
        val status = super.run(testName, myArgs)
        tracer.out().printfIndentln("myReporter.succeeded = %d", myReporter.succeeded: java.lang.Integer)
        tracer.out().printfIndentln("de.christofreichardt.scala.scalatest.summary = %s", System.getProperty("de.christofreichardt.scala.scalatest.summary"))
        val print =
          if (System.getProperties.containsKey("de.christofreichardt.scala.scalatest.summary")) {
            val out = new PrintStream(Files.newOutputStream(Paths.get(System.getProperty("de.christofreichardt.scala.scalatest.summary"))))
            (out, true)
          } else {
            (tracer.out(), false)
          }
        try {
          printSummary(myReporter, print._1)
        } finally {
          if (print._2) print._1.close()
        }

        status
      }
    } finally {
      tracer.close()
    }
  }

  def traceSuites(suite: Suite): Unit = {
    val tracer = getCurrentTracer()
    withTracer("Unit", this, "traceSuites(suite: Suite)") {
      tracer.out().printfIndentln("%s[id=%s]", suite.suiteName, suite.suiteId)
      suite.nestedSuites.foreach(suite => traceSuites(suite))
    }
  }

  def printSummary(myReporter: MyReporter, out: PrintStream): Unit = {
    def traverseSuites(current: Suite): Unit = {

      def findStartTime(testName: String): Option[String] = {
        myReporter.events
          .find(starting => {
            starting match {
              case starting: TestStarting => starting.suiteId == current.suiteId && testName == starting.testName
              case _ => false
            }
          })
          .map(starting => LocalDateTime.ofInstant(Instant.ofEpochMilli(starting.timeStamp), ZoneId.systemDefault()))
          .map(startTime => startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
      }

      out.printf("\n%s\n", current.suiteName)
      out.printf("=====================================\n")
      if (current.nestedSuites.length != 0) {
        out.printf("Nested suites: %s\n", current.nestedSuites.mkString(", "))
      }
      myReporter.events
        .filter(event => {
          event match {
            case event: TestSucceeded => event.suiteId == current.suiteId
            case event: TestFailed => event.suiteId == current.suiteId
            case event: TestIgnored => event.suiteId == current.suiteId
            case event: TestCanceled => event.suiteId == current.suiteId
            case _ => false
          }
        })
        .map(event => new TestInfo(event))
        .foreach(testInfo => {
          val startTime = testInfo.testName.flatMap(name => findStartTime(name))
          val endTime = testInfo.timeStamp.map(timeStamp => LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault()))
          val duration = testInfo.duration.map(millis => Duration.ofMillis(millis).toString)
          out.printf("%s[startTime=%s, endtime=%s, duration=%sms, status=%s]\n",
            testInfo.testName.getOrElse(""), startTime.getOrElse(""), endTime.getOrElse(""), duration.getOrElse(""),
            testInfo.status.getOrElse(""))
        })
      out.println()
      current.nestedSuites.foreach(nestedSuite => traverseSuites(nestedSuite))
    }

    traverseSuites(this)

    out.println()
    out.println("=====================================")
    out.printf("Succeeded: %d\n", myReporter.succeeded)
    out.printf("   Failed: %d\n", myReporter.failed)
    out.printf(" Canceled: %d\n", myReporter.canceled)
    out.printf("  Ignored: %d\n", myReporter.ignored)
    out.println("=====================================")
    out.println()
  }

  override def beforeAll(): Unit = {
    withTracer("Unit", this, "beforeAll") {
      TracerFactory.getInstance().getTracer("TestTracer").open()
    }
  }

  override def afterAll(): Unit = {
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