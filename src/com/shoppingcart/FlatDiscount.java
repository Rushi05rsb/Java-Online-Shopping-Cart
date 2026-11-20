package com.shoppingcart;

/**
 * Calculates a fixed flat amount discount on an order subtotal.
 */
public class FlatDiscount implements Discount {
    private double amount;

    // Initializes a flat discount with validation (must not be negative)
    public FlatDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Flat discount amount cannot be negative.");
        }
        this.amount = Order.round(amount);
    }

    // Calculates the discount amount, capped at the subtotal so amount payable never goes below zero
    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        return Math.min(amount, subtotal);
    }

    // Returns the flat discount amount
    public double getAmount() {
        return amount;
    }

    // Returns a readable description of the flat discount
    @Override
    public String toString() {
        return String.format("Flat ₹%.2f Off", amount);
    }
}
