package de.christofreichardt.jca;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;

public class ShamirsKeystore extends KeyStoreSpi implements Traceable {

    private KeyStore keyStore;

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

    @Override
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStore.load(stream, password);
    }

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
            try (FileInputStream fileInputStream = new FileInputStream(shamirsLoadParameter.getFile())) {
                this.keyStore.load(fileInputStream, shamirsProtection.getPassword());
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }

}
