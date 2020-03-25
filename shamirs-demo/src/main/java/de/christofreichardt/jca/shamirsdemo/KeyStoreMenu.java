package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.jca.ShamirsLoadParameter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
            String keyStoreFilename = this.shamirsLoadParameter.getFile().getName();
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
    public <T extends Command> void execute(T command) throws IOException, GeneralSecurityException {
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
                String algorithm = null;
                if (keyStoreEntry instanceof KeyStore.SecretKeyEntry) {
                    KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStoreEntry;
                    algorithm = secretKeyEntry.getSecretKey().getAlgorithm();
                }
                Set<KeyStore.Entry.Attribute> entryAttributes = keyStoreEntry.getAttributes();
                Map<String, KeyStore.Entry.Attribute> entryAttrMap = entryAttributes.stream()
                        .collect(Collectors.toMap(entryAttribut -> entryAttribut.getName(), Function.identity()));
                final String FRIENDLY_NAME_OOID = "1.2.840.113549.1.9.20", LOCAL_ID_OOID = "1.2.840.113549.1.9.21";
                String friendlyName = entryAttrMap.containsKey(FRIENDLY_NAME_OOID) ? entryAttrMap.get(FRIENDLY_NAME_OOID).getValue() : "null";
                String localId = entryAttrMap.containsKey(LOCAL_ID_OOID) ? entryAttrMap.get(LOCAL_ID_OOID).getValue() : "null";
                tracer.out().printfIndentln("friendlyName(%1$s) = %2$s, localId(%1$s) = %3$s, algorithm(%1$s) = %4$s", alias, friendlyName, localId, algorithm);
                System.console().printf("%s-> %s: friendlyName=%s, localId=%s, algorithm=%s\n",
                        this.app.getCurrentWorkspace().getFileName(), alias, friendlyName, localId, algorithm);
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
