/*
 *
 * Copyright (c) 2001 Torgeir Veimo
 * Copyright (c) 2002 Nicolas PERIDONT
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
 * Change Log:
 * iiimmddyyn  nnnnn  Description
 * ----------  -----  -------------------------------------------------------
 * gls031504         made debug output conditional
 */

package com.nothome.delta;

import java.io.*;

public class Checksum {
    
    public static final int BASE = 65521;
    public static final int S = (1 << 4); // 16
    
    public static boolean debug = false;
    
    protected int hashtable[];
    protected long checksums[];
    protected int prime;
    
    public Checksum() { }
    
    protected static final char single_hash[] = {
        /* Random numbers generated using SLIB's pseudo-random number generator. */
        0xbcd1, 0xbb65, 0x42c2, 0xdffe, 0x9666, 0x431b, 0x8504, 0xeb46,
        0x6379, 0xd460, 0xcf14, 0x53cf, 0xdb51, 0xdb08, 0x12c8, 0xf602,
        0xe766, 0x2394, 0x250d, 0xdcbb, 0xa678, 0x02af, 0xa5c6, 0x7ea6,
        0xb645, 0xcb4d, 0xc44b, 0xe5dc, 0x9fe6, 0x5b5c, 0x35f5, 0x701a,
        0x220f, 0x6c38, 0x1a56, 0x4ca3, 0xffc6, 0xb152, 0x8d61, 0x7a58,
        0x9025, 0x8b3d, 0xbf0f, 0x95a3, 0xe5f4, 0xc127, 0x3bed, 0x320b,
        0xb7f3, 0x6054, 0x333c, 0xd383, 0x8154, 0x5242, 0x4e0d, 0x0a94,
        0x7028, 0x8689, 0x3a22, 0x0980, 0x1847, 0xb0f1, 0x9b5c, 0x4176,
        0xb858, 0xd542, 0x1f6c, 0x2497, 0x6a5a, 0x9fa9, 0x8c5a, 0x7743,
        0xa8a9, 0x9a02, 0x4918, 0x438c, 0xc388, 0x9e2b, 0x4cad, 0x01b6,
        0xab19, 0xf777, 0x365f, 0x1eb2, 0x091e, 0x7bf8, 0x7a8e, 0x5227,
        0xeab1, 0x2074, 0x4523, 0xe781, 0x01a3, 0x163d, 0x3b2e, 0x287d,
        0x5e7f, 0xa063, 0xb134, 0x8fae, 0x5e8e, 0xb7b7, 0x4548, 0x1f5a,
        0xfa56, 0x7a24, 0x900f, 0x42dc, 0xcc69, 0x02a0, 0x0b22, 0xdb31,
        0x71fe, 0x0c7d, 0x1732, 0x1159, 0xcb09, 0xe1d2, 0x1351, 0x52e9,
        0xf536, 0x5a4f, 0xc316, 0x6bf9, 0x8994, 0xb774, 0x5f3e, 0xf6d6,
        0x3a61, 0xf82c, 0xcc22, 0x9d06, 0x299c, 0x09e5, 0x1eec, 0x514f,
        0x8d53, 0xa650, 0x5c6e, 0xc577, 0x7958, 0x71ac, 0x8916, 0x9b4f,
        0x2c09, 0x5211, 0xf6d8, 0xcaaa, 0xf7ef, 0x287f, 0x7a94, 0xab49,
        0xfa2c, 0x7222, 0xe457, 0xd71a, 0x00c3, 0x1a76, 0xe98c, 0xc037,
        0x8208, 0x5c2d, 0xdfda, 0xe5f5, 0x0b45, 0x15ce, 0x8a7e, 0xfcad,
        0xaa2d, 0x4b5c, 0xd42e, 0xb251, 0x907e, 0x9a47, 0xc9a6, 0xd93f,
        0x085e, 0x35ce, 0xa153, 0x7e7b, 0x9f0b, 0x25aa, 0x5d9f, 0xc04d,
        0x8a0e, 0x2875, 0x4a1c, 0x295f, 0x1393, 0xf760, 0x9178, 0x0f5b,
        0xfa7d, 0x83b4, 0x2082, 0x721d, 0x6462, 0x0368, 0x67e2, 0x8624,
        0x194d, 0x22f6, 0x78fb, 0x6791, 0xb238, 0xb332, 0x7276, 0xf272,
        0x47ec, 0x4504, 0xa961, 0x9fc8, 0x3fdc, 0xb413, 0x007a, 0x0806,
        0x7458, 0x95c6, 0xccaa, 0x18d6, 0xe2ae, 0x1b06, 0xf3f6, 0x5050,
        0xc8e8, 0xf4ac, 0xc04c, 0xf41c, 0x992f, 0xae44, 0x5f1b, 0x1113,
        0x1738, 0xd9a8, 0x19ea, 0x2d33, 0x9698, 0x2fe9, 0x323f, 0xcde2,
        0x6d71, 0xe37d, 0xb697, 0x2c4f, 0x4373, 0x9102, 0x075d, 0x8e25,
        0x1672, 0xec28, 0x6acb, 0x86cc, 0x186e, 0x9414, 0xd674, 0xd1a5
    };
    
    
    /**
     * assumes the buffer is of length S
     */
    public static long queryChecksum(byte buf[], int len) {
        int high = 0; int low = 0;
        for (int i = 0; i < len; i++) {
            low += single_hash[buf[i]+128];
            high += low;
        }
        return ((high & 0xffff) << 16) | (low & 0xffff);
    }
    
    public static long incrementChecksum(long checksum, byte out, byte in) {
        char old_c = single_hash[out+128];
        char new_c = single_hash[in+128];
        int low   = ((int)((checksum) & 0xffff) - old_c + new_c) & 0xffff;
        int high  = ((int)((checksum) >> 16) - (old_c * S) + low) & 0xffff;
        return (high << 16) | (low & 0xffff);
    }
    
    public static int generateHash(long checksum) {
        long high = (checksum >> 16) & 0xffff;
        long low = checksum & 0xffff;
        long it = (high >> 2) + (low << 3) + (high << 16);
        int hash = (int) (it ^ high ^ low);
        return hash > 0 ? hash : -hash;
    }
    
    /**
     * Initialize checksums for source. The checksum for the S bytes at offset
     * S * i is inserted into an array at index i.
     *
     * This is not good enough, we also need a hashtable into these indexes.
     *
     */
    public void generateChecksums(File sourceFile, int length) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        try {
            generateChecksums(fis, length);
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }
    }
    
    public void generateChecksums(InputStream sis, int length) throws IOException {
        
        InputStream is = new BufferedInputStream(sis);
        //int checksumcount = (int)sourceFile.length() / S;
        int checksumcount = (int) length / S;
        if (debug)  //gls031504
            System.out.println("generating checksum array of size " + checksumcount);
        
        checksums = new long[checksumcount];
        hashtable = new int[checksumcount];
        prime = findClosestPrime(checksumcount);
        
        if (debug)  //gls031504
            System.out.println("using prime " + prime);
        
        // generate cheksums at each interval
        for (int i = 0; i < checksumcount; i++) {
            
            byte buf[] = new byte[S];
            
            is.read(buf, 0, S);
            
            checksums[i] = queryChecksum(buf, S);
        }
        
        // generate hashtable entries for all checksums
        for (int i = 0; i < checksumcount; i++) hashtable[i] = -1;
        
        for (int i = 0; i < checksumcount; i++) {
            int hash = generateHash(checksums[i]) % prime;
            if (debug)
                System.out.println("checking with hash: " + hash);
            if (hashtable[hash] != -1) {
                if (debug)
                    System.out.println("hash table collision for index " + i);
            } else {
                hashtable[hash] = i;
            }
        }
        //System.out.println("checksums : " + printLongArray(checksums));
        //System.out.println("hashtable : " + printIntArray(hashtable));
    }
    
    public int findChecksumIndex(long checksum) {
        return hashtable[generateHash(checksum) % prime];
    }
    
    private static int findClosestPrime(int size) {
        int prime = (int) SimplePrime.belowOrEqual(size - 1);
        
        return (prime < 2) ? 1 : prime;
    }
    
    private String printIntArray(int[] a) {
        String result = "";
        result += "[";
        for (int i = 0; i < a.length; i++) {
            result += a[i];
            if (i != (a.length - 1))
                result += ",";
            else
                result += "]";
        }
        return result;
    }
    
    private String printLongArray(long[] a) {
        String result = "";
        result += "[";
        for (int i = 0; i < a.length; i++) {
            result += a[i];
            if (i != (a.length - 1))
                result += ",";
            else
                result += "]";
        }
        return result;
    }
}
