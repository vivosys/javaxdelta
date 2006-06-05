/*
 * SeekableSourceInputStream.java
 *
 * Created on May 26, 2006, 2:21 PM
 *
 */

package com.nothome.delta;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Heiko Klein
 */
public class SeekableSourceInputStream extends InputStream {
    SeekableSource ss;
    /** Creates a new instance of SeekableSourceInputStream */
    public SeekableSourceInputStream(SeekableSource ss) {
        this.ss = ss;
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        ss.read(b, 0, 1);
        return b[0];
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return ss.read(b,off,len);
    }

    public void close() throws IOException {
        ss.close();
    }
    
}
