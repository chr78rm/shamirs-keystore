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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import scala.jdk.CollectionConverters;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
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
            paths.add(Paths.get("..", "shamirs-secret-sharing", "json", "partition-3-1.json"));
            paths.add(Paths.get("..", "shamirs-secret-sharing", "json", "partition-3-2.json"));
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
            secretSharing.savePartition(SIZES, Paths.get("json", "roundtrip-2", "partition"));
            Path[] paths_1 = {Paths.get("json", "roundtrip-2", "partition-0.json")};
            assertThat(new ShamirsProtection(paths_1).getPassword()).isEqualTo(myPassword.toCharArray());
            Path[] paths_2 = {Paths.get("json", "roundtrip-2", "partition-1.json"), Paths.get("json", "roundtrip-2", "partition-2.json")};
            assertThat(new ShamirsProtection(paths_2).getPassword()).isEqualTo(myPassword.toCharArray());
            Path[] paths_3 = {Paths.get("json", "roundtrip-2", "partition-1.json")};
            Throwable catched = catchThrowable(() -> new ShamirsProtection(paths_3).getPassword());
            assertThat(catched).isInstanceOf(IllegalArgumentException.class);
            assertThat(catched).hasMessage("requirement failed: Too few sharepoints.");
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("KeyStore-1")
    void keyStore_1() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "keyStore_1()");

        try {
            String myPassword = "Super-sicheres-Passwort";
            final int SHARES = 8;
            final int THRESHOLD = 4;
            SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, myPassword);
            final int[] SIZES = {4, 2, 2};
            secretSharing.savePartition(SIZES, Paths.get("json", "keystore-1", "partition"));
            Path[] paths = {Paths.get("json", "keystore-1", "partition-0.json")};
            File keyStoreFile = Paths.get("pkcs12", "my-keystore-1.p12").toFile();
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            ShamirsLoadParameter shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, shamirsProtection);
            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            keyStore.load(shamirsLoadParameter);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                KeyStore.Entry entry = keyStore.getEntry(alias, shamirsProtection);
                tracer.out().printfIndentln("entry.getClass().getName() = %s", entry.getClass().getName());
            }
            final String ALIAS = "my-test-keypair";
            KeyStore.Entry keyStoreEntry = keyStore.getEntry(ALIAS, shamirsProtection);
            assertThat(keyStoreEntry).isNotNull();
            assertThat(keyStoreEntry).isInstanceOf(KeyStore.PrivateKeyEntry.class);
            assertThat(keyStore.entryInstanceOf(ALIAS, KeyStore.PrivateKeyEntry.class)).isTrue();
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
            X509Certificate x509Certificate = (X509Certificate) privateKeyEntry.getCertificate();
            String distinguishedName = "CN=Christof,L=Rodgau,ST=Hessen,C=DE";
            assertThat(x509Certificate.getIssuerX500Principal().getName()).isEqualTo(distinguishedName);
            assertThat(x509Certificate.getSubjectX500Principal().getName()).isEqualTo(distinguishedName);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("KeyStore-2")
    void keyStore_2() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "keyStore_2()");

        try {
            String[] slices = {"test-3.json", "test-4.json", "test-5.json", "test-6.json"};
            Set<Path> paths = Stream.of(slices).map(slice -> Paths.get("json", "keystore-2").resolve(slice))
                    .collect(Collectors.toSet());
            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            File keyStoreFile = Paths.get("pkcs12", "my-keystore-2.p12").toFile();
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

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }

}
