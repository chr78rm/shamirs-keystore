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
    assert(combination.n == items.size)
    assert(combination.k == k)
  }

  testWithTracing(this, "selectFirst-1") {
    val tracer = getCurrentTracer()
    val items = IndexedSeq(1, 2, 3, 4)
    val elements = IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER))
    val k = 2
    val combination = new Combination(elements, k).selectFirst()
    tracer.out().printfIndentln("combination = %s", combination)
    assert(combination.n == items.size - 1)
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
    assert(combination.n == items.size - 1)
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
