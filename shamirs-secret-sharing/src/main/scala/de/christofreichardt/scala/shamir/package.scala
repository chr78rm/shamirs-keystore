package de.christofreichardt.scala

package object shamir {
  import scala.annotation.tailrec

  /**
   * defines the certainty of a number being prime, that is between 2^64 primes computed by 
   * the underlying algorithm there is one false positive.
   */
  val CERTAINTY = 64
  
//  /**
//   * Formats the given bytes as comma separated hexadecimal values
//   * 
//   * @param bytes the to be formtted bytes
//   * @return the comma separated hexadecimal values
//   */
//  def formatBytes(bytes: Iterable[Byte]): String = bytes.map(b => String.format("0x%02X", b: java.lang.Byte)).mkString(",")
  
  /**
   * Checks if any two items within the Iterable are identical.
   * 
   * @param items an Iterable of some items
   * @return true if no pair of identical items has been found 
   */
  def pairWiseDifferent[T](items: Iterable[T]): Boolean = {
    @tailrec
    def check(ps: Iterable[T], xs: Set[T]): Boolean = {
      if (ps.isEmpty) true
      else {
        val x = ps.head
        if (xs.contains(x)) false
        else check(ps.tail, xs + x)
      }
    }
    check(items, Set.empty[T])
  }

  def bytes2BigInt(bytes: IndexedSeq[Byte]): BigInt = {
    val paddedBytes = 0x7F.toByte +: bytes
    BigInt(paddedBytes.toArray)
  }

  def bigIntToBytes(s: BigInt): IndexedSeq[Byte] = {
    val paddedBytes = s.toByteArray
    paddedBytes.tail
  }
}