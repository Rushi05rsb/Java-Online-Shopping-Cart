package com.shoppingcart;

/**
 * Represents a single product line item inside the cart with its chosen quantity.
 */
public class CartItem {
    private Product product;
    private int quantity;

    // Initializes a cart line item with product and quantity
    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.product = product;
        this.quantity = quantity;
    }

    // Returns the associated product
    public Product getProduct() {
        return product;
    }

    // Returns the quantity selected
    public int getQuantity() {
        return quantity;
    }

    // Updates the quantity of this line item
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    // Calculates subtotal for this item (unit price * quantity)
    public double getSubtotal() {
        return Order.round(product.getPrice() * quantity);
    }

    // Returns a formatted string representation of this cart item
    @Override
    public String toString() {
        return String.format("[ID: %d] %-14s | Price: ₹%10.2f | Qty: %-2d | Subtotal: ₹%10.2f",
                product.getProductId(), product.getProductName(),
                product.getPrice(), quantity, getSubtotal());
    }
}
