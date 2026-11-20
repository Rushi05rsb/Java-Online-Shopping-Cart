package com.shoppingcart;

/**
 * Simulates card payment processing with card number validation.
 */
public class CardPayment implements Payment {
    private String cardNumber;
    private String cardHolderName;

    // Initializes card payment details
    public CardPayment(String cardNumber, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    // Validates 16-digit card and processes payment
    @Override
    public boolean pay(double amount) {
        if (amount <= 0) {
            System.out.println("[Payment Error] Payment amount must be greater than zero.");
            return false;
        }
        // Validate card number has exactly 16 numeric digits
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            System.out.println("[Payment Failed] Invalid Card Number. Card number must be exactly 16 numeric digits.");
            return false;
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            System.out.println("[Payment Failed] Cardholder name cannot be empty.");
            return false;
        }
        String maskedCard = "XXXX-XXXX-XXXX-" + cardNumber.substring(12);
        System.out.println(String.format("[Payment Successful] ₹%.2f charged to Card (%s, Holder: %s).",
                amount, maskedCard, cardHolderName));
        return true;
    }

    // Returns a readable label for this payment method
    @Override
    public String toString() {
        return "Card Payment";
    }
}
