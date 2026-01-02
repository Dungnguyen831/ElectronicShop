package com.mycompany.model;

import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private int customerId;
    private Integer voucherId; // Có thể null
    private double totalAmount;
    private String paymentMethod;
    private int status;
    private Timestamp orderDate;

    public Order() {
    }

    public Order(int orderId, int userId, int customerId, Integer voucherId, double totalAmount, String paymentMethod, int status, Timestamp orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerId = customerId;
        this.voucherId = voucherId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.orderDate = orderDate;
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getVoucherId() { return voucherId; }
    public void setVoucherId(Integer voucherId) { this.voucherId = voucherId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
}