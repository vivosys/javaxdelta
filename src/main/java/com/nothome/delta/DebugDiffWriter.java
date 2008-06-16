/* 
 *
 * Copyright (c) 2001 Torgeir Veimo
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


/**
 * This class implements a GDIFF output queue, that will contatenate
 * subsequent copy statements when necessary, and write both
 * copy statement and insert statement to the specified OutputStream.
 *
 * The output follows the GDIFF file specification available at
 * http://www.w3.org/TR/NOTE-gdiff-19970901.html.
 */

import java.util.*;
import java.io.*;

public class DebugDiffWriter implements DiffWriter {
    
    byte buf[] = new byte[256]; int buflen = 0;
    
    public DebugDiffWriter() {}
    
    public void addCopy(int offset, int length) throws IOException {
        if (buflen > 0)
            writeBuf();
        System.err.println("COPY off: " + offset + ", len: " + length);
    }
    
    public void addData(byte b) throws IOException {
        if (buflen < 256)
            buf[buflen++] = b;
        else
            writeBuf();
    }
    private void writeBuf() {
        System.err.print("DATA: ");
        for (int ix = 0; ix < buflen; ix++) {
            if (buf[ix] == '\n')
                System.err.print("\\n");
            else
                System.err.print(String.valueOf((char)((char) buf[ix])));
            //System.err.print("0x" + Integer.toHexString(buf[ix]) + " "); // hex output
        }
        System.err.println("");
        buflen = 0;
    }
    
    public void flush() throws IOException { }
    public void close() throws IOException { }
}

