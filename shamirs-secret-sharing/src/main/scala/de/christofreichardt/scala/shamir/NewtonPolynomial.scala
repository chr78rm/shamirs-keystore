/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2024, Christof Reichardt
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

import de.christofreichardt.diagnosis.{AbstractTracer, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing

/**
 * Defines a Newton Polynomial.
 *
 * @constructor Creates an instance of a Newton Polynomial suitable for calculations using finite field algebra.
 * @param degree the degree of the polynomial
 * @param basis the Newton basis polynomials
 * @param coefficients The Newton coefficients
 * @param prime the prime modulus
 */
class NewtonPolynomial(
  val degree:       Int,
  val basis:        IndexedSeq[BigInt],  
  val coefficients: IndexedSeq[BigInt],
  val prime:        BigInt) extends Tracing {

  require(prime.isProbablePrime(CERTAINTY))

  /** alias for `degree` */
  val n: Int = degree

  require(basis.length == n)
  require(coefficients.length == n + 1)

  /** the residues of the `coefficients` mod `prime` */
  val c: Seq[BigInt] = coefficients.map(b => b.mod(prime))
  /** the residues of the `basis` mod `prime` */
  val xx: Seq[BigInt] = basis.map(b => b.mod(prime))

  require(pairwiseDifferent(xx.toList), "Basis values must be pairwise different.")

  def pairwiseDifferent(values: List[BigInt]): Boolean = values.distinct.length == values.length

  /**
   * Computes y = P(x).
   *
   * @param x the x value
   * @return the y value
   */
  def evaluateAt(x: BigInt): BigInt = {
    val tracer = getCurrentTracer()

    def evaluate(m: Int): BigInt = {
      tracer.out().printfIndentln("m = %d", m: Integer)
      if (m == n) c(n)
      else (evaluate(m + 1) * (x - xx(m)) + c(m)).mod(prime)
    }

    withTracer("BigInt", this, "evaluateAt(x: BigInt)") {
      tracer.out().printfIndentln("x = %s", x)
      evaluate(0)
    }
  }

  /**
   * Returns a string representation of the NewtonPolynomial.
   *
   * @return the string representation of the NewtonPolynomial
   */
  override def toString: String = String.format("NewtonPolynomial[degree=%d, c=(%s), x=(%s), prime=%s]", n: Integer, c.mkString(","), xx.mkString(","), prime)

  override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer
}