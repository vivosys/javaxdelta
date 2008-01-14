/*
 * SimplePrimeTest.java
 * JUnit based test
 *
 * Created on April 21, 2007, 8:24 PM
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
public class SimplePrimeTest extends TestCase {
    
    public SimplePrimeTest(String testName) {
        super(testName);
    }

    public void testTestPrime() {
        assertTrue(SimplePrime.testPrime(2L));
        assertTrue(SimplePrime.testPrime(3L));
        assertTrue(SimplePrime.testPrime(5L));
        assertTrue(SimplePrime.testPrime(7L));
        assertTrue(SimplePrime.testPrime(11L));
        assertTrue(SimplePrime.testPrime(13L));
        
        assertFalse(SimplePrime.testPrime(-3));
        assertFalse(SimplePrime.testPrime(1));
        assertFalse(SimplePrime.testPrime(4));
        assertFalse(SimplePrime.testPrime(6));
        assertFalse(SimplePrime.testPrime(9));
        assertFalse(SimplePrime.testPrime(15));
        assertFalse(SimplePrime.testPrime(25));
    }

    public void testAboveOrEqual() {
        assertEquals(2,SimplePrime.aboveOrEqual(2));
        assertEquals(2,SimplePrime.aboveOrEqual(-1));
        assertEquals(2,SimplePrime.aboveOrEqual(1));
        assertEquals(5,SimplePrime.aboveOrEqual(4));
        assertEquals(11,SimplePrime.aboveOrEqual(8));
        assertEquals(2147483659L, SimplePrime.aboveOrEqual(Integer.MAX_VALUE + 1L));        
    }

    public void testBelowOrEqual() {
        assertEquals(2,SimplePrime.belowOrEqual(2));
        assertEquals(0,SimplePrime.belowOrEqual(1));
        assertEquals(5,SimplePrime.belowOrEqual(6));
        assertEquals(7,SimplePrime.belowOrEqual(9));
        assertEquals(23,SimplePrime.belowOrEqual(28));
        assertEquals(Integer.MAX_VALUE, SimplePrime.belowOrEqual(Integer.MAX_VALUE)); // Integer.MAX_VALUE is a Mersienne prime
    }

    
}
