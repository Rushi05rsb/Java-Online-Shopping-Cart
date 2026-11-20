package com.shoppingcart;

/**
 * Simulates UPI (Unified Payments Interface) payment processing.
 */
public class UPIPayment implements Payment {
    private String upiId;

    // Initializes UPI payment with the user's UPI ID (VPA)
    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    // Validates UPI ID format (must contain '@' with text before and after) and processes payment
    @Override
    public boolean pay(double amount) {
        if (amount <= 0) {
            System.out.println("[Payment Error] Payment amount must be greater than zero.");
            return false;
        }
        // UPI ID must contain '@' and have characters before and after '@'
        if (upiId == null || !upiId.contains("@") || upiId.indexOf("@") == 0 || upiId.indexOf("@") == upiId.length() - 1) {
            System.out.println("[Payment Failed] Invalid UPI ID. Format must be username@bank (e.g., student@okaxis).");
            return false;
        }
        System.out.println(String.format("[Payment Successful] ₹%.2f transferred successfully via UPI ID: %s.",
                amount, upiId));
        return true;
    }

    // Returns a readable label for this payment method
    @Override
    public String toString() {
        return "UPI Payment";
    }
}
