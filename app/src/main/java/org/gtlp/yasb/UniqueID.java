package org.gtlp.yasb;

public class UniqueID {

    private static int counter = 0;

    public static int getNext() {
        return next();
    }

    private static int next() {
        return counter++;
    }
}
