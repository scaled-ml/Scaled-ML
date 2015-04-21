package io.scaledml.core.inputformats;

import io.scaledml.core.inputformats.ColumnsMask.ColumnType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColumnsMaskTest {

    @Test
    public void testParse1() {
        ColumnsMask mask = new ColumnsMask("lc[37]n");
        assertEquals(ColumnType.LABEL, mask.getCategory(0));
        assertEquals(ColumnType.CATEGORICAL, mask.getCategory(30));
        assertEquals(ColumnType.NUMERICAL, mask.getCategory(47));
        assertEquals(ColumnType.NUMERICAL, mask.getCategory(1024));
    }

    @Test
    public void testParse2() {
        ColumnsMask mask = new ColumnsMask("lc");
        assertEquals(ColumnType.LABEL, mask.getCategory(0));
        assertEquals(ColumnType.CATEGORICAL, mask.getCategory(30));
        assertEquals(ColumnType.CATEGORICAL, mask.getCategory(47));
        assertEquals(ColumnType.CATEGORICAL, mask.getCategory(1024));
    }

    @Test
    public void testParse3() {
        ColumnsMask mask = new ColumnsMask("ilcccccnnnnn");
        assertEquals(ColumnType.ID, mask.getCategory(0));
        assertEquals(ColumnType.LABEL, mask.getCategory(1));
        assertEquals(ColumnType.CATEGORICAL, mask.getCategory(4));
        assertEquals(ColumnType.NUMERICAL, mask.getCategory(1024));
    }
}