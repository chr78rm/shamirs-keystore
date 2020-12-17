/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2020, Christof Reichardt
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
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.jca.shamir.ShamirsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class App implements Traceable {

    final String currentDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    private Path currentWorkspace;
    private Menu menu;

    public App() throws IOException {
        this.setCurrentWorkspace(initWorkspace());
        this.menu = new MainMenu(this);
    }

    public Path getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(Path currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
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

    private void mainLoop() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "mainLoop()");
        try {
            do {
                try {
                    System.console().printf("\n");
                    System.console().printf("+---------------+\n");
                    System.console().printf("| Shamir's Demo |\n");
                    System.console().printf("+---------------+\n");
                    this.menu.print();
                    Menu.Command command = this.menu.readCommand();
                    if (command != null) {
                        System.console().printf("%s-> %s\n", this.currentWorkspace.getFileName(), command.getFullName());
                        this.menu.execute(command);
                    }
                } catch (IOException | IllegalArgumentException | NoSuchElementException | GeneralSecurityException ex) {
                    ex.printStackTrace();
                    Throwable throwable = ex;
                    do {
                        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                        for (StackTraceElement stackTraceElement : stackTraceElements) {
                            tracer.out().printfIndentln("  at %s.%s(%s:%d)", stackTraceElement.getClassName(),
                                    stackTraceElement.getMethodName(), stackTraceElement.getFileName(),
                                    stackTraceElement.getLineNumber());
                        }
                        throwable = throwable.getCause();
                        if (throwable != null) {
                            tracer.out().printfIndentln("caused by = %s: %s", throwable.getClass().getName(),
                                    throwable.getMessage());
                        }
                    } while (throwable != null);

                }
            } while(!this.menu.isExit());
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
                if (System.console() == null) {
                    throw new Error("There isn't any system console.");
                }
                Security.addProvider(new ShamirsProvider());
                App app = new App();
                app.mainLoop();
                System.console().printf("\n");
            } finally {
                tracer.wayout();
            }
        } finally {
            TracerFactory.getInstance().closePoolTracer();
        }
    }
}
