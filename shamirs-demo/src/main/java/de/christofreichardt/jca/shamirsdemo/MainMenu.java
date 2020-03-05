package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.scala.shamir.SecretMerging;
import de.christofreichardt.scala.shamir.SecretSharing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainMenu implements Menu, Traceable {

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
            System.console().printf("+---------------+\n");
            System.console().printf("| Shamir's Demo |\n");
            System.console().printf("+---------------+\n");
            System.console().printf("\n");
            System.console().printf("Current time: %s\n", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.console().printf("   Workspace: %s\n", this.app.getCurrentWorkspace().getFileName());
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
    public <T extends Command> void execute(T command) {
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
            String regex = "[A-Za-z0-9]{8,15}";
            do {
                password = System.console().readLine("Password (%s): ", regex);
            } while(!Pattern.matches(regex, password));

            int shares = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("Number of shares (%s): ", regex);
                if (Pattern.matches(regex, line)) {
                    shares = Integer.parseInt(line);
                }
            } while(shares == -1);

            int threshold = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("Threshold (%s): ", regex);
                if (Pattern.matches(regex, line)) {
                    threshold = Integer.parseInt(line);
                }
            } while(threshold == -1);

            String partition;
            regex = "[A-Za-z]{1,10}";
            do {
                partition = System.console().readLine("Name of partition (%s): ", regex);
            } while (!Pattern.matches(regex, partition));

            int partitions = -1;
            regex = "[0-9]+";
            do {
                String line = System.console().readLine("Number of partitions (%s): ", regex);
                if (Pattern.matches(regex, line)) {
                    partitions = Integer.parseInt(line);
                }
            } while(partitions == -1);

            int[] sizes = new int[partitions];
            int sum = 0;
            regex = "[0-9]+";
            for (int i = 0; i < partitions; i++) {
                int size = -1;
                do {
                    String line = System.console().readLine("Size[i=%d, sum=%d] (%s): ", i, sum, regex);
                    if (Pattern.matches(regex, line)) {
                        size = Integer.parseInt(line);
                    }
                } while(size == -1);
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
            String partitions = System.console().readLine("Partitions: ");
            String[] files = partitions.split(",");
            Path[] paths = new Path[files.length];
            int i = 0;
            for (String file : files) {
                Path path = this.app.getCurrentWorkspace().resolve(file);
                tracer.out().printfIndentln("path = %s", path);
                paths[i++] = path;
            }
            String password = new String(SecretMerging.apply(paths).password());

            System.console().printf("password = %s\n", password);
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

    void listWorkspace() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "listWorkspace()");
        try {
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
