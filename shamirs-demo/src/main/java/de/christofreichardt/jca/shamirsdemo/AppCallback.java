package de.christofreichardt.jca.shamirsdemo;

import java.nio.file.Path;

public interface AppCallback {
    Path getCurrentWorkspace();

    void setCurrentWorkspace(Path currentWorkspace);

    void setMenu(Menu menu);
}
