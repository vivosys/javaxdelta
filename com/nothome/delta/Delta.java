 /*
  *
  * Copyright (c) 2001 Torgeir Veimo
  * Copyright (c) 2002 Nicolas PERIDONT
  * Bug Fixes: Daniel Morrione dan@morrione.net
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

import java.io.*;

public class Delta {
    
    public final static int S = Checksum.S;
    public final static boolean debug = false;
    public final static int buff_size = 64 * S;
    
    public Delta() {
    }
    
    static public void computeDelta(SeekableSource source, InputStream targetIS, int targetLength, DiffWriter output)
    throws IOException, DeltaException {
        
        int sourceLength = (int) source.length();
        int targetidx = 0;
        
        Checksum checksum = new Checksum();
        
        if (debug) {
            System.out.println("source len: " + sourceLength);
            // System.out.println("target len: " + targetLength);
            System.out.println("using match length S = " + S);
        }
        
        checksum.generateChecksums(new SeekableSourceInputStream(source), sourceLength);
        source.seek(0);
        
        PushbackInputStream target =
                new PushbackInputStream(
                new BufferedInputStream(targetIS),
                buff_size);
        
        boolean done = false;
        byte buf[] = new byte[S];
        long hashf = 0;
        byte b[] = new byte[1];
        byte sourcebyte[] = new byte[S];
        
        if (targetLength - targetidx <= S) {
            //gls031504a start
            throw new DeltaException("Unable to compute delta, input file is too short");
            //gls031504a end
        }
        if (sourceLength <= S) {
            throw new DeltaException("Unable to compute delta, source file is too short");
        }
        
        // initialize first complete checksum.
        int bytesRead = target.read(buf, 0, S);
        targetidx += S;
        
        hashf = checksum.queryChecksum(buf, S);
        
        // The check for alternative hashf is only because I wanted to verify that the
        // update method really is correct. I will remove it shortly.
        long alternativehashf = hashf;
        
        if (debug)
            System.out.println(
                    "my hashf: " + hashf + ", adler32: " + alternativehashf);
        
        /*This flag indicates that we've run out of source bytes*/
        boolean sourceOutofBytes = false;
        
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
                    long start = System.currentTimeMillis();
                    
                    boolean ok = true;
                    byte[] sourceBuff = new byte[buff_size];
                    byte[] targetBuff = new byte[buff_size];
                    int source_idx = 0;
                    int target_idx = 0;
                    int tCount = 0;
                    
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
                        buf[0] = b[0]; // don't loose this byte
                        int remaining = targetLength - targetidx;
                        int readStatus=target.read(buf, 1, remaining);
                        targetidx += remaining;
                        for (int ix = 0; ix < (remaining + 1); ix++)
                            output.addData(buf[ix]);
                        done = true;
                    } else {
                        buf[0] = b[0];
                        target.read(buf, 1, S - 1);
                        targetidx += S - 1;
                        alternativehashf =
                                hashf = checksum.queryChecksum(buf, S);
                    }
                    continue; //continue loop
                }
            }
            
            if (targetLength - targetidx > 0) {
                // update the adler fingerpring with a single byte
                
                target.read(b, 0, 1);
                targetidx += 1;
                
                // insert instruction with the old byte we no longer use...
                output.addData(buf[0]);
                
                alternativehashf =
                        checksum.incrementChecksum(alternativehashf, buf[0], b[0]);
                
                for (int j = 0; j < 15; j++)
                    buf[j] = buf[j + 1];
                buf[15] = b[0];
                hashf = checksum.queryChecksum(buf, S);
                
                if (debug)
                    System.out.println(
                            "raw: "
                            + Integer.toHexString((int) hashf)
                            + ", incremental: "
                            + Integer.toHexString((int) alternativehashf));
                
            } else {
                for (int ix = 0; ix < S; ix++)
                    output.addData(buf[ix]);
                done = true;
            }
            
        }
    }
    
    static public void computeDelta(byte[] source, InputStream targetIS, int targetLength, DiffWriter output)
    throws IOException, DeltaException {
        computeDelta(new ByteArraySeekableSource(source), targetIS, targetLength,output);
    }
    static public void computeDelta(File sourceFile,
            File targetFile,
            DiffWriter output)
            throws IOException, DeltaException                               //gls031504a
    {
        int targetLength = (int) targetFile.length();
        SeekableSource source = new RandomAccessFileSeekableSource(new RandomAccessFile(sourceFile, "r"));
        InputStream targetIS = new FileInputStream(targetFile);
        try {
            computeDelta(source, targetIS, targetLength, output);
        } catch (IOException e) {
            throw e;
        } catch (DeltaException e) {
            throw e;
        } finally {
            output.flush();
            source.close();
            targetIS.close();
            output.close();
        }
    }
    
    // sample program to compute the difference between two input files.
    public static void main(String argv[]) {
        Delta delta = new Delta();
        
        if (argv.length != 3) {
            System.err.println("usage Delta [-d] source target [output]");
            System.err.println(
                    "either -d or an output filename must be specified.");
            System.err.println("aborting..");
            return;
        }
        try {
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
            
            //output.close();
            
            delta.computeDelta(sourceFile, targetFile, output);
            
            output.flush();
            output.close();
            if (debug) //gls031504a
                System.out.println("finished generating delta");
        }
        //gls031504a start
        catch (Exception e) {
            System.err.println("error while generating delta: " + e);
        }
        //gls031504a end
    }
}
