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

package de.christofreichardt.scala.diagnosis

import de.christofreichardt.diagnosis.TracerFactory
import de.christofreichardt.diagnosis.AbstractTracer

trait Tracing {
	def withTracer[T](resultTypeAsString: String, callee: AnyRef, methodSignature: String)(block: => T): T = {
	  val tracer = getCurrentTracer()
	  tracer.entry(resultTypeAsString, callee, methodSignature)
    try {
      block
    }
    finally {
      tracer.wayout()
    }
	}
	
	def getCurrentTracer(): AbstractTracer = TracerFactory.getInstance().getDefaultTracer()
}