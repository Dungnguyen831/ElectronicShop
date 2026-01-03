package com.mycompany.dao;

import com.mycompany.model.Supplier;
import com.mycompany.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean insert(Supplier s) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, address, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, s.getSupplierName());
            pstmt.setString(2, s.getContactPerson()); // Khớp cột contact_person
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getAddress());
            pstmt.setBoolean(5, s.isIsActive()); // Khớp cột is_active

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // In lỗi ra console để kiểm tra
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp
     */
    public boolean update(Supplier s) {
        String sql = "UPDATE suppliers SET supplier_name=?, contact_person=?, phone=?, address=?, is_active=? WHERE supplier_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getSupplierName());
            pstmt.setString(2, s.getContactPerson());
            pstmt.setString(3, s.getPhone());
            pstmt.setString(4, s.getAddress());
            pstmt.setBoolean(5, s.isIsActive());
            pstmt.setInt(6, s.getSupplierId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Tránh lỗi thông báo thành công giả
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa nhà cung cấp theo ID
     */
    public void delete(int id) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //hàm check số ddienj thoại
    private boolean isValidPhone(String phone) {
        // Regex: Bắt đầu bằng 0, theo sau là 9 chữ số (tổng 10 số)
        String regex = "^0\\d{9}$"; 
        return phone.matches(regex);
    }
    /**
     * Lấy toàn bộ danh sách nhà cung cấp
     */
    public List<Supplier> selectAll() {
        return selectBySql("SELECT * FROM suppliers");
    }

    /**
     * Tìm kiếm nhà cung cấp theo tên
     */
    public List<Supplier> selectByKeyword(String keyword) {
        String sql = "SELECT * FROM suppliers WHERE supplier_name LIKE ?";
        return selectBySql(sql, "%" + keyword + "%");
    }

    /**
     * Hàm dùng chung để thực hiện truy vấn SQL và đổ vào List
     */
    private List<Supplier> selectBySql(String sql, Object... args) {
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Supplier s = new Supplier();
                // Phải khớp chính xác tên cột trong database
                s.setSupplierId(rs.getInt("supplier_id"));
                s.setSupplierName(rs.getString("supplier_name"));
                s.setContactPerson(rs.getString("contact_person")); 
                s.setPhone(rs.getString("phone"));
                s.setAddress(rs.getString("address"));
                s.setIsActive(rs.getBoolean("is_active"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Quan trọng để debug khi bảng bị trắng
        }
        return list;
    }
}