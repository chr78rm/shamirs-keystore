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

package de.christofreichardt.scala.shamir

import de.christofreichardt.scalatest.MyFunSuite

class NewtonPolynomialSuite extends MyFunSuite {

  testWithTracing(this, "Evaluation-1") {
    val tracer = getCurrentTracer()
    val p = 17
    val degree = 2
    val c = IndexedSeq(BigInt(5), BigInt(12), BigInt(7))
    val basis = IndexedSeq(BigInt(3), BigInt(7))
    val poly = new NewtonPolynomial(degree, basis, c, p)
    tracer.out().printfIndentln("poly = %s", poly)
    /*
     * f(x) := c(0) + c(1)*(x - x(0)) + c(2)*(x - x(0))*(x - x(1))
     */
    val x = BigInt(2)
    val y = poly.evaluateAt(x)
    tracer.out().printfIndentln("y = %s", y)
    assert(y == BigInt(11))
  }

  testWithTracing(this, "Preconditions-1") {
    val tracer = getCurrentTracer()
    val p = 17
    val degree = 3
    val c = IndexedSeq(BigInt(5), BigInt(12), BigInt(7), BigInt(16))
    val basis = IndexedSeq(BigInt(3), BigInt(7), BigInt(24))
    val caught =
      intercept[IllegalArgumentException] {
        val poly = new NewtonPolynomial(degree, basis, c, p)
        tracer.out().printfIndentln("poly = %s", poly)
      }
    tracer.out().printfIndentln("caught.getMessage = %s", caught.getMessage)
    assert(caught.getMessage.endsWith("Basis values must be pairwise different."))
  }

//  ignore(this, "Always-Fail") {
//    fail("This is a test.")
//  }
//
//  testWithTracing(this, "Always-Cancel") {
//    cancel("This is a test.")
//  }
}