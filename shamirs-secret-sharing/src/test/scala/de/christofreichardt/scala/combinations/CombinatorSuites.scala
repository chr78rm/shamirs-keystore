package de.christofreichardt.scala.combinations

import de.christofreichardt.scalatest.{MyDummySuite, MySuites}

class CombinatorSuites extends MySuites(
  new MyDummySuite,
  new CombinationSuite,
  new BinomialCombinatorSuite) {
}
