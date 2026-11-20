/**
 * Online Shopping Cart - Vanilla Frontend JavaScript
 * Handles REST API communication using fetch(), dynamic DOM updates, and client validation.
 */

// Initialize page on DOM load
document.addEventListener('DOMContentLoaded', function () {
    loadProducts();
    loadCart();
    checkLastOrder();
});

// Formats a number into Indian Rupee currency format (e.g. ₹54,000.00)
function formatCurrency(amount) {
    var val = Number(amount) || 0;
    return '₹' + val.toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

// Shows a non-blocking floating banner for success or error feedback
function showNotification(message, isSuccess) {
    var banner = document.getElementById('notification-banner');
    var icon = document.getElementById('notification-icon');
    var text = document.getElementById('notification-text');

    banner.className = 'notification ' + (isSuccess ? 'success' : 'error');
    icon.textContent = isSuccess ? '✅ ' : '⚠️ ';
    text.textContent = message;
    banner.classList.remove('hidden');

    // Auto-hide after 5 seconds
    if (window.notifTimeout) {
        clearTimeout(window.notifTimeout);
    }
    window.notifTimeout = setTimeout(function () {
        hideNotification();
    }, 5000);
}

// Hides the notification banner
function hideNotification() {
    var banner = document.getElementById('notification-banner');
    banner.classList.add('hidden');
}

// Fetches and renders all products from GET /api/products
function loadProducts() {
    fetch('/api/products')
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data.success) {
                showNotification(data.message || 'Failed to load products.', false);
                return;
            }
            renderProductGrid(data.products || []);
        })
        .catch(function (err) {
            showNotification('Unable to reach server. Please check if WebServer is running.', false);
        });
}

// Renders the product cards into the catalog grid
function renderProductGrid(products) {
    var grid = document.getElementById('product-grid');
    if (!products || products.length === 0) {
        grid.innerHTML = '<p class="empty-text">No products available.</p>';
        return;
    }

    var html = '';
    for (var i = 0; i < products.length; i++) {
        var p = products[i];
        var isOut = p.stock <= 0;
        var stockClass = isOut ? 'out' : (p.stock < 10 ? 'low-stock' : 'in-stock');
        var stockLabel = isOut ? 'Out of Stock' : (p.stock + ' in stock');

        html += '<div class="product-card ' + (isOut ? 'out-of-stock' : '') + '">';
        html += '  <div class="product-card-top">';
        html += '    <span class="product-id-tag">ID: #' + p.id + '</span>';
        html += '    <h4 class="product-name">' + escapeHtml(p.name) + '</h4>';
        html += '    <div class="product-price">' + formatCurrency(p.price) + '</div>';
        html += '    <span class="stock-tag ' + stockClass + '">' + stockLabel + '</span>';
        html += '  </div>';
        html += '  <div class="product-card-bottom">';
        html += '    <input type="number" id="qty-input-' + p.id + '" class="qty-input" value="1" min="1" max="' + Math.max(1, p.stock) + '" ' + (isOut ? 'disabled' : '') + '>';
        html += '    <button class="btn btn-primary w-full" onclick="addToCart(' + p.id + ')" ' + (isOut ? 'disabled' : '') + '>';
        html +=        isOut ? 'Out of Stock' : 'Add to Cart';
        html += '    </button>';
        html += '  </div>';
        html += '</div>';
    }

    grid.innerHTML = html;
}

// Fetches and updates the cart preview from GET /api/cart
function loadCart() {
    fetch('/api/cart')
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data.success) return;
            renderCart(data);
        })
        .catch(function () {});
}

// Renders cart items, quantities, and price breakdown
function renderCart(data) {
    var container = document.getElementById('cart-items-container');
    var badge = document.getElementById('cart-count');
    var summary = document.getElementById('cart-summary');
    var items = data.items || [];

    // Calculate total item count for header badge
    var totalCount = 0;
    for (var i = 0; i < items.length; i++) {
        totalCount += items[i].quantity;
    }
    badge.textContent = totalCount;

    if (items.length === 0) {
        container.innerHTML = '<p class="empty-text">Your cart is currently empty.</p>';
        summary.classList.add('hidden');
        return;
    }

    var html = '';
    for (var j = 0; j < items.length; j++) {
        var it = items[j];
        html += '<div class="cart-item-row">';
        html += '  <div class="cart-item-info">';
        html += '    <div class="cart-item-name">' + escapeHtml(it.name) + '</div>';
        html += '    <div class="cart-item-unit">' + formatCurrency(it.price) + ' each</div>';
        html += '  </div>';
        html += '  <div class="cart-item-actions">';
        html += '    <input type="number" class="cart-qty-input" value="' + it.quantity + '" min="1" onchange="updateCartQuantity(' + it.id + ', this.value)">';
        html += '    <div class="cart-item-subtotal">' + formatCurrency(it.subtotal) + '</div>';
        html += '    <button class="btn-remove" title="Remove Item" onclick="removeFromCart(' + it.id + ')">&times;</button>';
        html += '  </div>';
        html += '</div>';
    }
    container.innerHTML = html;

    // Update pricing lines
    document.getElementById('cart-subtotal').textContent = formatCurrency(data.subtotal);
    document.getElementById('summary-discount-label').textContent = 'Discount (' + data.discountDescription + ')';
    document.getElementById('summary-discount-amount').textContent = '-' + formatCurrency(data.discount);
    document.getElementById('cart-taxable').textContent = formatCurrency(data.taxable);
    document.getElementById('cart-gst').textContent = formatCurrency(data.gst);
    document.getElementById('cart-final').textContent = formatCurrency(data.finalAmount);

    summary.classList.remove('hidden');
}

// Sends POST /api/cart/add to add item to cart
function addToCart(productId) {
    var qtyInput = document.getElementById('qty-input-' + productId);
    var quantity = parseInt(qtyInput ? qtyInput.value : '1', 10);

    if (isNaN(quantity) || quantity <= 0) {
        showNotification('Please enter a valid quantity greater than zero.', false);
        return;
    }

    fetch('/api/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'productId=' + encodeURIComponent(productId) + '&quantity=' + encodeURIComponent(quantity)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        showNotification(data.message, data.success);
        if (data.success) {
            loadCart();
            loadProducts(); // Update inventory
        }
    })
    .catch(function () {
        showNotification('Network error while adding product to cart.', false);
    });
}

// Sends POST /api/cart/remove to delete product line from cart
function removeFromCart(productId) {
    fetch('/api/cart/remove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'productId=' + encodeURIComponent(productId)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        showNotification(data.message, data.success);
        loadCart();
        loadProducts();
    })
    .catch(function () {
        showNotification('Network error while removing product.', false);
    });
}

// Sends POST /api/cart/update to alter quantity of existing cart item
function updateCartQuantity(productId, newQuantity) {
    var qty = parseInt(newQuantity, 10);
    if (isNaN(qty) || qty <= 0) {
        showNotification('Quantity must be a positive number. Use the delete button to remove.', false);
        loadCart(); // revert view
        return;
    }

    fetch('/api/cart/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'productId=' + encodeURIComponent(productId) + '&quantity=' + encodeURIComponent(qty)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        showNotification(data.message, data.success);
        loadCart();
        loadProducts();
    })
    .catch(function () {
        showNotification('Network error while updating cart quantity.', false);
    });
}

// Toggles discount input field based on selected discount type
function toggleDiscountInput() {
    var type = document.getElementById('discount-type').value;
    var group = document.getElementById('discount-value-group');
    var label = document.getElementById('discount-value-label');
    var input = document.getElementById('discount-value');

    if (type === 'none') {
        group.classList.add('hidden');
    } else if (type === 'percent') {
        group.classList.remove('hidden');
        label.textContent = 'Discount Percentage (0 to 100%)';
        input.placeholder = 'e.g. 10';
        input.max = '100';
    } else if (type === 'flat') {
        group.classList.remove('hidden');
        label.textContent = 'Flat Discount Amount (₹)';
        input.placeholder = 'e.g. 500';
        input.removeAttribute('max');
    }
}

// Submits POST /api/discount to configure promo discount
function handleApplyDiscount(event) {
    event.preventDefault();
    var type = document.getElementById('discount-type').value;
    var valInput = document.getElementById('discount-value');
    var value = valInput ? valInput.value : '0';

    if (type !== 'none' && (value === '' || isNaN(value) || Number(value) < 0)) {
        showNotification('Please enter a valid, non-negative discount value.', false);
        return;
    }

    fetch('/api/discount', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'type=' + encodeURIComponent(type) + '&value=' + encodeURIComponent(value)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        showNotification(data.message, data.success);
        loadCart();
    })
    .catch(function () {
        showNotification('Network error applying discount.', false);
    });
}

// Toggles input fields when selecting Card, UPI, or Cash
function handlePaymentTypeChange() {
    var selected = document.querySelector('input[name="paymentType"]:checked').value;
    document.getElementById('card-fields').classList.toggle('hidden', selected !== 'CARD');
    document.getElementById('upi-fields').classList.toggle('hidden', selected !== 'UPI');
    document.getElementById('cash-fields').classList.toggle('hidden', selected !== 'CASH');
}

// Handles checkout submission to POST /api/checkout
function handleCheckout(event) {
    event.preventDefault();
    var selectedType = document.querySelector('input[name="paymentType"]:checked').value;
    var bodyParams = 'paymentType=' + encodeURIComponent(selectedType);

    if (selectedType === 'CARD') {
        var cardNum = document.getElementById('card-number').value.trim();
        var cardHolder = document.getElementById('card-holder').value.trim();
        if (cardNum.length !== 16 || !/^\d{16}$/.test(cardNum)) {
            showNotification('Card number must be exactly 16 numeric digits.', false);
            return;
        }
        bodyParams += '&cardNumber=' + encodeURIComponent(cardNum) + '&cardHolderName=' + encodeURIComponent(cardHolder);
    } else if (selectedType === 'UPI') {
        var upiId = document.getElementById('upi-id').value.trim();
        if (!upiId || upiId.indexOf('@') <= 0 || upiId.indexOf('@') === upiId.length - 1) {
            showNotification('UPI ID must contain "@" (e.g., student@okaxis).', false);
            return;
        }
        bodyParams += '&upiId=' + encodeURIComponent(upiId);
    } else if (selectedType === 'CASH') {
        var cashGiven = document.getElementById('cash-given').value.trim();
        if (!cashGiven || isNaN(cashGiven) || Number(cashGiven) <= 0) {
            showNotification('Please enter a valid cash tendered amount.', false);
            return;
        }
        bodyParams += '&cashGiven=' + encodeURIComponent(cashGiven);
    }

    fetch('/api/checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: bodyParams
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        showNotification(data.message, data.success);
        loadCart();
        loadProducts();
        if (data.success) {
            checkLastOrder();
        }
    })
    .catch(function () {
        showNotification('Network error during checkout.', false);
    });
}

// Fetches GET /api/order and renders the invoice card
function checkLastOrder() {
    fetch('/api/order')
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data.success || !data.order) return;
            renderInvoice(data.order);
        })
        .catch(function () {});
}

// Renders the order summary invoice
function renderInvoice(order) {
    var invoiceCard = document.getElementById('invoice-card');
    document.getElementById('invoice-order-id').textContent = order.orderId;
    document.getElementById('invoice-status').textContent = order.status;
    document.getElementById('invoice-customer').textContent = order.customer + ' (' + order.email + ')';
    document.getElementById('invoice-payment-method').textContent = order.paymentType;

    var tbody = document.getElementById('invoice-items');
    var itemsHtml = '';
    var items = order.items || [];
    for (var i = 0; i < items.length; i++) {
        var it = items[i];
        itemsHtml += '<tr>';
        itemsHtml += '  <td>' + escapeHtml(it.name) + '</td>';
        itemsHtml += '  <td>' + formatCurrency(it.price) + '</td>';
        itemsHtml += '  <td>' + it.quantity + '</td>';
        itemsHtml += '  <td>' + formatCurrency(it.subtotal) + '</td>';
        itemsHtml += '</tr>';
    }
    tbody.innerHTML = itemsHtml;

    document.getElementById('invoice-subtotal').textContent = formatCurrency(order.subtotal);
    document.getElementById('invoice-discount-desc').textContent = 'Discount (' + order.discountDescription + ')';
    document.getElementById('invoice-discount-amt').textContent = '-' + formatCurrency(order.discount);
    document.getElementById('invoice-taxable').textContent = formatCurrency(order.taxable);
    document.getElementById('invoice-gst').textContent = formatCurrency(order.gst);
    document.getElementById('invoice-final').textContent = formatCurrency(order.finalAmount);

    invoiceCard.classList.remove('hidden');
}

// Basic helper to prevent XSS in dynamic HTML injection
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
