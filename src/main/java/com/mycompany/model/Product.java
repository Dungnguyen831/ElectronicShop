package com.mycompany.model;

import java.util.Date;

public class Product {
    private int productId, categoryId, supplierId, quantity;
    private String productName, barcode, image;
    private double importPrice, salePrice;
    private int status; // Chuyển từ boolean sang int
    private Date createdAt;
    private String categoryName;
    private String supplierName;

    public Product() {}
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
    // Getter và Setter cho status đã sửa
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    // ... Giữ nguyên các Getter/Setter khác như cũ ...
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
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
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}