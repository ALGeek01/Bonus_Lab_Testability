package org.example;

/**
 * Custom exception thrown when an invoice fails to be sent to the SAP system
 * This exception is used for error handling in SAP_BasedInvoiceSender
 */
public class FailToSendSAPInvoiceException extends RuntimeException {

    /**
     * Constructor with a message describing the failure
     * @param message Description of what went wrong
     */
    public FailToSendSAPInvoiceException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and the underlying cause
     * @param message Description of what went wrong
     * @param cause The underlying exception that caused this failure
     */
    public FailToSendSAPInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

