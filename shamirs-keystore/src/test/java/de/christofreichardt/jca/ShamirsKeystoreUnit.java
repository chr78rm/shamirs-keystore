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

package de.christofreichardt.jca;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.*;
import scala.jdk.CollectionConverters;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShamirsKeystoreUnit implements Traceable {

    @BeforeAll
    void systemProperties() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "systemProperties()");

        try {
            String[] propertyNames = System.getProperties().stringPropertyNames().toArray(new String[0]);
            Arrays.sort(propertyNames);
            for (String propertyName : propertyNames) {
                tracer.out().printfIndentln("%s = %s", propertyName, System.getProperty(propertyName));
            }
        } finally {
            tracer.wayout();
        }
    }

    @BeforeAll
    void setupProvider() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "setupProvider()");

        try {
            Security.addProvider(new ShamirsProvider());
            assertThat(Security.getProvider(ShamirsProvider.NAME)).isNotNull();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("SecretMerging-1")
    void secretMerging_1() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "secretMerging_1()");

        try {
            List<Path> paths = new ArrayList<>();
            paths.add(Path.of("..", "shamirs-secret-sharing", "json", "partition-3-1.json"));
            paths.add(Path.of("..", "shamirs-secret-sharing", "json", "partition-3-2.json"));
            SecretMerging secretMerging = SecretMerging.apply(CollectionConverters.ListHasAsScala(paths).asScala());

            tracer.out().printfIndentln("secretMerging.secretBytes() = (%s)", secretMerging.secretBytes().mkString(","));
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("RoundTrip-1")
    void roundTrip_1() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "roundTrip_1()");

        try {
            String myPassword = "Dies-ist-streng-geheim";
            final int SHARES = 8;
            final int THRESHOLD = 4;
            SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, myPassword);
            SecretMerging secretMerging = new SecretMerging(secretSharing.sharePoints(), secretSharing.prime());
            assertThat(secretMerging.password()).isEqualTo(myPassword.toCharArray());
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("RoundTrip-2")
    void roundTrip_2() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "roundTrip_1()");

        try {
            String myPassword = "Dies-ist-streng-geheim";
            final int SHARES = 8;
            final int THRESHOLD = 4;
            SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, myPassword);
            final int[] SIZES = {4, 2, 2};
            secretSharing.savePartition(SIZES, Path.of("json", "roundtrip-2", "partition"));
            Path[] paths_1 = {Path.of("json", "roundtrip-2", "partition-0.json")};
            assertThat(new ShamirsProtection(paths_1).getPassword()).isEqualTo(myPassword.toCharArray());
            Path[] paths_2 = {Path.of("json", "roundtrip-2", "partition-1.json"), Path.of("json", "roundtrip-2", "partition-2.json")};
            assertThat(new ShamirsProtection(paths_2).getPassword()).isEqualTo(myPassword.toCharArray());
            Path[] paths_3 = {Path.of("json", "roundtrip-2", "partition-1.json")};
            Throwable catched = catchThrowable(() -> new ShamirsProtection(paths_3).getPassword());
            assertThat(catched).isInstanceOf(IllegalArgumentException.class);
            assertThat(catched).hasMessage("requirement failed: Too few sharepoints.");
        } finally {
            tracer.wayout();
        }
    }

    @Nested
    @DisplayName("Prepared-Keystore")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PreparedKeystore {

        KeyStore keyStore;
        ShamirsProtection shamirsProtection;
        ShamirsLoadParameter shamirsLoadParameter;

        @BeforeAll
        void loadKeystore() throws GeneralSecurityException, IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "loadKeystore()");

            try {
                final String MY_PASSWORD = "Super-sicheres-Passwort";
                final int SHARES = 8;
                final int THRESHOLD = 4;
                SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, MY_PASSWORD);
                final int[] SIZES = {4, 2, 2};
                secretSharing.savePartition(SIZES, Path.of("json", "keystore-1", "partition"));
                Path[] paths = {Path.of("json", "keystore-1", "partition-0.json")};
                File keyStoreFile = Path.of("pkcs12", "my-keystore-1.p12").toFile();
                this.shamirsProtection = new ShamirsProtection(paths);
                this.shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, this.shamirsProtection);
                this.keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
                keyStore.load(this.shamirsLoadParameter);
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Enumeration")
        void enumerateEntries() throws GeneralSecurityException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "enumerateEntries()");

            try {
                tracer.out().printfIndentln("this.keyStore.size() = %d", this.keyStore.size());

                Enumeration<String> aliases = this.keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    KeyStore.Entry entry = this.keyStore.getEntry(alias, this.shamirsProtection);

                    tracer.out().printfIndentln(
                            "creationDate(%1$s) = %2$s, isCertificateEntry(%1$s) = %3$b, isKeyEntry(%1$s) = %4$b, " +
                                    "entryInstanceOf(%1$s, KeyStore.TrustedCertificateEntry.class) = %5$b, " +
                                    "entryInstanceOf(%1$s, KeyStore.PrivateKeyEntry.class) = %6$b, " +
                                    "entryInstanceOf(%1$s, KeyStore.SecretKeyEntry.class) = %7$b",
                            alias, this.keyStore.getCreationDate(alias), this.keyStore.isCertificateEntry(alias), this.keyStore.isKeyEntry(alias),
                            this.keyStore.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class),
                            this.keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class),
                            this.keyStore.entryInstanceOf(alias, KeyStore.SecretKeyEntry.class)
                    );
                }
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Private-Key-Entry")
        void privateKeyEntry() throws GeneralSecurityException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "privateKeyEntry()");

            try {
                final String PRIVATE_KEY_ALIAS = "my-test-keypair";
                KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(PRIVATE_KEY_ALIAS, this.shamirsProtection);
                assertThat(keyStoreEntry).isNotNull();
                assertThat(keyStoreEntry).isInstanceOf(KeyStore.PrivateKeyEntry.class);
                assertThat(this.keyStore.entryInstanceOf(PRIVATE_KEY_ALIAS, KeyStore.PrivateKeyEntry.class)).isTrue();
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                X509Certificate x509Certificate = (X509Certificate) privateKeyEntry.getCertificate();
                final String DISTINGUISHED_NAME = "CN=Christof,L=Rodgau,ST=Hessen,C=DE";
                assertThat(x509Certificate.getIssuerX500Principal().getName()).isEqualTo(DISTINGUISHED_NAME);
                assertThat(x509Certificate.getSubjectX500Principal().getName()).isEqualTo(DISTINGUISHED_NAME);
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Trusted-Certificate-Entry")
        void trustedCertificateEntry() throws GeneralSecurityException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "trustedCertificateEntry()");

            try {
                final String CERTIFICATE_ALIAS = "digicert";
                KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(CERTIFICATE_ALIAS, this.shamirsProtection);
                assertThat(keyStoreEntry).isNotNull();
                assertThat(keyStoreEntry).isInstanceOf(KeyStore.TrustedCertificateEntry.class);
                assertThat(this.keyStore.entryInstanceOf(CERTIFICATE_ALIAS, KeyStore.TrustedCertificateEntry.class)).isTrue();
                keyStoreEntry = this.keyStore.getEntry(CERTIFICATE_ALIAS, null);
                assertThat(keyStoreEntry).isNotNull();
                assertThat(keyStoreEntry).isInstanceOf(KeyStore.TrustedCertificateEntry.class);
                assertThat(this.keyStore.entryInstanceOf(CERTIFICATE_ALIAS, KeyStore.TrustedCertificateEntry.class)).isTrue();
                X509Certificate x509Certificate = (X509Certificate) this.keyStore.getCertificate(CERTIFICATE_ALIAS);
                assertThat(x509Certificate).isNotNull();
                final String ISSUER = "CN=DigiCert Global Root CA,OU=www.digicert.com,O=DigiCert Inc,C=US";
                final String SUBJECT = "CN=DigiCert SHA2 Secure Server CA,O=DigiCert Inc,C=US";
                assertThat(x509Certificate.getIssuerX500Principal().getName()).isEqualTo(ISSUER);
                assertThat(x509Certificate.getSubjectX500Principal().getName()).isEqualTo(SUBJECT);
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Secret-Key-Entry")
        void secretKeyEntry() throws GeneralSecurityException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "secretKeyEntry()");

            try {
                final String SECRET_KEY_ALIAS = "my-aes-key", KEYGENERATOR_ALG = "AES", KEY_FORMAT = "RAW";
                final int KEY_SIZE = 256;
                KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(SECRET_KEY_ALIAS, this.shamirsProtection);
                assertThat(keyStoreEntry).isNotNull();
                assertThat(keyStoreEntry).isInstanceOf(KeyStore.SecretKeyEntry.class);
                assertThat(this.keyStore.entryInstanceOf(SECRET_KEY_ALIAS, KeyStore.SecretKeyEntry.class)).isTrue();
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreEntry;
                SecretKey secretKey = secretKeyEntry.getSecretKey();
                assertThat(secretKey.getAlgorithm()).isEqualTo(KEYGENERATOR_ALG);
                assertThat(secretKey.getFormat()).isEqualTo(KEY_FORMAT);
                assertThat(secretKey.getEncoded().length).isEqualTo(KEY_SIZE / 8);
            } finally {
                tracer.wayout();
            }
        }
    }

    @Disabled
    @DisplayName("KeyStore-2")
    void keyStore_2() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "keyStore_2()");

        try {
            String[] slices = {"test-3.json", "test-4.json", "test-5.json", "test-6.json"};
            Set<Path> paths = Stream.of(slices).map(slice -> Path.of("json", "keystore-2").resolve(slice))
                    .collect(Collectors.toSet());
            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            File keyStoreFile = Path.of("pkcs12", "my-keystore-2.p12").toFile();
            ShamirsLoadParameter shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, shamirsProtection);
            keyStore.load(null, null);
            final String ALGORITHM = "AES";
            final int KEY_SIZE = 256;
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            final String ALIAS = "my-secret-key";
            keyStore.setEntry(ALIAS, secretKeyEntry, shamirsProtection);
            keyStore.store(shamirsLoadParameter);
            keyStore.load(shamirsLoadParameter);
            KeyStore.Entry keyStoreEntry = keyStore.getEntry(ALIAS, shamirsProtection);
            assertThat(keyStoreEntry).isNotNull();
            assertThat(keyStoreEntry).isInstanceOf(KeyStore.SecretKeyEntry.class);
            secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreEntry;
            assertThat(secretKeyEntry.getSecretKey().getAlgorithm()).isEqualTo(ALGORITHM);
            assertThat(secretKeyEntry.getSecretKey().getEncoded().length).isEqualTo(KEY_SIZE / 8);
        } finally {
            tracer.wayout();
        }
    }

    @Nested
    @DisplayName("Programmatic-Keystore")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ProgrammaticKeystore {

        KeyStore keyStore;
        ShamirsProtection shamirsProtection;
        ShamirsLoadParameter shamirsLoadParameter;
        Path keystorePath;

        @BeforeEach
        void init() throws GeneralSecurityException, IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "init()");

            try {
                String[] slices = {"test-3.json", "test-4.json", "test-5.json", "test-6.json"};
                Set<Path> paths = Stream.of(slices).map(slice -> Path.of("json", "keystore-2").resolve(slice))
                        .collect(Collectors.toSet());
                this.keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
                this.shamirsProtection = new ShamirsProtection(paths);
                this.keystorePath = Path.of("pkcs12", "my-keystore-2.p12");
                assertThat(Files.notExists(this.keystorePath)).isTrue();
                this.shamirsLoadParameter = new ShamirsLoadParameter(this.keystorePath.toFile(), shamirsProtection);
                keyStore.load(null, null);
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Secret-Key-Entry")
        void secretKeyEntry() throws GeneralSecurityException, IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "secretKeyEntry()");

            try {
                final String ALGORITHM = "AES";
                final int KEY_SIZE = 256;
                KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                keyGenerator.init(KEY_SIZE);
                SecretKey secretKey = keyGenerator.generateKey();
                KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
                final String ALIAS = "my-secret-aes-key";
                this.keyStore.setEntry(ALIAS, secretKeyEntry, this.shamirsProtection);
                this.keyStore.store(this.shamirsLoadParameter);
                this.keyStore.load(this.shamirsLoadParameter);
                KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(ALIAS, this.shamirsProtection);
                assertThat(keyStoreEntry).isNotNull();
                assertThat(keyStoreEntry).isInstanceOf(KeyStore.SecretKeyEntry.class);
                assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.SecretKeyEntry.class)).isTrue();
                assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.PrivateKeyEntry.class)).isFalse();
                assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.TrustedCertificateEntry.class)).isFalse();
                secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreEntry;
                assertThat(secretKeyEntry.getSecretKey().getAlgorithm()).isEqualTo(ALGORITHM);
                assertThat(secretKeyEntry.getSecretKey().getEncoded().length).isEqualTo(KEY_SIZE / 8);
            } finally {
                tracer.wayout();
            }
        }

        @Test
        @DisplayName("Private-Key-Entry")
        void privateKeyEntry() throws GeneralSecurityException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "privateKeyEntry()");

            try {
                final String ALGORITHM = "RSA";
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
                keyPairGenerator.initialize(4096);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                tracer.out().printfIndentln("keyPair.getPrivate().getAlgorithm() = %s, keyPair.getPrivate().getEncoded().length = %d",
                        keyPair.getPrivate().getAlgorithm(), keyPair.getPrivate().getEncoded().length);

                final int DAYS = 365;
                final String COMMON_NAME = "CN=Christof Reichardt", LOCALITY = "L=Rodgau", STATE = "ST=Hessen", COUNTRY = "C=Deutschland";
                final String SIGNATURE_ALGO = "SHA256withRSA", DISTINGUISHED_NAME = COMMON_NAME + ", " + LOCALITY + ", " + STATE + ", " + COUNTRY;
                Instant now = Instant.now();
                Date notBefore = Date.from(now);
                Date notAfter = Date.from(now.plus(Duration.ofDays(DAYS)));
                try {
                    ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGO).build(keyPair.getPrivate());
                    X500Name x500Name = new X500Name(DISTINGUISHED_NAME);
                    JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                            x500Name,
                            BigInteger.valueOf(now.toEpochMilli()),
                            notBefore,
                            notAfter,
                            x500Name,
                            keyPair.getPublic()
                    );
                    X509CertificateHolder x509CertificateHolder = certificateBuilder.build(contentSigner);
                    JcaX509CertificateConverter x509CertificateConverter = new JcaX509CertificateConverter();
                    x509CertificateConverter.setProvider(new BouncyCastleProvider());
                    X509Certificate x509Certificate = x509CertificateConverter.getCertificate(x509CertificateHolder);
                    final String ALIAS = "my-private-rsa-key";
                    this.keyStore.setEntry(
                            ALIAS,
                            new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new Certificate[]{x509Certificate}),
                            this.shamirsLoadParameter.getProtectionParameter()
                    );
                    this.keyStore.store(this.shamirsLoadParameter);
                    this.keyStore.load(this.shamirsLoadParameter);
                    KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(ALIAS, this.shamirsProtection);
                    assertThat(keyStoreEntry).isNotNull();
                    assertThat(keyStoreEntry).isInstanceOf(KeyStore.PrivateKeyEntry.class);
                    assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.SecretKeyEntry.class)).isFalse();
                    assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.PrivateKeyEntry.class)).isTrue();
                    assertThat(this.keyStore.entryInstanceOf(ALIAS, KeyStore.TrustedCertificateEntry.class)).isFalse();
                    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                    assertThat(privateKeyEntry.getPrivateKey().getAlgorithm()).isEqualTo(ALGORITHM);
                    x509Certificate = (X509Certificate) privateKeyEntry.getCertificate();
                    tracer.out().printfIndentln("x509Certificate.getSubjectX500Principal().getName() = %s",
                            x509Certificate.getSubjectX500Principal().getName());
                    assertThat(x509Certificate.getSubjectX500Principal().getName())
                            .contains(COMMON_NAME)
                            .contains(LOCALITY)
                            .contains(STATE)
                            .contains(COUNTRY);
                } catch (OperatorCreationException | IOException ex) {
                    throw new GeneralSecurityException(ex);
                }
            } finally {
                tracer.wayout();
            }
        }

        @AfterEach
        void exit() throws IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("void", this, "exit()");

            try {
                assertThat(Files.deleteIfExists(this.keystorePath)).isTrue();
            } finally {
                tracer.wayout();
            }
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }

}
