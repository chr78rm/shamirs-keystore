package de.christofreichardt

package object scala {
  
  /**
   * Formats the given bytes as comma separated hexadecimal values
   * 
   * @param bytes the to be formtted bytes
   * @return the comma separated hexadecimal values
   */
  def formatBytes(bytes: Iterable[Byte]): String = bytes.map(b => String.format("0x%02X", b: java.lang.Byte)).mkString(",")
  
}