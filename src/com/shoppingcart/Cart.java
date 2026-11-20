package com.shoppingcart;

import java.util.ArrayList;

/**
 * Manages the shopping cart for a customer.
 * Supports adding, removing, updating items, and calculating the cart subtotal.
 */
public class Cart {
    private Customer customer;
    private ArrayList<CartItem> items;

    // Initializes an empty shopping cart for a customer
    public Cart(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        this.customer = customer;
        this.items = new ArrayList<CartItem>();
    }

    // Adds a product with a specified quantity, merging with existing item if already present
    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        CartItem existingItem = findItemByProductId(product.getProductId());
        int currentInCart = (existingItem != null) ? existingItem.getQuantity() : 0;
        int totalRequested = currentInCart + quantity;

        if (totalRequested > product.getStockQuantity()) {
            throw new IllegalArgumentException("Cannot add " + quantity + " items. Available stock: "
                    + product.getStockQuantity() + " (already in cart: " + currentInCart + ").");
        }

        if (existingItem != null) {
            existingItem.setQuantity(totalRequested);
        } else {
            items.add(new CartItem(product, quantity));
        }
    }

    // Overloaded method: adds 1 unit of the given product to the cart (Method Overloading)
    public void addItem(Product product) {
        addItem(product, 1);
    }

    // Removes a product from the cart by its product ID
    public void removeItem(int productId) {
        CartItem item = findItemByProductId(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " is not in the cart.");
        }
        items.remove(item);
    }

    // Updates the quantity of an existing item in the cart
    public void updateQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero. To remove, use option 3.");
        }
        CartItem item = findItemByProductId(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product with ID " + productId + " is not in the cart.");
        }
        if (quantity > item.getProduct().getStockQuantity()) {
            throw new IllegalArgumentException("Cannot update to " + quantity + ". Only "
                    + item.getProduct().getStockQuantity() + " items available in stock.");
        }
        item.setQuantity(quantity);
    }

    // Calculates the total subtotal of all items currently in the cart
    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
        }
        return Order.round(subtotal);
    }

    // Clears all items from the cart
    public void clearCart() {
        items.clear();
    }

    // Helper method to look up a cart item by its product ID
    private CartItem findItemByProductId(int productId) {
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    // Returns a copy of the list of items in the cart (defensive copy)
    public ArrayList<CartItem> getItems() {
        return new ArrayList<CartItem>(items);
    }

    // Returns the customer who owns this cart
    public Customer getCustomer() {
        return customer;
    }

    // Checks whether the cart has no items
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
