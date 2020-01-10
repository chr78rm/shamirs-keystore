package de.christofreichardt.scala.combinations

class Element[T](val item: T, val state: State.Value) {
  override def toString: String = state.toString + "(" + item.toString + ")"
}
