package de.christofreichardt.scalatest

import org.scalatest.events.{Event, TestCanceled, TestFailed, TestIgnored, TestSucceeded}

class TestInfo(event: Event) {
  val infos = event match {
    case event: TestSucceeded => (Some(event.suiteId), Some(event.testName), Some(event.timeStamp), event.duration, Some("succeeded"))
    case event: TestFailed => (Some(event.suiteId), Some(event.testName), Some(event.timeStamp), event.duration, Some("failed"))
    case event: TestCanceled => (Some(event.suiteId), Some(event.testName), Some(event.timeStamp), event.duration, Some("canceled"))
    case event: TestIgnored => (Some(event.suiteId), Some(event.testName), Some(event.timeStamp), Option.empty[Long], Some("ignored"))
    case _ => (Option.empty[String], Option.empty[String], Option.empty[Long], Option.empty[Long], Option.empty[String])
  }

  val suiteId = infos._1
  val testName = infos._2
  val timeStamp = infos._3
  val duration = infos._4
  val status = infos._5
}
