package de.ngda.jca;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;

public class ShamirsLoadParameter implements KeyStore.LoadStoreParameter {

    final ShamirsProtection shamirsProtection;
    final File file;

    public ShamirsLoadParameter(File file, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.file = file;
    }

    @Override
    public ProtectionParameter getProtectionParameter() {
        return this.shamirsProtection;
    }

    public File getFile() {
        return file;
    }
}
