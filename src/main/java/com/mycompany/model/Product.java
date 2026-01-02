package com.mycompany.model;

import java.util.Date; // Đã đổi từ sql.Timestamp sang util.Date

public class Product {
    private int productId;
    private int categoryId;
    private int supplierId;
    private String productName;
    private String barcode;
    private double importPrice;
    private double salePrice;
    private int quantity;
    private String image;
    private int status;
    private Date createdAt; // Đổi kiểu dữ liệu ở đây
    private String categoryName;
    private String supplierName;

    public Product() {
    }

    public Product(int productId, int categoryId, int supplierId, String productName, String barcode, double importPrice, double salePrice, int quantity, String image, int status, Date createdAt) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.productName = productName;
        this.barcode = barcode;
        this.importPrice = importPrice;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.image = image;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    // Getter và Setter cho createdAt đã được cập nhật kiểu Date
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return productName;
    }
}