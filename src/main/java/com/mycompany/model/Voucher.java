package com.mycompany.model;

import java.sql.Date;

/**
 * Model đại diện cho bảng vouchers trong database
 * @author Nguyen Anh Dung
 */
public class Voucher {
    private int voucherId;       // voucher_id: int(11), Primary Key, Auto Increment
    private String code;         // code: varchar(20), Unique Key
    private int discountPercent; // discount_percent: int(11)
    private double maxDiscount;  // max_discount: decimal(15,0)
    private Date startDate;      // start_date: date
    private Date endDate;        // end_date: date
    private int quantity;        // quantity: int(11), Default 100
    private int status;          // status: tinyint(1), Default 1 (1: Active, 0: Inactive)

    // Constructor mặc định
    public Voucher() {
    }

    // Constructor đầy đủ tham số
    public Voucher(int voucherId, String code, int discountPercent, double maxDiscount, Date startDate, Date endDate, int quantity, int status) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quantity = quantity;
        this.status = status;
    }

    // Getter và Setter
    public int getVoucherId() { return voucherId; }
    public void setVoucherId(int voucherId) { this.voucherId = voucherId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    @Override
    public String toString() {
        return "Voucher{" + "code=" + code + ", discount=" + discountPercent + "%, status=" + (status == 1 ? "Active" : "Inactive") + '}';
    }
}