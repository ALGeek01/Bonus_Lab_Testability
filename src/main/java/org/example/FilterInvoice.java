package org.example;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class FilterInvoice {
    QueryInvoicesDAO dao;

    // Constructor with dependency injection - allows us to inject real or stubbed DAO
    public FilterInvoice(QueryInvoicesDAO dao) {
        this.dao = dao;
    }

    // Default constructor for backward compatibility - creates real dependencies
    public FilterInvoice() {
        Database db = new Database();
        this.dao = new QueryInvoicesDAO(db);
    }

    public List<Invoice> lowValueInvoices() {
            List<Invoice> all = dao.all();

            return all.stream()
                    .filter(invoice -> invoice.getValue() < 100)
                    .collect(toList());
    }
}
