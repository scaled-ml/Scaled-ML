package io.scaledml.core.util;


import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongList;

public class MultiListsViewLongList extends AbstractLongList {
    private final LongList[] lists;

    public MultiListsViewLongList(LongList... lists) {
        this.lists = lists;
    }

    @Override
    public int size() {
        int size = 0;
        for (LongList list : lists) {
            size += list.size();
        }
        return size;
    }

    @Override
    public long getLong(int i) {
        for (LongList list : lists) {
            if (i < list.size()) {
                return list.getLong(i);
            }
            i -= list.size();
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
