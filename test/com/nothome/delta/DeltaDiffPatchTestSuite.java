/*
 * DeltaDiffPatchTestSuite.java
 * JUnit based test
 *
 * Created on May 26, 2006, 9:06 PM
 */

package com.nothome.delta;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import junit.framework.*;

/**
 *
 * @author Heiko Klein
 */
public class DeltaDiffPatchTestSuite extends TestCase {
    String test1 = "Dies ist ein kleiner Test\n";
    String test2 = "Dies ist ein kleiner Test xx\nDiesmal modifiziert\n";
    
    public DeltaDiffPatchTestSuite(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        OutputStream os = new FileOutputStream(new File("test1.txt"));
        os.write(test1.getBytes());
        os.close();
        os = new FileOutputStream(new File("test2.txt"));
        os.write(test2.getBytes());
        os.close();
    }

    protected void tearDown() throws Exception {
        (new File("test1.txt")).delete();
        (new File("test2.txt")).delete();
        (new File("delta")).delete();
        (new File("patchedFile.txt")).delete();
    }

    public void testDeltaFile() {
        try {
            File test1File = new File("test1.txt");
            File test2File = new File("test2.txt");
            File patchedFile = new File("patchedFile.txt");
            File delta = new File("delta");
            DiffWriter output = new GDiffWriter(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(delta))));
            Delta.computeDelta(test1File, test2File, output);
            output.close();
            
            assertTrue(delta.exists());
            
            new GDiffPatcher(test1File, delta, patchedFile);
            assertTrue(patchedFile.exists());
 
            assertEquals(patchedFile.length(), test2.length());
            byte[] buf = new byte[test2.length()];
            (new FileInputStream(patchedFile)).read(buf);
            
            assertEquals(new String(buf), test2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testDeltaSeekableSource() {
        try {
            File test1File = new File("test1.txt");
            File test2File = new File("test2.txt");
            File patchedFile = new File("patchedFile.txt");
            File delta = new File("delta");
            DiffWriter doutput = new GDiffWriter(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(delta))));
            Delta.computeDelta(test1File, test2File, doutput);
            doutput.close();

            SeekableSource test1ss = new ByteArraySeekableSource(test1.getBytes());
            SeekableSource test2ss = new ByteArraySeekableSource(test2.getBytes());
            ByteArrayOutputStream deltaOS = new ByteArrayOutputStream();
            DiffWriter output = new GDiffWriter(new DataOutputStream(new BufferedOutputStream(deltaOS)));
            
            Delta.computeDelta(test1ss, new SeekableSourceInputStream(test2ss), (int) test2ss.length(), output);
            output.close();
            
            byte[] deltaBytes = deltaOS.toByteArray();
            assertEquals(deltaBytes.length, delta.length());
            byte[] fileDeltaBytes = new byte[deltaBytes.length];
            (new FileInputStream(delta)).read(fileDeltaBytes);
            assertEquals(new String(deltaBytes), new String(fileDeltaBytes));
            
            ByteArrayInputStream deltaIS = new ByteArrayInputStream(deltaBytes);
            ByteArrayOutputStream patchedOS = new ByteArrayOutputStream();
            new GDiffPatcher(test1ss, deltaIS, patchedOS);
           
            assertEquals(new String(patchedOS.toByteArray()), test2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DeltaDiffPatchTestSuite.class);
        return suite;
    }
    
}
