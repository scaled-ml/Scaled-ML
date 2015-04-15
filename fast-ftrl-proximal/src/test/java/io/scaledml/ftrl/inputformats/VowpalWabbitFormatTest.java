package io.scaledml.ftrl.inputformats;

import io.scaledml.ftrl.SparseItem;
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
                .featruresProcessor(new SimpleFeaturesProcessor().featuresNumber(500));
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
                .featruresProcessor(new SimpleFeaturesProcessor().featuresNumber(500));
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item);
        assertNotNull(item);
        assertEquals(1., item.label(), 0.000001);
        assertEquals(10, item.indexes().size());
    }

    @Test
    public void testParseNumerical() throws Exception {
        String line1 = "1 |сat1 feature1:2 |сat2 feature2:100.55 фича3:-123.4 | cat3 feature4 feature5:-17";
        InputFormat format = new VowpalWabbitFormat()
                .featruresProcessor(new SimpleFeaturesProcessor().featuresNumber(500));
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item);
        assertNotNull(item);
        assertEquals(1., item.label(), 0.000001);
        assertEquals(5, item.indexes().size());
        assertEquals(2., item.values().getDouble(0), 0.000001);
        assertEquals(100.55, item.values().getDouble(1), 0.000001);
        assertEquals(-123.4, item.values().getDouble(2), 0.000001);
        assertEquals(1., item.values().getDouble(3), 0.000001);
        assertEquals(-17., item.values().getDouble(4), 0.000001);

    }

    @Test
    public void testBigNamespace() {
        String line1 = "-1 |cat CAT01=8ba8b39a CAT02=68fd1e64 CAT03=1f89b562 CAT04=891b62e7 CAT05=e7e2fcab " +
                "CAT06=a8cd5504 CAT07=5b56befb CAT08=21ddcdc9 CAT09=fc055e07 CAT10=7d1526c6 CAT11=606b0dda " +
                "CAT12=80e26c9b CAT13=f54016b9 CAT14=8d51ec69 CAT15=07b5194c CA16=7e0ccccf CAT17=3a171ecb " +
                "CAT18=25c83c98 CAT19=e5ba7672 CAT20=7b4723c4 CAT21=37c9c164 CAT22=b1252a9d CAT23=de7995b8 " +
                "CAT24=9727dd16 CAT25=8d51ec69 CAT26=581e8232 CAT27=4918af02 CAT28=2824a5f6 CAT29=fb936136 " +
                "CAT30=b2cb9c98 CAT31=1adce6ef CAT32=cc651ac8 AT33=8a544033 CAT34=860584cc CAT35=fd323779  " +
                "CAT37=a73ee510 |num NUM01:0.0 NUM02:1.0 NUM03:2 NUM04:2 NUM05:16.0 NUM06:15 NUM07:2 NUM08:6.0 " +
                "NUM09:1 NUM10:1382.0 NUM11:1  NUM13:1 NUM14:1386.0 NUM15:1.0 NUM16:181 NUM17:4 NUM18:6.0 NUM19:7.0 " +
                "NUM20:0.0 NUM21:0 NUM22:5 NUM23:1382";
        InputFormat format = new VowpalWabbitFormat()
                .featruresProcessor(new SimpleFeaturesProcessor().featuresNumber(500));
        LineBytesBuffer line = new LineBytesBuffer(line1);
        SparseItem item = new SparseItem();
        format.parse(line, item);
        assertNotNull(item);
        assertEquals(0., item.label(), 0.000001);
        assertEquals(55, item.indexes().size());
    }
}