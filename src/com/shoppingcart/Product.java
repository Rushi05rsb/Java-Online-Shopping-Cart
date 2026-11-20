package com.shoppingcart;

/**
 * Represents a product available in the store catalog.
 * Manages product details, pricing, and available inventory.
 */
public class Product {
    private int productId;
    private String productName;
    private double price;
    private int stockQuantity;

    // Initializes a new product with basic validation
    public Product(int productId, String productName, double price, int stockQuantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than zero.");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.productId = productId;
        this.productName = productName;
        this.price = Order.round(price);
        this.stockQuantity = stockQuantity;
    }

    // Returns the unique product ID
    public int getProductId() {
        return productId;
    }

    // Returns the display name of the product
    public String getProductName() {
        return productName;
    }

    // Returns the unit price of the product
    public double getPrice() {
        return price;
    }

    // Returns the current available stock quantity
    public int getStockQuantity() {
        return stockQuantity;
    }

    // Reduces available stock after a confirmed purchase
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reduce must be greater than zero.");
        }
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Cannot reduce stock by " + quantity 
                    + ". Only " + stockQuantity + " available in stock.");
        }
        this.stockQuantity -= quantity;
    }

    // Returns a readable string summary of the product
    @Override
    public String toString() {
        return String.format("[ID: %d] %-14s | Price: ₹%10.2f | In Stock: %d",
                productId, productName, price, stockQuantity);
    }
}
