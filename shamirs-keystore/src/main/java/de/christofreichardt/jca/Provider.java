package de.christofreichardt.jca;

import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;

public final class Provider extends java.security.Provider {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "NGDA Crypto Lib";

	public Provider() {
		super(NAME, "0.0.1", "A pkcs12 KeyStore implementation which supports shared passwords.");
		put("KeyStore.ShamirsKeystore", "de.christofreichardt.jca.ShamirsKeystore");
	}

	private static final class ProviderService extends Provider.Service {

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
