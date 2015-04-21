package io.scaledml.ftrl;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {
    @Test
    public void testMain() throws Exception {
        //test parse options
        Main.main("--help");
    }
}