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

class BinomialCombinatorSuite extends MyFunSuite {

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4), 2)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4)
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
    val k = 2
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 3)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6), 3)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6)
    val k = 3
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 20)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6,7,8,9,10), 5)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6,7,8,9,10)
    val k = 5
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 252)
    tracer.out().printfIndentln("solutions = ")
    binomialCombinator.solutions.foreach(solution => tracer.out().printfIndentln("  %s", solution.mkString(",")))
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20), 5)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20)
    val k = 5
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 15504)
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,), 0)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4)
    val k = 0
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 1)
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,), 4)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4)
    val k = 4
    val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
    tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    assert(binomialCombinator.combination.start == 0)
    tracer.out().printfIndentln("size = %d", binomialCombinator.solutions.size)
    assert(binomialCombinator.solutions.size == 1)
  }

  testWithTracing(this, "BinomialCombinator(IndexedSeq(1,2,3,4,), 5)") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1,2,3,4)
    val k = 5
    val caught = intercept[IllegalArgumentException] {
      val binomialCombinator: BinomialCombinator[Int] = new BinomialCombinator[Int](items, k)
      tracer.out().printfIndentln("binomialCombinator = %s", binomialCombinator)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  testWithTracing(this, "LazyBinomialCombinator(6, 3)") {
    val tracer = getCurrentTracer()
    val n = 6
    val k = 3
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    tracer.out().println()
    tracer.out().printfIndentln("Solutions")
    tracer.out().printfIndentln("=========")
    solutions.foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().println()
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 20)
  }

  testWithTracing(this, "LazyBinomialCombinator(10, 5)") {
    val tracer = getCurrentTracer()
    val n = 10
    val k = 5
    val EXPECTED_SIZE = 252
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    tracer.out().println()
    tracer.out().printfIndentln("Solutions")
    tracer.out().printfIndentln("=========")
    solutions.take(59).foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().println()
    tracer.out().printfIndentln("...")
    tracer.out().println()
    solutions.drop(EXPECTED_SIZE - 10).foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == EXPECTED_SIZE)
  }

  testWithTracing(this, "LazyBinomialCombinator(20, 5)") {
    val tracer = getCurrentTracer()
    val n = 20
    val k = 5
    val EXPECTED_SIZE = 15504
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    tracer.out().println()
    tracer.out().printfIndentln("Solutions")
    tracer.out().printfIndentln("=========")
    solutions.take(20).foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().println()
    tracer.out().printfIndentln("...")
    tracer.out().println()
    solutions.drop(EXPECTED_SIZE - 20).foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 15504)
  }

  testWithTracing(this, "LazyBinomialCombinator(4, 0)") {
    val tracer = getCurrentTracer()
    val n = 4
    val k = 0
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    solutions.foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 1)
    assert(solutions.head == IndexedSeq.empty)
  }

  testWithTracing(this, "LazyBinomialCombinator(4, 4)") {
    val tracer = getCurrentTracer()
    val n = 4
    val k = 4
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    solutions.foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 1)
    assert(solutions.head == IndexedSeq(0,1,2,3))
  }

  testWithTracing(this, "LazyBinomialCombinator(4, 1)") {
    val tracer = getCurrentTracer()
    val n = 4
    val k = 1
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    solutions.foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 4)
  }

  testWithTracing(this, "LazyBinomialCombinator(0, 0)") {
    val tracer = getCurrentTracer()
    val n = 0
    val k = 0
    val lazyBinomialCombinator = new LazyBinomialCombinator(n, k)
    tracer.out().printfIndentln("lazyBinomialCombinator = %s", lazyBinomialCombinator)
    val solutions = lazyBinomialCombinator.produceAll
    solutions.foreach(solution => tracer.out().printfIndentln(solution.mkString(",")))
    tracer.out().printfIndentln("solutions.size = %d", solutions.size)
    assert(solutions.size == 1)
  }
}
