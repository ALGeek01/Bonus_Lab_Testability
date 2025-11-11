package org.example;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test class for SAP_BasedInvoiceSender
 * Uses Mockito to mock dependencies (FilterInvoice and SAP)
 * to test the sendLowValuedInvoices() method in isolation
 */
class SAP_BasedInvoiceSenderTest {

    /**
     * Test case: When there ARE low-value invoices, verify they are sent to SAP
     * 
     * This test verifies that:
     * 1. FilterInvoice.lowValueInvoices() is called to get the invoices
     * 2. For each invoice returned, sap.send() is called
     * 3. When all invoices succeed, the method returns an empty list
     * 
     * How this test works:
     * - We create MOCKS (fake objects) for both FilterInvoice and SAP
     * - We STUB the filter.lowValueInvoices() method to return a predefined list
     * - We inject these mocks into SAP_BasedInvoiceSender via constructor
     * - We call sendLowValuedInvoices() and verify SAP.send() was called for each invoice
     * - Using verify() we check that sap.send() was called exactly 3 times (once per invoice)
     */
    @Test
    void testWhenLowInvoicesSent() {
        // Arrange: Create mock objects for dependencies
        // Mock FilterInvoice - we don't want to execute real filtering logic or touch database
        FilterInvoice mockFilter = Mockito.mock(FilterInvoice.class);
        
        // Mock SAP - we don't want to actually send invoices to a real SAP system
        SAP mockSap = Mockito.mock(SAP.class);

        // Create a list of test invoices that will be returned by the mocked filter
        List<Invoice> testInvoices = Arrays.asList(
                new Invoice("Test Customer 1", 50),
                new Invoice("Test Customer 2", 75),
                new Invoice("Test Customer 3", 99)
        );

        // STUB the behavior: when filter.lowValueInvoices() is called, return our test list
        // This is done using Mockito's when().thenReturn() syntax
        when(mockFilter.lowValueInvoices()).thenReturn(testInvoices);

        // Create the system under test (SUT) with mocked dependencies
        SAP_BasedInvoiceSender sender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Act: Call the method we're testing (now returns List<Invoice> of failures)
        List<Invoice> failedInvoices = sender.sendLowValuedInvoices();

        // Assert: VERIFY that the mocked SAP.send() method was called for each invoice
        // verify() is a Mockito method that checks if a method was called on a mock object
        // We verify that sap.send() was called exactly 3 times (once for each invoice)
        verify(mockSap, times(3)).send(any(Invoice.class));

        // Additional verification: Check that send() was called with each specific invoice
        verify(mockSap).send(testInvoices.get(0));  // Verify first invoice was sent
        verify(mockSap).send(testInvoices.get(1));  // Verify second invoice was sent
        verify(mockSap).send(testInvoices.get(2));  // Verify third invoice was sent

        // Verify that lowValueInvoices() was called exactly once
        verify(mockFilter, times(1)).lowValueInvoices();

        // Verify that no invoices failed (all succeeded)
        assert failedInvoices.isEmpty() : "Expected no failed invoices";
    }

    /**
     * Test case: When there are NO invoices, verify SAP.send() is NOT called
     * 
     * This test verifies that:
     * 1. When filter returns an empty list, no SAP operations are performed
     * 2. The method handles empty lists gracefully without errors
     * 3. Returns an empty list (no failures since nothing was attempted)
     * 
     * How this test works:
     * - We create MOCKS for FilterInvoice and SAP
     * - We STUB filter.lowValueInvoices() to return an EMPTY list
     * - We call sendLowValuedInvoices()
     * - We VERIFY that sap.send() was NEVER called (times(0))
     * - This ensures we don't make unnecessary SAP calls when there's nothing to send
     */
    @Test
    void testWhenNoInvoices() {
        // Arrange: Create mock objects
        FilterInvoice mockFilter = Mockito.mock(FilterInvoice.class);
        SAP mockSap = Mockito.mock(SAP.class);

        // STUB the filter to return an EMPTY list
        // Collections.emptyList() creates an immutable empty list
        when(mockFilter.lowValueInvoices()).thenReturn(Collections.emptyList());

        // Create the system under test with mocked dependencies
        SAP_BasedInvoiceSender sender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Act: Call the method being tested
        List<Invoice> failedInvoices = sender.sendLowValuedInvoices();

        // Assert: VERIFY that sap.send() was NEVER called
        // never() is equivalent to times(0) - ensures the method was not invoked at all
        verify(mockSap, never()).send(any(Invoice.class));

        // Alternatively, you can use times(0) for the same verification:
        // verify(mockSap, times(0)).send(any(Invoice.class));

        // Verify that lowValueInvoices() was still called (to get the empty list)
        verify(mockFilter, times(1)).lowValueInvoices();

        // Verify that no failures are returned (empty in = empty out)
        assert failedInvoices.isEmpty() : "Expected no failed invoices";
    }

    /**
     * Test case: When SAP throws an exception for one invoice, verify:
     * 1. The exception is caught and handled gracefully
     * 2. Processing continues for remaining invoices
     * 3. The failed invoice is returned in the result list
     * 
     * This test demonstrates the error handling requirement (Requirement 3):
     * - One bad invoice should NOT stop the entire batch
     * - System must be resilient to failures
     * 
     * How this test works:
     * - We create MOCKS for FilterInvoice and SAP
     * - We STUB filter to return a list with 3 invoices
     * - We use doThrow().when() to make SAP throw an exception for the SECOND invoice only
     * - We verify that:
     *   a) sap.send() is called 3 times (all invoices are attempted)
     *   b) The failed invoice is returned in the result list
     *   c) The system doesn't crash or throw an unhandled exception
     */
    @Test
    void testThrowExceptionWhenBadInvoice() {
        // Arrange: Create mock objects
        FilterInvoice mockFilter = Mockito.mock(FilterInvoice.class);
        SAP mockSap = Mockito.mock(SAP.class);

        // Create test invoices - second one will fail
        Invoice goodInvoice1 = new Invoice("Good Customer 1", 50);
        Invoice badInvoice = new Invoice("Bad Customer", 75);    // This one will fail
        Invoice goodInvoice2 = new Invoice("Good Customer 2", 99);

        List<Invoice> testInvoices = Arrays.asList(goodInvoice1, badInvoice, goodInvoice2);

        // STUB the filter to return our test invoices
        when(mockFilter.lowValueInvoices()).thenReturn(testInvoices);

        // STUB SAP to throw an exception when trying to send the "bad" invoice
        // doThrow().when() is Mockito's way to make a mock throw an exception
        // We specify that ONLY when send(badInvoice) is called, throw FailToSendSAPInvoiceException
        doThrow(new FailToSendSAPInvoiceException("SAP system error"))
                .when(mockSap).send(badInvoice);

        // Create the system under test
        SAP_BasedInvoiceSender sender = new SAP_BasedInvoiceSender(mockFilter, mockSap);

        // Act: Call the method - should NOT throw an exception (should catch it internally)
        List<Invoice> failedInvoices = sender.sendLowValuedInvoices();

        // Assert: VERIFY that sap.send() was called for ALL 3 invoices
        // This proves that processing continued after the exception
        verify(mockSap, times(3)).send(any(Invoice.class));
        verify(mockSap).send(goodInvoice1);  // First invoice was sent
        verify(mockSap).send(badInvoice);     // Second invoice was attempted (threw exception)
        verify(mockSap).send(goodInvoice2);  // Third invoice was still sent despite #2 failing

        // Verify that ONLY the bad invoice is in the failed list
        assert failedInvoices.size() == 1 : "Expected exactly 1 failed invoice";
        assert failedInvoices.contains(badInvoice) : "Expected the bad invoice to be in failed list";
        assert !failedInvoices.contains(goodInvoice1) : "Good invoice 1 should not be in failed list";
        assert !failedInvoices.contains(goodInvoice2) : "Good invoice 2 should not be in failed list";

        // Verify that the filter was called
        verify(mockFilter, times(1)).lowValueInvoices();
    }
}

