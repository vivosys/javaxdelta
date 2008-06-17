 /*
  *
  * Copyright (c) 2001 Torgeir Veimo
  * Copyright (c) 2002 Nicolas PERIDONT
  * Bug Fixes: Daniel Morrione dan@morrione.net
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
  * Change Log:
  * iiimmddyyn  nnnnn  Description
  * ----------  -----  -------------------------------------------------------
  * gls100603a         Fixes from Torgeir Veimo and Dan Morrione
  * gls110603a         Stream not being closed thus preventing a file from
  *                       being subsequently deleted.
  * gls031504a         Error being written to stderr rather than throwing exception
  */

package com.nothome.delta;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;

/**
 * Class for computing deltas against a source.
 * The source file is read by blocks and a hash is computed per block.
 * Then the target is scanned for matching blocks.
 */
public class Delta {
    
    /**
     * Debug flag.
     */
    final static boolean debug = false;
    
    /**
     * Default size of 16.
     * Use a size like 4 for fairly small files, 64 or 128 for large files.
     */
    public static final int DEFAULT_CHUNK_SIZE = 1<<4;
    
    /**
     * Chunk Size.
     */
    private int S;
    
    public Delta() {
        setChunkSize(DEFAULT_CHUNK_SIZE);
    }
    
    /**
     * Sets the chunk size used.
     * Larger chunks are faster and use less memory, but create larger patches
     * as well.
     * 
     * @param size
     */
    public void setChunkSize(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Invalid size");
        S = size;
    }
    
    /**
     * Compares the source bytes with target bytes, writing to output.
     */
    public void compute(byte source[], byte target[], OutputStream output)
    throws IOException {
        compute(new ByteArraySeekableSource(target), 
                new ByteArrayInputStream(target), target.length,
                new GDiffWriter(output));
    }
    
    /**
     * Compares the source bytes with target input, writing to output.
     */
    public void compute(byte[] sourceBytes, InputStream inputStream,
            int targetSize, DiffWriter diffWriter) throws IOException {
        compute(new ByteArraySeekableSource(sourceBytes), 
                inputStream, targetSize, diffWriter);
    }
    
    /**
     * Compares the source with a target, writing to output.
     * 
     * @param target
     * @param output
     */
    public void compute(File sourceFile, File targetFile, DiffWriter output)
    throws IOException {
        RandomAccessFileSeekableSource source = new RandomAccessFileSeekableSource(new RandomAccessFile(sourceFile, "r"));
        InputStream is = new BufferedInputStream(new FileInputStream(targetFile));
        try {
            compute(source, is, (int)targetFile.length(), output);
        } finally {
            source.close();
            is.close();
        }
    }
    
    /**
     * Compares the source with a target, writing to output.
     * 
     * @param targetIS second file to compare with
     * @param targetLength target file length
     * @param output diff output
     * 
     * @throws IOException if diff generation fails
     */
    public void compute(SeekableSource source, InputStream targetIS, int targetLength, DiffWriter output)
    throws IOException {
        Checksum checksum = new Checksum();
        
        if (debug) {
            System.out.println("using match length S = " + S);
        }
        
        checksum.generateChecksums(new SeekableSourceInputStream(source), S);
        source.seek(0);
        
        int buff_size = 64 * S;
        PushbackInputStream target =
                new PushbackInputStream(
                new BufferedInputStream(targetIS),
                buff_size);
        
        boolean done = false;
        byte buf[] = new byte[S];
        long hashf = 0;
        byte b[] = new byte[1];
        byte sourcebyte[] = new byte[S];
        
        if (targetLength <= S || source.length() <= S) {
            // simply return the complete target as diff
            int readBytes;
            while ((readBytes = target.read(buf)) >= 0) {
                output.addData(buf, 0, readBytes);
            }
            return;
        }
        
        // initialize first complete checksum.
        int bytesRead = target.read(buf, 0, S);
        int targetidx = bytesRead;
        
        hashf = Checksum.queryChecksum(buf, S);
        
        if (debug)
            System.out.println("my hashf: " + hashf);
        
        /*This flag indicates that we've run out of source bytes*/
        boolean sourceOutofBytes = false;
        byte[] sourceBuff = new byte[buff_size];
        byte[] targetBuff = new byte[buff_size];
        
        while (!done) {
            
            int index = checksum.findChecksumIndex(hashf);
            if (index != -1) {
                
                boolean match = true;
                int offset = index * S;
                int length = S - 1;
                source.seek(offset);
                
                //				possible match, need to check byte for byte
                if (sourceOutofBytes == false
                        && source.read(sourcebyte, 0, S) != -1) {
                    for (int ix = 0; ix < S; ix++) {
                        if (sourcebyte[ix] != buf[ix]) {
                            match = false;
                        }
                    }
                } else {
                    sourceOutofBytes = true;
                }
                
                if (match & sourceOutofBytes == false) {
                    //System.out.println("before targetidx : " + targetidx );
                    // The length of the match is determined by comparing bytes.
                    
                    boolean ok = true;
                    int source_idx = 0;
                    int target_idx = 0;
                    
                    do {
                        source_idx = source.read(sourceBuff, 0, buff_size);
                        //System.out.print("Source: "+ source_idx);
                        if (source_idx == -1) {
                            /*Ran our of source bytes during match, so flag this*/
                            sourceOutofBytes = true;
                            //System.out.println("Source out ... target has: " + target.available());
                            break;
                        }
                        
                        /*Don't read more target bytes then source bytes ... this is *VERY* important*/
                        target_idx = target.read(targetBuff, 0, source_idx);
                        //System.out.println(" Target: "+target_idx);
                        if (target_idx == -1) {
                            /*Ran out of target bytes during this match, so we're done*/
                            //System.err.println("Ran outta bytes Sourceidx="+source_idx +" targetidx:"+target_idx );
                            break;
                        }
                        
                        int read_idx = Math.min(source_idx, target_idx);
                        int i = 0;
                        do {
                            targetidx++;
                            ++length;
                            ok = sourceBuff[i] == targetBuff[i];
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
                    
                    // this is a insert instruction
                    //System.out.println("output.addCopy("+offset+","+length+")");
                    output.addCopy(offset, length);
                    
                    if (targetLength - targetidx <= S-1) {
                        // eof reached, special case for last bytes
                        if (debug)
                            System.out.println("last part of file");
                        buf[0] = b[0]; // don't lose this byte
                        int remaining = targetLength - targetidx;
                        int readStatus=target.read(buf, 1, remaining);
                        targetidx += remaining;
                        output.addData(buf, 0, remaining + 1);
                        done = true;
                    } else {
                        buf[0] = b[0];
                        int count = target.read(buf, 1, S - 1);
                        targetidx += count;
                        hashf = Checksum.queryChecksum(buf, S);
                    }
                    continue; //continue loop
                }
            }
            
            if (targetLength - targetidx > 0) {
                // update the adler fingerpring with a single byte
                
                target.read(b, 0, 1);
                targetidx += 1;
                
                // insert instruction with the old byte we no longer use...
                output.addData(buf, 0, 1);
                
                hashf = Checksum.incrementChecksum(hashf, buf[0], b[0], S);
                
                int S1 = S - 1;
                for (int j = 0; j < S1; j++)
                    buf[j] = buf[j + 1];
                buf[S1] = b[0];
                
            } else {
                output.addData(buf, 0, S);
                done = true;
            }
            
        }
    }
    
    /**
     * Creates a patch using file names.
     */
    public static void main(String argv[]) throws Exception {
        if (argv.length != 3) {
            System.err.println("usage Delta [-d] source target [output]");
            System.err.println(
                    "either -d or an output filename must be specified.");
            System.err.println("aborting..");
            return;
        }
        DiffWriter output = null;
        File sourceFile = null;
        File targetFile = null;
        if (argv[0].equals("-d")) {
            sourceFile = new File(argv[1]);
            targetFile = new File(argv[2]);
            output = new DebugDiffWriter();
        } else {
            sourceFile = new File(argv[0]);
            targetFile = new File(argv[1]);
            output =
                new GDiffWriter(
                        new DataOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(new File(argv[2])))));
        }

        if (sourceFile.length() > Integer.MAX_VALUE
                || targetFile.length() > Integer.MAX_VALUE) {
            System.err.println(
                    "source or target is too large, max length is "
                    + Integer.MAX_VALUE);
            System.err.println("aborting..");
            return;
        }

        Delta d = new Delta();
        d.compute(sourceFile, targetFile, output);

        output.flush();
        output.close();
        if (debug) //gls031504a
            System.out.println("finished generating delta");
    }

}
