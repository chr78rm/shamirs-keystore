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
import java.security.DrbgParameters;
import static java.security.DrbgParameters.Capability.PR_AND_RESEED;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Generates passwords with specified length and basic character set.
 *
 * @author Christof Reichardt
 */
public class PasswordGenerator implements Traceable {

    static class ArrayUtils {
        static public char[] concat(char[] a1, char[] a2) {
            char[] result = new char[a1.length + a2.length];
            System.arraycopy(a1, 0, result, 0, a1.length);
            System.arraycopy(a2, 0, result, a1.length, a2.length);

            return result;
        }
    }

    private static final char[] ALPHANUMERIC = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0'};
    private static final char[] UMLAUTS = {'Ä', 'Ö', 'Ü', 'ä', 'ö', 'ü'};
    private static final char[] PUNCTUATION_AND_SYMBOLS = {'!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '<', '=', '>', '?'};
    private static final char[] ALPHANUMERIC_WITH_UMLAUTS = ArrayUtils.concat(ALPHANUMERIC, UMLAUTS);
    private static final char[] ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS = ArrayUtils.concat(ALPHANUMERIC, PUNCTUATION_AND_SYMBOLS);
    private static final char[] ALL = ArrayUtils.concat(ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS, UMLAUTS);

    /**
     * Defines the character set comprising alphanumeric symbols.
     *
     * @return all alphanumeric characters
     */
    public static char[] alphanumeric() {
        return Arrays.copyOf(ALPHANUMERIC, ALPHANUMERIC.length);
    }

    /**
     * Defines the character set comprising german umlauts.
     *
     * @return all german umlauts
     */
    public static char[] umlauts() {
        return Arrays.copyOf(UMLAUTS, UMLAUTS.length);
    }

    /**
     * Defines the character set comprising several punctuation marks and symbols.
     *
     * @return some punctuation marks and symbols
     */
    public static char[] punctuationAndSymbols() {
        return Arrays.copyOf(PUNCTUATION_AND_SYMBOLS, PUNCTUATION_AND_SYMBOLS.length);
    }

    /**
     * The union of {@link PasswordGenerator#alphanumeric() alphanumeric} and {@link PasswordGenerator#umlauts() umlaut} characters.
     *
     * @return all alphanumeric characters and all german umlauts
     */
    public static char[] alphanumericWithUmlauts() {
        return Arrays.copyOf(ALPHANUMERIC_WITH_UMLAUTS, ALPHANUMERIC_WITH_UMLAUTS.length);
    }

    /**
     * The union of {@link PasswordGenerator#alphanumeric() alphanumeric} and {@link PasswordGenerator#punctuationAndSymbols()} punctuationAndSymbol} characters.
     * @return all alphanumeric characters and some punctuation marks and symbols
     */
    public static char[] alphanumericWithPunctuationAndSymbols() {
        return Arrays.copyOf(ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS, ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS.length);
    }

    /**
     * The union of all specific character sets ({@link PasswordGenerator#alphanumeric() alphanumeric}, {@link PasswordGenerator#umlauts() umlauts},
     * {@link PasswordGenerator#punctuationAndSymbols() punctuationAndSymbols}).
     *
     * @return all of them
     */
    public static char[] all() {
        return Arrays.copyOf(ALL, ALL.length);
    }

    final SecureRandom secureRandom;
    final int length;
    final char[] symbols;

    /**
     * Creates a PasswordGenerator instance with the specified length and alphanumeric symbols as basic character set.
     *
     * @param length the length of the generated passwords
     * @throws GeneralSecurityException if no provider supports the required {@link SecureRandom SecureRandom} instance
     */
    public PasswordGenerator(int length) throws GeneralSecurityException {
        this(length, ALPHANUMERIC);
    }

    /**
     * Creates a PasswordGenerator instance with the specified length and given basic character set.
     *
     * @param length the length of the generated passwords
     * @param symbols the basic character set from which the passwords will be generated
     * @throws GeneralSecurityException if no provider supports the required {@link SecureRandom SecureRandom} instance
     */
    public PasswordGenerator(int length, char[] symbols) throws GeneralSecurityException {
        this.secureRandom = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(
                256, PR_AND_RESEED, "christof".getBytes()));
        this.length = length;
        this.symbols = Arrays.copyOf(symbols, symbols.length);
    }

    /**
     * Generates a stream of passwords which will be built from the basic character set with the specified length.
     *
     * @return a password stream
     */
    public Stream<CharSequence> generate() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Stream<CharSequence>", this, "generate()");
        try {
            return Stream.generate(() -> password());
        } finally {
            tracer.wayout();
        }
    }

    /**
     * Generates a stream of passwords which will be built from the basic character set with the specified length. All of the generated passwords will contain at least
     * one of the required characters.
     *
     * @param requiredChars each generated password contains at least one of them
     * @return a password stream
     */
    public Stream<CharSequence> generate(char[] requiredChars) {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Stream<CharSequence>", this, "generate(char[] requiredChars)");
        try {
            tracer.out().printfIndentln("requiredChars = %s", Arrays.toString(requiredChars));

            HashSet<char[]> requiredCharacterSet = new HashSet<>();
            requiredCharacterSet.add(requiredChars);

            return generate(requiredCharacterSet);
        } finally {
            tracer.wayout();
        }
    }

    private boolean matchRequiredCharSet(char[] requiredCharSet) {
        IntStream.Builder builder = IntStream.builder();
        for (char c : requiredCharSet) {
            builder.add(c);
        }

        boolean allFound = builder.build()
                .allMatch(c -> {
                    boolean found = false;
                    for (char symbol : this.symbols) {
                        found = c == symbol;
                        if (found) {
                            break;
                        }
                    }
                    return found;
                });

        return allFound;
    }

    private boolean requiredCharacterFound(CharSequence passwordSeq, Set<char[]> requiredCharSets) {
        return requiredCharSets.stream()
                .allMatch(requiredCharSet -> {
                    boolean matched = false;
                    for (char c : requiredCharSet) {
                        matched = passwordSeq.chars().anyMatch(passwordChar -> c == passwordChar);
                        if (matched) {
                            break;
                        }
                    }
                    return matched;
                });
    }

    /**
     *  Generates a stream of passwords which will be built from the basic character set with the specified length. All of the generated passwords will contain at least
     *  one of the characters of each required character set.
     *
     * @param requiredCharSets each generated password contains at least one character of each required character set
     * @return a password stream
     */
    public Stream<CharSequence> generate(Set<char[]> requiredCharSets) {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Stream<CharSequence>", this, "generate(char[] requiredChars)");
        try {
            requiredCharSets.forEach(requiredCharSet -> tracer.out().printfIndentln("requiredChars = %s", Arrays.toString(requiredCharSet)));

            boolean allRequiredCharsetsMatched = requiredCharSets.stream()
                    .allMatch(requiredCharSet -> matchRequiredCharSet(requiredCharSet));
            if (!allRequiredCharsetsMatched) {
                throw new IllegalArgumentException("Required character not present within given symbol set.");
            }

            return Stream.generate(() -> password())
                    .filter(password -> requiredCharacterFound(password, requiredCharSets));
        } finally {
            tracer.wayout();
        }
    }

    CharSequence password() {
        AbstractTracer tracer = TracerFactory.getInstance().getDefaultTracer();
        tracer.entry("CharSequence", this, "password()");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<this.length; i++) {
                int index = secureRandom.nextInt(this.symbols.length);
                stringBuilder.append(this.symbols[index]);
            }

            return stringBuilder;
        } finally {
            tracer.wayout();
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
