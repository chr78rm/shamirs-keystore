package de.christofreichardt.scala.shamir

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
    new JsonWebSignatureSuite()) {
  
}