package de.ngda.jca;

import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;

public class ShamirsLoadParameter implements KeyStore.LoadStoreParameter {

	final ShamirsProtection shamirsProtection;

	public ShamirsLoadParameter(ShamirsProtection shamirsProtection) {
		this.shamirsProtection = shamirsProtection;
	}

	@Override
	public ProtectionParameter getProtectionParameter() {
		return this.shamirsProtection;
	}

}
