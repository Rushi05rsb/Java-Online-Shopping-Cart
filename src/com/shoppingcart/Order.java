package com.shoppingcart;

import java.util.ArrayList;

/**
 * Represents a customer order, handling checkout, tax calculations, payment processing,
 * and invoice generation.
 */
public class Order {
    private int orderId;
    private Customer customer;
    private Cart cart;
    private ArrayList<CartItem> orderItems;
    private OrderStatus status;
    private PaymentType paymentType;
    private double subtotal;
    private double discountAmount;
    private double taxableAmount;
    private double taxAmount;
    private double finalAmount;
    private Discount appliedDiscount;

    // Single shared helper method to round double values to 2 decimal places
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Initializes order and takes a snapshot copy of cart items so clearing cart later does not erase order history
    public Order(int orderId, Customer customer, Cart cart) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be greater than zero.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null.");
        }
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create an order with an empty cart.");
        }

        this.orderId = orderId;
        this.customer = customer;
        this.cart = cart;
        this.status = OrderStatus.PENDING;

        // Take a defensive snapshot of cart items
        this.orderItems = new ArrayList<CartItem>();
        for (CartItem item : cart.getItems()) {
            this.orderItems.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.subtotal = cart.calculateSubtotal();
    }

    // Calculates tax (GST) on the taxable amount given a tax rate
    public double calculateTax(double taxableAmount, double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative.");
        }
        return round(taxableAmount * taxRate);
    }

    // Calculates final payable amount: taxable amount + tax
    public double calculateFinalAmount() {
        return round(this.taxableAmount + this.taxAmount);
    }

    // Executes the checkout process with stock validation, payment, and stock deduction
    public boolean placeOrder(Discount discount, Payment payment, PaymentType paymentType, double taxRate) {
        // Rule 6: Block duplicate checkout on already confirmed orders
        if (this.status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order #" + orderId + " is already confirmed and paid for.");
        }
        // Rule 1: Reject null objects
        if (payment == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type cannot be null.");
        }
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative.");
        }

        // Rule 2: Validate stock for all items BEFORE charging the customer
        for (CartItem item : orderItems) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStockQuantity()) {
                this.status = OrderStatus.FAILED;
                throw new IllegalStateException("Insufficient stock for product '" + product.getProductName()
                        + "'. Required: " + item.getQuantity() + ", Available: " + product.getStockQuantity());
            }
        }

        // Calculate discount polymorphically (Rule 8)
        this.appliedDiscount = discount;
        if (discount != null) {
            this.discountAmount = round(discount.calculateDiscount(this.subtotal));
            if (this.discountAmount > this.subtotal) {
                this.discountAmount = this.subtotal;
            }
        } else {
            this.discountAmount = 0.0;
        }

        // Rule 7: Discount can never make payable amount negative
        this.taxableAmount = round(this.subtotal - this.discountAmount);
        if (this.taxableAmount < 0) {
            this.taxableAmount = 0.0;
        }

        // Calculate tax and final amount
        this.taxAmount = calculateTax(this.taxableAmount, taxRate);
        this.finalAmount = calculateFinalAmount();
        this.paymentType = paymentType;

        // Rule 3: Call payment.pay(finalAmount)
        boolean paymentSuccess = payment.pay(this.finalAmount);

        if (paymentSuccess) {
            // Rule 4: On success -> CONFIRMED, reduce stock, then clear cart
            this.status = OrderStatus.CONFIRMED;
            for (CartItem item : orderItems) {
                item.getProduct().reduceStock(item.getQuantity());
            }
            this.cart.clearCart();
            return true;
        } else {
            // Rule 5: On failure -> FAILED, keep cart intact, do NOT reduce stock
            this.status = OrderStatus.FAILED;
            return false;
        }
    }

    // Displays an itemized, formatted order invoice / summary
    public void generateSummary() {
        System.out.println("\n======================================================================");
        System.out.println("                         ORDER INVOICE / SUMMARY                      ");
        System.out.println("======================================================================");
        System.out.println("Order ID       : #" + orderId);
        System.out.println("Order Status   : " + status);
        System.out.println("Customer Name  : " + customer.getName() + " (ID: " + customer.getCustomerId() + ")");
        System.out.println("Customer Email : " + customer.getEmail());
        if (paymentType != null) {
            System.out.println("Payment Method : " + paymentType);
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("%-5s | %-16s | %-12s | %-4s | %-12s",
                "ID", "Product Name", "Unit Price", "Qty", "Subtotal"));
        System.out.println("----------------------------------------------------------------------");
        for (CartItem item : orderItems) {
            Product p = item.getProduct();
            System.out.println(String.format("%-5d | %-16s | ₹%10.2f | %-4d | ₹%10.2f",
                    p.getProductId(), p.getProductName(), p.getPrice(), item.getQuantity(), item.getSubtotal()));
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("Cart Subtotal       : ₹%12.2f", subtotal));
        String discountDesc = (appliedDiscount != null) ? appliedDiscount.toString() : "None";
        System.out.println(String.format("Discount Applied    : -₹%11.2f (%s)", discountAmount, discountDesc));
        System.out.println(String.format("Taxable Amount      : ₹%12.2f", taxableAmount));
        System.out.println(String.format("GST (18%%)           : ₹%12.2f", taxAmount));
        System.out.println("----------------------------------------------------------------------");
        System.out.println(String.format("FINAL AMOUNT PAID   : ₹%12.2f", finalAmount));
        System.out.println("======================================================================\n");
    }

    // Returns the order ID
    public int getOrderId() {
        return orderId;
    }

    // Returns the order status
    public OrderStatus getStatus() {
        return status;
    }

    // Returns the final paid amount
    public double getFinalAmount() {
        return finalAmount;
    }

    // Returns the subtotal before discount
    public double getSubtotal() {
        return subtotal;
    }

    // Returns taxable amount
    public double getTaxableAmount() {
        return taxableAmount;
    }

    // Returns tax amount
    public double getTaxAmount() {
        return taxAmount;
    }

    // Returns discount amount
    public double getDiscountAmount() {
        return discountAmount;
    }

    // Returns the list of snapshot items
    public ArrayList<CartItem> getOrderItems() {
        return orderItems;
    }

    // Returns the customer who placed this order
    public Customer getCustomer() {
        return customer;
    }

    // Returns the payment type used for this order
    public PaymentType getPaymentType() {
        return paymentType;
    }

    // Returns the applied discount
    public Discount getAppliedDiscount() {
        return appliedDiscount;
    }
}
