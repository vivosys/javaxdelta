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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DebugDiffWriter implements DiffWriter {
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    
    public DebugDiffWriter() {}
    
    public void addCopy(int offset, int length) throws IOException {
        if (os.size() > 0)
            writeBuf();
        System.err.println("COPY off: " + offset + ", len: " + length);
    }
    
    public void addData(byte[] b, int offset, int length) throws IOException {
        os.write(b, offset, length);
        writeBuf();
    }
    private void writeBuf() {
        System.err.print("DATA: ");
        byte[] ba = os.toByteArray();
        for (int ix = 0; ix < ba.length; ix++) {
            if (ba[ix] == '\n')
                System.err.print("\\n");
            else
                System.err.print(String.valueOf((char)((char) ba[ix])));
            //System.err.print("0x" + Integer.toHexString(buf[ix]) + " "); // hex output
        }
        System.err.println("");
        os.reset();
    }
    
    public void flush() throws IOException { }
    public void close() throws IOException { }

}

