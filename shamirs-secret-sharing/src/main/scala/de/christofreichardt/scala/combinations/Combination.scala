package de.christofreichardt.scala.combinations

import de.christofreichardt.diagnosis.{AbstractTracer, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing

class Combination[T](val elements: IndexedSeq[Element[T]], val k: Int) extends Tracing {
  require(k >= 0)
  require(elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).forall(elem => elem.state == State.NEITHER))

  val start: Int = elements.size - elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).size
  require(start <= elements.size)

  val n = elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).size
  require(n >= k)

  def selectFirst(): Combination[T] = {
    withTracer("Combination[T]", this, "selectFirst()") {
      new Combination[T](IndexedSeq.tabulate(elements.size)(index => {
        if (index < start)  new Element(elements(index).item, elements(index).state)
        else if (index == start) new Element(elements(index).item, State.SELECTED)
        else new Element(elements(index).item, State.NEITHER)
      }), k - 1)
    }
  }

  def discardFirst(): Combination[T] = {
    withTracer("Combination[T]", this, "discardFirst()") {
      new Combination[T](IndexedSeq.tabulate(elements.size)(index => {
        if (index < start)  new Element(elements(index).item, elements(index).state)
        else if (index == start) new Element(elements(index).item, State.DISCARDED)
        else new Element(elements(index).item, State.NEITHER)
      }), k)
    }
  }

//  override def getCurrentTracer(): AbstractTracer = {
//    try {
//      TracerFactory.getInstance().getTracer("TestTracer")
//    }
//    catch {
//      case ex: TracerFactory.Exception => TracerFactory.getInstance().getDefaultTracer
//    }
//  }

  override def toString: String = {
    String.format("Combination[n=%d, k=%d, start=%d, elements=%s]", n, k, start, elements.mkString(","))
  }
}
