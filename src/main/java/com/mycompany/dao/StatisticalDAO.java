package com.mycompany.dao;

import com.mycompany.database.DatabaseConnection; // Nhớ sửa đúng package connection của bạn
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticalDAO {
    public double[] getRevenueAndProfit(java.util.Date from, java.util.Date to) {
        double[] data = {0, 0, 0};
        Connection conn = DatabaseConnection.getConnection();
        
        // SQL Thần thánh: Tính doanh thu và Tiền vốn trong 1 câu lệnh
        String sql = "SELECT " +
                     "   SUM(o.total_amount) as DoanhThu, " +
                     "   SUM(od.quantity * p.import_price) as TienVon " +
                     "FROM orders o " +
                     "JOIN order_details od ON o.order_id = od.order_id " +
                     "JOIN products p ON od.product_id = p.product_id " +
                     "WHERE o.status = 1 " +
                     "AND o.order_date BETWEEN ? AND ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                data[0] = rs.getDouble("DoanhThu");
                data[1] = rs.getDouble("TienVon");
                data[2] = data[0] - data[1]; // Lợi nhuận = Doanh thu - Vốn
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // 2. Hàm lấy chi tiết doanh thu theo từng ngày (Cho JTable)
    public List<Object[]> getRevenueByDate(java.util.Date from, java.util.Date to) {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        
        // Gom nhóm theo ngày
        String sql = "SELECT " +
                     "   DATE(o.order_date) as Ngay, " +
                     "   COUNT(DISTINCT o.order_id) as SoDon, " +
                     "   SUM(o.total_amount) as DoanhThu " +
                     "FROM orders o " +
                     "WHERE o.status = 1 " +
                     "AND o.order_date BETWEEN ? AND ? " +
                     "GROUP BY DATE(o.order_date) " +
                     "ORDER BY Ngay DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getDate("Ngay"),
                    rs.getInt("SoDon"),
                    rs.getDouble("DoanhThu")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public double getTotalImportCost(java.util.Date from, java.util.Date to) {
        double totalImport = 0;
        java.sql.Connection conn = DatabaseConnection.getConnection();

        // Tính tổng cột total_amount từ bảng imports theo ngày
        String sql = "SELECT SUM(total_amount) FROM imports WHERE import_date BETWEEN ? AND ?";

        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new java.sql.Timestamp(from.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(to.getTime()));
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                totalImport = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalImport;
    }
    
    // Hàm vẽ biểu đồ
    public List<Object[]> getRevenueLast7Days() {
        List<Object[]> list = new ArrayList<>();
        // Query: Lấy ngày và tổng tiền, gom nhóm theo ngày, trong 7 ngày qua
        String sql = "SELECT DATE_FORMAT(order_date, '%d/%m') as Ngay, SUM(total_amount) as TongTien " +
                     "FROM orders " +
                     "WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                     "AND status = 1 " + // Chỉ tính đơn thành công
                     "GROUP BY DATE_FORMAT(order_date, '%d/%m') " +
                     "ORDER BY order_date ASC";
        
        Connection conn = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("Ngay"),
                    rs.getDouble("TongTien")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}