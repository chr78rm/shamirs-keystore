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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainMenu implements Menu, Traceable {

    public enum MainCommand implements Command {
        SPLIT_PASSWORD("s", "split password"), MERGE_PASSWORD("m", "merge password"),
        OPEN_WORKSPACE("o", "open workspace"), CREATE_KEYSTOE("c", "create keystore"),
        LOAD_KEYSTORE("l", "load keystore"), EXIT("e", "exit");

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

            Map<Character, MainCommand> shortCuts = mainCommands.stream()
                    .collect(Collectors.toMap(mainCommand -> mainCommand.shortCut.charAt(0), Function.identity()));

            tracer.out().printfIndentln("shortCuts = %s", shortCuts);

            String line = null;
            MainCommand mainCommand = null;
            System.console().printf("\n");
            do {
                line = System.console().readLine("%s-> ", this.app.getCurrentWorkspace().getFileName());
                tracer.out().printfIndentln("line = %s, %d", line, (line != null ? line.length() : -1));
                tracer.out().flush();
                if (line != null) {
                    if (line.length() == 1) {
                        tracer.out().printfIndentln("%s, %b", line.charAt(0), shortCuts.containsKey(line.charAt(0)));
                        mainCommand = shortCuts.get(line.charAt(0));
                        break;
                    }
                }
            } while (line != null);

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
            String password = System.console().readLine("Password: ");
            int shares = Integer.parseInt(System.console().readLine("Number of shares: "));
            int threshold = Integer.parseInt(System.console().readLine("Threshold: "));
            String partition = System.console().readLine("Name of partition: ");
            int partitions = Integer.parseInt(System.console().readLine("Number of partitions: "));
            int[] sizes = new int[partitions];
            int sum = 0;
            for (int i = 0; i < partitions; i++) {
                int size = Integer.parseInt(System.console().readLine("Size[i=%d, sum=%d]: ", i, sum));
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

            tracer.out().printfIndentln("password = %s", password);
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
