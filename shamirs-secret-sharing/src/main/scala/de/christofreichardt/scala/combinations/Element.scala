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

/**
 * Acts as container for items which can either be SELECTED, DISCARDED or unprocessed (NEITHER).
 *
 * @constructor Creates a SELECTED, DISCARDED or unprocessed (NEITHER) Element.
 *
 * @param item the item
 * @param state SELECTED, DISCARDED or unprocessed (NEITHER)
 * @tparam T the type of the item
 */
class Element[T](val item: T, val state: State.Value) {
  override def toString: String = state.toString + "(" + item.toString + ")"
}
