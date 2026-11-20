package com.shoppingcart;

/**
 * Calculates a percentage-based discount on an order subtotal.
 */
public class PercentageDiscount implements Discount {
    private double percent;

    // Initializes a percentage discount with validation (0 to 100 percent)
    public PercentageDiscount(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percentage discount must be between 0 and 100.");
        }
        this.percent = percent;
    }

    // Calculates the discount amount, ensuring it never exceeds the subtotal
    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        double discount = Order.round((subtotal * percent) / 100.0);
        return Math.min(discount, subtotal);
    }

    // Returns the discount percentage
    public double getPercent() {
        return percent;
    }

    // Returns a readable description of the percentage discount
    @Override
    public String toString() {
        return String.format("%.1f%% Off", percent);
    }
}
