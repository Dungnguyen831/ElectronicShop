package com.mycompany.model;

import java.sql.Timestamp;

public class Customer {
    private int customerId;      // customer_id: int(11), Primary Key, Auto Increment
    private String fullName;     // full_name: varchar(100)
    private String phone;        // phone: varchar(20), Unique Key
    private String email;        // email: varchar(100), Nullable
    private String address;      // address: varchar(255), Nullable
    private int points;          // points: int(11), Default 0
    private Timestamp createdAt; // created_at: datetime, Default current_timestamp()

    // Constructor mặc định
    public Customer() {
    }

    // Constructor đầy đủ tham số (thường dùng khi lấy dữ liệu từ DB)
    public Customer(int customerId, String fullName, String phone, String email, String address, int points, Timestamp createdAt) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.points = points;
        this.createdAt = createdAt;
    }

    // Constructor không có ID và Timestamp (thường dùng khi thêm mới khách hàng)
    public Customer(String fullName, String phone, String email, String address, int points) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.points = points;
    }

    // Getter và Setter
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Customer{" + "id=" + customerId + ", name=" + fullName + ", phone=" + phone + ", points=" + points + '}';
    }
}
   
//    public Customer(int customerId, String fullName, String phone, String email, String address, int points) {
//        this.customerId = customerId;
//        this.fullName = fullName;
//        this.phone = phone;
//        this.email = email;
//        this.address = address;
//        this.points = points;
//    }



