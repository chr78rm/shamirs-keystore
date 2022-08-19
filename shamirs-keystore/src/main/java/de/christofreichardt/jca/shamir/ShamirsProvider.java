/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2022, Christof Reichardt
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

package de.christofreichardt.jca.shamir;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;

/**
 * A {@link Provider Provider} for the Java Security API allocating a {@link KeyStore KeyStore} instance of type {@code ShamirsKeystore}.
 *
 * @author Christof Reichardt
 */
public final class ShamirsProvider extends Provider {

	private static final long serialVersionUID = 1L;

	/** the provider name */
	public static final String NAME = "Christofs Crypto Lib";

	/**
	 * Constructs the specific {@link Provider Provider}. Such an instance is required to install the provider within an application.
	 */
	public ShamirsProvider() {
		super(NAME, "0.0.1", "A pkcs12 KeyStore implementation which supports shared passwords.");
		put("KeyStore.ShamirsKeystore", "de.christofreichardt.jca.shamir.ShamirsKeystore");
	}

	private static final class ProviderService extends Service {

		public ProviderService(Provider provider, String type, String algorithm, String className) {
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
