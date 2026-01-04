package com.mycompany.model;

import java.util.Date;

public class Import {
    private int importId;
    private int supplierId;
    private int userId;
    private double totalAmount;
    private Date createdAt;

    public Import() {}

    // Getter và Setter
    public int getImportId() { return importId; }
    public void setImportId(int importId) { this.importId = importId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}