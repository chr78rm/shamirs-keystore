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
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.jca.shamir.PasswordGenerator;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShamirsDemoUnit implements Traceable {

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

    class PasswordCollector implements Collector<String, Map<String, Integer>, Map<String, Integer>> {

        @Override
        public Supplier<Map<String, Integer>> supplier() {
            return () -> new HashMap<>();
        }

        @Override
        public BiConsumer<Map<String, Integer>, String> accumulator() {
            return (map, password) -> {
                if (!map.containsKey(password)) {
                    map.put(password, 1);
                } else {
                    int count = map.get(password);
                    map.put(password, ++count);
                }
            };
        }

        @Override
        public BinaryOperator<Map<String, Integer>> combiner() {
            return null;
        }

        @Override
        public Function<Map<String, Integer>, Map<String, Integer>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.IDENTITY_FINISH);
        }
    }

    @Test
    @DisplayName("passwordStream")
    void passwordStream() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "passwordStream()");

        try {
            final int LIMIT = 100, LENGTH = 15;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH);
            boolean exactlyOnce = passwordGenerator.generate()
                    .map(passwordCharSeq -> passwordCharSeq.toString())
                    .limit(LIMIT)
                    .peek(password -> tracer.out().printfIndentln("%s", password))
                    .collect(new PasswordCollector())
                    .entrySet().stream()
                    .allMatch(entry -> entry.getValue() == 1);

            assertThat(exactlyOnce).isTrue();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("encoding-1")
    void encoding_1() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "encoding_1()");

        try {
            final int LIMIT = 100, LENGTH = 25;
            final char[] SYMBOLS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                    'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', '0', 'Ä', 'Ö', 'Ü', 'ä', 'ö', 'ü', '#', '$', '%', '&', '(', ')', '*', '+', '-', '<', '=', '>',
                    '?', '§', '\u00C2', '\u00D4', '\u00DB'};
            final String[] LATIN1_SUPPLEMENT_SYMBOLS = {"Ä", "Ö", "Ü", "ä", "ö", "ü", "§", "\u00C2", "\u00D4", "\u00DB"};
            final int SHARES = 8;
            final int THRESHOLD = 4;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH, SYMBOLS);
            List<String> passwords = passwordGenerator.generate()
                    .map(passwordCharSeq -> passwordCharSeq.toString())
                    .filter(password -> Stream.of(LATIN1_SUPPLEMENT_SYMBOLS).anyMatch(seq -> password.contains(seq)))
                    .limit(LIMIT)
                    .peek(password -> tracer.out().printfIndentln("%1$s, UTF-8(%1$s) = %2$s, UTF-16(%1$s) = %3$s", password, formatBytes(password.getBytes(StandardCharsets.UTF_8)), formatBytes(password.getBytes(StandardCharsets.UTF_16))))
                    .collect(Collectors.toList());
            List<String> recoveredPasswords = passwords.stream()
                    .map(password -> new SecretSharing(SHARES, THRESHOLD, password))
                    .map(sharing -> new SecretMerging(sharing.sharePoints().take(THRESHOLD).toIndexedSeq(), sharing.prime()).password())
                    .map(password -> new String(password))
                    .collect(Collectors.toList());
            assertThat(passwords).isEqualTo(recoveredPasswords);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("encoding-2")
    void encoding_2() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "encoding_2()");

        try {
            final int LIMIT = 100, LENGTH = 25;
            final int SHARES = 8;
            final int THRESHOLD = 4;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH, PasswordGenerator.alphanumericWithUmlauts());
            List<String> passwords = passwordGenerator.generate()
                    .map(passwordCharSeq -> passwordCharSeq.toString())
                    .filter(password -> {
                        boolean matched = false;
                        char[] umlauts = PasswordGenerator.umlauts();
                        for (int i=0; i<umlauts.length && !matched; i++) {
                            matched = password.indexOf(Character.codePointAt(umlauts, i)) != -1;
                        }
                        return matched;
                    })
                    .limit(LIMIT)
                    .peek(password -> tracer.out().printfIndentln("%1$s, UTF-8(%1$s) = %2$s, UTF-16(%1$s) = %3$s", password, formatBytes(password.getBytes(StandardCharsets.UTF_8)), formatBytes(password.getBytes(StandardCharsets.UTF_16))))
                    .collect(Collectors.toList());
            List<String> recoveredPasswords = passwords.stream()
                    .map(password -> new SecretSharing(SHARES, THRESHOLD, password))
                    .map(sharing -> new SecretMerging(sharing.sharePoints().take(THRESHOLD).toIndexedSeq(), sharing.prime()).password())
                    .map(password -> new String(password))
                    .collect(Collectors.toList());
            assertThat(passwords).isEqualTo(recoveredPasswords);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("encoding-3")
    void encoding_3() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "encoding_3()");

        try {
            final int LIMIT = 10, LENGTH = 25;
            final int SHARES = 8;
            final int THRESHOLD = 4;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH, PasswordGenerator.alphanumericWithUmlauts());

            IntStream.Builder builder = IntStream.builder();
            for (char c : PasswordGenerator.umlauts()) {
                builder.add(c);
            }
            tracer.out().printfIndentln("PasswordGenerator.umlauts() = %s", Arrays.toString(builder.build().toArray()));

            List<CharSequence> passwords = passwordGenerator.generate(PasswordGenerator.umlauts())
                    .peek(password -> tracer.out().printfIndentln("password.chars() = %s", Arrays.toString(password.chars().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password.codePoints() = %s", Arrays.toString(password.codePoints().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password = %s", password))
                    .limit(LIMIT)
                    .collect(Collectors.toList());
            List<CharSequence> recoveredPasswords = passwords.stream()
                    .map(password -> new SecretSharing(SHARES, THRESHOLD, password))
                    .map(sharing -> new SecretMerging(sharing.sharePoints().take(THRESHOLD).toIndexedSeq(), sharing.prime()).password())
                    .map(password -> new StringBuilder().append(password))
                    .peek(password -> tracer.out().printfIndentln("password = %s", password))
                    .collect(Collectors.toList());
            boolean allMatched = true;
            for (int i=0; i<passwords.size(); i++) {
                allMatched = CharSequence.compare(passwords.get(i), recoveredPasswords.get(i)) == 0;
                if (!allMatched) {
                    break;
                }
            }
            assertThat(allMatched).isTrue();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("requiredCharacterSets_1")
    void requiredCharacterSets_1() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "requiredCharacterSets_1()");

        try {
            final int LIMIT = 50, LENGTH = 25;
            final int SHARES = 8;
            final int THRESHOLD = 4;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH, PasswordGenerator.all());

            final Set<char[]> requiredCharSets = new HashSet<>();
            requiredCharSets.add(PasswordGenerator.alphanumeric());
            requiredCharSets.add(PasswordGenerator.umlauts());
            requiredCharSets.add(PasswordGenerator.punctuationAndSymbols());

            List<CharSequence> passwords = passwordGenerator.generate(requiredCharSets)
                    .peek(password -> tracer.out().printfIndentln("password.chars() = %s", Arrays.toString(password.chars().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password.codePoints() = %s", Arrays.toString(password.codePoints().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password = %s", password))
                    .limit(LIMIT)
                    .collect(Collectors.toList());

            class RequiredCharsetProver implements Predicate<CharSequence> {
                @Override
                public boolean test(CharSequence charSequence) {
                    return requiredCharSets.stream()
                            .allMatch(requiredCharset -> {
                                boolean found = false;
                                for (int i = 0; i < charSequence.length() && !found; i++) {
                                    for (int j = 0; j < requiredCharset.length; j++) {
                                        if (requiredCharset[j] == charSequence.charAt(i)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                return found;
                            });
                }
            }
            assertThat(passwords.stream()
                    .allMatch(new RequiredCharsetProver())
            ).isTrue();

            List<CharSequence> recoveredPasswords = passwords.stream()
                    .map(password -> new SecretSharing(SHARES, THRESHOLD, password))
                    .map(sharing -> new SecretMerging(sharing.sharePoints().take(THRESHOLD).toIndexedSeq(), sharing.prime()).password())
                    .map(password -> new StringBuilder().append(password))
                    .peek(password -> tracer.out().printfIndentln("password = %s", password))
                    .collect(Collectors.toList());

            boolean allMatched = true;
            for (int i=0; i<passwords.size(); i++) {
                allMatched = CharSequence.compare(passwords.get(i), recoveredPasswords.get(i)) == 0;
                if (!allMatched) {
                    break;
                }
            }

            assertThat(allMatched).isTrue();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("requiredCharacterSets_2")
    void requiredCharacterSets_2() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "requiredCharacterSets_2()");

        try {
            final int LIMIT = 10, LENGTH = 25;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH, PasswordGenerator.alphanumericWithUmlauts());

            Set<char[]> requiredCharSets = new HashSet<>();
            requiredCharSets.add(PasswordGenerator.punctuationAndSymbols());

            Throwable thrown = catchThrowable(() -> passwordGenerator.generate(requiredCharSets)
                    .peek(password -> tracer.out().printfIndentln("password.chars() = %s", Arrays.toString(password.chars().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password.codePoints() = %s", Arrays.toString(password.codePoints().toArray())))
                    .peek(password -> tracer.out().printfIndentln("password = %s", password))
                    .limit(LIMIT)
                    .collect(Collectors.toList()));

            assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Required character not present within given symbol set.");
        } finally {
            tracer.wayout();
        }
    }

    private String formatBytes(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(String.format("0x%02x", bytes[i]));
            if (i < bytes.length - 1) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }

    @Test
    @DisplayName("ec-keypair")
    void ecKeyPair() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "ecKeyPair()");

        try {
            final String ALGO = "EC";
            final String KEYPAIR_GEN_TYPE = "KeyPairGenerator", KEYPAIR_GEN_EC_FILTER = KEYPAIR_GEN_TYPE + "." + ALGO;
            Provider[] providers = Security.getProviders(KEYPAIR_GEN_EC_FILTER);
            Stream.of(providers).forEach(
                    provider -> {
                        tracer.out().printfIndentln("%s-Provider = %s", KEYPAIR_GEN_EC_FILTER, provider.getName());
                        String[] lines = provider.getService(KEYPAIR_GEN_TYPE, ALGO).toString().split("\n");
                        for (String line : lines) {
                            tracer.out().printfIndentln("%s", line);
                        }
                    }
            );

            final String ALGO_PARAM_TYPE = "AlgorithmParameters", ALGO_PARAM_EC_FILTER = ALGO_PARAM_TYPE + "." + ALGO;
            providers = Security.getProviders(ALGO_PARAM_EC_FILTER);
            Stream.of(providers).forEach(
                    provider -> {
                        tracer.out().printfIndentln("%s-Provider = %s", ALGO_PARAM_EC_FILTER, provider.getName());
                        String[] lines = provider.getService(ALGO_PARAM_TYPE, ALGO).toString().split("\n");
                        for (String line : lines) {
                            tracer.out().printfIndentln("%s", line);
                        }
                    }
            );

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp521r1");
            keyPairGenerator.initialize(ecGenParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("erasePasswords")
    void erasePasswords() throws GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "erasePasswords()");

        try {
            final int LIMIT = 5, LENGTH = 15, SHARES = 12, THRESHOLD = 4;
            final char fillingChar = '*';
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH);
            List<CharSequence> erasablePasswords = passwordGenerator.generate()
                    .limit(LIMIT)
                    .collect(Collectors.toList());
            List<CharSequence> copiedPasswords = erasablePasswords.stream()
                    .map(password -> password.toString())
                    .collect(Collectors.toList());
            List<CharSequence> recoveredPasswords = erasablePasswords.stream()
                    .map(password -> {
                        SecretSharing secretSharing = new SecretSharing(SHARES, THRESHOLD, password);
                        PasswordGenerator.erase(password, fillingChar);
                        return secretSharing;
                    })
                    .map(sharing -> new SecretMerging(sharing.sharePoints().take(THRESHOLD).toIndexedSeq(), sharing.prime()).password())
                    .map(recoveredPassword -> CharBuffer.wrap(recoveredPassword))
                    .collect(Collectors.toList());
            assertThat(copiedPasswords.size()).isEqualTo(recoveredPasswords.size());
            for (int i = 0; i < copiedPasswords.size(); i++) {
                tracer.out().printfIndentln("%s  <->  %s", copiedPasswords.get(i), recoveredPasswords.get(i));
                assertThat(CharSequence.compare(copiedPasswords.get(i), recoveredPasswords.get(i))).isEqualTo(0);
            }
            char[] filling = new char[LENGTH];
            Arrays.fill(filling, fillingChar);
            String yardstick = new String(filling);
            assertThat(
                    erasablePasswords.stream()
                            .peek(erasedPassword -> tracer.out().printfIndentln("erasedPassword = %s", erasedPassword))
                            .allMatch(erasedPassword -> CharSequence.compare(erasedPassword, yardstick) == 0)
            ).isTrue();
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("eraseCharBuffer")
    void eraseCharBuffer() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "eraseCharBuffer()");
        try {
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 'T', 'e', 's', 't', '.'};
            CharBuffer charBuffer = CharBuffer.wrap(characters);
            CharBuffer readOnlyCharBuffer = CharBuffer.wrap(Arrays.copyOf(charBuffer.array(), charBuffer.array().length)).asReadOnlyBuffer();
            assertThat(CharSequence.compare("This is a Test.", charBuffer)).isEqualTo(0);
            assertThat(CharSequence.compare("This is a Test.", readOnlyCharBuffer)).isEqualTo(0);
            boolean erased = PasswordGenerator.erase(charBuffer, '*');
            charBuffer.clear();
            assertThat(erased).isTrue();
            assertThat(CharSequence.compare("***************", charBuffer)).isEqualTo(0);
            erased = PasswordGenerator.erase(readOnlyCharBuffer, '*');
            assertThat(erased).isFalse();
            assertThat(CharSequence.compare("***************", charBuffer)).isEqualTo(0);
            assertThat(CharSequence.compare("This is a Test.", readOnlyCharBuffer)).isEqualTo(0);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("eraseStringBuilder")
    void eraseStringBuilder() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "eraseStringBuilder()");
        try {
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 'T', 'e', 's', 't', '.'};
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(characters);
            assertThat(CharSequence.compare("This is a Test.", stringBuilder)).isEqualTo(0);
            boolean erased = PasswordGenerator.erase(stringBuilder, '*');
            assertThat(erased).isTrue();
            assertThat(CharSequence.compare("***************", stringBuilder)).isEqualTo(0);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("eraseStringBuffer")
    void eraseStringBuffer() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "eraseStringBuffer()");
        try {
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 'T', 'e', 's', 't', '.'};
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(characters);
            assertThat(CharSequence.compare("This is a Test.", stringBuffer)).isEqualTo(0);
            boolean erased = PasswordGenerator.erase(stringBuffer, '*');
            assertThat(erased).isTrue();
            assertThat(CharSequence.compare("***************", stringBuffer)).isEqualTo(0);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("cannotEraseString")
    void cannotEraseString() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "cannotEraseString()");
        try {
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 'T', 'e', 's', 't', '.'};
            String string = new String(characters);
            assertThat(CharSequence.compare("This is a Test.", string)).isEqualTo(0);
            boolean erased = PasswordGenerator.erase(string, '*');
            assertThat(erased).isFalse();
            assertThat(CharSequence.compare("This is a Test.", string)).isEqualTo(0);
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("bufferRefreshments_1")
    @Disabled
    void bufferRefreshments_1() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "bufferRefreshments_1()");
        try {
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 'T', 'e', 's', 't', '.'};
            CharBuffer charBuffer = CharBuffer.wrap(characters);
            tracer.out().printfIndentln("charBuffer.hasArray() = %b, charBuffer.isDirect() = %b", charBuffer.hasArray(), charBuffer.isDirect());
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
            char[] transfer = new  char[characters.length];
            charBuffer.get(transfer);
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
        } finally {
            tracer.wayout();
        }
    }

    @Test
    @DisplayName("bufferRefreshments_2")
    @Disabled
    void bufferRefreshments_2() throws CharacterCodingException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "bufferRefreshments_2()");
        try {
            tracer.out().printfIndentln("-- Wrap char array --");
            char[] characters = {'T', 'h', 'i', 's', ' ', 'i', 's', ' ', 'a', ' ', 't', 'e', 's', 't', '.'};
            CharBuffer charBuffer = CharBuffer.wrap(characters);
            tracer.out().printfIndentln("charBuffer.hasArray() = %b, charBuffer.isDirect() = %b", charBuffer.hasArray(), charBuffer.isDirect());
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());

            tracer.out().printfIndentln("-- Encode characters --");
            CharsetEncoder charsetEncoder = StandardCharsets.UTF_8.newEncoder();
            ByteBuffer byteBuffer = charsetEncoder.encode(charBuffer);
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
            tracer.out().printfIndentln("byteBuffer.hasArray() = %b, byteBuffer.isDirect() = %b", byteBuffer.hasArray(), byteBuffer.isDirect());
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());
            tracer.out().printfIndentln("byteBuffer.array() = %s", formatBytes(byteBuffer.array()));

            tracer.out().printfIndentln("-- Erase CharBuffer --");
            charBuffer.clear();
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
            char[] fillingChars = new char[charBuffer.remaining()];
            Arrays.fill(fillingChars, '*');
            charBuffer.put(fillingChars);
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
            tracer.out().printfIndentln("charBuffer = %s", charBuffer);
            charBuffer.clear();
            tracer.out().printfIndentln("charBuffer = %s", charBuffer);

            tracer.out().printfIndentln("-- Get encoded bytes --");
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            tracer.out().printfIndentln("bytes = %s", formatBytes(bytes));
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());

            tracer.out().printfIndentln("-- Erase ByteBuffer  --");
            byteBuffer.clear();
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());
            byte[] fillingBytes = new byte[byteBuffer.remaining()];
            Arrays.fill(fillingBytes, (byte) 0);
            byteBuffer.put(fillingBytes);
            tracer.out().printfIndentln("byteBuffer.array() = %s", formatBytes(byteBuffer.array()));
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());

            tracer.out().printfIndentln("-- Wrap encoded bytes  --");
            byteBuffer = ByteBuffer.wrap(bytes);
            tracer.out().printfIndentln("byteBuffer.array() = %s", formatBytes(byteBuffer.array()));
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());

            tracer.out().printfIndentln("-- Decode bytes  --");
            charBuffer = StandardCharsets.UTF_8.newDecoder().decode(byteBuffer);
            tracer.out().printfIndentln("byteBuffer.capacity() = %d, byteBuffer.limit() = %d, byteBuffer.position() = %d, byteBuffer.remaining() = %d",
                    byteBuffer.capacity(), byteBuffer.limit(), byteBuffer.position(), byteBuffer.remaining());
            tracer.out().printfIndentln("charBuffer.hasArray() = %b, charBuffer.isDirect() = %b", charBuffer.hasArray(), charBuffer.isDirect());
            tracer.out().printfIndentln("charBuffer.capacity() = %d, charBuffer.limit() = %d, charBuffer.position() = %d, charBuffer.remaining() = %d",
                    charBuffer.capacity(), charBuffer.limit(), charBuffer.position(), charBuffer.remaining());
            tracer.out().printfIndentln("charBuffer = %s", charBuffer);
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
