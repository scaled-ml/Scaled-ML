package io.scaledml.ftrl.inputformats;

import io.scaledml.ftrl.SparseItem;
import io.scaledml.ftrl.featuresprocessors.SimpleFeatruresProcessor;
import io.scaledml.ftrl.util.LineBytesBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VowpalWabbitFormatTest {

    @Test
    public void testParse() throws Exception {
        String line1 = "-1 |C1 1005 |banner_pos 1 |site_id 0a742914 |site_domain 510bd839 |site_category f028772b " +
                "|app_id ecad2386 |app_domain 7801e8d9 |app_category 07d7df22 |device_id a99f214a |device_ip 0cff710f " +
                "|device_model 76dc4769 |device_type 1 |device_conn_type 0 |C14 8330 |C15 320 |C16 50 |C17 761 |C18 3 " +
                "|C19 175 |C20 100075";
        InputFormat format = new VowpalWabbitFormat()
                .featruresProcessor(new SimpleFeatruresProcessor().featuresNumber(500));
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item);
        assertNotNull(item);
        assertEquals(0., item.label(), 0.000001);
        assertEquals(20, item.indexes().size());
    }

    @Test
    public void testParseUtf8() throws Exception {
        String line1 = "1 |КАТ1 ФИЧА1 |кат2 фича2 фича3 |запрос у попа была собака он ее любил ";
        InputFormat format = new VowpalWabbitFormat()
                .featruresProcessor(new SimpleFeatruresProcessor().featuresNumber(500));
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item);
        assertNotNull(item);
        assertEquals(1., item.label(), 0.000001);
        assertEquals(10, item.indexes().size());
    }
}