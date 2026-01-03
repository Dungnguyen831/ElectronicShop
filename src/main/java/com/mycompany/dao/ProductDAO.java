package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> searchProducts(String keyword, int categoryId) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE status = 1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (product_name LIKE ? OR barcode LIKE ?)");
        }
        if (categoryId > 0) {
            sql.append(" AND category_id = ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchStr = "%" + keyword + "%";
                ps.setString(index++, searchStr);
                ps.setString(index++, searchStr);
            }
            if (categoryId > 0) {
                ps.setInt(index, categoryId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setBarcode(rs.getString("barcode"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setQuantity(rs.getInt("quantity")); // Hàm này bạn viết đúng
                p.setImage(rs.getString("image"));
                list.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // --- HÀM NÀY BỊ THIẾU LOGIC, ĐÃ SỬA LẠI ---
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setProductName(rs.getString("product_name"));
                p.setSalePrice(rs.getDouble("sale_price"));
                
                // --- DÒNG QUAN TRỌNG BẠN BỊ THIẾU ---
                p.setQuantity(rs.getInt("quantity")); 
                // ------------------------------------
                
                // Tiện thể lấy luôn mấy cái khác cho đủ bộ nếu cần
                p.setBarcode(rs.getString("barcode"));
                p.setImage(rs.getString("image"));
                
                return p;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}