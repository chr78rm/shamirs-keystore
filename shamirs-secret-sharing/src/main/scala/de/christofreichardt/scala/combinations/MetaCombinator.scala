package de.christofreichardt.scala.combinations

class MetaCombinator(val n: Int) {
  lazy val solutions: IndexedSeq[LazyList[IndexedSeq[Int]]] = IndexedSeq.tabulate(n + 1)(k => new LazyBinomialCombinator(n, k).produceAll)
}
