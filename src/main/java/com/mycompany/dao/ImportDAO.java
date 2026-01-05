package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Import;
import com.mycompany.model.ImportDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {

    /**
     * Lưu phiếu nhập, chi tiết phiếu và cập nhật tồn kho + giá nhập mới nhất
     */
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

            // Lấy ID của phiếu nhập vừa tạo
            ResultSet rs = psImp.getGeneratedKeys();
            int importId = 0;
            if (rs.next()) importId = rs.getInt(1);

            // 2. Chuẩn bị câu lệnh SQL cho chi tiết và cập nhật sản phẩm
            // Lưu ý: Đã bỏ cột 'total' ở đây để tránh lỗi nếu database của bạn chưa có cột này
            String sqlDet = "INSERT INTO import_details (import_id, product_id, quantity, input_price) VALUES (?, ?, ?, ?)";
            
            // Cập nhật cả số lượng tồn (cộng dồn) và giá nhập (ghi đè giá mới nhất)
            String sqlUpdateStock = "UPDATE products SET quantity = quantity + ?, import_price = ? WHERE product_id = ?";

            PreparedStatement psDet = conn.prepareStatement(sqlDet);
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStock);

            for (ImportDetail d : details) {
                // Thêm chi tiết phiếu vào bảng import_details
                psDet.setInt(1, importId);
                psDet.setInt(2, d.getProductId());
                psDet.setInt(3, d.getQuantity());
                psDet.setDouble(4, d.getInputPrice());
                psDet.executeUpdate();

                // Cập nhật số lượng và giá nhập vào bảng products
                psUpdate.setInt(1, d.getQuantity());
                psUpdate.setDouble(2, d.getInputPrice()); 
                psUpdate.setInt(3, d.getProductId());
                psUpdate.executeUpdate();
            }

            conn.commit(); // Hoàn tất giao dịch thành công
            return true;
        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào, hủy bỏ toàn bộ các thao tác đã thực hiện (Rollback)
            try { 
                if (conn != null) conn.rollback(); 
            } catch (SQLException ex) { 
                ex.printStackTrace(); 
            }
            e.printStackTrace(); // Xem lỗi chi tiết ở Console nếu vẫn bị thông báo "Lỗi"
            return false;
        } finally {
            // Trả lại trạng thái AutoCommit và đóng kết nối nếu cần
            try { 
                if (conn != null) conn.setAutoCommit(true); 
            } catch (SQLException e) { 
                e.printStackTrace(); 
            }
        }
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