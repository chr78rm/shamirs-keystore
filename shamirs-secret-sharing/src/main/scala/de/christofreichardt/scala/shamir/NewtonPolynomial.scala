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

import de.christofreichardt.scala.diagnosis.Tracing
import de.christofreichardt.diagnosis.AbstractTracer
import de.christofreichardt.diagnosis.TracerFactory
import scala.collection.GenTraversable
import scala.annotation.tailrec

class NewtonPolynomial(
  val degree:       Int,
  val basis:        IndexedSeq[BigInt],  
  val coefficients: IndexedSeq[BigInt],
  val prime:        BigInt) extends Tracing {

  require(prime.isProbablePrime(CERTAINTY))
  val n = degree
  require(basis.length == n)
  require(coefficients.length == n + 1)
  val c = coefficients.map(b => b.mod(prime))
  val xx = basis.map(b => b.mod(prime))
  require(pairwiseDifferent(xx.toList), "Basis values must be pairwise different.")

  def pairwiseDifferent(values: List[BigInt]): Boolean = {
    values match {
      case List(v) => true
      case head :: tail => {
        if (tail.exists(v => v == head)) false
        else pairwiseDifferent(tail)
      }
    }
  }

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

  override def toString = String.format("NewtonPolynomial[degree=%d, c=(%s), x=(%s), prime=%s]", n: Integer, c.mkString(","), xx.mkString(","), prime)

  override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer
}