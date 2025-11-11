package org.example;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

}