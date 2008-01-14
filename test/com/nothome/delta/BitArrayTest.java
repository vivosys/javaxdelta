/*
 * BitArrayTest.java
 * JUnit based test
 *
 * Created on May 18, 2006, 9:50 AM
 * Copyright (c) 2006 Heiko Klein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
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
