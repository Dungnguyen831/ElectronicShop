package com.mycompany.model;

public class ImportDetail {
    private int detailId;
    private int importId;
    private int productId;
    private int quantity;
    private double inputPrice;

    // Constructor dùng khi thêm mới vào danh sách chờ
    public ImportDetail(int productId, int quantity, double inputPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.inputPrice = inputPrice;
    }

    // Getter và Setter
    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getImportId() { return importId; }
    public void setImportId(int importId) { this.importId = importId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getInputPrice() { return inputPrice; }
    public void setInputPrice(double inputPrice) { this.inputPrice = inputPrice; }
}