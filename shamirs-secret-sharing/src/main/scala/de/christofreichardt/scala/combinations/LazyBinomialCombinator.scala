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

import de.christofreichardt.diagnosis.{AbstractTracer, TracerFactory}
import de.christofreichardt.scala.diagnosis.Tracing

/**
 * Produces all combinations of k unordered integers which can be chosen from among n integers (k <= n) by applying a lexicographic
 * algorithm. The starting point is given by the lexicographic smallest word comprising k integers, e.g. (0,1,2) for k == 3. First, the algorithm
 * evaluates the rightmost column in order to find new combinations, e.g. (0,1,3), (0,1,4) and (0,1,5) for n == 6. Now the algorithm has run out of
 * options for the rightmost column and switches to (0,2,3) but not (0,2,1) since the latter would be identical to (0,1,2). In due course
 * more to the left columns have to be considered, e.g. at (0,4,5). The algorithm finishes if the options have run out for all columns. For
 * k == 3 and n == 6 that would be (3,4,5). The total number of solutions is given by the binomial coefficient "n choose k". For the given
 * example that would be 20. To compute actual solution sizes , see <a href="https://www.wolframalpha.com/input/?i=binomial+coefficient">WolframAlpha</a>.
 *
 * @constructor Creates a problem instance "n choose k".
 * @param n defines the basic set of integers
 * @param k the number of integers which are to be chosen from the basic set
 */
class LazyBinomialCombinator(val n: Int, val k: Int) extends Tracing {
  require(n >= k)

  /** The lexicographic smallest solution */
  val firstSolution: IndexedSeq[Int] = IndexedSeq.tabulate(this.k)(index => index)

  /** indicates the highest possible values for every column and therefore the terminating condition as well */
  val highWatermark: IndexedSeq[Int] = IndexedSeq.tabulate(this.k)(index => this.n - 1 - index).reverse

  /** we need to look backwards into a solution when checking for the next solution */
  val reversedIndices: IndexedSeq[Int] = this.firstSolution.indices.reverse

  /**
   * Checks if there is a next solution for a given word.
   *
   * @param solution the current solution
   * @return true if there is another solution
   */
  def hasNextSolution(solution: IndexedSeq[Int]): Boolean = this.reversedIndices.exists(reversedIndex => solution(reversedIndex) < this.highWatermark(reversedIndex))

  /**
   * Given a solution the lexicographic next solution will be produced, if any. Most of the time only the value of the last (lowest-order or rightmost) column changes from one solution
   * to the next solution. Sometimes more values are flipping. Depending on the position of the leftmost (discriminator) column with a changing value there are three cases to consider:
   *
   * (1) the value at the position of the dicriminator column will be incremented by one related to the value of the previous solution at this position
   *
   * (2) values to the left of the discriminator column will be copied from the previous solution
   *
   * (3) values to the right of the discriminator column will be incremeted one by one based on the new value at the discriminator column
   *
   * @param solution the current solution
   * @return some solution or none
   */
  def findNextSolution(solution: IndexedSeq[Int]): Option[IndexedSeq[Int]] = withTracer("Option[IndexedSeq[Int]]", this, "findNextSolution(solution: IndexedSeq[Int])") {

    def findNextColumn: Option[Int] = this.reversedIndices.find(reversedIndex => solution(reversedIndex) < this.highWatermark(reversedIndex))

    val nextColumn = findNextColumn
    nextColumn.map(column =>
      IndexedSeq.tabulate(this.k)(index =>
        if (index < column) solution(index)
        else if (index == column) solution(index) + 1
        else solution(column) + 1 + (index - column)
      )
    )
  }

  /**
   * Produces a `LazyList` of solutions given a particular solution.
   *
   * @param solution the starting point
   * @return a `LazyList` comprising the solutions
   */
  def solutions(solution: IndexedSeq[Int]): LazyList[IndexedSeq[Int]] = {
    if (hasNextSolution(solution)) {
      val nextSolution = findNextSolution(solution)
      LazyList.cons(solution, solutions(nextSolution.get))
    } else {
      LazyList.cons(solution, LazyList.empty)
    }
  }

  /**
   * Produces a `LazyList` of all solutions. Solutions are computed only when they are needed.
   * This is great if the solution space is very big.
   *
   * @return a `LazyList` comprising the solutions
   */
  def produceAll: LazyList[IndexedSeq[Int]] = solutions(this.firstSolution)

  /**
   * Gives a textual representation of the problem instance.
   *
   * @return the textual presentation
   */
  override def toString: String = String.format("LazyBinomialCombinator[n=%d, k=%d]", this.n, this.k)

}
