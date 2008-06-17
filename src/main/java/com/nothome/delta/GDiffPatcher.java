/*
 *
 * Copyright (c) 2001 Torgeir Veimo
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
 * gls031504a         Error being written to stderr rather than throwing exception
 */

package com.nothome.delta;

/**
 * This class patches an input file with a GDIFF patch fil�e.
 *
 * The patch file should follow the GDIFF file specification available at
 * http://www.w3.org/TR/NOTE-gdiff-19970901.html.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import static com.nothome.delta.GDiffWriter.*;

public class GDiffPatcher {

    public GDiffPatcher(File sourceFile, File patchFile, File outputFile)
		throws IOException
	{
        RandomAccessFileSeekableSource source =new RandomAccessFileSeekableSource(new RandomAccessFile(sourceFile, "r")); 
        InputStream patch = new FileInputStream(patchFile);
        OutputStream output = new FileOutputStream(outputFile);
        try {
            runPatch(source, patch, output);
        } catch (IOException e) {
            throw e;
        } finally {
            source.close();
            patch.close();
            output.close();
        }
    }
    public GDiffPatcher(byte[] source, InputStream patch, OutputStream output) throws IOException, PatchException{
        this(new ByteArraySeekableSource(source), patch, output);
    }
    
    public GDiffPatcher(SeekableSource source, InputStream patch, OutputStream out) throws IOException, PatchException {
        runPatch(source, patch, out);
    }
    
    private void runPatch(SeekableSource source, InputStream patch, OutputStream out) throws IOException {
        
        DataOutputStream outOS = new DataOutputStream(out);
        DataInputStream patchIS = new DataInputStream(patch);

        // the magic string is 'd1 ff d1 ff' + the version number
        if (patchIS.readUnsignedByte() != 0xd1 ||
                patchIS.readUnsignedByte() != 0xff ||
                patchIS.readUnsignedByte() != 0xd1 ||
                patchIS.readUnsignedByte() != 0xff ||
                patchIS.readUnsignedByte() != 0x04) {

            System.err.println("magic string not found, aborting!");
            return;
        }

        while (true) {
            int command = patchIS.readUnsignedByte();
            if (command == EOF)
                break;
            int length;
            int offset;
            
            if (command <= DATA_MAX) {
                append(command, patchIS, outOS);
                continue;
            }
            
            switch (command) {
            case DATA_USHORT: // ushort, n bytes following; append
                length = patchIS.readUnsignedShort();
                append(length, patchIS, outOS);
                break;
            case DATA_INT: // int, n bytes following; append
                length = patchIS.readInt();
                append(length, patchIS, outOS);
                break;
            case COPY_USHORT_UBYTE:
                offset = patchIS.readUnsignedShort();
                length = patchIS.readUnsignedByte();
                copy(offset, length, source, outOS);
                break;
            case COPY_USHORT_USHORT:
                offset = patchIS.readUnsignedShort();
                length = patchIS.readUnsignedShort();
                copy(offset, length, source, outOS);
                break;
            case COPY_USHORT_INT:
                offset = patchIS.readUnsignedShort();
                length = patchIS.readInt();
                copy(offset, length, source, outOS);
                break;
            case COPY_INT_UBYTE:
                offset = patchIS.readInt();
                length = patchIS.readUnsignedByte();
                copy(offset, length, source, outOS);
                break;
            case COPY_INT_USHORT:
                offset = patchIS.readInt();
                length = patchIS.readUnsignedShort();
                copy(offset, length, source, outOS);
                break;
            case COPY_INT_INT:
                offset = patchIS.readInt();
                length = patchIS.readInt();
                copy(offset, length, source, outOS);
                break;
            case COPY_LONG_INT:
                long loffset = patchIS.readLong();
                length = patchIS.readInt();
                copy(loffset, length, source, outOS);
                break;
            default: 
                throw new IllegalStateException("command " + command);
            }
        }
		outOS.flush();
    }

    private void copy(long offset, int length, SeekableSource source, OutputStream output)
		throws IOException, PatchException                               //gls031504a
	{
        if (offset+length > source.length())
		{
			throw new PatchException("truncated source file, aborting"); //gls031504a
        }
        byte buf[] = new byte[256];
        source.seek(offset);
        while (length > 0) {
            int len = length > 256 ? 256 : length;
            int res = source.read(buf, 0, len);
	    /*
            System.out.print("copy: " + offset + ", " + length + ":");
            for (int sx = 0; sx < len; sx++)
                if (buf[sx] == '\n')
                    System.err.print("\\n");
                else
                    System.out.print(String.valueOf((char)((char) buf[sx])));
            System.out.println("");
	    */
            output.write(buf, 0, res);
            length -= res;
        }
    }

    private void append(int length, InputStream patch, OutputStream output) throws IOException {
        byte buf[] = new byte[256];
        while (length > 0) {
            int len = Math.min(buf.length, length);
    	    int res = patch.read(buf, 0, len);
    	    if (res == -1)
    	        throw new EOFException("cannot read " + length);
            output.write(buf, 0, res);
            length -= res;
        }
    }

    // sample program to compute the difference between two input files.
    public static void main(String argv[]) {

        if (argv.length != 3) {
            System.err.println("usage GDiffPatch source patch output");
            System.err.println("aborting..");
            return;
        }
        try {
            File sourceFile = new File(argv[0]);
            File patchFile = new File(argv[1]);
            File outputFile = new File(argv[2]);

            if (sourceFile.length() > Integer.MAX_VALUE ||
            patchFile.length() > Integer.MAX_VALUE) {
                System.err.println("source or patch is too large, max length is " + Integer.MAX_VALUE);
                System.err.println("aborting..");
                return;
            }
            new GDiffPatcher(sourceFile, patchFile, outputFile);

            System.out.println("finished patching file");

        } catch (Exception ioe) {                                   //gls031504a
            System.err.println("error while patching: " + ioe);
        }
    }
}

