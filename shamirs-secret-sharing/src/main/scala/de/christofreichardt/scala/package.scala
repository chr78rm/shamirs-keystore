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