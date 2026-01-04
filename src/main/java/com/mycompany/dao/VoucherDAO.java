package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Voucher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    // 1. Lấy tất cả (Dùng để hiển thị lên bảng)
    public List<Voucher> getAll() {
        List<Voucher> list = new ArrayList<>();
        String sql = "SELECT * FROM vouchers ORDER BY voucher_id DESC"; // Mới nhất lên đầu
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm mới
    public boolean add(Voucher v) {
        String sql = "INSERT INTO vouchers (code, discount_percent, max_discount, start_date, end_date, quantity, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCode());
            ps.setInt(2, v.getDiscountPercent());
            ps.setDouble(3, v.getMaxDiscount());
            ps.setDate(4, v.getStartDate());
            ps.setDate(5, v.getEndDate());
            ps.setInt(6, v.getQuantity());
            ps.setInt(7, v.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 3. Sửa
    public boolean update(Voucher v) {
        String sql = "UPDATE vouchers SET code=?, discount_percent=?, max_discount=?, start_date=?, end_date=?, quantity=?, status=? WHERE voucher_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCode());
            ps.setInt(2, v.getDiscountPercent());
            ps.setDouble(3, v.getMaxDiscount());
            ps.setDate(4, v.getStartDate());
            ps.setDate(5, v.getEndDate());
            ps.setInt(6, v.getQuantity());
            ps.setInt(7, v.getStatus());
            ps.setInt(8, v.getVoucherId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa
    public boolean delete(int id) {
        String sql = "DELETE FROM vouchers WHERE voucher_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 5. Tìm kiếm theo Mã (Cho ô tìm kiếm Admin)
    public List<Voucher> search(String keyword) {
        List<Voucher> list = new ArrayList<>();
String sql = "SELECT * FROM vouchers WHERE code LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 6. Tìm chính xác (Cho màn hình Bán hàng - Logic cũ của bạn)
    public Voucher findByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE code = ? AND status = 1 AND quantity > 0 AND CURDATE() BETWEEN start_date AND end_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Hàm phụ trợ map dữ liệu
    private Voucher mapRow(ResultSet rs) throws SQLException {
        return new Voucher(
            rs.getInt("voucher_id"),
            rs.getString("code"),
            rs.getInt("discount_percent"),
            rs.getDouble("max_discount"),
            rs.getDate("start_date"),
            rs.getDate("end_date"),
            rs.getInt("quantity"),
            rs.getInt("status")
        );
    }
    
    public boolean checkVoucherUsed(int customerId, int voucherId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE customer_id = ? AND voucher_id = ? AND status = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, voucherId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Trả về true nếu đã sử dụng
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}