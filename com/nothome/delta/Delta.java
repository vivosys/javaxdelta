/*
 *
 * Copyright (c) 2001 Torgeir Veimo
 * Copyright (c) 2002 Nicolas PERIDONT
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

import java.io.*;
import java.util.*;

public class Delta {

	public final static int S = Checksum.S;
	public final static boolean debug = false;
	public final static int buff_size = 64*S;


	public Delta() { }

	public void computeDelta(File sourceFile, File targetFile, DiffWriter output) throws IOException {

		int targetLength = (int) targetFile.length();
		int sourceLength = (int) sourceFile.length();
		int targetidx = 0;

		Checksum checksum = new Checksum();

		if (debug) {
			System.out.println("source len: " + sourceLength);
			System.out.println("target len: " + targetLength);
			System.out.println("using match length S = " + S);
		}

		checksum.generateChecksums(sourceFile, sourceLength);

		PushbackInputStream target = new PushbackInputStream(new BufferedInputStream(new FileInputStream(targetFile)),buff_size);
		RandomAccessFile source = new RandomAccessFile(sourceFile, "r");


		boolean done = false;
		byte buf[] = new byte[S];
		long hashf = 0; byte b[] = new byte[1]; byte sourcebyte[] = new byte[S];

		if (targetLength-targetidx <= S) {
			System.err.println("too short input file");
			return;
		}

		// initialize first complete checksum.
		target.read(buf, 0, S);
		targetidx += S;

		hashf = checksum.queryChecksum(buf, S);

		// The check for alternative hashf is only because I wanted to verify that the
		// update method really is correct. I will remove it shortly.
		long alternativehashf = hashf;

		if (debug)
			System.out.println("my hashf: " + hashf + ", adler32: " + alternativehashf);

		while (!done) {

			int index = checksum.findChecksumIndex(hashf);
			if (index != -1) {

				// possible match, need to check byte for byte
				boolean match = true;
				int offset = index * S;
				int length = S - 1;
				source.seek(offset);
				source.read(sourcebyte, 0, S);
				for (int ix = 0; ix < S; ix++) {
						if (sourcebyte[ix] != buf[ix]) {
								match = false;
						}
				}

				if (match) {
					//System.out.println("before targetidx : " + targetidx );
					// The length of the match is determined by comparing bytes.
					long start = System.currentTimeMillis();

					boolean ok = true;
					byte[] sourceBuff = new byte[buff_size];
					byte[] targetBuff = new byte[buff_size];
					int source_idx = 0;
					int target_idx = 0;

					do{
						source_idx = source.read(sourceBuff, 0, buff_size);
						target_idx = target.read(targetBuff, 0, buff_size);
						int read_idx = Math.min(source_idx,target_idx);
						int i = 0;
						do {
							targetidx++;
							++length;
							ok = sourceBuff[i] == targetBuff[i];
							i++;
							if(!ok) {
								b[0] = targetBuff[i-1];
								target.unread(targetBuff,i,target_idx-i);
							}
						} while(i < read_idx && ok);
					} while(ok && targetLength-targetidx > 0);

					// this is a insert instruction
					//System.out.println("output.addCopy("+offset+","+length+")");
					output.addCopy(offset, length);

					if (targetLength-targetidx <= S) { // eof reached, special case for last bytes
						if (debug)
							System.out.println("last part of file");
						buf[0] = b[0]; // don't loose this byte
						int remaining = targetLength-targetidx;
						target.read(buf, 1 , remaining);
						targetidx += remaining;
						for (int ix = 0; ix < (remaining + 1); ix++)
							output.addData(buf[ix]);
						done = true;
					} else {
						buf[0] = b[0];
						target.read(buf, 1, S - 1);
						targetidx += S-1;
						alternativehashf = hashf = checksum.queryChecksum(buf, S);
					}
					continue; //continue loop
				}
			}

			if (targetLength-targetidx > 0) {
				// update the adler fingerpring with a single byte

				target.read(b, 0, 1);
				targetidx += 1;

				// insert instruction with the old byte we no longer use...
				output.addData(buf[0]);

				alternativehashf = checksum.incrementChecksum(alternativehashf, buf[0], b[0]);

				for (int j = 0; j < 15; j++)
						buf[j] = buf[j+1];
				buf[15] = b[0];
				hashf = checksum.queryChecksum(buf, S);


				if (debug)
					System.out.println("raw: " + Integer.toHexString((int)hashf) + ", incremental: " + Integer.toHexString((int)alternativehashf));

			} else {
				for (int ix = 0; ix < S; ix++)
					output.addData(buf[ix]);
				done = true;
			}

		}
		output.close();
	}



	// sample program to compute the difference between two input files.
	public static void main(String argv[]) {

		Delta delta = new Delta();

		if (argv.length != 3) {
			System.err.println("usage Delta [-d] source target [output]");
			System.err.println("either -d or an output filename must be specified.");
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
				output = new GDiffWriter(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(argv[2])))));
			}

			if (sourceFile.length() > Integer.MAX_VALUE || targetFile.length() > Integer.MAX_VALUE) {
				System.err.println("source or target is too large, max length is " + Integer.MAX_VALUE);
				System.err.println("aborting..");
				return;
			}

			//output.close();

			delta.computeDelta(sourceFile, targetFile, output);
			output.flush();
			output.close();
			System.out.println("finished generating delta");
		} catch (IOException ioe) {
			System.err.println("error while generating delta; " + ioe);
		}
	}
}

