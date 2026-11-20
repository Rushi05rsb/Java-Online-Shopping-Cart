package com.shoppingcart;

/**
 * Interface representing a discount calculation contract on an order subtotal.
 */
public interface Discount {
    // Calculates the monetary discount value for a given subtotal
    double calculateDiscount(double subtotal);
}
