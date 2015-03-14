package io.scaledml;

import org.junit.Test;

import static org.junit.Assert.*;

public class VowpalWabbitFormatTest {

    @Test
    public void testParse() throws Exception {
        String line1 = "-1 |C1 1005 |banner_pos 1 |site_id 0a742914 |site_domain 510bd839 |site_category f028772b " +
                "|app_id ecad2386 |app_domain 7801e8d9 |app_category 07d7df22 |device_id a99f214a |device_ip 0cff710f " +
                "|device_model 76dc4769 |device_type 1 |device_conn_type 0 |C14 8330 |C15 320 |C16 50 |C17 761 |C18 3 " +
                "|C19 175 |C20 100075";
        VowpalWabbitFormat format = new VowpalWabbitFormat();
        SparseItem item = format.parse(line1, 500);
        assertNotNull(item);
        assertEquals(-1.f, item.getLabel(), 0.000001);
        assertEquals(20, item.getIndexes().size());
    }
}