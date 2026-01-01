/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author Nguyen Anh Dung
 */

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private int roleId; // 1: Admin, 2: Staff

    public User() {
    }

    public User(String username, String password, String fullName, int roleId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
    }

    // Getter và Setter (Bắt buộc để truy cập dữ liệu)
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
}
