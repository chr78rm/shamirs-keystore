package de.christofreichardt.jca.shamirsdemo;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;

import java.io.IOException;
import java.util.Map;

abstract public class AbstractMenu implements Menu, Traceable {

    final App app;
    final Map<String, Command> shortCuts;

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
