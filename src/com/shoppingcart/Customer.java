package com.shoppingcart;

/**
 * Represents a customer who shops in the online store.
 * Holds customer identifier, full name, and email address.
 */
public class Customer {
    private int customerId;
    private String name;
    private String email;

    // Initializes a customer with validation
    public Customer(int customerId, String name, String email) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be greater than zero.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    // Returns the customer ID
    public int getCustomerId() {
        return customerId;
    }

    // Returns the customer name
    public String getName() {
        return name;
    }

    // Returns the customer email
    public String getEmail() {
        return email;
    }

    // Returns a readable string representation of the customer
    @Override
    public String toString() {
        return "Customer #" + customerId + " (" + name + ", " + email + ")";
    }
}
