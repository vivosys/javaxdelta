/*
 * DeltaException.java
 *
 * Created on June 6, 2006, 9:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.nothome.delta;

/**
 *
 * @author Heiko Klein
 */
public class DeltaException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>DeltaException</code> without detail message.
     */
    public DeltaException() {
    }
    
    
    /**
     * Constructs an instance of <code>DeltaException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DeltaException(String msg) {
        super(msg);
    }
}
