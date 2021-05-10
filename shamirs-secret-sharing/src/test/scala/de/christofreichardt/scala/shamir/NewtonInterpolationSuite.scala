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

package de.christofreichardt.scala.shamir

import de.christofreichardt.scalatest.MyFunSuite
import de.christofreichardt.scala.utils.RandomGenerator

class NewtonInterpolationSuite extends MyFunSuite {

  /*
   * Supporting points must be pairwise different and unambiguous.
   */
  testWithTracing(this, "Preconditions-1") {
    val tracer = getCurrentTracer()
    val prime = BigInt(7)
    val violatingPreconditionPoints: IndexedSeq[(BigInt, BigInt)] = IndexedSeq((BigInt(5), BigInt(25)), (BigInt(4), BigInt(16)), (BigInt(5), BigInt(30)))
    val caught = intercept[IllegalArgumentException] {
      new NewtonInterpolation(violatingPreconditionPoints, prime)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  /*
   * We need a prime for computing within a finite field.
   */
  testWithTracing(this, "Preconditions-2") {
    val tracer = getCurrentTracer()
    val prime = BigInt(6)
    val violatingPreconditionPoints: IndexedSeq[(BigInt, BigInt)] = IndexedSeq((BigInt(5), BigInt(25)), (BigInt(4), BigInt(16)), (BigInt(6), BigInt(30)))
    val caught = intercept[IllegalArgumentException] {
      new NewtonInterpolation(violatingPreconditionPoints, prime)
    }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
  }

  /*
   * Tests the subroutine for computing expressions of the form (x(i) - x(0))*(x(i) - x(1))* ... *(x(i) - x(j)), i > j 
   */
  testWithTracing(this, "Differences-1") {
    val tracer = getCurrentTracer()
    val prime = BigInt(41)
    val ps = IndexedSeq((BigInt(5), BigInt(25)), (BigInt(22), BigInt(16)), (BigInt(17), BigInt(28)), (BigInt(31), BigInt(8))) // actually only the x-ccordinates will be evaluated
    val interpolation = new NewtonInterpolation(ps, prime)
    val product = interpolation.multiplyDifferences(3, 2, ps.map(p => p._1))
    tracer.out().printfIndentln("product = %s", product)
    assert(product == BigInt(37))
  }

  /*
   * P0=(5,25), P1=(22,16), P2=(17,28), P3=(31,8), prime = 41
   * 
   * We take the supporting points above and feed them into the newton interpolation algorithm. This
   * gives us the Newton coefficients and hence a Newton polynomial which contains these points.
   */
  testWithTracing(this, "Coefficients-1") {
    val tracer = getCurrentTracer()
    val prime = BigInt(41)
    val ps = IndexedSeq((BigInt(5), BigInt(25)), (BigInt(22), BigInt(16)), (BigInt(17), BigInt(28)), (BigInt(31), BigInt(8)))
    val interpolation = new NewtonInterpolation(ps, prime)
    tracer.out().printfIndentln("interpolation = %s", interpolation)
    val DEGREE = 3
    val newtonPolynomial = interpolation.newtonPolynomial
    tracer.out().printfIndentln("newtonPolynomial = %s", newtonPolynomial)
    assert(DEGREE == newtonPolynomial.degree)
    assert(ps.forall(p => newtonPolynomial.evaluateAt(p._1) == p._2))
  }

  /*
   * P0=(3,78), P1=(22,12), P2=(27,89), P3=(31,8), P4=(72,97), prime = 101
   * 
   * We take the supporting points above and feed them into the newton interpolation algorithm. This
   * gives us the Newton coefficients and hence a Newton polynomial which contains these points.
   */
  testWithTracing(this, "Coefficients-2") {
    val tracer = getCurrentTracer()
    val prime = BigInt(101)
    val ps = IndexedSeq((BigInt(3), BigInt(78)), (BigInt(22), BigInt(12)), (BigInt(27), BigInt(89)), (BigInt(31), BigInt(8)), (BigInt(72), BigInt(97)))
    val interpolation = new NewtonInterpolation(ps, prime)
    val DEGREE = 4
    val newtonPolynomial = interpolation.newtonPolynomial
    tracer.out().printfIndentln("newtonPolynomial = %s", newtonPolynomial)
    assert(DEGREE == newtonPolynomial.degree)
    assert(ps.forall(p => newtonPolynomial.evaluateAt(p._1) == p._2))
  }

  /*
   * p(x) := 23x^3 + 15x^2 + 32x + 7 (mod 89)
   *
   * First, this test samples some random points by using the canonical form. Next
   * we feed this points into the interpolation algorithm to compute the
   * Newton Polynomial. Finally we compare the whole co-domain (inclusive the
   * original points) calculated by the Horner scheme against the results from
   * the canonical form.
   */
  testWithTracing(this, "Interpolation-1") {
    val tracer = getCurrentTracer()
    val coefficients = IndexedSeq(BigInt(23), BigInt(15), BigInt(32), BigInt(7))
    val prime = 89
    val DEGREE = 3
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == DEGREE)
    assert(!polynomial.isZero)
    val randomGenerator = new RandomGenerator
    val BIT_SIZE = 16
    val SAMPLES = polynomial.degree + 1 // == 4, we need (n + 1) supporting points to compute a polynom of degree n
    val ps = randomGenerator.bigIntStream(BIT_SIZE, prime)
      .distinct
      .take(SAMPLES)
      .map(x => (x, polynomial.evaluateAt(x)))
      .toIndexedSeq
    tracer.out().printfIndentln("points = %s", ps.mkString(","))
    val interpolation = new NewtonInterpolation(ps, prime)
    val newtonPolynomial = interpolation.newtonPolynomial
    assert(newtonPolynomial.degree == polynomial.degree)
    tracer.out().printfIndentln("newtonPolynomial = %s", newtonPolynomial)
    val coDomain = Range(0, prime).map(x => {
      (BigInt(x), newtonPolynomial.evaluateAt(x))
    })
    assert(ps.forall(p => coDomain.contains(p)))
    assert(coDomain.forall(p => p._2 == polynomial.evaluateAt(p._1)))
  }

  /*
   * p(x) := r(8)x^8 + r(7)x^7 + r(6)x^6 + r(5)x^5 + r(4)x^4 + r(3)x^3 + r(2)x^2 + r(1)x + r(0) (mod p), p prime, 512 <= p < 1024
   *
   * Now we even compute a random polynom in canonical form with degree 8 and
   * sample some random points. Next we feed this points into the interpolation
   * algorithm to compute the Newton polynomial. Finally we compare the whole
   * co-domain (inclusive the original points) calculated by the Horner scheme
   * against the results from the canonical form.
   */
  testWithTracing(this, "Interpolation-2") {
    val tracer = getCurrentTracer()
    val randomGenerator = new RandomGenerator
    val PRIME_BIT_SIZE = 10
    val DEGREE = 8
    val prime = randomGenerator
      .bigPrimeStream(PRIME_BIT_SIZE, CERTAINTY)
      .head
    assert(prime >= BigInt(512) && prime  < BigInt(1024))
    val BIT_SIZE = 32
    val coefficients = randomGenerator
      .bigIntStream(BIT_SIZE)
      .map(c => c.mod(prime))
      .filter(c => c != BigInt(0))
      .take(DEGREE + 1)
      .toIndexedSeq
    val polynomial = new Polynomial(coefficients, prime)
    tracer.out().printfIndentln("polynomial = %s", polynomial)
    assert(polynomial.degree == DEGREE)
    assert(!polynomial.isZero)
    val SAMPLES = polynomial.degree + 1 // == 9, we need (n + 1) supporting points to compute a polynom of degree n
    val ps = randomGenerator.bigIntStream(BIT_SIZE, prime)
      .distinct
      .take(SAMPLES)
      .map(x => (x, polynomial.evaluateAt(x)))
      .toIndexedSeq
    tracer.out().printfIndentln("points = %s", ps.mkString(","))
    val interpolation = new NewtonInterpolation(ps, prime)
    val newtonPolynomial = interpolation.newtonPolynomial
    assert(newtonPolynomial.degree == polynomial.degree)
    tracer.out().printfIndentln("newtonPolynomial = %s", newtonPolynomial)
    val coDomain = Range(0, prime.toInt).map(x => {
      (BigInt(x), newtonPolynomial.evaluateAt(x))
    })
    assert(ps.forall(p => coDomain.contains(p)))
    assert(coDomain.forall(p => p._2 == polynomial.evaluateAt(p._1)))
  }
}