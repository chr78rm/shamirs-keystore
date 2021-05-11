/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2020, Christof Reichardt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.christofreichardt.scala

import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

/**
 * Contains the core classes needed for sharing or merging of secrets.
 */
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

  /**
   * Converts the given bytes into a non-negative `BigInt` number by padding 0x7F upfront.
   *
   * @param bytes the to be converted bytes
   * @return the resulting non-negative BigInt number
   */
  def bytes2BigInt(bytes: IndexedSeq[Byte]): BigInt = {
    val paddedBytes = 0x7F.toByte +: bytes
    BigInt(paddedBytes.toArray)
  }

  /**
   * Converts the given `BigInt` number into bytes by discarding the leading byte.
   *
   * @param s the to be converted `BigInt` number
   * @return the resulting bytes
   */
  def bigIntToBytes(s: BigInt): IndexedSeq[Byte] = {
    val paddedBytes = s.toByteArray
    paddedBytes.tail.toIndexedSeq
  }

  /**
   * Encodes a given character sequence as bytes ba applying UTF-8 encoding.
   *
   * @param charSequence the to be encoded characters
   * @return the encoded character sequence
   */
  def charSequenceToByteArray(charSequence: CharSequence): Array[Byte] = {
    val charBuffer = CharBuffer.wrap(charSequence)
    val encoder = StandardCharsets.UTF_8.newEncoder()
    val byteBuffer = encoder.encode(charBuffer)
    val bytes = Array.ofDim[Byte](byteBuffer.remaining())
    byteBuffer.get(bytes)
    bytes
  }
}