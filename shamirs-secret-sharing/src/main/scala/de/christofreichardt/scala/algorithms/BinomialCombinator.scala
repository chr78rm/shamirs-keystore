package de.christofreichardt.scala.algorithms

import de.christofreichardt.scala.diagnosis.Tracing

import scala.collection.immutable.Queue

/**
  * This class computes all combinations of k items selected from a given set of cardinality n without repetitions. The number of combinations
  * is given by the binomial coefficient.
  *
  *                              n!
  * Binomial coefficient := -------------
  *                         k! * (n - k)!
  *
  *  E.g. the number of combinations of 2 items selected from a set of cardinality 4 is 4!/[2! * (4 - 2)!] = 24/(2 * 2) = 6.
  *  Given M = {1,2,3,4} and k = 2 the combinations are C = {(1, 2), (1, 3), (2, 3), (1, 4), (2, 4), (3, 4)}.
  *
  * @param possibilities the set to choose from
  * @param k the number of items to be selected for each combination
  * @tparam T the type of the items within the set
  */
class BinomialCombinator[T](val possibilities: Set[T], val k: Int) extends Tracing {

  case class Item(val currentChoices: List[T], val take: Boolean, val currentSelection: Set[T]) {
    val nextSelection: Set[T] =
      if (take) currentSelection + currentChoices.head
      else currentSelection
    val nextChoices: List[T] = currentChoices.tail
    val hasNextChoices: Boolean = !nextChoices.isEmpty
    val isComplete: Boolean = nextSelection.size == k
    val isCompletable: Boolean = nextSelection.size + nextChoices.size >= k

    override def toString: String = String.format("Item[currentChoices = List(%s), take = %b, currentSelection = Set(%s), nextSelection = Set(%s), nextChoices = List(%s), hasNextChoices = %s]",
      currentChoices.mkString(","), take: java.lang.Boolean, currentSelection.mkString(","), nextSelection.mkString(","), nextChoices.mkString(","), hasNextChoices: java.lang.Boolean)
  }

  def computeCombinations(choices: List[T]): List[Set[T]] = {
    val tracer = getCurrentTracer()
    withTracer("List[Set[T]]", this, "computeCombinations(choices: List[T])") {
      tracer.out().printfIndentln("choices = List(%s)", choices.mkString(","))
      val solutions: List[Set[T]] = List.empty
      val queue: Queue[Item] = Queue(Item(choices, true, Set.empty), Item(choices, false, Set.empty))

      def compute(i: Int, queue: Queue[Item], solutions: List[Set[T]]): List[Set[T]] = {
        tracer.out().printfIndentln("i = %d, queue = Queue(%s), solutions = List(%s)", i: java.lang.Integer, queue.mkString(","), solutions.mkString(","))
        if (queue.isEmpty) solutions
        else {
          val (item, remainingQueue: Queue[Item]) = queue.dequeue
          if (item.isCompletable) {
            val item1 = Item(item.nextChoices, true, item.nextSelection)
            val item2 = Item(item.nextChoices, false, item.nextSelection)
            val (nextSolutions: List[Set[T]], nextQueue: Queue[Item]) =
              if (item1.isComplete  && item2.isComplete) (addSolutions(item1, item2, solutions), remainingQueue)
              else if (item1.isComplete) (addSolution(item1, solutions), appendItem(item2, remainingQueue))
              else if (item2.isComplete) (addSolution(item2, solutions), appendItem(item1, remainingQueue))
              else (solutions, appendItems(item1, item2, remainingQueue))
            compute(i + 1, nextQueue, nextSolutions)
          } else compute(i + 1, remainingQueue, solutions)
        }
      }

      def addSolution(item: Item, solutions: List[Set[T]]): List[Set[T]] = solutions :+ (item.nextSelection)
      def addSolutions(item1: Item, item2: Item, solutions: List[Set[T]]): List[Set[T]] = solutions :+ (item1.nextSelection) :+ (item2.nextSelection)
      def appendItem(item: Item, queue: Queue[Item]): Queue[Item] = queue.enqueue(item)
      def appendItems(item1: Item, item2: Item, queue: Queue[Item]): Queue[Item] = queue.enqueue(item1).enqueue(item2)

      compute(0, queue, solutions)
    }
  }

  val combinations: List[Set[T]] = computeCombinations(possibilities.toList)
}
