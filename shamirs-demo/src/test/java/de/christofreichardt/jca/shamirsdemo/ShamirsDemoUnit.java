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

package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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
                    map.put(password, count++);
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
                            matched = password.indexOf(Character.codePointAt(umlauts, i)) != -1 ? true : false;
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

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
