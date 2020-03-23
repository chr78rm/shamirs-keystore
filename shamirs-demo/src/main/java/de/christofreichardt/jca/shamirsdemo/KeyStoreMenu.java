package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.jca.ShamirsLoadParameter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeyStoreMenu extends AbstractMenu {

    public enum KeystoreCommand implements Command {
        LIST_ENTRIES("l", "list entries"), SECRET_KEY("s", "secret key"),
        PRIVATE_KEY("p", "private key"), CERTIFICATE("t", "certificate"),
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

    public KeyStoreMenu(App app, KeyStore keyStore, ShamirsLoadParameter shamirsLoadParameter) {
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
            System.console().printf("%s-> KeyStore menu [%s]\n", this.app.getCurrentWorkspace().getFileName(), this.shamirsLoadParameter.getFile().getName());
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
    public <T extends Command> void execute(T command) throws IOException, GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "execute(Command command)");
        try {
            tracer.out().printfIndentln("command = %s", command);

            KeystoreCommand keystoreCommand = KeystoreCommand.valueOf(command.toString());
            if (keystoreCommand == KeystoreCommand.MAIN_MENU) {
                this.keyStore.store(this.shamirsLoadParameter);
                this.app.setMenu(new MainMenu(this.app));
            } else if (keystoreCommand == KeystoreCommand.SECRET_KEY) {
                addSecretKey();
            }
        } finally {
            tracer.wayout();
        }
    }

    void addSecretKey() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "addSecretKey()");
        try {
            String regex = "AES|ChaCha20|HmacSHA512";
            String algorithm;
            do {
                algorithm = System.console().readLine("%s-> Algorithm (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, algorithm));

            regex = "128|256|512";
            int keySize = -1;
            do {
                String line = System.console().readLine("%s-> Keysize (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
                if (Pattern.matches(regex, line)) {
                    keySize = Integer.parseInt(line);
                }
            } while (keySize == -1);

            regex = "[A-Za-z0-9-]{5,25}";
            String alias;
            do {
                alias = System.console().readLine("%s-> Alias (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, alias));

            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(alias, secretKeyEntry, this.shamirsLoadParameter.getProtectionParameter());
            keyStore.store(this.shamirsLoadParameter);
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
