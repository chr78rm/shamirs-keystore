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

package de.christofreichardt.jca.shamir;

import de.christofreichardt.scala.shamir.SecretMerging;

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Collection;

public class ShamirsProtection implements KeyStore.ProtectionParameter {
    final Path[] paths;
    final char[] password;

    public ShamirsProtection(Path[] paths) {
        this.paths = paths;
        this.password = mergePassword();
    }

    public ShamirsProtection(Collection<Path> paths) {
        this.paths = paths.toArray(new Path[0]);
        this.password = mergePassword();
    }

    private char[] mergePassword() {
        return SecretMerging.apply(this.paths).password();
    }

    public char[] getPassword() {
        return password;
    }
}
