package de.christofreichardt.scala.utils

import scala.BigInt
import scala.Stream
import scala.util.Random
import scala.annotation.tailrec

/**
 * @author Christof Reichardt
 */
class RandomGenerator(secureRandom: java.security.SecureRandom) {
  val random = new Random(secureRandom)
  
  def this() = this(new java.security.SecureRandom)
  
  final def bigIntStream(numberOfBits: Int, p: BigInt): Stream[BigInt] = {
    val next = BigInt(numberOfBits, random).mod(p)
    Stream.cons(next, bigIntStream(numberOfBits, p))
  }
  
  final def distinctBigIntStream(numberOfBits: Int, p: BigInt, consumedSet: Set[BigInt]): Stream[BigInt] = {
    val MAX_DEPTH = 100
    @tailrec
    def findNextBigInt(i: Int): BigInt = {
      if (i > MAX_DEPTH) throw new NoSuchElementException
      val possibleBigInt =  BigInt(numberOfBits, random).mod(p)
      if (!consumedSet.contains(possibleBigInt)) possibleBigInt
      else findNextBigInt(i + 1)
    }
    val next = findNextBigInt(0)
    Stream.cons(next, distinctBigIntStream(numberOfBits, p, consumedSet + next))
  }
  
  final def bigIntStream(numberOfBits: Int): Stream[BigInt] = {
    val next = BigInt(numberOfBits, random)
    Stream.cons(next, bigIntStream(numberOfBits))
  }
  
  final def bigPrimeStream(numberOfBits: Int, certainty: Int): Stream[BigInt] = {
    val next = BigInt(numberOfBits, certainty, random)
    Stream.cons(next, bigPrimeStream(numberOfBits, certainty))
  }
  
  final def bitStream: Stream[Boolean] = {
    val next = this.random.nextBoolean()
    Stream.cons(next, bitStream)
  }
  
  final def intStream(upperLimit: Int): Stream[Int] = {
    val next = this.random.nextInt(upperLimit)
    Stream.cons(next, intStream(upperLimit))
  }
  
  final def distinctIntStream(upperLimit: Int, consumedSet: Set[Int]): Stream[Int] = {
    @tailrec
    def findNextInt: Int = {
      if (consumedSet.size == upperLimit) throw new NoSuchElementException
      val possibleInt = this.random.nextInt(upperLimit)
      if (!consumedSet.contains(possibleInt)) possibleInt
      else findNextInt
    }
    val next = findNextInt
    Stream.cons(next, distinctIntStream(upperLimit, consumedSet + next))
  }
  
  final def byteStream: Stream[Byte] = {
    val next = new Array[Byte](1)
    this.random.nextBytes(next)
    Stream.cons(next(0), byteStream)
  }
}