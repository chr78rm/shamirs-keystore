package de.christofreichardt.scala.combinations

object State extends Enumeration {
  type State = Value
  val SELECTED, DISCARDED, NEITHER = Value
}
