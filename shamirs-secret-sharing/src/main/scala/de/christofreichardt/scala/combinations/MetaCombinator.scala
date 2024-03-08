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

package de.christofreichardt.scala.combinations

/**
 * Produces all combinations from 'n choose 0' up to 'n choose n', that is the total number of the to be produced combinations
 * can be read from the nth line of Pascal's triangle.
 *
 * @param n denotes the number of elements in the basic set
 */
class MetaCombinator(val n: Int) {
  lazy val solutions: IndexedSeq[LazyList[IndexedSeq[Int]]] = IndexedSeq.tabulate(n + 1)(k => new LazyBinomialCombinator(n, k).produceAll)
}
