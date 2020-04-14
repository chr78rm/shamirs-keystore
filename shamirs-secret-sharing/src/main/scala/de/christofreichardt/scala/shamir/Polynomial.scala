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

class Polynomial(
  val coefficients: IndexedSeq[BigInt],
  val prime:        BigInt)
  extends Tracing {

  require(prime.isProbablePrime(CERTAINTY))

  val a = coefficients.dropWhile(c => c == BigInt(0))
  val degree = a.length - 1
  val isZero: Boolean = degree == -1

  def evaluateAt(x: BigInt): BigInt = {
    withTracer("BigInt", this, "evaluateAt(x: BigInt)") {
      val tracer = getCurrentTracer()
      tracer.out().printfIndentln("x = %s", x)
      if (isZero) BigInt(0)
      else {
        Range.inclusive(0, degree)
          .map(i => {
            tracer.out().printfIndentln("a(%d) = %s", degree - i: Integer, a(i))
            (a(i) * x.modPow(degree - i, prime)).mod(prime)
          })
          .foldLeft(BigInt(0))((t0, t1) => (t0 + t1).mod(prime))
      }
    }
  }

  override def toString = String.format("Polynomial[a=(%s), degree=%d, isZero=%b, prime=%s]", a.mkString(","), degree: Integer, isZero: java.lang.Boolean, prime)

  override def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer
}