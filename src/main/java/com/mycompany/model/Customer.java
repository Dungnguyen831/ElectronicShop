/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Nguyen Anh Dung
 */
public class Customer {
    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private int points;

    public Customer() {}

    public Customer(int customerId, String fullName, String phone, String email, String address, int points) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.points = points;
    }

    // Getter & Setter (Bắt buộc phải có đủ)
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
}
