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
