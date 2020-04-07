package de.christofreichardt.jca.shamirsdemo;

public class ArrayUtils {
    static public char[] concat(char[] a1, char[] a2) {
        char[] result = new char[a1.length + a2.length];
        System.arraycopy(a1, 0, result, 0, a1.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);

        return result;
    }
}
