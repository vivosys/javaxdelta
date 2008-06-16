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

/**
 *
 * @author Heiko Klein
 */
public class ByteArraySeekableSource implements SeekableSource {
    byte[] source;
    long lastPos = 0;
    
    /**
     * Creates a new instance of ByteArraySeekableSource
     */
    public ByteArraySeekableSource(byte[] source) {
        this.source = source;
    }
    public void seek(long pos) throws IOException {
        lastPos = pos;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        int maxLength = source.length - (int) lastPos;
        if (maxLength <= 0) {
            return -1;
        }
        if (maxLength < len) {
            len = maxLength;
        }
        System.arraycopy(source, (int) lastPos, b, off, len);
        lastPos += len;
        return len;
    }
    public long length() throws IOException {
        return source.length;
    }
    public void close() throws IOException {
        source = null;
    }
}
