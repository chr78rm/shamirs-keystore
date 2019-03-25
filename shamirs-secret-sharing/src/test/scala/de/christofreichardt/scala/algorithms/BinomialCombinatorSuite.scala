package de.christofreichardt.scala.algorithms

import de.christofreichardt.scalatest.MyFunSuite

class BinomialCombinatorSuite extends MyFunSuite {

  testWithTracing(this, "Combinations-1") {
    val tracer = getCurrentTracer
    val combinator = new BinomialCombinator[Int](Set(1,2,3,4,5,6,7,8), 4)
    tracer.out().printfIndentln("combinations = (%s)", combinator.combinations.mkString(", "))
    tracer.out().printfIndentln("size = %d", combinator.combinations.size: Integer)
    assert(combinator.combinations.size == 70)
  }

  testWithTracing(this, "Combinations-2") {
    val tracer = getCurrentTracer
    val combinator = new BinomialCombinator[Int](Set(1,2,3,4,5,6), 3)
    tracer.out().printfIndentln("combinations = (%s)", combinator.combinations.mkString(", "))
    tracer.out().printfIndentln("size = %d", combinator.combinations.size: Integer)
    assert(combinator.combinations.size == 20)
    combinator.combinations.map(combination => combination.toList.sorted.mkString)
      .sorted
      .foreach(combination => tracer.out().printfIndentln("combination = %s", combination))
  }

  testWithTracing(this, "Combinations-3") {
    val tracer = getCurrentTracer
    val combinator = new BinomialCombinator[Int](Range.inclusive(1,15).toSet, 4)
    tracer.out().printfIndentln("size = %d", combinator.combinations.size: Integer)
    assert(combinator.combinations.size == 1365)
  }

//  testWithTracing(this, "Combinations-4") {
//    val tracer = getCurrentTracer
//    val combinator = new BinomialCombinator[Int](Range.inclusive(1,15).toSet, 7)
//    tracer.out().printfIndentln("size = %d", combinator.combinations.size: Integer)
//    assert(combinator.combinations.size == 6435)
//  }

}
