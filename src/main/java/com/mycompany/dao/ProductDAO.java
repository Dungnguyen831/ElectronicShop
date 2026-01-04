package com.mycompany.dao;

import com.mycompany.model.Product;
import com.mycompany.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ProductDAO {

    public boolean insert(Product p) {
        String sql = "INSERT INTO products (category_id, supplier_id, product_name, barcode, import_price, sale_price, quantity, image, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setProductParams(pstmt, p);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Product p) {
        String sql = "UPDATE products SET category_id=?, supplier_id=?, product_name=?, barcode=?, import_price=?, sale_price=?, quantity=?, image=?, status=? WHERE product_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setProductParams(pstmt, p);
            pstmt.setInt(10, p.getProductId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void delete(int id) {
      // 1. Kiểm tra sản phẩm có tồn tại trong các bảng chi tiết hay không
      if (isProductInUse(id)) {
          JOptionPane.showMessageDialog(null, 
              "Không thể xóa sản phẩm này vì đã có lịch sử nhập hàng hoặc bán hàng!", 
              "Lỗi ràng buộc dữ liệu", 
              JOptionPane.ERROR_MESSAGE);
          return;
      }

      // 2. Nếu không tồn tại, tiến hành xóa
      String sql = "DELETE FROM products WHERE product_id = ?";
      try (Connection conn = DatabaseConnection.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setInt(1, id);
          int rows = pstmt.executeUpdate();
          if (rows > 0) {
              JOptionPane.showMessageDialog(null, "Xóa sản phẩm thành công!");
          }
      } catch (SQLException e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(null, "Lỗi hệ thống khi xóa sản phẩm!");
      }
  }

  /**
   * Hàm kiểm tra product_id có tồn tại trong import_details hoặc order_details không
   */
  private boolean isProductInUse(int productId) {
      // Truy vấn kiểm tra đồng thời ở cả 2 bảng bằng UNION
      String sql = "SELECT product_id FROM import_details WHERE product_id = ? " +
                   "UNION " +
                   "SELECT product_id FROM order_details WHERE product_id = ?";

      try (Connection conn = DatabaseConnection.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setInt(1, productId);
          pstmt.setInt(2, productId);

          try (ResultSet rs = pstmt.executeQuery()) {
              return rs.next(); // Nếu rs.next() là true tức là có tồn tại ít nhất 1 dòng
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
      return false;
  }
    public List<Product> selectAll() {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     "ORDER BY p.product_id DESC";
        return selectBySql(sql);
    }

    public List<Product> selectByKeyword(String keyword) {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     "WHERE p.product_name LIKE ? OR p.barcode LIKE ?";
        return selectBySql(sql, "%" + keyword + "%", "%" + keyword + "%");
    }
    //cập nhật trạng thái 
    public boolean updateStatus(int productId, int status) {
        String sql = "UPDATE products SET status = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, productId);

            int rowsAffected = pstmt.executeUpdate();

            // In ra console để debug xem lệnh có thực sự chạy không
            System.out.println("Sản phẩm ID " + productId + " đã cập nhật status: " + status + ". Số dòng bị ảnh hưởng: " + rowsAffected);

            return rowsAffected > 0; // Trả về true nếu cập nhật thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void setProductParams(PreparedStatement pstmt, Product p) throws SQLException {
        pstmt.setInt(1, p.getCategoryId());
        pstmt.setInt(2, p.getSupplierId());
        pstmt.setString(3, p.getProductName());
        pstmt.setString(4, p.getBarcode());
        pstmt.setDouble(5, p.getImportPrice());
        pstmt.setDouble(6, p.getSalePrice());

        // Đẩy số lượng vào tham số thứ 7
        pstmt.setInt(7, p.getQuantity());
        pstmt.setString(8, p.getImage());

        // RÀNG BUỘC: Nếu số lượng <= 0, ép status về 0 (Ngừng bán)
        int finalStatus = (p.getQuantity() <= 0) ? 0 : p.getStatus();
        pstmt.setInt(9, finalStatus); 
    }

    private List<Product> selectBySql(String sql, Object... args) {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setSupplierId(rs.getInt("supplier_id"));
                p.setProductName(rs.getString("product_name"));
                p.setBarcode(rs.getString("barcode"));
                p.setImportPrice(rs.getDouble("import_price"));
                p.setSalePrice(rs.getDouble("sale_price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setImage(rs.getString("image"));
                p.setStatus(rs.getInt("status")); // Lấy kiểu int
                p.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Gán tên từ câu lệnh JOIN
                try {
                    p.setCategoryName(rs.getString("category_name"));
                    p.setSupplierName(rs.getString("supplier_name"));
                } catch (SQLException ex) {
                    // Trường hợp select không JOIN (như các hàm cũ) thì bỏ qua
                }
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Product> selectByCategoryId(int categoryId) {
    String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                 "FROM products p " +
                 "LEFT JOIN categories c ON p.category_id = c.category_id " +
                 "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                 "WHERE p.category_id = ?";
    return selectBySql(sql, categoryId);
    }
    
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
