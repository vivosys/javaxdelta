/*
 * DeltaDiffPatchTestSuite.java
 * JUnit based test
 *
 * Created on May 26, 2006, 9:06 PM
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

			assertEquals( patchedFile.length(), string2.length() );
			byte[] buf = new byte[string2.length()];
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
