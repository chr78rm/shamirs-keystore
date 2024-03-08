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

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.*;
import java.security.*;
import java.security.KeyStore.LoadStoreParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

/**
 * The actual implementation of the JCA engine class {@link KeyStoreSpi KeyStoreSpi}. This interpretation uses some specific
 * {@link KeyStore.LoadStoreParameter KeyStore.LoadStoreParameter} and {@link KeyStore.ProtectionParameter KeyStore.ProtectionParameter} classes, see
 * {@link ShamirsLoadParameter ShamirsLoadParameter} and {@link ShamirsProtection ShamirsProtection}. The idea is that the password required to
 * load the KeyStore has been splitted by Shamirs Secret Sharing algorithm into several secret shares. Some subset of these shares is needed to
 * recover the original password. Those methods which aren't expecting one of the protection parameter simply delegate to the underlying
 * PKCS#12 KeyStore implementation of the JDK.
 *
 * @author Christof Reichardt
 */
public class ShamirsKeystore extends KeyStoreSpi implements Traceable {

    private KeyStore keyStore;

    /**
     * Creates a PKCS#12 KeyStore instance provided by the Java platform.
     */
    public ShamirsKeystore() {
        super();
        try {
            this.keyStore = KeyStore.getInstance("pkcs12");
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            return this.keyStore.getKey(alias, password);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        try {
            return this.keyStore.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        try {
            return this.keyStore.getCertificate(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        try {
            return this.keyStore.getCreationDate(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        this.keyStore.setKeyEntry(alias, key, password, chain);
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        this.keyStore.setKeyEntry(alias, key, chain);
    }

    /**
     * This method expects a {@link ShamirsProtection ShamirsProtection} instance as {@link KeyStore.ProtectionParameter KeyStore.ProtectionParameter}. Otherwise
     * an {@link IllegalArgumentException IllegalArgumentException} will be thrown. After the recovering of the password the underlying PKCS#12 Keystore of the
     * JDK will be called.
     *
     * @param alias get the {@link KeyStore.Entry KeyStore.Entry} for this alias
     * @param protectionParameter the {@link ShamirsProtection ShamirsProtection} used to protect this entry
     * @return the {@link KeyStore.Entry KeyStore.Entry} for the specified alias, or null if there is no such entry
     * @throws KeyStoreException if the operation failed
     * @throws NoSuchAlgorithmException if the algorithm for recovering the entry cannot be found, that would be an algorithm for password based encryption
     * @throws UnrecoverableEntryException if the key entry cannot be recovered, e.g. the specified {@code protectionParameter} were insufficient or invalid
     */
    @Override
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protectionParameter) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore.Entry entry;
        if (!this.keyStore.isCertificateEntry(alias)) {
            if (!(protectionParameter instanceof ShamirsProtection)) {
                throw new IllegalArgumentException("ShamirsProtection required.");
            }

            ShamirsProtection shamirsProtection = (ShamirsProtection) protectionParameter;
            KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(shamirsProtection.getPassword());
            entry = this.keyStore.getEntry(alias, passwordProtection);
        } else {
            entry = this.keyStore.getEntry(alias, null);
        }

        return entry;
    }

    /**
     * This method expects a {@link ShamirsProtection ShamirsProtection} instance as {@link KeyStore.ProtectionParameter KeyStore.ProtectionParameter}. Otherwise
     * an {@link IllegalArgumentException IllegalArgumentException} will be thrown. After the recovering of the password the underlying PKCS#12 Keystore of the
     * JDK will be called.
     *
     * @param alias save the {@link KeyStore.Entry KeyStore.Entry} under this alias
     * @param entry the {@link KeyStore.Entry KeyStore.Entry} to save
     * @param protectionParameter the {@link ShamirsProtection ShamirsProtection} used to protect the Entry
     * @throws KeyStoreException if this operation fails
     */
    @Override
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protectionParameter) throws KeyStoreException {
        if (!(protectionParameter instanceof ShamirsProtection)) {
            throw new IllegalArgumentException("ShamirsProtection required.");
        }

        ShamirsProtection shamirsProtection = (ShamirsProtection) protectionParameter;
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(shamirsProtection.getPassword());
        this.keyStore.setEntry(alias, entry, passwordProtection);
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        this.keyStore.setCertificateEntry(alias, cert);
    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        this.keyStore.deleteEntry(alias);
    }

    @Override
    public Enumeration<String> engineAliases() {
        try {
            return this.keyStore.aliases();
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        try {
            return this.keyStore.containsAlias(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int engineSize() {
        try {
            return this.keyStore.size();
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        try {
            return this.keyStore.isKeyEntry(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        try {
            return this.keyStore.isCertificateEntry(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        try {
            return this.keyStore.getCertificateAlias(cert);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        try {
            this.keyStore.store(stream, password);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method expects a {@link ShamirsLoadParameter ShamirsLoadParameter} instance as {@link KeyStore.LoadStoreParameter KeyStore.LoadStoreParameter}. Otherwise
     * an {@link IllegalArgumentException IllegalArgumentException} will be thrown. After the recovering of the password the underlying PKCS#12 Keystore of the
     * JDK will be called.
     *
     * @param loadStoreParameter the {@link ShamirsLoadParameter ShamirsLoadParameter} that specifies how to store the keystore
     * @throws IOException if there was an I/O problem with data
     * @throws NoSuchAlgorithmException if the appropriate data integrity algorithm could not be found
     * @throws CertificateException if any of the certificates included in the keystore data could not be stored
     */
    @Override
    public void engineStore(LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(loadStoreParameter instanceof ShamirsLoadParameter)) {
            throw new IllegalArgumentException("ShamirsLoadParameter required.");
        }

        ShamirsLoadParameter shamirsLoadParameter = (ShamirsLoadParameter) loadStoreParameter;
        ShamirsProtection shamirsProtection = (ShamirsProtection) shamirsLoadParameter.getProtectionParameter();
        if (shamirsLoadParameter.getFile().isPresent()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(shamirsLoadParameter.getFile().get())) {
                engineStore(fileOutputStream, shamirsProtection.getPassword());
            }
        } else {
            OutputStream outputStream = shamirsLoadParameter.getOutputStream().orElseThrow(() -> new IOException("Missing OutputStream."));
            engineStore(outputStream, shamirsProtection.getPassword());
        }
    }

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStore.load(stream, password);
    }

    /**
     * This method expects a {@link ShamirsLoadParameter ShamirsLoadParameter} instance as {@link KeyStore.LoadStoreParameter KeyStore.LoadStoreParameter}. Otherwise
     * an {@link IllegalArgumentException IllegalArgumentException} will be thrown. After the recovering of the password the underlying PKCS#12 Keystore of the
     * JDK will be called.
     *
     * @param loadStoreParameter the {@link ShamirsLoadParameter ShamirsLoadParameter} that specifies how to load the keystore
     * @throws IOException if there is an I/O or format problem with the keystore data or if the recovered password was incorrect
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @throws CertificateException if any of the certificates in the keystore could not be loaded
     */
    @Override
    public void engineLoad(LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "engineLoad(LoadStoreParameter loadStoreParameter)");

        try {
            if (!(loadStoreParameter instanceof ShamirsLoadParameter)) {
                throw new IllegalArgumentException("Need ShamirsLoadParameter.");
            }

            ShamirsLoadParameter shamirsLoadParameter = (ShamirsLoadParameter) loadStoreParameter;
            ShamirsProtection shamirsProtection = (ShamirsProtection) shamirsLoadParameter.getProtectionParameter();
            if (shamirsLoadParameter.getFile().isPresent()) {
                try (FileInputStream fileInputStream = new FileInputStream(shamirsLoadParameter.getFile().get())) {
                    engineLoad(fileInputStream, shamirsProtection.getPassword());
                }
            } else {
                InputStream inputStream = shamirsLoadParameter.getInputStream().orElseThrow(() -> new IOException("Missing InputStream."));
                engineLoad(inputStream, shamirsProtection.getPassword());
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        try {
            return this.keyStore.entryInstanceOf(alias, entryClass);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Switched off.
     *
     * @return the NullTracer
     */
    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getDefaultTracer();
    }

}
