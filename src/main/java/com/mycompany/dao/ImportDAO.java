package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Import;
import com.mycompany.model.ImportDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {

    
    public boolean saveImportOrder(Import imp, List<ImportDetail> details) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu giao dịch để đảm bảo dữ liệu đồng nhất

            // 1. Lưu vào bảng imports - Đã thêm cột user_id để tránh lỗi "Field 'user_id' doesn't have a default value"
            String sqlImp = "INSERT INTO imports (supplier_id, total_amount, user_id) VALUES (?, ?, ?)";
            PreparedStatement psImp = conn.prepareStatement(sqlImp, Statement.RETURN_GENERATED_KEYS);
            
            psImp.setInt(1, (imp.getSupplierId() > 0) ? imp.getSupplierId() : 1);
            psImp.setDouble(2, imp.getTotalAmount());
            psImp.setInt(3, (imp.getUserId() > 0) ? imp.getUserId() : 1); // Đảm bảo luôn có ID người dùng
            
            psImp.executeUpdate();

            // Lấy ID của phiếu nhập vừa tạo để lưu vào bảng chi tiết
            ResultSet rs = psImp.getGeneratedKeys();
            int importId = 0;
            if (rs.next()) importId = rs.getInt(1);

            // 2. Chuẩn bị câu lệnh lưu chi tiết và cập nhật sản phẩm
            String sqlDet = "INSERT INTO import_details (import_id, product_id, quantity, input_price) VALUES (?, ?, ?, ?)";
            // Cập nhật cả số lượng tồn kho VÀ giá nhập mới nhất vào bảng products
            String sqlUpdate = "UPDATE products SET quantity = quantity + ?, import_price = ? WHERE product_id = ?";

            PreparedStatement psDet = conn.prepareStatement(sqlDet);
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);

            for (ImportDetail d : details) {
                // Lưu vào bảng import_details
                psDet.setInt(1, importId);
                psDet.setInt(2, d.getProductId());
                psDet.setInt(3, d.getQuantity());
                psDet.setDouble(4, d.getInputPrice());
                psDet.executeUpdate();

                // Cập nhật kho: Cộng dồn số lượng và cập nhật giá nhập mới
                psUpdate.setInt(1, d.getQuantity());
                psUpdate.setDouble(2, d.getInputPrice()); // Cập nhật giá nhập mới ở đây
                psUpdate.setInt(3, d.getProductId());
                psUpdate.executeUpdate();
            }

            conn.commit(); // Lưu tất cả thay đổi vào Database
            return true;
        } catch (Exception e) {
            System.err.println("--- LỖI HỆ THỐNG CHI TIẾT ---");
            e.printStackTrace(); // In lỗi ra Console để kiểm tra nếu có vấn đề
            
            if (conn != null) {
try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // Lấy danh sách phiếu nhập hiển thị lên bảng (kèm tên nhà cung cấp và tên sản phẩm)
    public List<Object[]> selectAllJoined() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT i.import_id, i.import_date, s.supplier_name, p.product_name, "
                   + "d.quantity, d.input_price, (d.quantity * d.input_price) as total "
                   + "FROM imports i "
                   + "JOIN suppliers s ON i.supplier_id = s.supplier_id "
                   + "JOIN import_details d ON i.import_id = d.import_id "
                   + "JOIN products p ON d.product_id = p.product_id "
                   + "ORDER BY i.import_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("import_id"),
                    rs.getTimestamp("import_date"),
                    rs.getString("supplier_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("input_price"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Tìm kiếm phiếu nhập theo khoảng ngày
    public List<Object[]> selectByDateRange(java.util.Date start, java.util.Date end) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT i.import_id, i.import_date, s.supplier_name, p.product_name, "
                   + "d.quantity, d.input_price, (d.quantity * d.input_price) as total "
                   + "FROM imports i "
                   + "JOIN suppliers s ON i.supplier_id = s.supplier_id "
                   + "JOIN import_details d ON i.import_id = d.import_id "
                   + "JOIN products p ON d.product_id = p.product_id "
                   + "WHERE i.import_date >= ? AND i.import_date <= ? "
                   + "ORDER BY i.import_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(end.getTime()));
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("import_id"),
                    rs.getTimestamp("import_date"),
                    rs.getString("supplier_name"),
rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("input_price"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }


    /**
     * Lấy lịch sử nhập kho đã JOIN các bảng (Dùng cho ListImport)
     */
    // Thêm hàm này vào class ImportDAO
        public List<Object[]> selectDetailsByImportId(int importId) {
            List<Object[]> list = new ArrayList<>();
            String sql = "SELECT p.product_name, d.quantity, d.input_price, (d.quantity * d.input_price) as total "
                       + "FROM import_details d "
                       + "JOIN products p ON d.product_id = p.product_id "
                       + "WHERE d.import_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, importId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("input_price"),
                        rs.getDouble("total")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        public List<Object[]> selectAllImports() {
            List<Object[]> list = new ArrayList<>();
            String sql = "SELECT i.import_id, i.import_date, s.supplier_name, i.total_amount "
                       + "FROM imports i JOIN suppliers s ON i.supplier_id = s.supplier_id "
                       + "ORDER BY i.import_date DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("import_id"), rs.getTimestamp("import_date"),
                        rs.getString("supplier_name"), rs.getDouble("total_amount")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }
    /**
     * Lọc lịch sử nhập theo ngày
     */
    public List<Object[]> selectImportsByDate(java.util.Date start, java.util.Date end) {
      List<Object[]> list = new ArrayList<>();
      // Câu lệnh SQL này chỉ lấy thông tin tổng quát của phiếu nhập
      String sql = "SELECT i.import_id, i.import_date, s.supplier_name, i.total_amount "
                 + "FROM imports i "
                 + "JOIN suppliers s ON i.supplier_id = s.supplier_id "
                 + "WHERE i.import_date BETWEEN ? AND ? "
                 + "ORDER BY i.import_date DESC";

      try (Connection conn = DatabaseConnection.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
          // Thiết lập thời gian kết thúc đến cuối ngày (23:59:59)
          ps.setTimestamp(2, new java.sql.Timestamp(end.getTime()));

          ResultSet rs = ps.executeQuery();
          while (rs.next()) {
              list.add(new Object[]{
                  rs.getInt("import_id"),
                  rs.getTimestamp("import_date"),
                  rs.getString("supplier_name"),
                  rs.getDouble("total_amount")
              });
          }
      } catch (SQLException e) { e.printStackTrace(); }
      return list;
  
    }

}