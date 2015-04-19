package io.scaledml.ftrl.options;

import io.scaledml.core.inputformats.ColumnsMask;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ColumnsInfoTest {

       @Test
    public void parsesLongMask() {
        ColumnsMask ilcnn = new ColumnsMask("ilcnn");
        assertEquals(ColumnsMask.ColumnType.ID, ilcnn.getCategory(0));
        assertEquals(ColumnsMask.ColumnType.LABEL, ilcnn.getCategory(1));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(2));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(3));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(4));
    }

    @Test
    public void parsesWithBrackets1() {
        ColumnsMask ilcnn = new ColumnsMask("ic[1]n");
        assertEquals(ColumnsMask.ColumnType.ID, ilcnn.getCategory(0));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(1));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(2));
    }

    @Test
    public void parsesWithBrackets2() {
        ColumnsMask ilcnn = new ColumnsMask("ic[2]n");
        assertEquals(ColumnsMask.ColumnType.ID, ilcnn.getCategory(0));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(1));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(2));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(3));
    }

    @Test
    public void parsesWithBracketsAndTheRest() {
        ColumnsMask ilcnn = new ColumnsMask("lc[2]n[4]c");
        assertEquals(ColumnsMask.ColumnType.LABEL, ilcnn.getCategory(0));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(1));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(2));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(3));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(4));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(5));
        assertEquals(ColumnsMask.ColumnType.NUMERICAL, ilcnn.getCategory(6));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(7));
        assertEquals(ColumnsMask.ColumnType.CATEGORICAL, ilcnn.getCategory(100));
    }

}