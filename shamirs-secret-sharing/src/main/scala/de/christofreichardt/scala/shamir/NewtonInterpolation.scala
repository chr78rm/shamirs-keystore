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

import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory
import scala.annotation.tailrec

/**
 * Implements Newtons interpolation algorithm. All calculations will be carried out by finite field algebra.
 *
 * @constructor Creates a new NewtonInterpolation by applying some supporting points and a prime number.
 *
 * @param supportingPoints some pairwise different supporting points
 * @param prime            a prime number
 */
class NewtonInterpolation(val supportingPoints: IndexedSeq[(BigInt, BigInt)], val prime: BigInt) extends Tracing {

  require(prime.isProbablePrime(CERTAINTY), String.format("%s isn't prime.", prime))
  require(pairWiseDifferent(supportingPoints), "Supporting points must be pairwise different and unambiguous.")

  /** n supporting points give a polynom of degree n - 1 */
  val degree: Int = supportingPoints.length - 1

  /**
   * Creates lazily the NewtonPolynomial by computing the Newton Coefficients. For the definition of a NewtonPolynomial with degree n - 1 we need only n - 1 x values projected
   * from the supporting points whereas n of them are needed for the computation of the coefficients.
   */
  lazy val newtonPolynomial: NewtonPolynomial = new NewtonPolynomial(degree, supportingPoints.take(supportingPoints.length - 1).map(p => p._1), computeCoefficients(), prime)

  def pairWiseDifferent(points: IndexedSeq[(BigInt, BigInt)]): Boolean = {
    @tailrec
    def check(ps: IndexedSeq[(BigInt, BigInt)], xs: Set[BigInt]): Boolean = {
      if (ps.isEmpty) true
      else {
        val x = ps.head._1
        if (xs.contains(x)) false
        else check(ps.tail, xs + x)
      }
    }

    check(points, Set.empty[BigInt])
  }

  /**
   * Computes below expression.
   *
   * <pre>
   *   (x(i) - x(0))*(x(i) - x(1))* ... *(x(i) - x(j)), i > j
   * </pre>
   *
   * Expressions of this form need to be evaluated during the calculation
   * of the Newton coefficients.
   *
   * @param i references the x-ccordinate of a supporting point (always in minuend position)
   * @param j denotes the upper index of the x-coordinates in subtrahend position 
   * @return the value of the term (mod prime)
   */
  def multiplyDifferences(i: Int, j: Int, xs: IndexedSeq[BigInt]): BigInt = {
    withTracer("BigInt", this, "multiplyDifferences(i: Int, j: Int, xs: IndexedSeq[BigInt])") {
      val tracer = getCurrentTracer()
      tracer.out().printfIndentln("i = %d, j = %d, xs = (%s)", i: Integer, j: Integer, xs.mkString(","))
      require(i > j)
      Range.inclusive(0, j)
        .map(k => {
          tracer.out().printfIndentln("xs(%d) = %s, xs(%d) = %s", i: Integer, xs(i), k: Integer, xs(k))
          (xs(i) - xs(k)).mod(prime)
        })
        .foldLeft(BigInt(1))((x1, x2) => (x1 * x2).mod(prime))
    }
  }

  /**
   * Supporting points :=  (x(0), y(0)), ..., (x(n), y(n)).
   *
   * Computes the newton coefficients c(n)...c(0) by dynamic programming. 
   *
   * <pre>
   *         y(n) - c(0) - c(1)*(x(n) - x(0)) - ... - c(n-1)*((x(n) - x(0))*...*(x(n) - x(n-2))
   * c(n) := ---------------------------------------------------------------------------------- (mod prime)
   *                             (x(n) - x(0))* ... *(x(n) - x(n-1))
   *
   *         y(1) - c(0)
   * c(1) := ----------- (mod prime)
   *         x(1) - x(0)
   *
   * c(0) := y(0) (mod prime)
   * </pre>
   *
   * Following applies: (n + 1) == number of supporting points. This gives a polynom of degree n 
   * with (n + 1) Newton coefficients.
   *
   * @return the calculated newton coefficients
   */
  def computeCoefficients(): IndexedSeq[BigInt] = {
    val tracer = getCurrentTracer()
    val memoCoefficients: scala.collection.mutable.Map[Int, BigInt] = scala.collection.mutable.Map.empty[Int, BigInt]

    def computeCoefficient(index: Int): BigInt = {
      withTracer("BigInt", this, "computeCoefficient(index: Int)") {
        tracer.out().printfIndentln("index = %d, memoCoefficients = %s", index: Integer, memoCoefficients)

        if (memoCoefficients.contains(index)) memoCoefficients(index)
        else if (index == 0) {
          memoCoefficients += (index -> supportingPoints(0)._2)
          memoCoefficients(index)
        } else {
          val subtrahend =
            Range.inclusive(0, index - 1)
              .map(i => {
                val c = computeCoefficient(i)
                val product =
                  if (i == 0) BigInt(1)
                  else multiplyDifferences(index, i - 1, supportingPoints.map(p => p._1))
                c * product
              })
              .foldLeft(BigInt(0))((t0, t1) => (t0 + t1).mod(prime))
          val y = supportingPoints(index)._2
          val numerator = (y - subtrahend).mod(prime)
          tracer.out().printfIndentln("numerator = %s", numerator)
          tracer.out().printfIndentln("index = %d", index: Integer)
          val denominator = multiplyDifferences(index, index - 1, supportingPoints.map(p => p._1))
          tracer.out().printfIndentln("denominator = %s", denominator)
          val c = (numerator * denominator.modInverse(prime)).mod(prime)
          memoCoefficients += (index -> c)
          memoCoefficients(index)
        }
      }
    }

    withTracer("IndexedSeq[BigInt]", this, "computeCoefficients()") {
      computeCoefficient(supportingPoints.length - 1)
      Range(0, supportingPoints.length).map(i => memoCoefficients(i))
    }
  }

  override def toString: String = String.format("NewtonInterpolation[supportingPoints=(%s), prime=%s]", supportingPoints.mkString(","), prime)

  override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer

}