/*
 * PatchException.java
 *
 * Created on June 6, 2006, 9:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.nothome.delta;

/**
 *
 * @author Heiko Klein
 */
public class PatchException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>PatchException</code> without detail message.
     */
    public PatchException() {
    }
    
    
    /**
     * Constructs an instance of <code>PatchException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PatchException(String msg) {
        super(msg);
    }
}
