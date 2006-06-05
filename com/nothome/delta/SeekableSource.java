/*
 * SeekableSource.java
 *
 * Created on May 17, 2006, 12:33 PM
 *
 */

package com.nothome.delta;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a wrapper interface which presents the functions use in
 * GDiffPatcher for the RandomAccessFile
 * 
 * @author Heiko Klein
 */
public interface SeekableSource {
    
    public void seek(long pos) throws IOException ;
    public int read(byte[] b, int off, int len) throws IOException ;
    public void close() throws IOException;
    public long length() throws IOException ;
    
    
    
}
