package com.mycompany.model;

public class OrderDetail {
    private int detailId;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double subtotal; // Cột này trong DB là generated, nhưng trong Java ta có thể tính toán

    public OrderDetail() {
    }

    // Constructor dùng để thêm vào giỏ hàng (chưa có ID đơn hàng)
    public OrderDetail(int productId, int quantity, double unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // Constructor đầy đủ từ DB
    public OrderDetail(int detailId, int orderId, int productId, int quantity, double unitPrice, double subtotal) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.subtotal = this.quantity * this.unitPrice; // Tự động cập nhật thành tiền
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice; 
        this.subtotal = this.quantity * this.unitPrice;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    private String productName;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
