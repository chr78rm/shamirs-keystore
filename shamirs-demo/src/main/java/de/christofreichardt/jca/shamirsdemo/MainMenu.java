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
import de.christofreichardt.jca.shamir.PasswordGenerator;
import de.christofreichardt.jca.shamir.ShamirsLoadParameter;
import de.christofreichardt.jca.shamir.ShamirsProtection;
import de.christofreichardt.jca.shamir.ShamirsProvider;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainMenu  extends AbstractMenu {

    final static Pattern PARTITION_PATTERN = Pattern.compile("[A-Za-z]{1,10}");

    public enum MainCommand implements Command {
        SPLIT_PASSWORD("s", "split password"), MERGE_PASSWORD("m", "merge password"),
        OPEN_WORKSPACE("o", "open workspace"), CREATE_KEYSTORE("c", "create keystore"),
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

    private boolean exit = false;

    public MainMenu(AppCallback app) {
        super(app);
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
            System.console().printf("   %20s", MainCommand.CREATE_KEYSTORE.getDisplayName());
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
    public Map<String, Command> computeShortCutMap() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Map<String, Command>", this, "computeShortCutMap()");
        try {
            EnumSet<MainCommand> mainCommands = EnumSet.allOf(MainCommand.class);

            tracer.out().printfIndentln("mainCommands = %s", mainCommands);

            Map<String, Command> shortCuts = mainCommands.stream()
                    .collect(Collectors.toMap(mainCommand -> mainCommand.getShortCut(), Function.identity()));

            tracer.out().printfIndentln("shortCuts = %s", shortCuts);

            return shortCuts;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public void execute(Command command) throws IOException, GeneralSecurityException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("MainCommand", this, "execute(Command command)");
        try {
            tracer.out().printfIndentln("command = %s", command);

            MainCommand mainCommand = MainCommand.valueOf(command.toString());
            switch (mainCommand) {
                case SPLIT_PASSWORD:
                    splitPassword();
                    break;
                case MERGE_PASSWORD:
                    mergePassword();
                    break;
                case LOAD_KEYSTORE:
                    loadKeystore();
                    break;
                case LIST_WORKSPACE:
                    listWorkspace();
                    break;
                case CREATE_KEYSTORE:
                    createKeystore();
                    break;
                case OPEN_WORKSPACE:
                    openWorkspace();
                    break;
                case EXIT:
                    this.exit = true;
                    break;
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public boolean isExit() {
        return this.exit;
    }

    void splitPassword() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "splitPassword()");
        try {
            final int LENGTH = 25;
            PasswordGenerator passwordGenerator = new PasswordGenerator(LENGTH);
            CharSequence proposal = passwordGenerator.generate().findFirst().orElseThrow();

            CharSequence passwordSeq = this.console.readCharacters("[A-Za-z0-9-]{8,45}", "Password", proposal);
            int shares = this.console.readInt("[0-9]+", "Number of shares");
            int threshold = this.console.readInt("[0-9]+", "Threshold");
            String partition = this.console.readString(PARTITION_PATTERN, "Name of partition");
            int slices = this.console.readInt("[0-9]+", "Number of slices");

            int[] sizes = new int[slices];
            int sum = 0;
            String regex = "[0-9]+";
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
            SecretSharing secretSharing = new SecretSharing(shares, threshold, passwordSeq);
            tracer.out().printfIndentln("secretSharing = %s", secretSharing);
            PasswordGenerator.erase(passwordSeq, '\u0000');

            String certificationMethod = this.console.readString("All|Slices|None", "Certification method", "None");
            tracer.out().printfIndentln("certificationMethod = %s", certificationMethod);
            if (Objects.equals("All", certificationMethod)) {
                SecretSharing.CertificationResult certificationResult = secretSharing.certified();
                tracer.out().printfIndentln("certificationResult = %s", certificationResult);
                secretSharing.savePartition(sizes, this.app.getCurrentWorkspace().resolve(partition));
                System.console().printf("-------------------------------------------------------------\n");
                System.console().printf("certificationResult = %s\n", certificationResult);
            } else if (Objects.equals("Slices", certificationMethod)) {
                SecretSharing.CertificationResult certificationResult = secretSharing.saveCertifiedPartition(sizes, this.app.getCurrentWorkspace().resolve(partition));
                tracer.out().printfIndentln("certificationResult = %s", certificationResult);
                System.console().printf("-------------------------------------------------------------\n");
                System.console().printf("certificationResult = %s\n", certificationResult);
            } else {
                secretSharing.savePartition(sizes, this.app.getCurrentWorkspace().resolve(partition));
            }
        } finally {
            tracer.wayout();
        }
    }

    void mergePassword() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "mergePassword()");
        try {
            String regex = "(" + PARTITION_PATTERN.pattern() + "-[0-9]+" + "\\.json(,( )*)?)+" + "(" + PARTITION_PATTERN.pattern() + "-[0-9]+\\.json)?";
            String slices = this.console.readString(regex, "Slices");
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

    void loadKeystore() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "loadKeystore()");
        try {
            String keystoreName = this.console.readString("[A-Za-z-]{1,20}", "Keystore name");
            File keyStoreFile = this.app.getCurrentWorkspace().resolve(keystoreName + ".p12").toFile();

            String regex = "(" + PARTITION_PATTERN.pattern() + "-[0-9]+" + "\\.json(,( )*)?)+" + "(" + PARTITION_PATTERN.pattern() + "-[0-9]+\\.json)?";
            String slices = this.console.readString(regex, "Slices");
            String[] files = slices.split(",");
            Set<Path> paths = Stream.of(files).map(file -> this.app.getCurrentWorkspace().resolve(file.trim()))
                    .peek(path -> tracer.out().printfIndentln("path = %s", path))
                    .collect(Collectors.toSet());

            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            ShamirsLoadParameter shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, shamirsProtection);
            keyStore.load(shamirsLoadParameter);

            this.app.setMenu(new KeyStoreMenu(this.app, keyStore, shamirsLoadParameter));
        } finally {
            tracer.wayout();
        }
    }

    void createKeystore() throws GeneralSecurityException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "createKeystore()");
        try {
            String keystoreName = this.console.readString("[A-Za-z-]{1,20}", "Keystore name");
            File keyStoreFile = this.app.getCurrentWorkspace().resolve(keystoreName + ".p12").toFile();

            String regex = "(" + PARTITION_PATTERN.pattern() + "-[0-9]+" + "\\.json(,( )*)?)+" + "(" + PARTITION_PATTERN.pattern() + "-[0-9]+\\.json)?";
            String slices = this.console.readString(regex, "Slices");
            String[] files = slices.split(",");
            Set<Path> paths = Stream.of(files).map(file -> this.app.getCurrentWorkspace().resolve(file.trim()))
                    .peek(path -> tracer.out().printfIndentln("path = %s", path))
                    .collect(Collectors.toSet());

            KeyStore keyStore = KeyStore.getInstance("ShamirsKeystore", Security.getProvider(ShamirsProvider.NAME));
            ShamirsProtection shamirsProtection = new ShamirsProtection(paths);
            ShamirsLoadParameter shamirsLoadParameter = new ShamirsLoadParameter(keyStoreFile, shamirsProtection);
            keyStore.load(null, null);

            this.app.setMenu(new KeyStoreMenu(this.app, keyStore, shamirsLoadParameter));
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
                Set<String> keystoreFiles = paths
                        .map(path -> path.getFileName().toString())
                        .filter(fileName -> fileName.endsWith(".p12"))
                        .map(fileName -> fileName.substring(0, fileName.length() - ".p12".length()))
                        .collect(Collectors.toSet());
                System.console().printf("%s-> Keystores: %s\n", this.app.getCurrentWorkspace().getFileName(), keystoreFiles);
            }
        } finally {
            tracer.wayout();
        }
    }

    void openWorkspace() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "openWorkspace()");
        try {
            final int LIMIT = 2;
            Path workspaceDir = Paths.get(".", "workspace");
            System.console().printf("%s-> Workspaces:\n\n", this.app.getCurrentWorkspace().getFileName());
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(workspaceDir, path -> path.toFile().isDirectory())) {
                int count = 0;
                Iterator<Path> iter = paths.iterator();
                while (iter.hasNext()) {
                    Path path = iter.next();
                    System.console().printf("   %20s", path.getFileName());
                    if (++count == LIMIT) {
                        System.console().printf("\n");
                        count = 0;
                    }
                }
                if (count == 0) {
                    System.console().printf("\n");
                } else {
                    System.console().printf("\n\n");
                }
            }

            Path workspace = Path.of(this.console.readString("[A-Za-z0-9-]{8,45}", "Workspace"));
            workspace = workspaceDir.resolve(workspace);
            if (Files.exists(workspace) && !Files.isDirectory(workspace)) {
                throw new IOException(String.format("%s isn't a directory.", workspace));
            }
            if (!Files.exists(workspace)) {
                Files.createDirectory(workspace);
            }
            this.app.setCurrentWorkspace(workspace);
        } finally {
            tracer.wayout();
        }
    }
}
