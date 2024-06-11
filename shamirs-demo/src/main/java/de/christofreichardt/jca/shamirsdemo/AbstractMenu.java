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
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

abstract public class AbstractMenu implements Menu, Traceable {

    final AppCallback app;
    private Map<String, Command> shortCuts;

    class Console {
        String readString(String regex, String label) {
            String input;
            do {
                input = System.console().readLine("%s-> %s (%s): ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex);
            } while (!Pattern.matches(regex, input));

            return input;
        }

        String readString(String regex, String label, String proposal) {
            String input;
            do {
                input = (System.console().readLine("%s-> %s (%s) [%s]: ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal));
                input = input.isEmpty() ? proposal : input;
            } while (!Pattern.matches(regex, input));

            return input;
        }

        CharSequence readCharSequence(String regex, String label, CharSequence proposal) throws IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("CharSequence", this, "readCharSequence(String regex, String label, CharSequence proposal)");

            try {
                final int MAX_LENGTH = 100;
                Reader reader = System.console().reader();
                CharBuffer input;
                do {
                    input = CharBuffer.allocate(MAX_LENGTH);
                    tracer.out().printfIndentln("input.limit() = %d", input.limit());
                    tracer.out().printfIndentln("System.lineSeparator().length() = %d", System.lineSeparator().length());
                    System.console().printf("%s-> %s (%s) [%s]: ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal);
                    int actualRead = reader.read(input);
                    tracer.out().printfIndentln("actualRead = %d", actualRead);
                    int length = actualRead - System.lineSeparator().length();
                    tracer.out().printfIndentln("length = %d", length);
                    if (length > 0) {
                        input.rewind();
                        input = CharBuffer.wrap(input.subSequence(0, length));
                    } else {
                        input = CharBuffer.wrap(proposal);
                    }
                    tracer.out().printfIndentln("input = %s", input);
                    tracer.out().flush();
                } while (!Pattern.matches(regex, input));

                return input;
            } finally {
                tracer.wayout();
            }
        }
        
        CharSequence readCharacters(String regex, String label, CharSequence proposal) throws IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("CharSequence", this, "readCharacters(String regex, String label, CharSequence proposal)");

            try {
                final boolean echo = Objects.equals("ON", System.getProperty("de.christofreichardt.jca.shamirsdemo.console.echo", "OFF"));
                final char BACK_SPACE = '\u0008';
                final int LINE_FEED = 0x0A, CARRIAGE_RETURN = 0x0D;
                Reader reader = System.console().reader();
                StringBuilder input;
                tracer.out().printfIndentln("echo = %b", echo);
                tracer.out().printfIndentln("reader.getClass().getName() = %s", reader.getClass().getName());
                tracer.out().flush();
                do {
                    input = new StringBuilder();
                    System.console().printf("%s-> %s (%s) [%s]: ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal);
                    int c, index = 0;
                    do {
                        c = reader.read();
                        tracer.out().printfIndentln("index = %d, c = 0x%X, c = %d", index, c, c);
                        tracer.out().flush();
                        if (c != BACK_SPACE) {
                            index++;
                            if (echo) {
                                System.console().writer().print((char) c);
                            }
                            input.appendCodePoint(c);
                        } else {
                            if (index > 0) {
                                index--;
                                input.deleteCharAt(index);
                                if (echo) {
                                    System.console().writer().print(BACK_SPACE);
                                    System.console().writer().print(' ');
                                    System.console().writer().print(BACK_SPACE);
                                }
                            }
                        }
                    } while (c != LINE_FEED && c != CARRIAGE_RETURN);
                    if (echo) {
                        System.console().writer().println();
                    }
                    tracer.out().printfIndentln("input.chars() = %s", Arrays.toString(input.chars().toArray()));
                    tracer.out().printfIndentln("input.codePoints() = %s", Arrays.toString(input.codePoints().toArray()));
                    input.deleteCharAt(input.length() - 1);
                    tracer.out().printfIndentln("input = %s, input.length() = %d", input, input.length());
                    tracer.out().flush();
                    if (input.length() == 0) {
                        input = new StringBuilder(proposal);
                    }
                } while (!Pattern.matches(regex, input));

                return input;
            } finally {
                tracer.wayout();
            }
        }

        CharSequence readChars(String regex, String label, CharSequence proposal) throws IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("CharSequence", this, "readChars(String regex, String label, CharSequence proposal)");

            try {
                final int JDK_8308591 = 22; // see https://www.oracle.com/java/technologies/javase/22-relnote-issues.html#JDK-8308591
                int jdkSpecVersion = Integer.parseInt(System.getProperty("java.specification.version"));
                String jdkConsole = System.getProperty("jdk.console");
                tracer.out().printfIndentln("java.specification.version = %d, jdk.console = %s", jdkSpecVersion, jdkConsole);
                if (jdkSpecVersion < JDK_8308591 || Objects.equals("java.base", jdkConsole)) {
                    return readCharSequence(regex, label, proposal);
                } else {
                    return readCharacters(regex, label, proposal);
                }
            } finally {
                tracer.wayout();
            }
        }

        CharSequence readPassword(String regex, String label, CharSequence proposal) throws IOException {
            AbstractTracer tracer = getCurrentTracer();
            tracer.entry("CharSequence", this, "readPassword(String regex, String label, CharSequence proposal)");

            try {
                CharSequence input;
                do {
                    char[] buf = System.console().readPassword("%s-> %s (%s) [%s]: ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal);
                    input = buf.length == 0 ? proposal : CharBuffer.wrap(buf);
                } while (!Pattern.matches(regex, input));

                return input;
            } finally {
                tracer.wayout();
            }
        }

        String readString(Pattern pattern, String label) {
            String input;
            do {
                input = System.console().readLine("%s-> %s (%s): ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, pattern.pattern());
            } while (!pattern.matcher(input).matches());

            return input;
        }

        int readInt(String regex, String label) {
            Integer input = null;
            do {
                String line = System.console().readLine("%s-> %s (%s): ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex);
                if (Pattern.matches(regex, line)) {
                    input = Integer.parseInt(line);
                }
            } while (input == null);

            return input;
        }
    }

    final Console console = new Console();

    public AbstractMenu(AppCallback app) {
        this.app = app;
    }

    @Override
    public Command readCommand() {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Command", this, "readCommand()");
        try {
            if (Objects.isNull(this.shortCuts)) {
                this.shortCuts = computeShortCutMap();
            }
            Command selectedCommand = null;
            System.console().printf("\n");
            do {
                String line = System.console().readLine("%s-> ", this.app.getCurrentWorkspace().getFileName());
                tracer.out().printfIndentln("line = %s, %d", line, (line != null ? line.length() : -1));
                tracer.out().flush();
                if (line != null) {
                    if (this.shortCuts.containsKey(line)) {
                        selectedCommand = this.shortCuts.get(line);
                    } else {
                        selectedCommand = this.shortCuts.values().stream()
                                .filter(command -> command.getFullName().startsWith(line))
                                .findFirst()
                                .orElseThrow();
                    }
                    tracer.out().printfIndentln("command = %s", selectedCommand);
                    tracer.out().flush();
                }
            } while (selectedCommand == null);

            return selectedCommand;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
