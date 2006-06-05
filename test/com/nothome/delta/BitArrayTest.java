/*
 * BitArrayTest.java
 * JUnit based test
 *
 * Created on May 18, 2006, 9:50 AM
 */

package com.nothome.delta;

import junit.framework.*;

/**
 *
 * @author Heiko Klein
 */
public class BitArrayTest extends TestCase {
    BitArray ba = new BitArray(100);
    
    public BitArrayTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BitArrayTest.class);
        return suite;
    }

    
    /**
     * Test of set method, of class com.nothome.delta.BitArray.
     */
    public void testSet() {
        System.out.println("set");
        
        int pos = 0;
        boolean value = true;
        
        ba.set(56, true);
        assertTrue(ba.get(56));
        
        ba.set(56, false);
        assertFalse(ba.get(56));
        
        
    }

    
}
