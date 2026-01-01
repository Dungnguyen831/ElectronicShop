/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Nguyen Anh Dung
 */

public class UserDAO {

    // 1. Chức năng Đăng nhập
    public User checkLogin(String username, String password) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection cons = DatabaseConnection.getConnection();
             PreparedStatement ps = cons.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password); // Thực tế nên mã hóa MD5/BCrypt, nhưng demo ta dùng text thường
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setRoleId(rs.getInt("role_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    // 2. Chức năng Đăng ký (Thêm nhân viên mới)
    public boolean register(User u) {
        // Mặc định role_id = 2 (Staff) và shift_id = 1 (Ca sáng) để test
        String sql = "INSERT INTO users(username, password, full_name, role_id, shift_id) VALUES(?, ?, ?, ?, 1)";
        
        try (Connection cons = DatabaseConnection.getConnection();
             PreparedStatement ps = cons.prepareStatement(sql)) {
            
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setInt(4, 2); // Mặc định là nhân viên
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
