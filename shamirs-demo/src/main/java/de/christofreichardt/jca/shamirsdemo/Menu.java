package de.christofreichardt.jca.shamirsdemo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public interface Menu {

    interface Command {
        String getShortCut();
        String getFullName();
        String getDisplayName();
    }

    void print();
    Command readCommand() throws IOException;
    <T extends Command> void execute(T command) throws IOException, GeneralSecurityException;
    boolean isExit();
    Map<String, Command> computeShortCutMap();
}
