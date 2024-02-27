/*
 * Shamirs Keystore
 *
 * Copyright (C) 2017, 2022, Christof Reichardt
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
                input = input.length() == 0 ? proposal : input;
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
                    System.console().printf("%s-> %s (%s) [%s]: ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal);
                    int actualRead = reader.read(input);
                    int length = actualRead - System.lineSeparator().length();
                    if (length > 0) {
                        input.rewind();
                        input = CharBuffer.wrap(input.subSequence(0, length));
                    } else {
                        input = CharBuffer.wrap(proposal);
                    }
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
