/*
 * RandomAccessFileSeekableSource.java
 *
 * Created on May 17, 2006, 1:45 PM
 *
 */

package com.nothome.delta;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Heiko Klein
 */
public class RandomAccessFileSeekableSource implements SeekableSource {
    RandomAccessFile raf;
    
    public RandomAccessFileSeekableSource(RandomAccessFile raf) {
        this.raf = raf;
    }
    public void seek(long pos) throws IOException {
        raf.seek(pos);
    }
    
    public int read (byte[] b, int off, int len) throws IOException {
            return raf.read(b, off, len);
    }
    public long length() throws IOException {
        return raf.length();
    }
    
    public void close() throws IOException{
        raf.close();
    }
}
