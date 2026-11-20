package com.shoppingcart;

/**
 * Interface representing a payment method contract.
 * Enables runtime polymorphism across Card, UPI, and Cash payments.
 */
public interface Payment {
    // Processes payment of the specified amount; returns true if successful, false otherwise
    boolean pay(double amount);
}
