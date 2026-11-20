# Online Shopping Cart System (Core Java)

A robust, console-based **Online Shopping Cart System** written strictly in Core Java (JDK 17+) for first-year college students. Designed to be clean, readable, modular, and easy to explain line-by-line during college practical exams and viva-voce assessments.

---

## 🎯 Project Objectives
1. **Core Java Mastery**: Demonstrate foundational object-oriented programming (OOP) principles without external dependencies or complex frameworks.
2. **Real-world Business Logic**: Implement realistic e-commerce flows including catalog browsing, cart operations, dynamic discounts, Indian GST (18%) calculation, payment simulations, and atomic checkout.
3. **Clean Code & Defensiveness**: Validate all user inputs and application state using built-in exceptions (`IllegalArgumentException` and `IllegalStateException`) and prevent common bugs (e.g., negative amounts, overselling stock, double-charging).
4. **Collegiate Practical Readiness**: Provide simple, straightforward methods (under 20 lines) with meaningful comments, zero magic numbers, and transparent control flows.

---

## ✨ Features
- **Product Catalog**: Preloaded catalog with IDs, names, prices in Indian Rupees (₹), and real-time inventory counts.
- **Cart Management**: Add items, update quantities, remove items, view cart subtotal, and automatically merge duplicate line items.
- **Dynamic Discounts**:
  - `PercentageDiscount`: Configurable 0% to 100% discount.
  - `FlatDiscount`: Fixed rupee discount capped at the subtotal so balance never goes negative.
- **Multi-Channel Simulated Payments (Polymorphism)**:
  - `CardPayment`: Validates 16-digit card numbers and masks sensitive digits.
  - `UPIPayment`: Validates presence of `@` and valid handle formatting.
  - `CashPayment`: Validates cash tendered and accurately calculates change returned.
- **Atomic Checkout Logic**:
  - Pre-checkout stock verification.
  - Payment execution before stock alteration.
  - Stock is deducted **only** upon successful payment.
  - Snapshot copy of cart items ensures order history remains intact even after cart clearing.
  - Double checkout prevention on confirmed orders.
- **Crash-Proof Console UI**: Robust input parsing that catches `NumberFormatException` so typing letters or blank lines will never crash the program.

---

## 🧩 OOP Concepts Demonstrated

| OOP Concept | File(s) Where It Appears | How It Is Implemented |
|---|---|---|
| **Encapsulation** | `Product.java`, `Customer.java`, `CartItem.java`, `Order.java` | All class fields are declared `private` and accessed via validated public getters and setters. |
| **Interfaces** | `Discount.java`, `Payment.java` | Contracts defining abstract operations (`calculateDiscount`, `pay`) without concrete implementation. |
| **Interface Inheritance** | `PercentageDiscount.java`, `FlatDiscount.java`, `CardPayment.java`, `UPIPayment.java`, `CashPayment.java` | Classes implement interfaces (`implements Payment`, `implements Discount`), inheriting type hierarchy. |
| **Polymorphism (Runtime)** | `Order.java`, `Main.java` | `payment.pay(finalAmount)` and `discount.calculateDiscount(subtotal)` dynamically dispatch to the appropriate concrete class at runtime. |
| **Abstraction** | `Payment.java`, `Discount.java` | Hides implementation details of specific discount algorithms and payment mechanisms behind clean interface contracts. |
| **Method Overloading** | `Cart.java` | `addItem(Product, int)` and `addItem(Product)` (which defaults quantity to 1) share the same name with different parameter lists. |
| **Method Overriding** | All Payment classes, Discount classes, `Product.java`, `CartItem.java` | Classes override `toString()` for custom string formatting and `@Override` interface methods (`pay()`, `calculateDiscount()`). |
| **Composition** | `Cart.java`, `Order.java`, `CartItem.java` | `Cart` has-a list of `CartItem` objects; `CartItem` has-a `Product`; `Order` has-a `Customer` and a snapshot list of items. |
| **Collections (`ArrayList`)**| `Cart.java`, `Order.java`, `Main.java` | Dynamic lists (`ArrayList<CartItem>`, `ArrayList<Product>`) used to store catalog items and cart lines. |
| **Enums** | `PaymentType.java`, `OrderStatus.java` | Strongly typed domain constants (`CARD`, `UPI`, `CASH` and `PENDING`, `CONFIRMED`, `FAILED`, `CANCELLED`). |
| **Constructors** | All model classes | Parameterized constructors enforcing initialization invariants and validating constraints immediately. |
| **Exception Handling** | `Product.java`, `Cart.java`, `Order.java`, `Main.java` | Built-in exceptions (`IllegalArgumentException`, `IllegalStateException`) thrown for invalid business states and handled in `Main.java`. |

---

## 🛠️ Technologies & Environment
- **Language**: Java 17+ (Standard Edition)
- **External Dependencies**: None (Pure Standard Java Library: `java.util.Scanner`, `java.util.ArrayList`)
- **IDE**: Visual Studio Code (or any standard Java IDE / terminal)
- **Target OS**: Windows, macOS, Linux

---

## 📁 Project Structure
```text
OnlineShoppingCart/
├── src/com/shoppingcart/
│   ├── Product.java             # Product entity & inventory reduction
│   ├── Customer.java            # Customer details
│   ├── CartItem.java            # Individual cart line item & subtotal
│   ├── Cart.java                # Shopping cart collection & item merging
│   ├── Discount.java            # Discount interface
│   ├── PercentageDiscount.java  # Percentage discount strategy
│   ├── FlatDiscount.java        # Flat rupee discount strategy
│   ├── Payment.java             # Payment interface
│   ├── CardPayment.java         # 16-digit card payment simulation
│   ├── UPIPayment.java          # UPI ID payment simulation
│   ├── CashPayment.java         # Cash tendered & change calculation
│   ├── Order.java               # Order state, tax, checkout, & summary
│   ├── PaymentType.java         # Payment method enum
│   ├── OrderStatus.java         # Order status enum
│   └── Main.java                # Interactive console UI & menu loop
├── .gitignore                   # Git ignore for build artifacts
└── README.md                    # Project documentation
```

---

## 💻 How to Compile and Run (VS Code Terminal)

### 1. Open Terminal in the Project Folder
Open VS Code, press ``Ctrl + ` `` (or `Terminal -> New Terminal`), and navigate to the project root directory:
```powershell
cd OnlineShoppingCart
```

### 2. Compile All Java Source Files
Compile all source files into the `out` destination directory:
```powershell
javac -d out src/com/shoppingcart/*.java
```

### 3. Run the Application
Execute the main class from the compiled classpath:
```powershell
java -cp out com.shoppingcart.Main
```

---

## 📋 Calculation Example (18% GST Verification)
- **Selected Items**:
  - 1 x Laptop @ ₹50,000.00 = ₹50,000.00
  - 2 x Headphones @ ₹2,000.00 = ₹4,000.00
- **Cart Subtotal**: ₹50,000.00 + ₹4,000.00 = **₹54,000.00**
- **10% Percentage Discount**: 10% of ₹54,000.00 = **₹5,400.00**
- **Taxable Amount**: ₹54,000.00 - ₹5,400.00 = **₹48,600.00**
- **GST (18%)**: ₹48,600.00 × 0.18 = **₹8,748.00**
- **Final Payable Amount**: ₹48,600.00 + ₹8,748.00 = **₹57,348.00**

---

## 🖥️ Sample Console Output

```text
============================================================
       WELCOME TO THE ONLINE SHOPPING CART SYSTEM          
============================================================
Logged in as: Customer #101 (Rahul Sharma, rahul.sharma@example.com)

----------------------- MAIN MENU --------------------------
 1. View Available Products
 2. Add Product to Cart
 3. Remove Product from Cart
 4. Update Product Quantity
 5. View Shopping Cart
 6. Apply Percentage Discount
 7. Apply Flat Discount
 8. Checkout
 9. View Order Summary
10. Exit
------------------------------------------------------------
Enter your choice (1-10): 8

-------------------- CHECKOUT SUMMARY ----------------------
Subtotal       : ₹54000.00
Discount       : -₹5400.00
Taxable Amount : ₹48600.00
GST (18%)      : ₹8748.00
Total Payable  : ₹57348.00
------------------------------------------------------------
Select Payment Method:
1. Card Payment (16 digits)
2. UPI Payment (e.g. name@bank)
3. Cash Payment (Tender cash)
4. Cancel Checkout
Enter choice (1-4): 1
Enter 16-digit Card Number: 1234567812345678
Enter Cardholder Name: Rahul Sharma
[Payment Successful] ₹57348.00 charged to Card (XXXX-XXXX-XXXX-5678, Holder: Rahul Sharma).

>>> ORDER CONFIRMED! Order #1001 placed successfully. <<<

======================================================================
                         ORDER INVOICE / SUMMARY                      
======================================================================
Order ID       : #1001
Order Status   : CONFIRMED
Customer Name  : Rahul Sharma (ID: 101)
Customer Email : rahul.sharma@example.com
Payment Method : CARD
----------------------------------------------------------------------
ID    | Product Name     | Unit Price   | Qty  | Subtotal    
----------------------------------------------------------------------
1     | Laptop           | ₹  50000.00  | 1    | ₹  50000.00 
2     | Headphones       | ₹   2000.00  | 2    | ₹   4000.00 
----------------------------------------------------------------------
Cart Subtotal       : ₹    54000.00
Discount Applied    : -₹    5400.00 (10.0% Off)
Taxable Amount      : ₹    48600.00
GST (18%)           : ₹     8748.00
----------------------------------------------------------------------
FINAL AMOUNT PAID   : ₹    57348.00
======================================================================
```

---

## 👥 Team Contribution Table

| Student | Assigned Modules / Classes | Responsibilities & Achievements |
|---|---|---|
| **Student 1** | `Product.java`, `Customer.java`, `CartItem.java`, `Cart.java` | Designed entities, inventory reduction constraints, defensive cart operations, item duplicate merge, and method overloading (`addItem`). |
| **Student 2** | `Discount.java`, `PercentageDiscount.java`, `FlatDiscount.java` | Created discount interface hierarchy, percentage capping (0–100%), and non-negative subtotal clipping. |
| **Student 3** | `Payment.java`, `CardPayment.java`, `UPIPayment.java`, `CashPayment.java` | Engineered payment interface, 16-digit card validation, UPI regex/format checks, cash change calculations, and polymorphism. |
| **Student 4** | `Order.java`, `PaymentType.java`, `OrderStatus.java`, `Main.java` | Orchestrated checkout lifecycle, GST tax calculations, snapshot cart preservation, crash-proof menu loop, and invoice generation. |

---

## 🌐 Web Version

An offline, zero-dependency, modern single-page web interface built strictly with Java's standard `com.sun.net.httpserver.HttpServer` and Vanilla HTML5/CSS3/JavaScript.

### 📦 Added Files
```text
OnlineShoppingCart/
├── src/com/shoppingcart/WebServer.java   # Lightweight HTTP server & JSON REST endpoints
└── web/
    ├── index.html                        # Semantic HTML5 layout
    ├── style.css                         # Pure responsive CSS (Grid & Flexbox)
    └── app.js                            # Vanilla JavaScript (fetch() API)
```

### 🚀 How to Run the Web Version

1. **Compile**:
   ```powershell
   javac -d out src/com/shoppingcart/*.java
   ```
2. **Start Web Server**:
   ```powershell
   java -cp out com.shoppingcart.WebServer
   ```
3. **Open Browser**:
   Navigate to: [http://localhost:8080](http://localhost:8080) (or `http://localhost:8081` if port 8080 is occupied).

### 📡 REST API Endpoints
All endpoints return standard JSON: `{"success": true/false, "message": "...", ...data}`.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | Returns the list of all products in the catalog with real-time stock. |
| `GET` | `/api/cart` | Returns current cart items and live price preview (Subtotal, Discount, Taxable, GST, Total). |
| `POST` | `/api/cart/add` | Adds product to cart by `productId` and `quantity`. |
| `POST` | `/api/cart/remove` | Removes product line item by `productId`. |
| `POST` | `/api/cart/update` | Updates quantity of a product in the cart. |
| `POST` | `/api/discount` | Applies promo discount (`type`: `percent`, `flat`, or `none` and `value`). |
| `POST` | `/api/checkout` | Processes payment and creates order (`paymentType`: `CARD`, `UPI`, `CASH` + credentials). |
| `GET` | `/api/order` | Returns persistent invoice summary of the last completed order. |

### 🏛️ Architecture Note
```text
[ Browser (HTML / CSS / app.js) ]
              │
         fetch() API
              ▼
[ com.sun.net.httpserver.HttpServer (WebServer.java) ]
              │
       Java Object Calls
              ▼
[ Domain Classes (Product, Cart, Discount, Payment, Order) ]
```
The browser uses standard `fetch()` to call the Java `WebServer`. The server parses form data, invokes the core business logic methods on our existing OOP classes (`Cart`, `Order.placeOrder()`, `Payment.pay()`), and serializes the state to clean JSON with `StringBuilder`.

### 📸 Screenshot Placeholder
```text
+-------------------------------------------------------------------------------+
|  🛒 QuickShop Cart               👤 Rahul Sharma              [ Cart (3) ]    |
+------------------------------------+------------------------------------------+
|  Available Products                |  Your Shopping Cart                      |
|  +------------------------------+  |  - Laptop x 1              ₹50,000.00    |
|  | Laptop           ₹50,000.00  |  |  - Headphones x 2           ₹4,000.00    |
|  | Stock: 9 [ 1 ] [Add to Cart] |  |  -------------------------------------   |
|  +------------------------------+  |  Subtotal:                 ₹54,000.00    |
|  | Headphones        ₹2,000.00  |  |  Discount (10.0% Off):     -₹5,400.00    |
|  | Stock: 23 [ 1 ] [Add to Cart]|  |  Taxable:                  ₹48,600.00    |
|  +------------------------------+  |  GST (18%):                 ₹8,748.00    |
|                                    |  Total Payable:            ₹57,348.00    |
|                                    |  [ 💳 Card ] [ 📱 UPI ] [ 💵 Cash ]      |
|                                    |  Card Number: [ 4111222233334444 ]       |
|                                    |  [ Pay Now & Place Order ]               |
+------------------------------------+------------------------------------------+
```

