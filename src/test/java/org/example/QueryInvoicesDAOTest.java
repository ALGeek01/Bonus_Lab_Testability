package org.example;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class QueryInvoicesDAOTest {

    private static Database db;
    private static QueryInvoicesDAO dao;

    @BeforeAll
    static void setupDatabase() {
        // Initialize the real database connection for integration testing
        db = new Database();
        dao = new QueryInvoicesDAO(db);
    }

    @BeforeEach
    void clearDatabase() {
        // Clear database before each test to ensure test isolation
        dao.clear();
    }

    /**
     * Integration test for FilterInvoice.lowValueInvoices() method
     * This test uses REAL dependencies (no stubs/mocks)
     * - Uses actual Database connection
     * - Uses actual QueryInvoicesDAO
     * - Tests the complete integration of FilterInvoice with database
     */
    @Test
    void filterInvoiceTest() {
        // Arrange: Set up test data in the real database
        // Add invoices with different values to test filtering logic
        dao.save(new Invoice("Customer A", 50));   // Low value - should be included
        dao.save(new Invoice("Customer B", 150));  // High value - should be excluded
        dao.save(new Invoice("Customer C", 75));   // Low value - should be included
        dao.save(new Invoice("Customer D", 200));  // High value - should be excluded
        dao.save(new Invoice("Customer E", 99));   // Low value - should be included (edge case)

        // Act: Create FilterInvoice with real dependencies and call the method
        FilterInvoice filter = new FilterInvoice(); // Uses default constructor with real dependencies
        List<Invoice> lowValueInvoices = filter.lowValueInvoices();

        // Assert: Verify that only invoices with value < 100 are returned
        assertThat(lowValueInvoices)
                .hasSize(3)  // Should contain exactly 3 invoices
                .extracting(Invoice::getCustomer)
                .containsExactlyInAnyOrder("Customer A", "Customer C", "Customer E");

        // Additional verification: ensure the values are indeed less than 100
        assertThat(lowValueInvoices)
                .allMatch(invoice -> invoice.getValue() < 100);
    }

    /**
     * Unit test for FilterInvoice.lowValueInvoices() method using STUBBED dependencies
     * This test uses Mockito to stub/mock the DAO - NO database interaction
     * Benefits of stubbing:
     * - Faster execution (no database operations)
     * - No database setup/cleanup needed
     * - Tests only the filtering logic in isolation
     * - More predictable and stable tests
     */
    @Test
    void filterInvoiceStubbedTest() {
        // Arrange: Create a mock/stub of QueryInvoicesDAO
        // Mockito.mock() creates a fake version of the DAO that we can control
        QueryInvoicesDAO stubbedDao = Mockito.mock(QueryInvoicesDAO.class);

        // Define the behavior of the stubbed DAO using when().thenReturn()
        // When dao.all() is called, return our predefined list of invoices
        // This simulates what the database would return WITHOUT actually querying the database
        List<Invoice> mockInvoices = Arrays.asList(
                new Invoice("Mock Customer 1", 30),    // Low value - should be included
                new Invoice("Mock Customer 2", 120),   // High value - should be excluded
                new Invoice("Mock Customer 3", 85),    // Low value - should be included
                new Invoice("Mock Customer 4", 100),   // Edge case: exactly 100 - should be excluded
                new Invoice("Mock Customer 5", 45)     // Low value - should be included
        );
        when(stubbedDao.all()).thenReturn(mockInvoices);

        // Act: Create FilterInvoice with the stubbed DAO (dependency injection)
        // This is why we refactored FilterInvoice to accept DAO in constructor
        FilterInvoice filter = new FilterInvoice(stubbedDao);
        List<Invoice> lowValueInvoices = filter.lowValueInvoices();

        // Assert: Verify that only invoices with value < 100 are filtered correctly
        assertThat(lowValueInvoices)
                .hasSize(3)  // Should contain exactly 3 invoices with value < 100
                .extracting(Invoice::getCustomer)
                .containsExactlyInAnyOrder("Mock Customer 1", "Mock Customer 3", "Mock Customer 5");

        // Additional verification: all returned invoices have value less than 100
        assertThat(lowValueInvoices)
                .allMatch(invoice -> invoice.getValue() < 100);

        // Verify that Invoice with value=100 is excluded (boundary condition)
        assertThat(lowValueInvoices)
                .noneMatch(invoice -> invoice.getCustomer().equals("Mock Customer 4"));
    }

}