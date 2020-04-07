package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.scala.shamir.SecretMerging;

import java.nio.file.Path;
import java.security.DrbgParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.security.DrbgParameters.Capability.PR_AND_RESEED;

public class PasswordGenerator implements Traceable {

    private static final char[] ALPHANUMERIC = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0'};
    private static final char[] UMLAUTS = {'Ä', 'Ö', 'Ü', 'ä', 'ö', 'ü'};
    private static final char[] PUNCTUATION_AND_SYMBOLS = {'!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '<', '=', '>', '?'};
    private static final char[] ALPHANUMERIC_WITH_UMLAUTS = ArrayUtils.concat(ALPHANUMERIC, UMLAUTS);
    private static final char[] ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS = ArrayUtils.concat(ALPHANUMERIC, PUNCTUATION_AND_SYMBOLS);
    private static final char[] ALL = ArrayUtils.concat(ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS, UMLAUTS);

    public static char[] alphanumeric() {
        return Arrays.copyOf(ALPHANUMERIC, ALPHANUMERIC.length);
    }

    public static char[] umlauts() {
        return Arrays.copyOf(UMLAUTS, UMLAUTS.length);
    }

    public static char[] alphanumericWithUmlauts() {
        return Arrays.copyOf(ALPHANUMERIC_WITH_UMLAUTS, ALPHANUMERIC_WITH_UMLAUTS.length);
    }

    public static char[] alphanumericWithPunctuationAndSymbols() {
        return Arrays.copyOf(ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS, ALPHANUMERIC_WITH_PUNCTUATION_AND_SYMBOLS.length);
    }

    public static char[] all() {
        return Arrays.copyOf(ALL, ALL.length);
    }

    final SecureRandom secureRandom;
    final int length;
    final char[] symbols;

    public PasswordGenerator(int length) throws GeneralSecurityException {
        this(length, ALPHANUMERIC);
    }

    public PasswordGenerator(int length, char[] symbols) throws GeneralSecurityException {
        this.secureRandom = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(
                256, PR_AND_RESEED, "christof".getBytes()));
        this.length = length;
        this.symbols = Arrays.copyOf(symbols, symbols.length);
    }

    Stream<String> generate() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Stream<String>", this, "generate()");
        try {
            return Stream.generate(() -> password());
        } finally {
            tracer.wayout();
        }
    }

    String password() {
        AbstractTracer tracer = TracerFactory.getInstance().getDefaultTracer();
        tracer.entry("String", PasswordGenerator.class, "password()");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<this.length; i++) {
                int index = secureRandom.nextInt(this.symbols.length);
                stringBuilder.append(this.symbols[index]);
            }

            return stringBuilder.toString();
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
