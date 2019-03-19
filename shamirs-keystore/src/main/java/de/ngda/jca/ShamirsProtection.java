package de.ngda.jca;

import java.nio.file.Path;
import java.security.KeyStore;

public class ShamirsProtection implements KeyStore.ProtectionParameter {
    final Path[] paths;

    public ShamirsProtection(Path[] paths) {
        this.paths = paths;
    }

    char[] mergePassword() {
        return null;
    }
}
