/*
 * BitArray.java
 * Copyright (c) 2006 Heiko Klein
 *
 * Created on May 18, 2006, 9:22 AM
 * 
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

/**
 *
 * @author Heiko Klein
 */
public class BitArray {
    int[] implArray;
    int size;
    final static int INT_SIZE = 32;
    /** Creates a new instance of BitArray */
    public BitArray(int size) {
        int implSize = (int) (size / INT_SIZE) + 1;
        implArray = new int[implSize];
    }
    public void set(int pos, boolean value) {
        int implPos = (int) (pos / INT_SIZE);
        int bitMask = 1 << (pos & (INT_SIZE - 1));            
        if (value) {
            implArray[implPos] |= bitMask; // set true if true
        } else {
            implArray[implPos] &= ~bitMask;
         }
    }
    
    public boolean get(int pos) {
        int implPos = (int) (pos / INT_SIZE);
        int bitMask = 1 << (pos & (INT_SIZE - 1));
        return (implArray[implPos] & bitMask) != 0;
    }
    
}
