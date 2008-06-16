/*
 * DeltaDiffPatchTestSuite.java
 * JUnit based test
 *
 * Created on May 26, 2006, 9:06 PM
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
 */

package com.nothome.delta;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

/**
 * Testing the boundaries.
 * 
 * @author Heiko Klein
 * @author Stefan Liebig
 */
public class DeltaDiffPatchBoundariesTest extends TestCase {

	public void testCase1() throws Exception {
		assertTrue( run( "0123456789abcdef", "0123456789abcdef" ) );
	}

	public void testCase2() throws Exception {
		assertTrue( run( "0123456789abcdef", "0123456789abcdef+" ) );
	}

	public void testCase3() throws Exception {
		assertTrue( run( "0123456789abcdef0", "0123456789abcdef0+" ) );
	}

	public void testCase4() throws Exception {
		assertTrue( run( "0123456789abcdef0123456789abcdef", "0123456789abcdef0123456789abcdef+" ) );
	}

        public void testCase5() throws Exception {
		assertTrue( run( "0123456789abcdef0123456789abcdef", "0123456789abcdef" ) );
	}

        public void testCase6() throws Exception {
		assertTrue( run( "Seite reserviert. Hier soll demnächst etwas über mich stehen.", "Seite reserviert. Hier soll demnächst etwas über mich stehen. (Test der Umlaute)" ) );
	}

        
        private boolean run( String string1, String string2 ) throws Exception {
		File test1File = new File( "test1.txt" );
		File test2File = new File( "test2.txt" );

		OutputStream os = new FileOutputStream( test1File );
		os.write( string1.getBytes() );
		os.close();
		os = new FileOutputStream( test2File );
		os.write( string2.getBytes() );
		os.close();

		File patchedFile = new File( "patchedFile.txt" );
		File deltaFile = new File( "delta" );

		try {
			DiffWriter output = new GDiffWriter( new DataOutputStream( new BufferedOutputStream( new FileOutputStream( deltaFile ) ) ) );
			Delta.computeDelta( test1File, test2File, output );
			output.close();

			assertTrue( deltaFile.exists() );

			new GDiffPatcher( test1File, deltaFile, patchedFile );
			assertTrue( patchedFile.exists() );

			assertEquals( patchedFile.length(), string2.getBytes().length );
			byte[] buf = new byte[string2.getBytes().length];
			new FileInputStream( patchedFile ).read( buf );

			String got = new String( buf );
			assertEquals( string2, got );
		} catch ( DeltaException e ) {
			throw e;
		} finally {
                    test1File.delete();
                    test2File.delete();
                    deltaFile.delete();
                    patchedFile.delete();
                }
		return true;
	}

}
