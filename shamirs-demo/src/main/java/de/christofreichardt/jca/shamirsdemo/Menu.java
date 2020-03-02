package de.christofreichardt.jca.shamirsdemo;

import java.io.IOException;

public interface Menu {

    interface Command {
        String getShortCut();
        String getFullName();
        String getDisplayName();
    }

    void print();
    Command readCommand() throws IOException;
    <T extends Command> void execute(T command);
    boolean isExit();
}
