package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

abstract public class AbstractMenu implements Menu, Traceable {

    final App app;
    final Map<String, Command> shortCuts;

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
                input = (System.console().readLine("%s-> %s (%s): %s ", AbstractMenu.this.app.getCurrentWorkspace().getFileName(), label, regex, proposal));
                input = input.length() == 0 ? proposal : input;
            } while (!Pattern.matches(regex, input));

            return input;
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

    public AbstractMenu(App app) {
        this.app = app;
        this.shortCuts = computeShortCutMap();
    }

    @Override
    public Command readCommand() throws IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("Command", this, "readCommand()");
        try {
            Command command = null;
            System.console().printf("\n");
            do {
                String line = System.console().readLine("%s-> ", this.app.getCurrentWorkspace().getFileName());
                tracer.out().printfIndentln("line = %s, %d", line, (line != null ? line.length() : -1));
                tracer.out().flush();
                if (line != null) {
                    String found = this.shortCuts.keySet().stream()
                            .filter(shortCut -> line.startsWith(shortCut))
                            .findFirst()
                            .orElseThrow();

                    tracer.out().printfIndentln("found = %s, %b, %s", found, this.shortCuts.containsKey(found), this.shortCuts.get(found));
                    tracer.out().flush();

                    command = this.shortCuts.get(found);
                }
            } while (command == null);

            return command;
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentPoolTracer();
    }
}
