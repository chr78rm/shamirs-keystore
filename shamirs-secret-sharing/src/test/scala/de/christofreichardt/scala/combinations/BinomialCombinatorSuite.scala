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

package de.christofreichardt.scala.combinations

import de.christofreichardt.scalatest.MyFunSuite

class BinomialCombinatorSuite extends MyFunSuite {
  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4), 2)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4)
    val n = items.size
    val k = 2
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 6)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3), 2)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3)
    val n = items.size
    val k = 2
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    val solutions = binomialCombinator.produce
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 3)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6), 3)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6)
    val n = items.size
    val k = 3
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    val solutions = binomialCombinator.produce
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 20)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6,7,8,9,10), 5)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6,7,8,9,10)
    val n = items.size
    val k = 5
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    val solutions = binomialCombinator.produce
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 252)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20), 5)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20)
    val n = items.size
    val k = 5
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    val solutions = binomialCombinator.produce
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 15504)
  }
}
