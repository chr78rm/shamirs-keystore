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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.util.Optional;

public class ShamirsLoadParameter implements KeyStore.LoadStoreParameter {

    final ShamirsProtection shamirsProtection;
    final File file;
    final String name;
    final InputStream inputStream;
    final OutputStream outputStream;

    public ShamirsLoadParameter(File file, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.file = file;
        this.name = this.file.getName();
        this.inputStream = null;
        this.outputStream = null;
    }

    public ShamirsLoadParameter(InputStream inputStream, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.inputStream = inputStream;
        this.name = String.valueOf(this.inputStream.hashCode());
        this.file = null;
        this.outputStream = null;
    }

    public ShamirsLoadParameter(OutputStream outputStream, ShamirsProtection shamirsProtection) {
        this.shamirsProtection = shamirsProtection;
        this.outputStream = outputStream;
        this.file = null;
        this.inputStream = null;
        this.name = String.valueOf(this.outputStream.hashCode());
    }

    @Override
    public ProtectionParameter getProtectionParameter() {
        return this.shamirsProtection;
    }

    public Optional<File> getFile() {
        return Optional.ofNullable(this.file);
    }

    public String getName() {
        return name;
    }

    public Optional<InputStream> getInputStream() {
        return Optional.ofNullable(this.inputStream);
    }

    public Optional<OutputStream> getOutputStream() {
        return Optional.ofNullable(this.outputStream);
    }
}
