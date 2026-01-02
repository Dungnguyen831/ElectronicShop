package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Customer;
import java.sql.*;

public class CustomerDAO {

    // Tìm khách hàng theo SĐT
    public Customer findByPhone(String phone) {
        Customer c = null;
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                c = new Customer();
                c.setCustomerId(rs.getInt("customer_id"));
                c.setFullName(rs.getString("full_name"));
                c.setPhone(rs.getString("phone"));
                c.setEmail(rs.getString("email"));
                c.setAddress(rs.getString("address"));
                c.setPoints(rs.getInt("points"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return c;
    }

    // Thêm khách hàng mới
    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO customers(full_name, phone, email, address, points) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setInt(5, 0); // Mặc định 0 điểm
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}