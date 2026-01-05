package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection;
import com.mycompany.model.Order; // Đảm bảo bạn đã có Model Order
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HomeDAO {

    private Connection conn = DatabaseConnection.getConnection();

    // 1. Lấy tổng doanh thu hôm nay
    public double getTodayRevenue() {
        double revenue = 0;
        // CURDATE() lấy ngày hiện tại của hệ thống Database
        String sql = "SELECT SUM(total_amount) FROM orders WHERE DATE(order_date) = CURDATE() AND status = 1"; 
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                revenue = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenue;
    }

    // 2. Lấy số đơn hàng hôm nay
    public int getTodayOrderCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURDATE()";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // 3. Lấy tổng số khách hàng
    public int getTotalCustomers() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM customers";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // 4. Lấy số lượng sản phẩm sắp hết hàng (Ví dụ: Tồn kho < 10)
    public int getLowStockCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM products WHERE quantity < 10 AND status = 1";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // 5. Lấy danh sách 5 đơn hàng mới nhất
    public List<Order> getRecentOrders() {
        List<Order> list = new ArrayList<>();
        // JOIN bảng user và customer để lấy tên hiển thị
        String sql = "SELECT o.*, u.full_name as staff_name, c.full_name as customer_name " +
                     "FROM orders o " +
                     "LEFT JOIN users u ON o.user_id = u.user_id " +
                     "LEFT JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_date DESC LIMIT 5"; // Chỉ lấy 5 đơn mới nhất
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setOrderDate(rs.getTimestamp("order_date"));
                o.setStatus(rs.getInt("status"));
                // Các trường phụ
                o.setStaffName(rs.getString("staff_name"));
                o.setCustomerName(rs.getString("customer_name"));
                
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Object[]> getTopSellingProducts() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT p.product_name, SUM(od.quantity) as sold_qty " +
                     "FROM order_details od " +
                     "JOIN orders o ON od.order_id = o.order_id " +
                     "JOIN products p ON od.product_id = p.product_id " +
                     "WHERE MONTH(o.order_date) = MONTH(CURRENT_DATE()) " +
                     "AND YEAR(o.order_date) = YEAR(CURRENT_DATE()) " +
                     "AND o.status = 1 " + // Chỉ tính đơn thành công
                     "GROUP BY p.product_id, p.product_name " +
                     "ORDER BY sold_qty DESC LIMIT 5";
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("product_name"),
                    rs.getInt("sold_qty")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Object[]> getTopEmployees() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT u.full_name, COUNT(o.order_id) as total_orders, SUM(o.total_amount) as total_sales " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE MONTH(o.order_date) = MONTH(CURRENT_DATE()) " +
                     "AND YEAR(o.order_date) = YEAR(CURRENT_DATE()) " +
                     "AND o.status = 1 " +
                     "GROUP BY u.user_id, u.full_name " +
                     "ORDER BY total_orders DESC LIMIT 5";
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("full_name"),
                    rs.getInt("total_orders"),
                    rs.getDouble("total_sales")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}