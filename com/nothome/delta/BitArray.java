/*
 * BitArray.java
 *
 * Created on May 18, 2006, 9:22 AM
 *
 */

package com.nothome.delta;

/**
 *
 * @author Heiko Klein
 */
public class BitArray {
    int[] implArray;
    int size;
    /** Creates a new instance of BitArray */
    public BitArray(int size) {
        int implSize = (int) (size / Integer.SIZE) + 1;
        implArray = new int[implSize];
    }
    public void set(int pos, boolean value) {
        int implPos = (int) (pos / Integer.SIZE);
        int bitMask = 1 << (pos & (Integer.SIZE - 1));            
        if (value) {
            implArray[implPos] |= bitMask; // set true if true
        } else {
            implArray[implPos] &= ~bitMask;
         }
    }
    
    public boolean get(int pos) {
        int implPos = (int) (pos / Integer.SIZE);
        int bitMask = 1 << (pos & (Integer.SIZE - 1));
        return (implArray[implPos] & bitMask) != 0;
    }
    
}
