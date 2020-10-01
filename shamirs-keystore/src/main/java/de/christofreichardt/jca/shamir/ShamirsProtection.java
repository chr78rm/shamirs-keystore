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

import javax.json.JsonArray;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collection;

public class ShamirsProtection implements KeyStore.ProtectionParameter, Destroyable {
    private final char[] password;
    private boolean destroyed = false;

    /**
     * Creates a Shamir protection parameter by providing the paths to the different slices containing the shares.
     *
     * @param paths path array to the JSON files (slices) containing the shares
     */
    public ShamirsProtection(Path[] paths) {
        this.password = mergePassword(paths);
    }

    public ShamirsProtection(Collection<Path> paths) {
        this(paths.toArray(new Path[0]));
    }

    // @TODO test it
    public ShamirsProtection(JsonArray slices) {
        this.password = mergePassword(slices);
    }

    private char[] mergePassword(Path[] paths) {
        return SecretMerging.apply(paths).password();
    }

    private char[] mergePassword(JsonArray slices) {
        return SecretMerging.apply(slices).password();
    }

    public char[] getPassword() {
        if (destroyed) {
            throw new IllegalStateException("Password has been cleared.");
        }
        return this.password;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        this.destroyed = true;
        if (this.password != null) {
            Arrays.fill(this.password, ' ');
        }
    }

    @Override
    public boolean isDestroyed() {
        return this.destroyed;
    }
}
