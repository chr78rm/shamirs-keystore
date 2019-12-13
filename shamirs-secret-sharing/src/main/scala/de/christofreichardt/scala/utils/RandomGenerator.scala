package de.christofreichardt.scala.utils

import scala.annotation.tailrec
import scala.util.Random

/**
 * @author Christof Reichardt
 */
class RandomGenerator(secureRandom: java.security.SecureRandom) {
  val random = new Random(secureRandom)
  
  def this() = this(new java.security.SecureRandom)
  
  final def bigIntStream(numberOfBits: Int, p: BigInt): LazyList[BigInt] = {
    val next = BigInt(numberOfBits, random).mod(p)
    LazyList.cons(next, bigIntStream(numberOfBits, p))
  }
  
  final def distinctBigIntStream(numberOfBits: Int, p: BigInt, consumedSet: Set[BigInt]): LazyList[BigInt] = {
    val MAX_DEPTH = 100
    @tailrec
    def findNextBigInt(i: Int): BigInt = {
      if (i > MAX_DEPTH) throw new NoSuchElementException
      val possibleBigInt =  BigInt(numberOfBits, random).mod(p)
      if (!consumedSet.contains(possibleBigInt)) possibleBigInt
      else findNextBigInt(i + 1)
    }
    val next = findNextBigInt(0)
    LazyList.cons(next, distinctBigIntStream(numberOfBits, p, consumedSet + next))
  }
  
  final def bigIntStream(numberOfBits: Int): LazyList[BigInt] = {
    val next = BigInt(numberOfBits, random)
    LazyList.cons(next, bigIntStream(numberOfBits))
  }
  
  final def bigPrimeStream(numberOfBits: Int, certainty: Int): LazyList[BigInt] = {
    val next = BigInt(numberOfBits, certainty, random)
    LazyList.cons(next, bigPrimeStream(numberOfBits, certainty))
  }
  
  final def bitStream: LazyList[Boolean] = {
    val next = this.random.nextBoolean()
    LazyList.cons(next, bitStream)
  }
  
  final def intStream(upperLimit: Int): LazyList[Int] = {
    val next = this.random.nextInt(upperLimit)
    LazyList.cons(next, intStream(upperLimit))
  }
  
  final def distinctIntStream(upperLimit: Int, consumedSet: Set[Int]): LazyList[Int] = {
    @tailrec
    def findNextInt: Int = {
      if (consumedSet.size == upperLimit) throw new NoSuchElementException
      val possibleInt = this.random.nextInt(upperLimit)
      if (!consumedSet.contains(possibleInt)) possibleInt
      else findNextInt
    }
    val next = findNextInt
    LazyList.cons(next, distinctIntStream(upperLimit, consumedSet + next))
  }
  
  final def byteStream: LazyList[Byte] = {
    val next = new Array[Byte](1)
    this.random.nextBytes(next)
    LazyList.cons(next(0), byteStream)
  }
}