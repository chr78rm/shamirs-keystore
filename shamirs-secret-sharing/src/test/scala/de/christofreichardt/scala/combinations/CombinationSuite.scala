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

package de.christofreichardt.scala.combinations

import de.christofreichardt.scalatest.MyFunSuite

class CombinationSuite extends MyFunSuite {
  testWithTracing(this, "Combination(4, 2)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 2
    val combination = new Combination(elements, k)
    tracer.out().printfIndentln("combination = %s", combination)
    assert(combination.remaining == items.size)
    assert(combination.k == k)
  }

  testWithTracing(this, "selectFirst-1") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 2
    val combination = new Combination(elements, k).selectFirst()
    tracer.out().printfIndentln("combination = %s", combination)
    assert(combination.remaining == items.size - 1)
    assert(combination.k == k - 1)
  }

  testWithTracing(this, "selectFirst-2") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 0
    assertThrows[IllegalArgumentException] {
      val combination = new Combination(elements, k).selectFirst()
    }
  }

  testWithTracing(this, "discardFirst-1") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 2
    val combination = new Combination(elements, k).discardFirst()
    tracer.out().printfIndentln("combination = %s", combination)
    assert(combination.remaining == items.size - 1)
    assert(combination.k == k)
  }

  testWithTracing(this, "discardFirst-2") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 4
    assertThrows[IllegalArgumentException] {
      val combination = new Combination(elements, k).discardFirst()
    }
  }
}
