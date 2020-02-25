package de.christofreichardt.jca;

import de.christofreichardt.diagnosis.TracerFactory;
import java.io.InputStream;
import java.security.Security;

public class App {

    public static void main(String[] args) throws TracerFactory.Exception {
        System.console().printf("Hello, Shamir's Demo ...\n");

        TracerFactory.getInstance().reset();
        InputStream resourceAsStream = App.class.getClassLoader().getResourceAsStream("de/christofreichardt/jca/trace-config.xml");
        if (resourceAsStream != null) {
            TracerFactory.getInstance().readConfiguration(resourceAsStream);
        }
        TracerFactory.getInstance().openPoolTracer();

        try {
            Security.addProvider(new ShamirsProvider());
        } finally {
            TracerFactory.getInstance().closePoolTracer();
        }
    }

}
