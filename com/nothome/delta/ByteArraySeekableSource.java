/*
 * ByteArraySeekableSource.java
 *
 * Created on May 17, 2006, 12:41 PM
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
