/*
 * SimplePrime.java
 *
 * Created on April 21, 2007, 8:02 PM
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
