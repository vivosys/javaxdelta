/*
 * SimplePrime.java
 *
 * Created on April 21, 2007, 8:02 PM
 * Copyright (c) 2007 Heiko Klein
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
 *
 */

package com.nothome.delta;

/**
 *
 * @author Heiko Klein
 */
public class SimplePrime {
    
    /**
     * Creates a new instance of SimplePrime
     */
    private SimplePrime() {
    }

    /**
     * @return lowest number below or equal the current prime, return 0 for number < 2
     */
    static public long belowOrEqual(long number) {
        if (number < 2) {
            return 0;
        } else if (number == 2) {
            return 2;
        }
        if ((number & 1) == 0) { // even
            number--;
        }
        while (! testPrime(number)) {
            number -= 2;
            if (number <= 2) {
                return 2;
            }
        }
        return number;
    }
    
    static public long aboveOrEqual(long number) {
        if (number <= 2) {
            return 2;
        }
        if ((number & 1) == 0) { // even
            number++;
        }
        while (! testPrime(number)) {
            number += 2;
            if (number < 0) {
                return 0; // overflow
            }
        }
        return number;
    }
    
    static public boolean testPrime(long number) {
        if (number == 2) {
            return true;
        }
        if (number < 2) {
            return false;
        }
        if ((number & 1) == 0) { // even
            return false;
        }
        long sqrt = (long) Math.floor(Math.sqrt(number));
        for (long i = 3; i <= sqrt; i+=2) {
            if ((number % i) == 0) {
                return false;
            }
        }
        return true;
    }
}
