/*
 * EratosthenesPrimes.java
 *
 * Created on May 17, 2006, 3:33 PM
 *
 */

package com.nothome.delta;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * EratosthenesPrimes calculates primes using the Sieve of Eratosthenes.
 * It contains mostly static methods, storing the primes of the last
 * search.
 *
 * If you intent to search a large number of primes, try to initialize
 * the sieve with a number larger than the largest number you are looking
 * for.
 *
 * The implementation will stores the complete sieve (this is more memory efficient than
 * storing the primes).
 * This can require a  some memory! (256MB for all ~100mio. primes up to MAX_INTEGER).
 *
 * You might want to release the sieve when your sure you retrieved all primes.
 *
 * Calculation of primes up to MAX_INTEGER takes about 2min (1:30 for server vm) on a
 * 2GHz PC.
 *
 * @author Heiko Klein
 */
public class EratosthenesPrimes {
    //static int[] primes;
    static BitArray sieve;
    static int lastInit = -1;
    
    
    /**
     * reset the sieve and clear all allocated memory
     */
    synchronized static public void reset() {
        sieve = null;
        lastInit = -1;
    }
    
    synchronized static public void init(int maxNumber) {
        if (maxNumber <= lastInit) {
            return;
        }
        int sqrt = (int) Math.ceil(Math.sqrt(maxNumber));
        lastInit = maxNumber;
        
        // Reducing speed and memory requirements by 2
        // while working in the %2 space.
        // Using >> 1 shift operator in case the compiler doesn't
        // translate /2 efficiently.
        maxNumber >>= 1;
        maxNumber++;
        sqrt >>= 1;
        sqrt++;
        //boolean[] sieve = new boolean[maxNumberCeil + 1];
        sieve = new BitArray(maxNumber + 1);
        // false: cannot get devided
        // true: can get devided
        sieve.set(0,true); // 1 is no prime
        //sieve[0] = true;
        for (int i = 1; i <= sqrt; i++) {
            if (! sieve.get(i)) {
                //if (!sieve[i]) {
                // 2*i+1 is now a prime for sure
                int currentPrime = (i<<1) + 1;
                // n^2+2kn
                for (int j = i*((i<<1)+2); j <= maxNumber; j += currentPrime) {
                    sieve.set(j, true);
                    //sieve[j] = true;
                }
            }
        }
        
    }

    synchronized static public int[] getPrimes(int maxNumber) {
        int primesNo = primesCount(maxNumber);
        if (primesNo <= 0) {
            return new int[]{};
        }
        if (maxNumber == 2) {
            return new int[]{2};
        }
	init(maxNumber);
        int[] primes = new int[primesNo];
        int maxNumber_2 = (maxNumber-1) >> 1;
        int prime = 0;
        primes[prime++] = 2; // has not been calculated for performance
        for (int i = 1; i <= maxNumber_2; i++) {
            if (!sieve.get(i)) {
                //if (!sieve[i]) {
                primes[prime++] = ((i << 1) + 1);
            }
        }
        return primes;
    }
    
    synchronized static public int primesCount(int number) {
        if (number < 2) {
            return 0;
        }
        init(number);
        int maxNumber_2 = (number-1) >> 1;
        // get number of primes
        int primesNo = 1; // add 2
        for (int i = 1; i <= maxNumber_2; i++) {
            if (!sieve.get(i)) {
                //if (!sieve[i]) {
                primesNo++;
            }
        }
        return primesNo;
    }
    
    
    /**
     * @return -1 if no prime exists below the value (0,1)
     */
    synchronized static public int belowOrEqual(int number) {
        if (number < 2) return -1;
        if (number == 2) return 2;
	init(number);
        int maxNumber_2  = (number-1) >> 1;
        for (int i = maxNumber_2; i > 0; i--) {
            if (! sieve.get(i)) {
                return ((i << 1) + 1);
            }
        }
        return -1;
    }
    /**
     * @return -1 if no prime exists below the value (0,1,2)
     */
    static public int below(int number) {
        return belowOrEqual(number-1);
    }
}
