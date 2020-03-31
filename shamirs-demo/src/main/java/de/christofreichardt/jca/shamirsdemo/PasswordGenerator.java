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
import java.util.stream.Stream;

import static java.security.DrbgParameters.Capability.PR_AND_RESEED;

public class PasswordGenerator implements Traceable {
    static public final String[] SYMBOLS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
            "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "0"};

    final SecureRandom secureRandom;
    final int length;

    public PasswordGenerator(int length) throws GeneralSecurityException {
        this.secureRandom = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(
                256, PR_AND_RESEED, "christof".getBytes()));
        this.length = length;
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
                int index = secureRandom.nextInt(SYMBOLS.length);
                stringBuilder.append(SYMBOLS[index]);
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
