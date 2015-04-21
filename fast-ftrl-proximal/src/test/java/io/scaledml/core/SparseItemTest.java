package io.scaledml.core;

import io.scaledml.core.inputformats.InputFormat;
import io.scaledml.core.inputformats.VowpalWabbitFormat;
import io.scaledml.core.util.LineBytesBuffer;
import io.scaledml.ftrl.featuresprocessors.SimpleFeaturesProcessor;
import org.junit.Test;

import static org.junit.Assert.*;

public class SparseItemTest {

    @Test
    public void testWriteRead() throws Exception {
        SparseItem item = createSparseItem("-1 |C1 1005 |banner_pos 1 |site_id 0a742914 |site_domain 510bd839 |site_category f028772b " +
                "|app_id ecad2386 |app_domain 7801e8d9 |app_category 07d7df22 |device_id a99f214a |device_ip 0cff710f " +
                "|device_model 76dc4769 |device_type 1 |device_conn_type 0 |C14 8330 |C15 320 |C16 50 |C17 761 |C18 3 " +
                "|C19 175 |C20 100075");
        assertEquals(20, item.indexes().stream().distinct().count());
        testWriteRead(item);
    }

    @Test
    public void testWriteRead2() throws Exception {
        String line1 = "1 |сat1 feature1:2 |сat2 feature2:100.55 фича3:-123.4 | cat3 feature4 feature5:-17";
        SparseItem item = createSparseItem(line1);
        assertEquals(5, item.indexes().stream().distinct().count());
        testWriteRead(item);
    }

    private void testWriteRead(SparseItem item) {
        LineBytesBuffer bb = new LineBytesBuffer();
        item.write(bb);
        SparseItem other = new SparseItem();
        other.read(bb);
        assertEquals(item, other);
    }

    private SparseItem createSparseItem(String line1) {
        InputFormat format = new VowpalWabbitFormat()
                .featruresProcessor(new SimpleFeaturesProcessor());
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item, 0);
        return item;
    }
}