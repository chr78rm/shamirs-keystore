package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.jca.ShamirsLoadParameter;
import de.christofreichardt.jca.ShamirsProtection;
import de.christofreichardt.jca.ShamirsProvider;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainMenu implements Menu, Traceable {

    final static Pattern PARTITION_PATTERN = Pattern.compile("[A-Za-z]{1,10}");

    public enum MainCommand implements Command {
        SPLIT_PASSWORD("s", "split password"), MERGE_PASSWORD("m", "merge password"),
        OPEN_WORKSPACE("o", "open workspace"), CREATE_KEYSTOE("c", "create keystore"),
        LIST_WORKSPACE("li", "list workspace"),
        LOAD_KEYSTORE("lo", "load keystore"), EXIT("e", "exit");

        String shortCut;
        String fullName;

        MainCommand(String shortCut, String fullName) {
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

    private final App app;
    private boolean exit = false;

    public MainMenu(App app) {
        this.app = app;
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
            System.console().printf("%s-> Main menu\n", this.app.getCurrentWorkspace().getFileName());
            System.console().printf("\n");
            System.console().printf("   %20s", MainCommand.SPLIT_PASSWORD.getDisplayName());
            System.console().printf("   %20s", MainCommand.MERGE_PASSWORD.getDisplayName());
            System.console().printf("   %20s", MainCommand.OPEN_WORKSPACE.getDisplayName());
            System.console().printf("\n");
            System.console().printf("   %20s", MainCommand.CREATE_KEYSTOE.getDisplayName());
            System.console().printf("   %20s", MainCommand.LOAD_KEYSTORE.getDisplayName());
            System.console().printf("   %20s", MainCommand.LIST_WORKSPACE.getDisplayName());
            System.console().printf("\n");
            System.console().printf("   %20s", MainCommand.EXIT.getDisplayName());
            System.console().printf("\n");
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public MainCommand readCommand() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("MainCommand", this, "readCommand()");
        try {
            EnumSet<MainCommand> mainCommands = EnumSet.allOf(MainCommand.class);

            tracer.out().printfIndentln("mainCommands = %s", mainCommands);

            Map<String, MainCommand> shortCuts = mainCommands.stream()
                    .collect(Collectors.toMap(mainCommand -> mainCommand.getShortCut(), Function.identity()));

            tracer.out().printfIndentln("shortCuts = %s", shortCuts);

            MainCommand mainCommand = null;
            System.console().printf("\n");
            do {
                String line = System.console().readLine("%s-> ", this.app.getCurrentWorkspace().getFileName());
                tracer.out().printfIndentln("line = %s, %d", line, (line != null ? line.length() : -1));
                tracer.out().flush();
                if (line != null) {
                    String found = shortCuts.keySet().stream()
                            .filter(shortCut -> line.startsWith(shortCut))
                            .findFirst()
                            .orElseThrow();

                    tracer.out().printfIndentln("found = %s, %b, %s", found, shortCuts.containsKey(found), shortCuts.get(found));
                    tracer.out().flush();

                    mainCommand = shortCuts.get(found);
                }
            } while (mainCommand == null);

            return mainCommand;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public <T extends Command> void execute(T command) throws IOException, GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("MainCommand", this, "execute(Command command)");
        try {
            tracer.out().printfIndentln("command = %s", command);

            MainCommand mainCommand = MainCommand.valueOf(command.toString());
            if (mainCommand == MainCommand.SPLIT_PASSWORD) {
                splitPassword();
            } else if (mainCommand == MainCommand.MERGE_PASSWORD) {
                mergePassword();
            } else if (mainCommand == MainCommand.LOAD_KEYSTORE) {
                loadKeystore();
            } else if (mainCommand == MainCommand.LIST_WORKSPACE) {
                listWorkspace();
            } else if (mainCommand == MainCommand.CREATE_KEYSTOE) {
                createKeystore();
            } else if (mainCommand == MainCommand.EXIT) {
                this.exit = true;
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public boolean isExit() {
        return this.exit;
    }

    void splitPassword() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "splitPassword()");
        try {
            String password;
            String regex = "[A-Za-z0-9-]{8,45}";
            do {
                password = System.console().readLine("%s-> Password (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, password));

            int shares = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("%s-> Number of shares (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
                if (Pattern.matches(regex, line)) {
                    shares = Integer.parseInt(line);
                }
            } while (shares == -1);

            int threshold = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("%s-> Threshold (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
                if (Pattern.matches(regex, line)) {
                    threshold = Integer.parseInt(line);
                }
            } while (threshold == -1);

            String partition;
            do {
                partition = System.console().readLine("%s-> Name of partition (%s): ", this.app.getCurrentWorkspace().getFileName(), PARTITION_PATTERN.pattern());
            } while (!PARTITION_PATTERN.matcher(partition).matches());

            int slices = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("%s-> Number of slices (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
                if (Pattern.matches(regex, line)) {
                    slices = Integer.parseInt(line);
                }
            } while (slices == -1);

            int[] sizes = new int[slices];
            int sum = 0;
            regex = "[0-9]+";
            for (int i = 0; i < slices; i++) {
                int size = -1;
                do {
                    String line = System.console().readLine("%s-> Size[i=%d, sum=%d] (%s): ", this.app.getCurrentWorkspace().getFileName(), i, sum, regex);
                    if (Pattern.matches(regex, line)) {
                        size = Integer.parseInt(line);
                    }
                } while (size == -1);
                sizes[i] = size;
                sum += size;
            }
            SecretSharing secretSharing = new SecretSharing(shares, threshold, password.getBytes(StandardCharsets.UTF_8));

            tracer.out().printfIndentln("secretSharing = %s", secretSharing);

            secretSharing.savePartition(sizes, this.app.getCurrentWorkspace().resolve(partition));
        } finally {
            tracer.wayout();
        }
    }

    void mergePassword() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "mergePassword()");
        try {
            String regex = "(" + PARTITION_PATTERN.pattern() + "-[0-9]+" + "\\.json(,( )*)?)+" + "(" + PARTITION_PATTERN.pattern() + "-[0-9]+\\.json)?";
            String slices;
            do {
                slices = System.console().readLine("%s-> Slices (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, slices));
            String[] files = slices.split(",");
            Path[] paths = new Path[files.length];
            int i = 0;
            for (String file : files) {
                Path path = this.app.getCurrentWorkspace().resolve(file.trim());
                tracer.out().printfIndentln("path = %s", path);
                paths[i++] = path;
            }
            String password = new String(SecretMerging.apply(paths).password());

            System.console().printf("%s-> password = %s\n", this.app.getCurrentWorkspace().getFileName(), password);
        } finally {
            tracer.wayout();
        }
    }

    void loadKeystore() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "loadKeystore()");
        try {
        } finally {
            tracer.wayout();
        }
    }

    void createKeystore() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "createKeystore()");
        try {
            String regex = "[A-Za-z-]{1,20}";
            String keystoreName;
            do {
                keystoreName = System.console().readLine("%s-> Keystore name (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, keystoreName));
            File keyStoreFile = this.app.getCurrentWorkspace().resolve(keystoreName + ".p12").toFile();

            String slices;
            regex = "(" + PARTITION_PATTERN.pattern() + "-[0-9]+" + "\\.json(,( )*)?)+" + "(" + PARTITION_PATTERN.pattern() + "-[0-9]+\\.json)?";
            do {
                slices = System.console().readLine("%s-> Slices (%s): ", this.app.getCurrentWorkspace().getFileName(), regex);
            } while (!Pattern.matches(regex, slices));
            String[] files = slices.split(",");
            Set<Path> paths = Stream.of(files).map(file -> this.app.getCurrentWorkspace().resolve(file.trim()))
                    .peek(path -> tracer.out().printfIndentln("path = %s", path))
                    .collect(Collectors.toSet());

            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            ShamirsLoadParameter shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, shamirsProtection);
            keyStore.load(null, null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry("my-secret-key", secretKeyEntry, shamirsProtection);
            keyStore.store(shamirsLoadParameter);
        } finally {
            tracer.wayout();
        }
    }

    void listWorkspace() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "listWorkspace()");
        try {
            Set<String> partitions;
            try (Stream<Path> paths = Files.list(this.app.getCurrentWorkspace())) {
                partitions = paths
                        .map(path -> path.getFileName().toString())
                        .filter(fileName -> fileName.endsWith(".json"))
                        .map(fileName -> fileName.substring(0, fileName.length() - ".json".length()))
                        .filter(fileName -> PARTITION_PATTERN.matcher(fileName).matches())
                        .peek(partition -> tracer.out().printfIndentln("partition = %s", partition))
                        .collect(Collectors.toSet());
                System.console().printf("%s-> Partitions: %s\n", this.app.getCurrentWorkspace().getFileName(), partitions);
            }

            partitions.stream()
                    .forEach(partition -> {
                        Pattern pattern = Pattern.compile(partition + "-[0-9]+\\.json");
                        try {
                            try (Stream<Path> paths = Files.list(this.app.getCurrentWorkspace())) {
                                List<String> slices = paths
                                        .map(path -> path.getFileName().toString())
                                        .filter(fileName -> pattern.matcher(fileName).matches())
                                        .sorted()
                                        .collect(Collectors.toList());
                                System.console().printf("%s-> Slices(%s): %s\n", this.app.getCurrentWorkspace().getFileName(), partition, slices);
                            }
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
            try (Stream<Path> paths = Files.list(this.app.getCurrentWorkspace())) {

            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
