/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.common;

/**
 *
 * @author vozka
 */
public class IllegalPaymentException extends RuntimeException {

    /**
     * Creates a new instance of <code>IllegalPaymentException</code> without
     * detail message.
     */
    public IllegalPaymentException() {
    }

    /**
     * Constructs an instance of <code>IllegalPaymentException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalPaymentException(String msg) {
        super(msg);
    }
    
    public IllegalPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
