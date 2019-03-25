package de.christofreichardt.scala.shamir

import de.christofreichardt.scala.algorithms.BinomialCombinatorSuite
import de.christofreichardt.scalatest.MySuites
import de.christofreichardt.scalatest.MyDummySuite
import de.christofreichardt.scala.jws.JsonWebSignatureSuite

class ShamirSuites extends MySuites(
    new MyDummySuite(),
    new NewtonPolynomialSuite(),
    new NewtonInterpolationSuite(),
    new PolynomialSuite(),
    new SecretSharingSuite(),
    new SecretMergingSuite(),
    new JsonWebSignatureSuite(),
    new BinomialCombinatorSuite()) {
  
}