package org.example;

import java.util.ArrayList;
import java.util.List;

// Class responsible for sending low-valued invoices to the SAP system
public class SAP_BasedInvoiceSender {

    private final FilterInvoice filter;  // Dependency for filtering invoices
    private final SAP sap;  // Dependency for sending invoices to the SAP system

    // Constructor that uses dependency injection to initialize the filter and sap objects
    public SAP_BasedInvoiceSender(FilterInvoice filter, SAP sap) {
        this.filter = filter;
        this.sap = sap;
    }

    /**
     * Method to send all low-valued invoices to the SAP system with error handling
     * 
     * NEW BEHAVIOR (Requirement 3):
     * - If an invoice fails to send, the system continues processing remaining invoices
     * - Failed invoices are collected and returned at the end
     * - This ensures one failure doesn't stop the entire batch process
     * 
     * @return List of invoices that failed to send (empty list if all succeeded)
     */
    public List<Invoice> sendLowValuedInvoices() {
        List<Invoice> lowValuedInvoices = filter.lowValueInvoices();
        List<Invoice> failedInvoices = new ArrayList<>();  // Track invoices that fail to send

        // Iterate through each invoice and attempt to send it
        for (Invoice invoice : lowValuedInvoices) {
            try {
                // Try to send the invoice to SAP
                sap.send(invoice);
            } catch (FailToSendSAPInvoiceException e) {
                // If sending fails, add to failed list and continue with next invoice
                // We don't rethrow the exception - this allows processing to continue
                failedInvoices.add(invoice);
                
                // In a production system, you might want to log this error:
                // logger.error("Failed to send invoice for customer: " + invoice.getCustomer(), e);
            }
        }

        // Return the list of failed invoices (caller can decide how to handle them)
        return failedInvoices;
    }
}
