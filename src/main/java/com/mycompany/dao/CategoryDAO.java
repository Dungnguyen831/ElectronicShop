package com.mycompany.dao;


import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        // Thêm tùy chọn mặc định
        list.add(new Category(0, "Tất cả danh mục",null));
        
        String sql = "SELECT * FROM categories";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("category_id"), 
                    rs.getString("category_name"),
                     rs.getString("description")   
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}