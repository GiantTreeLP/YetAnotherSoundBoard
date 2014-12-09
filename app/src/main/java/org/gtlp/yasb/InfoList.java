package org.gtlp.yasb;

import java.util.ArrayList;

public class InfoList<E> extends ArrayList<E> {
    @Override
    public E get(int index) {
        try {
            return super.get(index);
        } catch (IndexOutOfBoundsException e) {
            return (E) "";
        }
    }
}
