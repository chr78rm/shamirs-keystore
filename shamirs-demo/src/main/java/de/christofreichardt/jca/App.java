package de.christofreichardt.jca;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class App implements Traceable {

    final String currentDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    Path currentWorkspace;

    public App() throws IOException {
        this.currentWorkspace = initWorkspace();
    }

    void printMenu() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "printMenu()");
        try {
            System.console().printf("\n");
            System.console().printf("+---------------+\n");
            System.console().printf("| Shamir's Demo |\n");
            System.console().printf("+---------------+\n");
            System.console().printf("\n");
            System.console().printf("Current time: %s\n", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.console().printf("   Workspace: %s\n", this.currentDate);
            System.console().printf("\n");
            System.console().printf("   %20s", "(S)plit password");
            System.console().printf("   %20s", "(M)erge password");
            System.console().printf("   %20s", "(O)pen Workspace");
            System.console().printf("\n");
            System.console().printf("   %20s", "(C)reate Keystore");
            System.console().printf("   %20s", "(L)oad Keystore");
            System.console().printf("\n");
        } finally {
            tracer.wayout();
        }
    }

    Path initWorkspace() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "initWorkspace()");
        try {
            final int RECENT_WORKSPACE_DIRS = 5;
            Path workspaceDir = Paths.get(".", "workspace");
            Stream.of(workspaceDir.toFile().listFiles(file -> file.isDirectory()))
                    .sorted((file1, file2) -> file1.getName().compareTo(file2.getName()))
                    .forEachOrdered(file -> tracer.out().printfIndentln("workspace = %s", file));
            Path workspace = workspaceDir.resolve(this.currentDate);
            if (Files.exists(workspace)) {
                if (!Files.isDirectory(workspace)) {
                    throw new IOException(String.format("%s isn't a directory."));
                }
            } else {
                Files.createDirectory(workspace);
            }

            return workspace;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }

    public static void main(String[] args) throws TracerFactory.Exception, IOException {
        TracerFactory.getInstance().reset();
        InputStream resourceAsStream = App.class.getClassLoader().getResourceAsStream("de/christofreichardt/jca/trace-config.xml");
        if (resourceAsStream != null) {
            TracerFactory.getInstance().readConfiguration(resourceAsStream);
        }
        TracerFactory.getInstance().openPoolTracer();

        try {
            AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
            tracer.initCurrentTracingContext();
            tracer.entry("void", App.class, "main(String[] args)");
            
            List<String> propertyNames = new ArrayList<>(System.getProperties().stringPropertyNames());
            propertyNames.stream()
                    .sorted()
                    .forEach((propertyName) -> {
                        tracer.out().printfIndentln("%s = %s", propertyName, System.getProperties().getProperty(propertyName));
                    });
            try {
                Security.addProvider(new ShamirsProvider());
                App app = new App();
                app.printMenu();
                System.console().printf("\n");
            } finally {
                tracer.wayout();
            }
        } finally {
            TracerFactory.getInstance().closePoolTracer();
        }
    }
}
