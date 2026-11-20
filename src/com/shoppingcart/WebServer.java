package com.shoppingcart;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Built-in HTTP server providing a REST-like API and static file hosting
 * for the Online Shopping Cart web frontend.
 */
public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int FALLBACK_PORT = 8081;
    private static final double GST_RATE = 0.18;

    // In-memory data store shared across web requests
    private static Customer customer = new Customer(101, "Rahul Sharma", "rahul.sharma@example.com");
    private static Cart cart = new Cart(customer);
    private static ArrayList<Product> catalog = new ArrayList<Product>();
    private static Discount currentDiscount = null;
    private static Order lastOrder = null;
    private static int nextOrderId = 1001;

    // Starts the HTTP server and registers all API and static file routes
    public static void main(String[] args) {
        // Preload sample catalog identical to Main.java
        catalog.add(new Product(1, "Laptop", 50000.00, 10));
        catalog.add(new Product(2, "Headphones", 2000.00, 25));
        catalog.add(new Product(3, "Mouse", 800.00, 40));
        catalog.add(new Product(4, "Keyboard", 1500.00, 30));
        catalog.add(new Product(5, "USB Drive", 600.00, 50));

        HttpServer server = null;
        int activePort = DEFAULT_PORT;

        try {
            server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        } catch (IOException e) {
            System.out.println("Notice: Port " + DEFAULT_PORT + " is busy. Trying fallback port " + FALLBACK_PORT + "...");
            try {
                server = HttpServer.create(new InetSocketAddress(FALLBACK_PORT), 0);
                activePort = FALLBACK_PORT;
            } catch (IOException ex) {
                System.err.println("Fatal: Could not bind to port " + DEFAULT_PORT + " or " + FALLBACK_PORT);
                ex.printStackTrace();
                return;
            }
        }

        // Register REST API endpoints
        server.createContext("/api/products", new ProductsHandler());
        server.createContext("/api/cart/add", new AddCartHandler());
        server.createContext("/api/cart/remove", new RemoveCartHandler());
        server.createContext("/api/cart/update", new UpdateCartHandler());
        server.createContext("/api/cart", new CartHandler());
        server.createContext("/api/discount", new DiscountHandler());
        server.createContext("/api/checkout", new CheckoutHandler());
        server.createContext("/api/order", new OrderHandler());

        // Register static web asset handler at root
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default single/worker executor
        server.start();

        System.out.println("==============================================================");
        System.out.println(" Online Shopping Cart Web Server Started Successfully!         ");
        System.out.println(" Access URL: http://localhost:" + activePort);
        System.out.println(" Preloaded: 5 products | Default Customer: Rahul Sharma");
        System.out.println("==============================================================");
    }

    // Handles GET /api/products - returns list of all products in catalog
    private static class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"products\":[");
            for (int i = 0; i < catalog.size(); i++) {
                Product p = catalog.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getProductId()).append(",");
                sb.append("\"name\":\"").append(escapeJson(p.getProductName())).append("\",");
                sb.append("\"price\":").append(p.getPrice()).append(",");
                sb.append("\"stock\":").append(p.getStockQuantity());
                sb.append("}");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // Handles GET /api/cart - returns current cart items and live price preview
    private static class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            double subtotal = cart.calculateSubtotal();
            double discountAmt = 0.0;
            String discountDesc = "None";

            if (currentDiscount != null) {
                discountAmt = Order.round(currentDiscount.calculateDiscount(subtotal));
                if (discountAmt > subtotal) {
                    discountAmt = subtotal;
                }
                discountDesc = currentDiscount.toString();
            }

            double taxable = Order.round(Math.max(0.0, subtotal - discountAmt));
            double tax = Order.round(taxable * GST_RATE);
            double finalAmount = Order.round(taxable + tax);

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":true,");
            sb.append("\"subtotal\":").append(subtotal).append(",");
            sb.append("\"discount\":").append(discountAmt).append(",");
            sb.append("\"discountDescription\":\"").append(escapeJson(discountDesc)).append("\",");
            sb.append("\"taxable\":").append(taxable).append(",");
            sb.append("\"gst\":").append(tax).append(",");
            sb.append("\"finalAmount\":").append(finalAmount).append(",");
            sb.append("\"items\":[");

            ArrayList<CartItem> items = cart.getItems();
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                Product p = item.getProduct();
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getProductId()).append(",");
                sb.append("\"name\":\"").append(escapeJson(p.getProductName())).append("\",");
                sb.append("\"price\":").append(p.getPrice()).append(",");
                sb.append("\"quantity\":").append(item.getQuantity()).append(",");
                sb.append("\"subtotal\":").append(item.getSubtotal());
                sb.append("}");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // Handles POST /api/cart/add - adds a product with quantity to the cart
    private static class AddCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);

            try {
                int productId = Integer.parseInt(params.get("productId"));
                int quantity = Integer.parseInt(params.get("quantity"));

                Product product = findProductById(productId);
                if (product == null) {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Product not found.\"}");
                    return;
                }

                cart.addItem(product, quantity);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Added " 
                        + quantity + " x " + escapeJson(product.getProductName()) + " to cart.\"}");
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid numeric values provided.\"}");
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles POST /api/cart/remove - removes a product from the cart
    private static class RemoveCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);

            try {
                int productId = Integer.parseInt(params.get("productId"));
                cart.removeItem(productId);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Product removed from cart.\"}");
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid product ID.\"}");
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles POST /api/cart/update - updates product quantity in the cart
    private static class UpdateCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);

            try {
                int productId = Integer.parseInt(params.get("productId"));
                int quantity = Integer.parseInt(params.get("quantity"));
                cart.updateQuantity(productId, quantity);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Quantity updated successfully.\"}");
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid quantity value.\"}");
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles POST /api/discount - configures active percentage or flat discount
    private static class DiscountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            String type = params.get("type");

            try {
                if ("percent".equalsIgnoreCase(type)) {
                    double percent = Double.parseDouble(params.get("value"));
                    currentDiscount = new PercentageDiscount(percent);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Applied " + percent + "% discount.\"}");
                } else if ("flat".equalsIgnoreCase(type)) {
                    double amount = Double.parseDouble(params.get("value"));
                    currentDiscount = new FlatDiscount(amount);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Applied flat ₹" + String.format("%.2f", amount) + " discount.\"}");
                } else {
                    currentDiscount = null;
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Discount removed.\"}");
                }
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid discount number.\"}");
            } catch (IllegalArgumentException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles POST /api/checkout - processes payment, updates inventory, and places order
    private static class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            if (cart.isEmpty()) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Cannot checkout: Cart is empty!\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            String paymentTypeStr = params.get("paymentType");

            Payment payment = null;
            PaymentType paymentType = null;

            try {
                if ("CARD".equalsIgnoreCase(paymentTypeStr)) {
                    paymentType = PaymentType.CARD;
                    String cardNum = params.get("cardNumber");
                    String holder = params.get("cardHolderName");
                    if (holder == null || holder.trim().isEmpty()) {
                        holder = customer.getName();
                    }
                    payment = new CardPayment(cardNum, holder);
                } else if ("UPI".equalsIgnoreCase(paymentTypeStr)) {
                    paymentType = PaymentType.UPI;
                    String upiId = params.get("upiId");
                    payment = new UPIPayment(upiId);
                } else if ("CASH".equalsIgnoreCase(paymentTypeStr)) {
                    paymentType = PaymentType.CASH;
                    double cashGiven = Double.parseDouble(params.get("cashGiven"));
                    payment = new CashPayment(cashGiven);
                } else {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid payment method selected.\"}");
                    return;
                }

                // Create Order snapshot
                Order order = new Order(nextOrderId, customer, cart);

                // Execute polymorphic checkout and payment
                boolean success = order.placeOrder(currentDiscount, payment, paymentType, GST_RATE);

                if (success) {
                    lastOrder = order;
                    nextOrderId++;
                    currentDiscount = null; // Reset discount after successful order

                    StringBuilder sb = new StringBuilder();
                    sb.append("{");
                    sb.append("\"success\":true,");
                    sb.append("\"message\":\"Order #").append(order.getOrderId()).append(" confirmed successfully!\",");
                    sb.append("\"orderId\":").append(order.getOrderId()).append(",");
                    sb.append("\"finalAmount\":").append(order.getFinalAmount());
                    sb.append("}");
                    sendJsonResponse(exchange, 200, sb.toString());
                } else {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Payment failed. Details invalid or insufficient cash tendered.\"}");
                }
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Invalid numeric value in payment fields.\"}");
            } catch (IllegalArgumentException | IllegalStateException e) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Handles GET /api/order - returns summary of the last completed order
    private static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            if (lastOrder == null) {
                sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"No order placed yet.\"}");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"success\":true,");
            sb.append("\"order\":{");
            sb.append("\"orderId\":").append(lastOrder.getOrderId()).append(",");
            sb.append("\"status\":\"").append(lastOrder.getStatus()).append("\",");
            sb.append("\"customer\":\"").append(escapeJson(lastOrder.getCustomer().getName())).append("\",");
            sb.append("\"email\":\"").append(escapeJson(lastOrder.getCustomer().getEmail())).append("\",");
            sb.append("\"paymentType\":\"").append(lastOrder.getPaymentType()).append("\",");
            sb.append("\"subtotal\":").append(lastOrder.getSubtotal()).append(",");
            sb.append("\"discount\":").append(lastOrder.getDiscountAmount()).append(",");
            String discDesc = (lastOrder.getAppliedDiscount() != null) ? lastOrder.getAppliedDiscount().toString() : "None";
            sb.append("\"discountDescription\":\"").append(escapeJson(discDesc)).append("\",");
            sb.append("\"taxable\":").append(lastOrder.getTaxableAmount()).append(",");
            sb.append("\"gst\":").append(lastOrder.getTaxAmount()).append(",");
            sb.append("\"finalAmount\":").append(lastOrder.getFinalAmount()).append(",");
            sb.append("\"items\":[");

            ArrayList<CartItem> items = lastOrder.getOrderItems();
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                Product p = item.getProduct();
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getProductId()).append(",");
                sb.append("\"name\":\"").append(escapeJson(p.getProductName())).append("\",");
                sb.append("\"price\":").append(p.getPrice()).append(",");
                sb.append("\"quantity\":").append(item.getQuantity()).append(",");
                sb.append("\"subtotal\":").append(item.getSubtotal());
                sb.append("}");
            }
            sb.append("]}");
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // Serves static web assets (index.html, style.css, app.js) from the web/ folder
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Locate web/ folder across possible running directories
            File baseDir = new File("web");
            if (!baseDir.exists()) {
                baseDir = new File("OnlineShoppingCart/web");
            }

            File targetFile = new File(baseDir, path.replaceFirst("^/", ""));

            if (!targetFile.exists() || targetFile.isDirectory()) {
                String error = "<h1>404 Not Found</h1><p>The requested file was not found.</p>";
                byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
                return;
            }

            String contentType = getContentType(targetFile.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, targetFile.length());

            OutputStream os = exchange.getResponseBody();
            FileInputStream fis = new FileInputStream(targetFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            fis.close();
            os.close();
        }
    }

    // Determines MIME type based on file extension
    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (fileName.endsWith(".json")) return "application/json; charset=UTF-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    // Helper method to look up a product in the catalog
    private static Product findProductById(int productId) {
        for (Product p : catalog) {
            if (p.getProductId() == productId) {
                return p;
            }
        }
        return null;
    }

    // Reads full HTTP request body into a UTF-8 String
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    // Parses URL-encoded form data into a Map
    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> map = new HashMap<String, String>();
        if (formData == null || formData.trim().isEmpty()) {
            return map;
        }
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = (idx < pair.length() - 1)
                        ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                        : "";
                map.put(key, value);
            }
        }
        return map;
    }

    // Sends standard 405 Method Not Allowed response
    private static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        byte[] bytes = "{\"success\":false,\"message\":\"Method not allowed\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(405, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Sends a JSON response with status code and closes exchange
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Helper method to escape string characters for JSON output
    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"') sb.append("\\\"");
            else if (ch == '\\') sb.append("\\\\");
            else if (ch == '\b') sb.append("\\b");
            else if (ch == '\f') sb.append("\\f");
            else if (ch == '\n') sb.append("\\n");
            else if (ch == '\r') sb.append("\\r");
            else if (ch == '\t') sb.append("\\t");
            else sb.append(ch);
        }
        return sb.toString();
    }
}
