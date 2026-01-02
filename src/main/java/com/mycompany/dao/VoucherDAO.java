package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Voucher;
import java.sql.*;

public class VoucherDAO {

    public Voucher findByCode(String code) {
        Voucher v = null;
        // Kiểm tra: Trạng thái Active (1), Số lượng > 0, Ngày hiện tại nằm trong hạn dùng
        String sql = "SELECT * FROM vouchers WHERE code = ? AND status = 1 AND quantity > 0 AND CURDATE() BETWEEN start_date AND end_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                v = new Voucher();
                v.setVoucherId(rs.getInt("voucher_id"));
                v.setCode(rs.getString("code"));
                v.setDiscountPercent(rs.getInt("discount_percent"));
                v.setMaxDiscount(rs.getDouble("max_discount"));
                v.setQuantity(rs.getInt("quantity"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }
}