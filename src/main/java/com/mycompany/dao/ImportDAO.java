package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Import;
import com.mycompany.model.ImportDetail;
import java.sql.*;
import java.util.List;

public class ImportDAO {

    public boolean saveImportOrder(Import imp, List<ImportDetail> details) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch (Transaction)

            // 1. Lưu thông tin chung vào bảng imports
            String sqlImp = "INSERT INTO imports (supplier_id, user_id, total_amount) VALUES (?, ?, ?)";
            PreparedStatement psImp = conn.prepareStatement(sqlImp, Statement.RETURN_GENERATED_KEYS);
            psImp.setInt(1, imp.getSupplierId());
            psImp.setInt(2, imp.getUserId());
            psImp.setDouble(3, imp.getTotalAmount());
            psImp.executeUpdate();

            ResultSet rs = psImp.getGeneratedKeys();
            int importId = 0;
            if (rs.next()) importId = rs.getInt(1);

            // 2. Lưu chi tiết và CẬP NHẬT TỒN KHO bảng products
            String sqlDet = "INSERT INTO import_details (import_id, product_id, quantity, input_price) VALUES (?, ?, ?, ?)";
            // Cập nhật cột 'quantity' trong bảng products dựa trên ID
            String sqlUpdateStock = "UPDATE products SET quantity = quantity + ?, import_price=? WHERE product_id = ?";

            PreparedStatement psDet = conn.prepareStatement(sqlDet);
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock);

            for (ImportDetail d : details) {
                // Thêm chi tiết phiếu
                psDet.setInt(1, importId);
                psDet.setInt(2, d.getProductId());
                psDet.setInt(3, d.getQuantity());
                psDet.setDouble(4, d.getInputPrice());
                psDet.executeUpdate();

                // Cập nhật số lượng tồn kho cho sản phẩm
                psUpdate.setInt(1, d.getQuantity());
                psUpdate.setDouble(2, d.getInputPrice());
                psUpdate.setInt(3, d.getProductId());
                psUpdate.executeUpdate();
            }

            conn.commit(); // Hoàn tất giao dịch
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}