/*
 * ByteArraySeekableSource.java
 *
 * Created on May 17, 2006, 12:41 PM
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
 *
 */

package com.nothome.delta;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Wraps a byte buffer as a source
 * 
 * @author Heiko Klein
 */
public class ByteArraySeekableSource implements SeekableSource {
    
    private ByteBuffer bb;
    private ByteBuffer cur;
    
    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public ByteArraySeekableSource(byte[] source) {
        if (source == null)
            throw new NullPointerException("source");
        this.bb = ByteBuffer.wrap(source);
    }
    
    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public ByteArraySeekableSource(ByteBuffer bb) {
        if (bb == null)
            throw new NullPointerException("bb");
        this.bb = bb;
    }
    
    public void seek(long pos) throws IOException {
        bb.rewind();
        cur = bb.slice();
        if (pos > cur.limit())
            throw new IllegalArgumentException("pos " + pos + " cannot seek " + cur.limit());
        cur.position((int) pos);
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        if (!bb.hasRemaining())
            return -1;
        int read = Math.min(len, bb.remaining());
        bb.get(b, off, read);
        return read;
    }
    
    public long length() throws IOException {
        return bb.limit();
    }
    
    public void close() throws IOException {
        bb = null;
        cur = null;
    }
}
