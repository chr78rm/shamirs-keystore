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

package de.christofreichardt.scala.shamir

import de.christofreichardt.scalatest.MyFunSuite
import de.christofreichardt.scala.utils.RandomGenerator

class PolynomialSuite extends MyFunSuite {

  testWithTracing(this, "Evaluation-1") {
    val tracer = getCurrentTracer()
    val coefficients = IndexedSeq(BigInt(5), BigInt(12), BigInt(0))
    val prime = 7
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == 2)
    assert(!polynomial.isZero)
    assert(polynomial.evaluateAt(BigInt(7)) == BigInt(0))
    assert(polynomial.evaluateAt(BigInt(5)) == BigInt(3))
  }

  testWithTracing(this, "Evaluation-2") {
    val tracer = getCurrentTracer()
    val coefficients = IndexedSeq(BigInt(0))
    val prime = 7
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == -1)
    assert(polynomial.isZero)
    assert(polynomial.evaluateAt(BigInt(7)) == BigInt(0))
    assert(Range(0, prime).forall(x => polynomial.evaluateAt(x) == BigInt(0)))
  }

  testWithTracing(this, "Evaluation-3") {
    val tracer = getCurrentTracer()
    val coefficients = IndexedSeq(BigInt(0), BigInt(5), BigInt(12), BigInt(0))
    val prime = 7
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == 2)
    assert(!polynomial.isZero)
    assert(polynomial.evaluateAt(BigInt(7)) == BigInt(0))
    assert(polynomial.evaluateAt(BigInt(5)) == BigInt(3))
  }

  testWithTracing(this, "Evaluation-4") {
    val tracer = getCurrentTracer()
    val coefficients = IndexedSeq(BigInt(23), BigInt(15), BigInt(32), BigInt(7))
    val prime = 89
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == 3)
    assert(!polynomial.isZero)
    val randomGenerator = new RandomGenerator
    val BIT_SIZE = 16
    val SAMPLES = 4
    val points = randomGenerator.bigIntStream(BIT_SIZE, prime)
      .take(SAMPLES)
      .map(x => (x, polynomial.evaluateAt(x)))
    tracer.out().printfIndentln("points = %s", points.mkString(","))  
  }
}