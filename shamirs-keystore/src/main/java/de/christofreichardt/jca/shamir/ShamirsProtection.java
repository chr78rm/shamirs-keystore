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

import de.christofreichardt.scala.shamir.SecretMerging;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collection;
import jakarta.json.JsonArray;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

/**
 * Protects a {@link KeyStore KeyStore} instance and its entries by applying Shamirs Secret Sharing algorithm. Passwords have been
 * splitted into several secret shares and have to been merged again to recover the original password. In order to create
 * a {@code ShamirsProtection} instance someone has to provide access to a certain subset of secret shares, e.g. paths to different
 * slices (JSON files) of shares.
 *
 * @author Christof Reichardt
 */
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

    /**
     * Creates a Shamir protection parameter by providing the paths to the different slices containing the shares.
     *
     * @param paths a {@code Collection} of {@code Path}s pointing to the JSON files (slices) containing the shares
     */
    public ShamirsProtection(Collection<Path> paths) {
        this(paths.toArray(new Path[0]));
    }

    /**
     * Creates a Shamir protection parameter by providing a {@link JsonArray JsonArray} comprising slices of secret shares.
     *
     * @param slices a {@link JsonArray JsonArray} comprising slices of secret shares
     */
    public ShamirsProtection(JsonArray slices) {
        this.password = mergePassword(slices);
    }

    private char[] mergePassword(Path[] paths) {
        return SecretMerging.apply(paths).password();
    }

    private char[] mergePassword(JsonArray slices) {
        return SecretMerging.apply(slices).password();
    }

    /**
     * Returns the recovered password, provided that the instance hasn't been destroyed.
     *
     * @return the recovered password
     */
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
