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
