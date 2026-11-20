package com.shoppingcart;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main application class providing a 10-option interactive console menu.
 * Serves as the entry point for the Online Shopping Cart System.
 */
public class Main {
    private static final double GST_RATE = 0.18; // 18% GST

    // Main entry point for the console shopping cart system
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Preload sample catalog of 5 products
        ArrayList<Product> catalog = new ArrayList<Product>();
        catalog.add(new Product(1, "Laptop", 50000.00, 10));
        catalog.add(new Product(2, "Headphones", 2000.00, 25));
        catalog.add(new Product(3, "Mouse", 800.00, 40));
        catalog.add(new Product(4, "Keyboard", 1500.00, 30));
        catalog.add(new Product(5, "USB Drive", 600.00, 50));

        // Create default customer and cart
        Customer customer = new Customer(101, "Rahul Sharma", "rahul.sharma@example.com");
        Cart cart = new Cart(customer);

        Discount activeDiscount = null;
        Order lastOrder = null;
        int nextOrderId = 1001;

        System.out.println("============================================================");
        System.out.println("       WELCOME TO THE ONLINE SHOPPING CART SYSTEM          ");
        System.out.println("============================================================");
        System.out.println("Logged in as: " + customer);

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readInt(scanner, "Enter your choice (1-10): ");

            switch (choice) {
                case 1:
                    viewProducts(catalog);
                    break;
                case 2:
                    addProductToCart(scanner, catalog, cart);
                    break;
                case 3:
                    removeProductFromCart(scanner, cart);
                    break;
                case 4:
                    updateProductQuantity(scanner, cart);
                    break;
                case 5:
                    viewCart(cart, activeDiscount);
                    break;
                case 6:
                    activeDiscount = applyPercentageDiscount(scanner);
                    break;
                case 7:
                    activeDiscount = applyFlatDiscount(scanner);
                    break;
                case 8:
                    Order completed = handleCheckout(scanner, customer, cart, activeDiscount, nextOrderId);
                    if (completed != null) {
                        lastOrder = completed;
                        nextOrderId++;
                        activeDiscount = null; // Reset discount after successful checkout
                    }
                    break;
                case 9:
                    viewOrderSummary(lastOrder);
                    break;
                case 10:
                    System.out.println("\nThank you for shopping with us! Have a wonderful day.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 10.");
                    break;
            }
        }

        scanner.close();
    }

    // Displays the 10-option main menu
    private static void displayMenu() {
        System.out.println("\n----------------------- MAIN MENU --------------------------");
        System.out.println(" 1. View Available Products");
        System.out.println(" 2. Add Product to Cart");
        System.out.println(" 3. Remove Product from Cart");
        System.out.println(" 4. Update Product Quantity");
        System.out.println(" 5. View Shopping Cart");
        System.out.println(" 6. Apply Percentage Discount");
        System.out.println(" 7. Apply Flat Discount");
        System.out.println(" 8. Checkout");
        System.out.println(" 9. View Order Summary");
        System.out.println("10. Exit");
        System.out.println("------------------------------------------------------------");
    }

    // Displays available products in the store catalog
    private static void viewProducts(ArrayList<Product> catalog) {
        System.out.println("\n==================== AVAILABLE PRODUCTS ====================");
        System.out.println(String.format("%-5s | %-16s | %-12s | %-10s", "ID", "Product Name", "Price", "Stock"));
        System.out.println("------------------------------------------------------------");
        for (Product product : catalog) {
            System.out.println(String.format("%-5d | %-16s | ₹%10.2f | %-10d",
                    product.getProductId(), product.getProductName(), product.getPrice(), product.getStockQuantity()));
        }
        System.out.println("============================================================");
    }

    // Handles adding a product to the cart with input validation
    private static void addProductToCart(Scanner scanner, ArrayList<Product> catalog, Cart cart) {
        viewProducts(catalog);
        int productId = readInt(scanner, "Enter Product ID to add: ");
        Product product = findProductById(catalog, productId);
        if (product == null) {
            System.out.println("Error: Invalid product ID. Product does not exist.");
            return;
        }

        int quantity = readInt(scanner, "Enter Quantity to add: ");
        try {
            cart.addItem(product, quantity);
            System.out.println("Success: Added " + quantity + " x '" + product.getProductName() + "' to cart.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    // Handles removing a product from the cart
    private static void removeProductFromCart(Scanner scanner, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is currently empty.");
            return;
        }
        viewCart(cart, null);
        int productId = readInt(scanner, "Enter Product ID to remove: ");
        try {
            cart.removeItem(productId);
            System.out.println("Success: Product removed from cart.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    // Handles updating item quantity in the cart
    private static void updateProductQuantity(Scanner scanner, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Your cart is currently empty.");
            return;
        }
        viewCart(cart, null);
        int productId = readInt(scanner, "Enter Product ID to update: ");
        int newQuantity = readInt(scanner, "Enter new Quantity: ");
        try {
            cart.updateQuantity(productId, newQuantity);
            System.out.println("Success: Quantity updated successfully.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    // Displays the current shopping cart items, subtotal, and any active discount
    private static void viewCart(Cart cart, Discount discount) {
        System.out.println("\n====================== SHOPPING CART =======================");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            System.out.println("============================================================");
            return;
        }
        System.out.println(String.format("%-5s | %-16s | %-12s | %-4s | %-12s",
                "ID", "Product Name", "Unit Price", "Qty", "Subtotal"));
        System.out.println("------------------------------------------------------------");
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            System.out.println(String.format("%-5d | %-16s | ₹%10.2f | %-4d | ₹%10.2f",
                    p.getProductId(), p.getProductName(), p.getPrice(), item.getQuantity(), item.getSubtotal()));
        }
        System.out.println("------------------------------------------------------------");
        double subtotal = cart.calculateSubtotal();
        System.out.println(String.format("Cart Subtotal: ₹%.2f", subtotal));
        if (discount != null) {
            double discAmt = Order.round(discount.calculateDiscount(subtotal));
            System.out.println(String.format("Applied Discount (%s): -₹%.2f", discount, discAmt));
            System.out.println(String.format("Estimated Taxable Amount: ₹%.2f", Math.max(0.0, subtotal - discAmt)));
        }
        System.out.println("============================================================");
    }

    // Prompts user and sets a percentage discount
    private static Discount applyPercentageDiscount(Scanner scanner) {
        double percent = readDouble(scanner, "Enter discount percentage (0 to 100): ");
        try {
            PercentageDiscount discount = new PercentageDiscount(percent);
            System.out.println(String.format("Success: Applied %.1f%% discount.", percent));
            return discount;
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
    }

    // Prompts user and sets a flat monetary discount
    private static Discount applyFlatDiscount(Scanner scanner) {
        double amount = readDouble(scanner, "Enter flat discount amount (₹): ");
        try {
            FlatDiscount discount = new FlatDiscount(amount);
            System.out.println(String.format("Success: Applied flat ₹%.2f discount.", amount));
            return discount;
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
    }

    // Orchestrates checkout: selects payment method, charges, reduces stock, and clears cart
    private static Order handleCheckout(Scanner scanner, Customer customer, Cart cart,
                                       Discount discount, int orderId) {
        if (cart.isEmpty()) {
            System.out.println("Cannot checkout: Your cart is empty! Add products first.");
            return null;
        }

        // Display current bill estimate
        double subtotal = cart.calculateSubtotal();
        double discountAmt = (discount != null) ? discount.calculateDiscount(subtotal) : 0.0;
        double taxable = Order.round(Math.max(0.0, subtotal - discountAmt));
        double tax = Order.round(taxable * GST_RATE);
        double finalAmount = Order.round(taxable + tax);

        System.out.println("\n-------------------- CHECKOUT SUMMARY ----------------------");
        System.out.println(String.format("Subtotal       : ₹%.2f", subtotal));
        System.out.println(String.format("Discount       : -₹%.2f", discountAmt));
        System.out.println(String.format("Taxable Amount : ₹%.2f", taxable));
        System.out.println(String.format("GST (18%%)      : ₹%.2f", tax));
        System.out.println(String.format("Total Payable  : ₹%.2f", finalAmount));
        System.out.println("------------------------------------------------------------");

        System.out.println("Select Payment Method:");
        System.out.println("1. Card Payment (16 digits)");
        System.out.println("2. UPI Payment (e.g. name@bank)");
        System.out.println("3. Cash Payment (Tender cash)");
        System.out.println("4. Cancel Checkout");
        int paymentChoice = readInt(scanner, "Enter choice (1-4): ");

        Payment payment = null;
        PaymentType paymentType = null;

        if (paymentChoice == 1) {
            paymentType = PaymentType.CARD;
            String cardNum = readLine(scanner, "Enter 16-digit Card Number: ");
            String cardHolder = readLine(scanner, "Enter Cardholder Name: ");
            payment = new CardPayment(cardNum, cardHolder);
        } else if (paymentChoice == 2) {
            paymentType = PaymentType.UPI;
            String upiId = readLine(scanner, "Enter UPI ID (must contain '@'): ");
            payment = new UPIPayment(upiId);
        } else if (paymentChoice == 3) {
            paymentType = PaymentType.CASH;
            System.out.println(String.format("Total payable amount is: ₹%.2f", finalAmount));
            double cashTendered = readDouble(scanner, "Enter Cash Tendered (₹): ");
            payment = new CashPayment(cashTendered);
        } else {
            System.out.println("Checkout cancelled. Returning to main menu.");
            return null;
        }

        try {
            // Create order (copies cart items as defensive snapshot)
            Order order = new Order(orderId, customer, cart);

            // Execute order placement with polymorphic payment and discount
            boolean success = order.placeOrder(discount, payment, paymentType, GST_RATE);
            if (success) {
                System.out.println("\n>>> ORDER CONFIRMED! Order #" + order.getOrderId() + " placed successfully. <<<");
                order.generateSummary();
                return order;
            } else {
                System.out.println("\n>>> CHECKOUT FAILED: Payment unsuccessful. Cart items and stock remain unchanged. <<<");
                return null;
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Checkout Error: " + ex.getMessage());
            return null;
        }
    }

    // Displays the summary of the last completed order
    private static void viewOrderSummary(Order lastOrder) {
        if (lastOrder == null) {
            System.out.println("No completed order found. Please checkout an order first.");
            return;
        }
        lastOrder.generateSummary();
    }

    // Helper method to look up a product in the catalog by ID
    private static Product findProductById(ArrayList<Product> catalog, int productId) {
        for (Product product : catalog) {
            if (product.getProductId() == productId) {
                return product;
            }
        }
        return null;
    }

    // Safely reads an integer from console, rejecting non-numeric input without crashing
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid whole number.");
            }
        }
    }

    // Safely reads a double from console, rejecting non-numeric input without crashing
    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal or number.");
            }
        }
    }

    // Safely reads a non-empty string line from console
    private static String readLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }
}
