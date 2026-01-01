package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.OrderDetail;
import java.sql.*;
import java.util.List;

public class OrderDAO {

    public boolean createOrder(int userId, int customerId, Integer voucherId, double totalAmount, List<OrderDetail> details) {
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        PreparedStatement psStock = null;
        PreparedStatement psVoucher = null;
        PreparedStatement psPoint = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // 1. Insert ORDER
            String sqlOrder = "INSERT INTO orders(user_id, customer_id, voucher_id, total_amount, payment_method) VALUES(?, ?, ?, ?, 'CASH')";
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setInt(2, customerId);
            
            if (voucherId != null) psOrder.setInt(3, voucherId);
            else psOrder.setNull(3, Types.INTEGER);
            
            psOrder.setDouble(4, totalAmount);
            psOrder.executeUpdate();

            ResultSet rs = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // 2. Insert DETAIL & 3. Trừ KHO
            String sqlDetail = "INSERT INTO order_details(order_id, product_id, quantity, unit_price) VALUES(?, ?, ?, ?)";
            String sqlStock = "UPDATE products SET quantity = quantity - ? WHERE product_id = ?";
            
            psDetail = conn.prepareStatement(sqlDetail);
            psStock = conn.prepareStatement(sqlStock);

            for (OrderDetail item : details) {
                psDetail.setInt(1, orderId);
                psDetail.setInt(2, item.getProductId());
                psDetail.setInt(3, item.getQuantity());
                psDetail.setDouble(4, item.getUnitPrice());
                psDetail.addBatch();

                psStock.setInt(1, item.getQuantity());
                psStock.setInt(2, item.getProductId());
                psStock.addBatch();
            }
            psDetail.executeBatch();
            psStock.executeBatch();

            // 4. Trừ VOUCHER
            if (voucherId != null) {
                String sqlVoucher = "UPDATE vouchers SET quantity = quantity - 1 WHERE voucher_id = ?";
                psVoucher = conn.prepareStatement(sqlVoucher);
                psVoucher.setInt(1, voucherId);
                psVoucher.executeUpdate();
            }

            // 5. Cộng ĐIỂM
            int pointsEarned = (int) (totalAmount / 100000);
            if (pointsEarned > 0) {
                String sqlPoint = "UPDATE customers SET points = points + ? WHERE customer_id = ?";
                psPoint = conn.prepareStatement(sqlPoint);
                psPoint.setInt(1, pointsEarned);
                psPoint.setInt(2, customerId);
                psPoint.executeUpdate();
            }

            conn.commit(); 
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
    }
}