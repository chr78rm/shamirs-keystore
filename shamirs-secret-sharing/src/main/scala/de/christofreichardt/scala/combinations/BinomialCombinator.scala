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

package de.christofreichardt.scala.combinations

import de.christofreichardt.diagnosis.{AbstractTracer, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing

import scala.collection.mutable.ListBuffer

class BinomialCombinator[T](items: IndexedSeq[T], val k: Int) extends Tracing {
  require(items.size >= k)

  val combination: Combination[T] = new Combination(IndexedSeq.tabulate(items.size)(index => new Element(items(index), State.NEITHER)), k)

  lazy val solutions: List[IndexedSeq[T]] = {
    produce
      .toList
      .map(seq => seq.filter(element => element.state == State.SELECTED))
      .map(seq => seq.map(element => element.item))
  }

  def produce: ListBuffer[IndexedSeq[Element[T]]] = {

    def combinate(solutions: ListBuffer[IndexedSeq[Element[T]]], current: Combination[T]): Unit = {
      withTracer("Unit", this, "combinate(solutions: ListBuffer[IndexedSeq[Element[T]]])") {
        val tracer = getCurrentTracer()
        tracer.out().printfIndentln("current = %s", current)
        if (current.k == 0) solutions.append(current.elements)
        else {
          combinate(solutions, current.selectFirst())
          if (current.k < current.n) combinate(solutions, current.discardFirst())
        }
      }
    }

    withTracer("ListBuffer[IndexedSeq[Element[T]]]", this, "produce()") {
      val solutions = ListBuffer.empty[IndexedSeq[Element[T]]]
      combinate(solutions, this.combination)
      solutions
    }
  }

  override def toString: String = String.format("BinomialCombinator[combination=%s]", this.combination)

//  override def getCurrentTracer(): AbstractTracer = {
//    try {
//      TracerFactory.getInstance().getTracer("TestTracer")
//    }
//    catch {
//      case ex: TracerFactory.Exception => TracerFactory.getInstance().getDefaultTracer
//    }
//  }
}
