/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2021, Christof Reichardt
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

class MyDummySuite extends MyFunSuite {
  
  testWithTracing(this, "System Properties") {
    val propertyNames = System.getProperties().stringPropertyNames.toArray(new Array[String](0)).sorted
    propertyNames.foreach(propertyName => getCurrentTracer().out().printfIndentln("%s = %s", propertyName, System.getProperty(propertyName)))
  }

}