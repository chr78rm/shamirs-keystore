package de.christofreichardt.scala.combinations

import de.christofreichardt.scala.diagnosis.Tracing

class LazyBinomialCombinator(val n: Int, val k: Int) extends Tracing {

  val firstSolution: IndexedSeq[Int] = IndexedSeq.tabulate(this.k)(index => index)

  def hasNextSolution(solution: IndexedSeq[Int]): Boolean =
    solution.indices.zip(solution.indices.reverse)
      .find(index => solution(index._2) < n - 1 - index._1)
      .isDefined

  def findNextSolution(solution: IndexedSeq[Int]): Option[IndexedSeq[Int]] = {

    def findNextColumn: Option[Int] =
      solution.indices.zip(solution.indices.reverse)
        .find(index => solution(index._2) < n - 1 - index._1)
        .map(index => index._2)

    val nextColumn = findNextColumn
    nextColumn.map(column =>
      IndexedSeq.tabulate(this.k)(index =>
        if (index < column) solution(index)
        else if (index == column) solution(index) + 1
        else solution(column) + 1 + (index - column)
      )
    )
  }

  def solutions(solution: IndexedSeq[Int]): LazyList[IndexedSeq[Int]] = {
    if (hasNextSolution(solution)) {
      val nextSolution = findNextSolution(solution)
      LazyList.cons(solution, solutions(nextSolution.get))
    } else {
      LazyList.cons(solution, LazyList.empty)
    }
  }

}
