package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {

    /**
     * Lấy tất cả dữ liệu đã JOIN từ 4 bảng
     */
    public List<Object[]> selectAllJoined() {
        List<Object[]> list = new ArrayList<>();
        // Sử dụng đúng tên cột import_date và total như trong Database
        String sql = "SELECT i.import_id, i.import_date, s.supplier_name, p.product_name, "
                   + "d.quantity, d.input_price, d.total "
                   + "FROM imports i "
                   + "JOIN suppliers s ON i.supplier_id = s.supplier_id "
                   + "JOIN import_details d ON i.import_id = d.import_id "
                   + "JOIN products p ON d.product_id = p.product_id "
                   + "ORDER BY i.import_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("import_id"),
                    rs.getTimestamp("import_date"), // Sửa đúng tên cột
                    rs.getString("supplier_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("input_price"),
                    rs.getDouble("total")
                };
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lọc dữ liệu theo khoảng thời gian
     */
    public List<Object[]> selectByDateRange(java.util.Date start, java.util.Date end) {
        List<Object[]> list = new ArrayList<>();
        // Sửa: Thay created_at bằng import_date
        String sql = "SELECT i.import_id, i.import_date, s.supplier_name, p.product_name, "
                   + "d.quantity, d.input_price, d.total "
                   + "FROM imports i "
                   + "JOIN suppliers s ON i.supplier_id = s.supplier_id "
                   + "JOIN import_details d ON i.import_id = d.import_id "
                   + "JOIN products p ON d.product_id = p.product_id "
                   + "WHERE i.import_date BETWEEN ? AND ? "
                   + "ORDER BY i.import_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Chuyển đổi java.util.Date sang java.sql.Timestamp
            ps.setTimestamp(1, new Timestamp(start.getTime()));
            ps.setTimestamp(2, new Timestamp(end.getTime()));
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("import_id"),
                    rs.getTimestamp("import_date"), // Sửa đúng tên cột
                    rs.getString("supplier_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("input_price"),
                    rs.getDouble("total")
                };
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}