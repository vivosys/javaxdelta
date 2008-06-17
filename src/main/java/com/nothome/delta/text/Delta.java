 /*
  *
  * Copyright (c) 2001 Torgeir Veimo
  * Copyright (c) 2002 Nicolas PERIDONT
  * Bug Fixes: Daniel Morrione dan@morrione.net
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * Calcutes a delta (patch) between two text sources.
 * 
 * <li>TODO Set the checksum block size based on input size.
 * <li>TODO Update the checksum, rather than recalculate each time
 * 
 * @author Elias Ross
 */
public class Delta {
    
    public final static boolean debug = false;
    
    private SeekableSource source;
    private Reader targetReader;
    private int targetLength;
    
    /**
     * Constructs a new Delta with seekable source, target reader, and target reader length.
     */
    public Delta(SeekableSource source, Reader targetReader, long l) {
        if (source == null)
            throw new NullPointerException("source");
        if (targetReader == null)
            throw new NullPointerException("target");
        this.source = source;
        this.targetReader = targetReader;
        // TODO support Long
        this.targetLength = (int)l;
    }
    
    /**
     * Constructs a new Delta.
     */
    public Delta(CharSequence source, CharSequence target) {
        this.source = new CharBufferSeekableSource(source);
        this.targetReader = new StringReader(target.toString());
        this.targetLength = target.length();
    }
    
    /**
     * Generates and outputs a delta using the default {@link GDiffTextWriter} format.
     */
    public void writeDelta(Writer output) throws IOException
    {
        writeDelta(new GDiffTextWriter(output));
    }

    /**
     * Returns a delta as a string.
     * @see #writeDelta(Writer)
     */
    public String getDelta() throws IOException
    {
        StringWriter sw = new StringWriter();
        writeDelta(sw);
        return sw.toString();
    }
    
    /**
     * Generates and outputs a delta.
     */
    public void writeDelta(DiffTextWriter output)
    throws IOException {
        writeDelta0(output);
        output.flush();
    }
        
    /**
     * 
     * @param output
     * @throws IOException
     */
    private void writeDelta0(DiffTextWriter output)
    throws IOException {
        Checksum checksum = new Checksum(source);
        
        int S = Checksum.S;
        int buff_size = 64 * S;
        
        source.seek(0);
        
        PushbackReader target = new PushbackReader(targetReader, buff_size);
        
        char buf[] = new char[S];
        char b[] = new char[1];
        char sourcechar[] = new char[S];
        CharBuffer sourcecharbuf = CharBuffer.wrap(sourcechar);
        
        if (targetLength <= S || checksum.getLength() <= S) {
            // simply return the complete target as diff
            int readBytes;
            while ((readBytes = target.read(buf)) >= 0) {
                output.addData(CharBuffer.wrap(buf, 0, readBytes));
            }
            return;
        }
        
        // initialize first complete checksum.
        int bytesRead = target.read(buf, 0, S);
        int targetidx = bytesRead;
        
        long hashf = Checksum.queryChecksum(CharBuffer.wrap(buf));
        
        /*This flag indicates that we've run out of source bytes*/
        boolean sourceOutofBytes = false;
        
        boolean done = false;
        while (!done) {
            
            Integer index = checksum.findChecksumIndex(hashf);
            if (index != null) {
                
                boolean match = true;
                int offset = index * S;
                int length = S - 1;
                source.seek(offset);
                sourcecharbuf.clear();
                
                //				possible match, need to check byte for byte
                if (sourceOutofBytes == false
                        && source.read(sourcecharbuf) == S) {
                    for (int ix = 0; ix < S; ix++) {
                        if (sourcechar[ix] != buf[ix]) {
                            match = false;
                        }
                    }
                } else {
                    sourceOutofBytes = true;
                }
                
                if (match & sourceOutofBytes == false) {
                    
                    boolean ok = true;
                    CharBuffer sourceBuff = CharBuffer.allocate(buff_size);
                    char[] targetBuff = new char[buff_size];
                    int source_idx = 0;
                    int target_idx = 0;
                    
                    do {
                        source_idx = source.read(sourceBuff);
                        //System.out.print("Source: "+ source_idx);
                        if (source_idx == -1) {
                            /*Ran our of source bytes during match, so flag this*/
                            sourceOutofBytes = true;
                            //System.out.println("Source out ... target has: " + target.available());
                            break;
                        }
                        sourceBuff.flip();
                        
                        /*Don't read more target bytes then source bytes ... this is *VERY* important*/
                        target_idx = target.read(targetBuff, 0, source_idx);
                        if (target_idx == -1) {
                            break;
                        }
                        
                        int read_idx = Math.min(source_idx, target_idx);
                        int i = 0;
                        do {
                            targetidx++;
                            ++length;
                            ok = sourceBuff.get() == targetBuff[i];
                            i++;
                            if (!ok) {
                                b[0] = targetBuff[i - 1];
                                
                                if (target_idx != -1) {
                                    target.unread(
                                            targetBuff,
                                            i,
                                            target_idx - i);
                                }
                            }
                        } while (i < read_idx && ok);
                        b[0] = targetBuff[i-1]; //gls100603a (fix from Dan Morrione)
                    }
                    while(ok && targetLength-targetidx > 0);
                    
                    output.addCopy(offset, length);
                    
                    if (targetLength - targetidx <= S-1) {
                        // eof reached, special case for last bytes
                        buf[0] = b[0];
                        int remaining = targetLength - targetidx;
                        if (debug)
                            System.out.println("last part of file " + remaining);
                        int readStatus=target.read(buf, 1, remaining);
                        targetidx += remaining;
                        output.addData(CharBuffer.wrap(buf, 0, remaining + 1));
                        done = true;
                    } else {
                        buf[0] = b[0];
                        int count = target.read(buf, 1, S - 1);
                        targetidx += count;
                    }
                    continue; //continue loop
                }
            }
            
            if (targetLength - targetidx > 0) {
                // update the adler fingerpring with a single byte
                
                target.read(b, 0, 1);
                targetidx += 1;
                
                // insert instruction with the old byte we no longer use...
                output.addData(CharBuffer.wrap(buf, 0, 1));
                
                int S1 = S -1;
                for (int j = 0; j < S1; j++)
                    buf[j] = buf[j + 1];
                buf[S1] = b[0];
                hashf = Checksum.queryChecksum(CharBuffer.wrap(buf));
                
                if (debug)
                    System.out.println(
                            "raw: "
                            + Integer.toHexString((int) hashf));
                
            } else {
                output.addData(CharBuffer.wrap(buf));
                done = true;
            }
            
        }
    }
    
    static Reader forFile(File name) throws FileNotFoundException {
        FileInputStream f1 = new FileInputStream(name);
        InputStreamReader isr = new InputStreamReader(f1);
        return new BufferedReader(isr);
    }
    
    static CharSequence toString(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int read = r.read();
            if (read == -1)
                break;
            sb.append((char)read);
        }
        return sb;
    }
    
    public static void main(String s[]) throws IOException {
        if (s.length != 2) {
            System.err.println("Usage: java ...Delta file1 file2 [> somefile]");
            return;
        }
        Reader r1 = forFile(new File(s[0]));
        File f2 = new File(s[1]);
        Reader r2 = forFile(f2);
        CharSequence sb = toString(r1);
        Delta d = new Delta(new CharBufferSeekableSource(sb), r2, f2.length());
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        d.writeDelta(new GDiffTextWriter(osw));
        osw.close();
    }
    
}
