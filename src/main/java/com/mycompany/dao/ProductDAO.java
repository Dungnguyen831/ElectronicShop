package com.mycompany.dao;

import com.mycompany.model.Product;
import com.mycompany.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void setProductParams(PreparedStatement pstmt, Product p) throws SQLException {
        pstmt.setInt(1, p.getCategoryId());
        pstmt.setInt(2, p.getSupplierId());
        pstmt.setString(3, p.getProductName());
        pstmt.setString(4, p.getBarcode());
        pstmt.setDouble(5, p.getImportPrice());
        pstmt.setDouble(6, p.getSalePrice());
        pstmt.setInt(7, p.getQuantity());
        pstmt.setString(8, p.getImage());
        pstmt.setInt(9, p.getStatus()); // Kiểu int: 0-Ngừng, 1-Đang bán
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
