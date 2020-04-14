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

package de.christofreichardt.jca;

import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;

public final class ShamirsProvider extends java.security.Provider {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "Christofs Crypto Lib";

	public ShamirsProvider() {
		super(NAME, "0.0.1", "A pkcs12 KeyStore implementation which supports shared passwords.");
		put("KeyStore.ShamirsKeystore", "de.christofreichardt.jca.ShamirsKeystore");
	}

	private static final class ProviderService extends ShamirsProvider.Service {

		public ProviderService(java.security.Provider provider, String type, String algorithm, String className) {
			super(provider, type, algorithm, className, null, null);
		}

		@Override
		public Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException {
			String type = getType();
			String algo = getAlgorithm();
			try {
				if (type.equals("KeyStore")) {
					if (algo.equals("ShamirsKeystore")) {
						return new ShamirsKeystore();
					}
				}
			} catch (Exception ex) {
				throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + " using " + NAME, ex);
			}
			throw new ProviderException("No impl for " + algo + " " + type);
		}

	}
}
