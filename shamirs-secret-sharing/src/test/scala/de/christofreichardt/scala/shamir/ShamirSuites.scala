package de.christofreichardt.scala.shamir

import de.christofreichardt.scala.combinations.BinomialCombinatorSuite
import de.christofreichardt.scala.jws.JsonWebSignatureSuite
import de.christofreichardt.scalatest.{MyDummySuite, MySuites}

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