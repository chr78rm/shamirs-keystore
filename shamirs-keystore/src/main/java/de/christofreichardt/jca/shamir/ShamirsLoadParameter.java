/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2024, Christof Reichardt
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.util.Optional;

/**
 * Specifies how to load and store a {@link KeyStore KeyStore} instance of type {@code ShamirsKeystore}.
 *
 * @author Christof Reichardt
 */
public class ShamirsLoadParameter implements KeyStore.LoadStoreParameter {

    final ShamirsProtection shamirsProtection;
    final File file;
    final String name;
    final InputStream inputStream;
    final OutputStream outputStream;

    /**
     * The given {@link File File} object denotes the location of a PKCS#12 keystore within the file system.
     *
     * @param file a PKCS#12 keystore
     * @param shamirsProtection the parameter used to protect keystore data
     */
    public ShamirsLoadParameter(File file, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.file = file;
        this.name = this.file.getName();
        this.inputStream = null;
        this.outputStream = null;
    }

    /**
     * The given {@link InputStream InputStream} object will be used to load a PKCS#12 keystore.
     *
     * @param inputStream provides data to load a PKCS#12 keystore
     * @param shamirsProtection the parameter used to protect keystore data
     */
    public ShamirsLoadParameter(InputStream inputStream, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.inputStream = inputStream;
        this.name = String.valueOf(this.inputStream.hashCode());
        this.file = null;
        this.outputStream = null;
    }

    /**
     * The given {@link OutputStream OutputStream} object will be used to store a PKCS#12 keystore.
     *
     * @param outputStream receives data to store a PKCS#12 keystore
     * @param shamirsProtection the parameter used to protect keystore data
     */
    public ShamirsLoadParameter(OutputStream outputStream, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.outputStream = outputStream;
        this.file = null;
        this.inputStream = null;
        this.name = String.valueOf(this.outputStream.hashCode());
    }

    /**
     * Returns the {@link ShamirsProtection ShamirsProtection} instance used to protect the PKCS#12 keystore.
     *
     * @return the {@code ShamirsProtection} instance
     */
    @Override
    public ProtectionParameter getProtectionParameter() {
        return this.shamirsProtection;
    }

    /**
     * Returns the KeyStore file if applicable, otherwise the {@link Optional Optional} is empty.
     *
     * @return an optional {@code File} instance
     */
    public Optional<File> getFile() {
        return Optional.ofNullable(this.file);
    }

    /**
     * Returns the file name of the KeyStore if applicable, otherwise the string representation from the hashcode of the given
     * {@link InputStream InputStream} or {@link OutputStream OutputStream}.
     *
     * @return the file name of the KeyStore if applicable
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link InputStream InputStream} providing the data of the PKCS#12 keystore, if applicable.
     *
     * @return an optional {@code InputStream} instance
     */
    public Optional<InputStream> getInputStream() {
        return Optional.ofNullable(this.inputStream);
    }

    /**
     * Returns the {@link OutputStream OutputStream} instance receiving the data of the PKCS#12 keystore, if applicable.
     *
     * @return an optional {@code OutputStream} instance
     */
    public Optional<OutputStream> getOutputStream() {
        return Optional.ofNullable(this.outputStream);
    }
}
