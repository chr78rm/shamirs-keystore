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
}