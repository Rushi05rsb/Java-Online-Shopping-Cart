package com.shoppingcart;

/**
 * Simulates cash payment, checking if tendered cash is sufficient and calculating change.
 */
public class CashPayment implements Payment {
    private double cashGiven;

    // Initializes cash payment with the amount tendered by customer
    public CashPayment(double cashGiven) {
        this.cashGiven = Order.round(cashGiven);
    }

    // Validates cash given against payable amount, prints change, and processes payment
    @Override
    public boolean pay(double amount) {
        if (amount <= 0) {
            System.out.println("[Payment Error] Payment amount must be greater than zero.");
            return false;
        }
        if (cashGiven < amount) {
            System.out.println(String.format("[Payment Failed] Insufficient cash given. Required: ₹%.2f, Tendered: ₹%.2f.",
                    amount, cashGiven));
            return false;
        }
        double change = Order.round(cashGiven - amount);
        System.out.println(String.format("[Payment Successful] Paid ₹%.2f in Cash. Tendered: ₹%.2f | Change Returned: ₹%.2f.",
                amount, cashGiven, change));
        return true;
    }

    // Returns a readable label for this payment method
    @Override
    public String toString() {
        return "Cash Payment";
    }
}
