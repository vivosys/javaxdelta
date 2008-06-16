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
 * Change Log:
 * iiimmddyyn  nnnnn  Description
 * ----------  -----  -------------------------------------------------------
 * gls100603a         Fix from Torgeir Veimo            
 */

package com.nothome.delta;


/**
 * The output follows the GDIFF file specification available at
 * http://www.w3.org/TR/NOTE-gdiff-19970901.html.
 */

import java.util.*;
import java.io.*;

public class GDiffWriter implements DiffWriter {
    
    byte buf[] = new byte[256]; int buflen = 0;

    protected boolean debug = false;
    
    //Vector writeQueue = new Vector();
    DataOutputStream output = null;
    
    public GDiffWriter(DataOutputStream os) throws IOException {
        this.output = os;
        // write magic string "d1 ff d1 ff 04"
        output.writeByte(0xd1);
        output.writeByte(0xff);
        output.writeByte(0xd1);
        output.writeByte(0xff);
        output.writeByte(0x04);
    }
    
    public void setDebug(boolean flag) { debug = flag; }
       
    public void addCopy(int offset, int length) throws IOException {
        if (buflen > 0)
            writeBuf();
        
        //output debug data        
        if (debug)
            System.err.println("COPY off: " + offset + ", len: " + length);
        
        // output real data
        byte command;
        if (offset > Integer.MAX_VALUE) {
            // use long, int format
            output.writeByte(255);
            // Actually, we don't support longer files than int.MAX_VALUE at the moment..
        } else if (offset < 65536)  {
            if (length < 256) {                
                // use ushort, ubyte
                output.writeByte(249);
                output.writeShort(offset);
                output.writeByte(length);
            } else if (length > 65535) {
                // use ushort, int
                output.writeByte(251);
                output.writeShort(offset);
                output.writeInt(length);
            } else {
                // use ushort, ushort
                output.writeByte(250);
                output.writeShort(offset);
                output.writeShort(length);
            }
        } else {
            if (length < 256) {
                // use int, ubyte
                output.writeByte(252);
                output.writeInt(offset);
                output.writeByte(length);
            } else if (length > 65535) {
                // use int, int
                output.writeByte(254);
                output.writeInt(offset);
                output.writeInt(length);
            } else {
                // use int, ushort
                output.writeByte(253);
                output.writeInt(offset);
                output.writeShort(length);
            }
        }
    }
    
    public void addData(byte b) throws IOException {
        if (buflen >= 246)
            writeBuf();
        buf[buflen] = b; buflen++;
    }
    
    private void writeBuf() throws IOException {
        // output debug data
        if (debug) {
            System.err.print("DATA:");
            for (int ix = 0; ix < buflen; ix++) {
                if (buf[ix] == '\n')
                    System.err.print("\\n");
                else
                    System.err.print(String.valueOf((char)((char) buf[ix])));
            }
            System.err.println("");
        }
        
        if (buflen > 0) {
            // output real data
            output.writeByte(buflen);
            output.write(buf, 0, buflen);            
        }
        buflen = 0;
    }
    
    public void flush() throws IOException 
    { 
    	if (buflen > 0) 
    		writeBuf(); 
		buflen = 0;		//gls100603a Fix from Torgeir Veimo
    	output.flush(); 
    }
    public void close() throws IOException { this.flush();  }
}

