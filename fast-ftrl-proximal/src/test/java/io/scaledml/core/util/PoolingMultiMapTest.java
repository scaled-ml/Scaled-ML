package io.scaledml.core.util;


import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class PoolingMultiMapTest {

    @Test
    public void testMultimap() {
        PoolingMultiMap<LineBytesBuffer, LineBytesBuffer> mm =
                new PoolingMultiMap<>(LineBytesBuffer::new, LineBytesBuffer::new, new LineBytesBuffer[0]);
        LineBytesBuffer k1 = new LineBytesBuffer("abc");
        LineBytesBuffer k2 = new LineBytesBuffer("ab");
        LineBytesBuffer v1 = new LineBytesBuffer("def");
        LineBytesBuffer v2 = new LineBytesBuffer("de");
        LineBytesBuffer v3 = new LineBytesBuffer("h");
        LineBytesBuffer v4 = new LineBytesBuffer("");
        LineBytesBuffer key = new LineBytesBuffer();
        key.setContentOf(k1);
        mm.appendNextValue(key).setContentOf(v1);
        mm.appendNextValue(key).setContentOf(v2);
        key.setContentOf(k2);
        mm.appendNextValue(key).setContentOf(v3);
        mm.appendNextValue(key).setContentOf(v4);
        assertThat(mm.keys(), hasItems(k1, k2));
        assertThat(mm.getValues(k1), hasItems(v1, v2));
        assertThat(mm.getValues(k2), hasItems(v3, v4));
        mm.clear();
        Assert.assertTrue(Iterables.isEmpty(mm.keys()));
        key.setContentOf(k1);
        mm.appendNextValue(key).setContentOf(v4);
        assertThat(mm.keys(), hasItems(k1));
    }

}