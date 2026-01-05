package com.mycompany.dao;

import com.mycompany.model.Order;
import java.util.ArrayList;
import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.OrderDetail;
import java.sql.*;
import java.util.List;

public class OrderDAO {
    public boolean createOrder(int userId, int customerId, Integer voucherId, double totalAmount, List<OrderDetail> details, int usedPoints) {
        Connection conn = null;
        //Khai báo các biến preparedStatement để truyền tham số vào câu lệnh
        PreparedStatement psOrder = null;
        PreparedStatement psDetail = null;
        PreparedStatement psStock = null;
        PreparedStatement psVoucher = null;
        PreparedStatement psPoint = null;

        try {
            //Kết nối db
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BẮT ĐẦU TRANSACTION

            // 1. Insert bảng ORDERS
            String sqlOrder = "INSERT INTO orders(user_id, customer_id, voucher_id, total_amount, payment_method) VALUES(?, ?, ?, ?, 'CASH')";
            //Trả về id của đơn hàng 
            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setInt(2, customerId);
            
            if (voucherId != null) psOrder.setInt(3, voucherId);
            else psOrder.setNull(3, Types.INTEGER);
            
            psOrder.setDouble(4, totalAmount);
            psOrder.executeUpdate();

            // Lấy Order ID vừa tạo gán vào biên orderID 
            ResultSet rs = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // 2. Insert ORDER_DETAILS và 3. Trừ Kho PRODUCTS
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

            // 4. Trừ số lượng VOUCHER (Nếu có dùng)
            if (voucherId != null) {
                String sqlVoucher = "UPDATE vouchers SET quantity = quantity - 1 WHERE voucher_id = ?";
                psVoucher = conn.prepareStatement(sqlVoucher);
                psVoucher.setInt(1, voucherId);
                psVoucher.executeUpdate();
            }

            // 5. XỬ LÝ ĐIỂM TÍCH LŨY
            // Tính điểm thưởng mới (100k = 1 điểm)
            int pointsEarned = (int) (totalAmount / 100000);
            
            // Cập nhật điểm cho khách (Trừ điểm dùng + Cộng điểm mới)
            if (usedPoints > 0 || pointsEarned > 0) {
                String sqlPoint = "UPDATE customers SET points = points - ? + ? WHERE customer_id = ?";
                psPoint = conn.prepareStatement(sqlPoint);
                psPoint.setInt(1, usedPoints);   
                psPoint.setInt(2, pointsEarned); 
                psPoint.setInt(3, customerId);
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
    // 1. Lấy danh sách đơn hàng
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.order_id, u.full_name AS staff_name,
                   c.full_name AS customer_name,
                   o.total_amount, o.payment_method,
                   o.status, o.order_date
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.user_id
            LEFT JOIN customers c ON o.customer_id = c.customer_id
            ORDER BY o.order_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setStaffName(rs.getString("staff_name"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setStatus(rs.getInt("status"));
                o.setOrderDate(rs.getTimestamp("order_date"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Tìm kiếm đơn hàng
    public List<Order> searchOrders(String keyword) {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.order_id, u.full_name AS staff_name,
                   c.full_name AS customer_name,
                   o.total_amount, o.payment_method,
                   o.status, o.order_date
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.user_id
            LEFT JOIN customers c ON o.customer_id = c.customer_id
            WHERE o.order_id LIKE ?
               OR u.full_name LIKE ?
               OR c.full_name LIKE ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword + "%";
            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setStaffName(rs.getString("staff_name"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setStatus(rs.getInt("status"));
                o.setOrderDate(rs.getTimestamp("order_date"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. Cập nhật đơn hàng
    public boolean updateOrder(int orderId, String paymentMethod, int status) {
        String sql = "UPDATE orders SET payment_method=?, status=? WHERE order_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, paymentMethod);
            ps.setInt(2, status);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. Hủy đơn hàng
    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE orders SET status = 0 WHERE order_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}