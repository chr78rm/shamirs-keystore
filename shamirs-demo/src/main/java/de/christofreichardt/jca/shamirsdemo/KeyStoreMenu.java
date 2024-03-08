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

package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.jca.shamir.ShamirsLoadParameter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class KeyStoreMenu extends AbstractMenu {

    public enum KeystoreCommand implements Command {
        LIST_ENTRIES("l", "list entries"), SECRET_KEY("s", "secret key"),
        PRIVATE_KEY("p", "private key"), CERTIFICATE("c", "certificate"),
        MAIN_MENU("m", "main menu");

        String shortCut;
        String fullName;

        KeystoreCommand(String shortCut, String fullName) {
            this.shortCut = shortCut;
            this.fullName = fullName;
        }

        @Override
        public String getShortCut() {
            return this.shortCut;
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public String getDisplayName() {
            return this.fullName.replaceFirst(this.shortCut, "(" + this.shortCut + ")");
        }
    }

    private final KeyStore keyStore;
    private final ShamirsLoadParameter shamirsLoadParameter;

    public KeyStoreMenu(AppCallback app, KeyStore keyStore, ShamirsLoadParameter shamirsLoadParameter) {
        super(app);
        this.keyStore = keyStore;
        this.shamirsLoadParameter = shamirsLoadParameter;
    }

    @Override
    public void print() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "printMenu()");
        try {
            System.console().printf("\n");
            System.console().printf("Current time: %s\n", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.console().printf("   Workspace: %s\n", this.app.getCurrentWorkspace().getFileName());
            System.console().printf("\n");
            String keyStoreFilename = this.shamirsLoadParameter.getName();
            System.console().printf("%s-> KeyStore menu [%s]\n", this.app.getCurrentWorkspace().getFileName(),
                    keyStoreFilename.substring(0, keyStoreFilename.length() - ".p12".length()));
            System.console().printf("\n");
            System.console().printf("   %20s", KeystoreCommand.LIST_ENTRIES.getDisplayName());
            System.console().printf("   %20s", KeystoreCommand.PRIVATE_KEY.getDisplayName());
            System.console().printf("   %20s", KeystoreCommand.SECRET_KEY.getDisplayName());
            System.console().printf("\n");
            System.console().printf("   %20s", KeystoreCommand.CERTIFICATE.getDisplayName());
            System.console().printf("   %20s", KeystoreCommand.MAIN_MENU.getDisplayName());
            System.console().printf("\n");
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public Map<String, Command> computeShortCutMap() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Map<String, Command>", this, "computeShortCutMap()");
        try {
            EnumSet<KeystoreCommand> keystoreCommands = EnumSet.allOf(KeystoreCommand.class);

            tracer.out().printfIndentln("keystoreCommands = %s", keystoreCommands);

            Map<String, Command> shortCuts = keystoreCommands.stream()
                    .collect(Collectors.toMap(keystoreCommand -> keystoreCommand.getShortCut(), Function.identity()));

            tracer.out().printfIndentln("shortCuts = %s", shortCuts);

            return shortCuts;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public void execute(Command command) throws IOException, GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "execute(Command command)");
        try {
            tracer.out().printfIndentln("command = %s", command);

            KeystoreCommand keystoreCommand = KeystoreCommand.valueOf(command.toString());
            switch (keystoreCommand) {
                case MAIN_MENU:
                    this.keyStore.store(this.shamirsLoadParameter);
                    this.app.setMenu(new MainMenu(this.app));
                    break;
                case SECRET_KEY:
                    addSecretKey();
                    break;
                case LIST_ENTRIES:
                    listEntries();
                    break;
                case PRIVATE_KEY:
                    addPrivateKey();
                    break;
                case CERTIFICATE:
                    addCertificate();
                    break;
            }
        } finally {
            tracer.wayout();
        }
    }

    void addSecretKey() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "addSecretKey()");
        try {
            String algorithm = this.console.readString("AES|ChaCha20|HmacSHA512", "Algorithm");
            int keySize = this.console.readInt("128|256|512", "Keysize");
            String alias = this.console.readString("[A-Za-z0-9-]{5,25}", "Alias");

            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            this.keyStore.setEntry(alias, secretKeyEntry, this.shamirsLoadParameter.getProtectionParameter());
            this.keyStore.store(this.shamirsLoadParameter);
        } finally {
            tracer.wayout();
        }
    }

    void listEntries() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "listEntries()");
        try {
            Iterator<String> iter = this.keyStore.aliases().asIterator();
            while (iter.hasNext()) {
                String alias = iter.next();
                KeyStore.Entry keyStoreEntry = this.keyStore.getEntry(alias, this.shamirsLoadParameter.getProtectionParameter());
                String algorithm = null, keyEntryType = null;
                if (keyStoreEntry instanceof KeyStore.SecretKeyEntry) {
                    KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreEntry;
                    algorithm = secretKeyEntry.getSecretKey().getAlgorithm();
                    keyEntryType = "Secret Key";
                } else if (this.keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                    algorithm = privateKeyEntry.getPrivateKey().getAlgorithm();
                    keyEntryType = "Private Key";
                } else if (this.keyStore.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class)) {
                    KeyStore.TrustedCertificateEntry trustedCertificateEntry = (KeyStore.TrustedCertificateEntry) keyStoreEntry;
                    algorithm = trustedCertificateEntry.getTrustedCertificate().getPublicKey().getAlgorithm();
                    keyEntryType = "Trusted Certificate";
                }
                Set<KeyStore.Entry.Attribute> entryAttributes = keyStoreEntry.getAttributes();
                Map<String, KeyStore.Entry.Attribute> entryAttrMap = entryAttributes.stream()
                        .peek(entryAttribut -> tracer.out().printfIndentln("attr: %s = %s", entryAttribut.getName(), entryAttribut.getValue()))
                        .collect(Collectors.toMap(entryAttribut -> entryAttribut.getName(), Function.identity()));
                final String FRIENDLY_NAME_OOID = "1.2.840.113549.1.9.20", LOCAL_ID_OOID = "1.2.840.113549.1.9.21",
                        TRUSTED_KEY_USAGE_OID = "2.16.840.1.113894.746875.1.1";
                String friendlyName = entryAttrMap.containsKey(FRIENDLY_NAME_OOID) ? entryAttrMap.get(FRIENDLY_NAME_OOID).getValue() : "null";
                String localId = entryAttrMap.containsKey(LOCAL_ID_OOID) ? entryAttrMap.get(LOCAL_ID_OOID).getValue() : "null";
                String trustedKeyUsage = entryAttrMap.containsKey(TRUSTED_KEY_USAGE_OID) ? entryAttrMap.get(TRUSTED_KEY_USAGE_OID).getValue() : "null";
                tracer.out().printfIndentln("friendlyName(%1$s) = %2$s, localId(%1$s) = %3$s, algorithm(%1$s) = %4$s, trustedKeyUsage(%1$s) = %5$s",
                        alias, friendlyName, localId, algorithm, trustedKeyUsage);
                System.console().printf("%s-> %s: friendlyName=%s, localId=%s, algorithm=%s, keytype=%s\n",
                        this.app.getCurrentWorkspace().getFileName(), alias, friendlyName, localId, algorithm, keyEntryType);
            }
        } finally {
            tracer.wayout();
        }
    }

    void addPrivateKey() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "addPrivateKey()");
        try {
            String algorithm = this.console.readString("DSA|RSA|EC", "Keygenerator algorithm");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
            String signatureAlgo;
            switch (algorithm) {
                case "DSA":
                    keyPairGenerator.initialize(2048);
                    signatureAlgo = "SHA256withDSA";
                    break;
                case "RSA":
                    keyPairGenerator.initialize(4096);
                    signatureAlgo = "SHA256withRSA";
                    break;
                case "EC":
                    ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp521r1");
                    keyPairGenerator.initialize(ecGenParameterSpec);
                    signatureAlgo = "SHA256withECDSA";
                    break;
                default:
                    throw new NoSuchAlgorithmException(String.format("%s is not supported.", algorithm));
            }
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            tracer.out().printfIndentln("keyPair.getPrivate().getAlgorithm() = %s, keyPair.getPrivate().getEncoded().length = %d",
                    keyPair.getPrivate().getAlgorithm(), keyPair.getPrivate().getEncoded().length);

            int validity = this.console.readInt("[0-9]+", "Validity");
            String commonName = this.console.readString("[A-Za-z- ]{5,30}", "Common Name");
            String locality = this.console.readString("[A-Za-z- ]{5,30}", "Locality");
            String state = this.console.readString("[A-Za-z- ]{5,30}", "State");
            String country = this.console.readString("[A-Za-z- ]{5,30}", "Country");
            String distinguishedName = String.format("CN=%s, L=%s, ST=%s, C=%s", commonName, locality, state, country);
            String alias = this.console.readString("[a-z0-9-]{5,25}", "Alias");

            Instant now = Instant.now();
            Date notBefore = Date.from(now);
            Date notAfter = Date.from(now.plus(Duration.ofDays(validity)));
            try {
                ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgo).build(keyPair.getPrivate());
                X500Name x500Name = new X500Name(distinguishedName);
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
                this.keyStore.setEntry(
                        alias,
                        new KeyStore.PrivateKeyEntry(keyPair.getPrivate(),
                                new Certificate[]{x509Certificate}),
                        this.shamirsLoadParameter.getProtectionParameter()
                );
            } catch (OperatorCreationException ex) {
                throw new GeneralSecurityException(ex);
            }
        } finally {
            tracer.wayout();
        }
    }

    void addCertificate() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "addCertificate()");
        try {
            String url = this.console.readString("https://[A-Za-z-\\./]{5,30}", "URL");
            String alias = this.console.readString("[A-Za-z0-9-]{5,25}", "Alias");

            final int TIME_OUT = 30;
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(Duration.ofSeconds(TIME_OUT))
                    .build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            try {
                HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
                Optional<SSLSession> sslSession = httpResponse.sslSession();
                sslSession.ifPresentOrElse(
                        session -> {
                            try {
                                Certificate[] certificates = session.getPeerCertificates();
                                tracer.out().printfIndentln("certificates.length = %d", certificates.length);
                                for (int i = 0; i < certificates.length; i++) {
                                    this.keyStore.setCertificateEntry(alias + i, certificates[i]);
                                }
                            } catch (SSLPeerUnverifiedException | KeyStoreException ex) {
                                tracer.logException(LogLevel.ERROR, ex, getClass(), "addCertificate()");
                            }
                        },
                        () -> tracer.logMessage(
                                LogLevel.WARNING, "No ssl session available.", getClass(), "addCertificate()"
                        )
                );
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
