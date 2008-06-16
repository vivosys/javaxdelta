/*
 *
 * Copyright (c) 2001 Torgeir Veimo
 * Copyright (c) 2002 Nicolas PERIDONT
 * Copyright (c) 2006 Heiko Klein
 * Copyright (c) 2008 Elias Ross
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

package com.nothome.delta.text;

import gnu.trove.TLongIntHashMap;

import java.io.IOException;
import java.nio.CharBuffer;

class Checksum {
    
    public static final int S = (1 << 4); // 16
    
    private static final char single_hash[] = com.nothome.delta.Checksum.getSingleHash();
    protected TLongIntHashMap checksums = new TLongIntHashMap();
    protected long length;
    
    public Checksum(Readable sis) throws IOException {
        CharBuffer buf = CharBuffer.allocate(S);
            
        // generate cheksums at each interval
        int i = 0;
        while (true) {
            int count = sis.read(buf);
            if (count == -1)
                break;
            buf.flip();
            length += count;
            checksums.put(queryChecksum(buf), i++);
            buf.clear();
        }
    }
    
    private static char hash(char c) {
        byte b = (byte)c;
        return single_hash[b+128];
    }
    
    public static long queryChecksum(CharSequence buf) {
        int high = 0; int low = 0;
        for (int i = 0; i < buf.length(); i++) {
            low += hash(buf.charAt(i));
            high += low;
        }
        return ((high & 0xffff) << 16) | (low & 0xffff);
    }
    
    public static long incrementChecksum(long checksum, char out, char in) {
        char old_c = hash(out);
        char new_c = hash(in);
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
    
    public int findChecksumIndex(long checksum) {
        return checksums.get(checksum);
    }
    
    /**
     * Returns length of the source.
     */
    long getLength() {
        return length;
    }

    /**
     * Returns a debug <code>String</code>.
     */
    @Override
    public String toString()
    {
        return super.toString() +
            " checksums=" + this.checksums +
            " length=" + this.length +
            "";
    }
    
}
