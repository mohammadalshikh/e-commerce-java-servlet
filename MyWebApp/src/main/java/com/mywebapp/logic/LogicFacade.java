package com.mywebapp.logic;

import com.mywebapp.logic.custom_errors.*;
import com.mywebapp.logic.models.*;

import java.util.ArrayList;
import java.io.*;
import java.util.UUID;

public class LogicFacade {

    public LogicFacade() {
    }

    //*******************************************************************************
    //* domain logic functions
    //*******************************************************************************

    public void createProduct(String name, String description, String vendor, String urlSlug, double price) throws ProductAlreadyExistsException, DataMapperException {

        if (Product.productAlreadyExists(name, description, vendor, urlSlug, price)) {
            throw new ProductAlreadyExistsException("This product already exists");
        }

        Product product = new Product(name, description, vendor, urlSlug, price);
        product.addProductToDb();
    }

    public void updateProduct(String name, String description, String vendor, String urlSlug, String sku, double price) throws ProductNotFoundException, DataMapperException {
        Product product = Product.findProductBySku(UUID.fromString(sku));
        product.updateProductInDb(name, description, vendor, urlSlug, price);

    }

    public Product getProduct(String sku) throws ProductNotFoundException, DataMapperException {
        return Product.findProductBySku(UUID.fromString(sku));
    }

    public Product getProductBySlug(String urlSlug) throws ProductNotFoundException, DataMapperException {
        return Product.findProductBySlug(urlSlug);
    }

    public ArrayList<? extends Product> getCart(String customer_id) throws UserNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        return CartItem.findCartItemsByCartId(customer.getCartId());
    }

    public void addProductToCart(String customer_id, String sku) throws UserNotFoundException, ProductNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        Cart cart = new Cart(customer.getCartId());
        cart.incrementItem(UUID.fromString(sku));
    }

    public void removeProductFromCart(String customer_id, String sku) throws UserNotFoundException, ProductNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        Cart cart = new Cart(customer.getCartId());

        CartItem item = CartItem.findCartItemBySkuAndCartId(UUID.fromString(sku), customer.getCartId());
        item.setQuantity(1);
        cart.decrementItem(UUID.fromString(sku));
    }

    public void decrementProductInCart(String customer_id, String sku) throws UserNotFoundException, ProductNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        Cart cart = new Cart(customer.getCartId());
        cart.decrementItem(UUID.fromString(sku));
    }

    public void setProductQuantityInCart(String customer_id, String sku, int quantity) throws UserNotFoundException, DataMapperException, ProductNotFoundException {
        Customer customer = Customer.getCustomer(customer_id);
        CartItem item = CartItem.findCartItemBySkuAndCartId(UUID.fromString(sku), customer.getCartId());
        item.setQuantity(quantity);
    }

    public void clearCart(String customer_id) throws UserNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        customer.clearCart();
    }

    public void createOrder(String customer_id, String shippingAddress) throws UserNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        Order order = new Order(customer.getCustomerId(), shippingAddress);
        order.placeOrder(customer.getCartId());
        customer.clearCart();
    }

    public ArrayList<Order> getOrdersByCustomer(String customer_id) throws UserNotFoundException, DataMapperException {
        Customer customer = Customer.getCustomer(customer_id);
        return Order.getOrdersByCustomer(customer.getCustomerId());
    }

    public Order getOrderDetails(String userType, String customer_id, int orderId) throws DataMapperException, CustomerOrderMismatchException, UserNotFoundException, OrderNotFoundException {
        Order order = Order.getOrderByGuid(orderId);

        if (!customer_id.isEmpty() && userType.equals("user")) {
            Customer customer = Customer.getCustomer(customer_id);
            if (!order.getCustomerId().equals(customer.getCustomerId())) {
                throw new CustomerOrderMismatchException("This order does not belong to this customer");
            }
        }

        return order;
    }

    public ArrayList<Order> getAllOrders() throws DataMapperException, OrderNotFoundException {
        return Order.getAllOrders();
    }

    public void shipOrder(int orderId) throws DataMapperException, OrderNotFoundException {
        Order order = Order.getOrderByGuid(orderId);
        order.ship();
    }

    public File downloadProductCatalog() throws DataMapperException, FileDownloadException {
        return Product.downloadProductCatalog();
    }

    public ArrayList<Product> getProducts() throws DataMapperException {
        return Product.getAllProducts();
    }

    public String createCustomer() throws DataMapperException {
        Customer customer = new Customer();
        customer.addCustomerToDb();
        return customer.getCustomerId().toString();
    }

}

