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