/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2021, Christof Reichardt
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

/**
 * A particular combination of either selected, discarded or unprocessed items.
 *
 * @constructor Creates a particular combination.
 *
 * @param elements the items
 * @param k the number of to be selected items
 * @tparam T the type of the items
 */
class Combination[T](val elements: IndexedSeq[Element[T]], val k: Int) extends Tracing {
  require(k >= 0)
  require(elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).forall(elem => elem.state == State.NEITHER))

  val start: Int = elements.size - elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).size
  require(start <= elements.size)

  val remaining = elements.dropWhile(elem => elem.state == State.SELECTED || elem.state == State.DISCARDED).size
  require(remaining >= k)

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
    String.format("Combination[remaining=%d, k=%d, start=%d, elements=%s]", remaining, k, start, elements.mkString(","))
  }
}
