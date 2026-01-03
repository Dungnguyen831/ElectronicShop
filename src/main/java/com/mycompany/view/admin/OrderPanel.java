/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view.admin;

/**
 *
 * @author Administrator
 */
import com.mycompany.dao.OrderDAO;
import com.mycompany.model.Order;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class OrderPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private OrderDAO orderDAO = new OrderDAO();

    // 👉 THÊM: formatter tiền
    private final DecimalFormat df = new DecimalFormat("#,##0");

    public OrderPanel() {
        setLayout(new BorderLayout());

        // Top
        JPanel top = new JPanel();
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnDetail = new JButton("Chi tiết đơn hàng");

        top.add(new JLabel("Tìm kiếm:"));
        top.add(txtSearch);
        top.add(btnSearch);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);
        top.add(btnDetail);
        add(top, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "Nhân viên", "Khách hàng", "Tổng tiền", "Thanh toán", "Trạng thái", "Ngày"}, 0
        );
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        btnSearch.addActionListener(e -> search());
        btnRefresh.addActionListener(e -> loadData());
        btnDelete.addActionListener(e -> cancelOrder());
        btnDetail.addActionListener(e -> showDetail());
    }

    private void loadData() {
        model.setRowCount(0);
        List<Order> list = orderDAO.getAllOrders();
        for (Order o : list) {
            model.addRow(new Object[]{
                o.getOrderId(),
                o.getStaffName(),
                o.getCustomerName(),
                df.format(o.getTotalAmount()), // ✅ SỬA HIỂN THỊ
                o.getPaymentMethod(),
                o.getStatus() == 1 ? "Đã TT" : "Hủy",
                o.getOrderDate()
            });
        }
    }

    private void search() {
        model.setRowCount(0);
        List<Order> list = orderDAO.searchOrders(txtSearch.getText());
        for (Order o : list) {
            model.addRow(new Object[]{
                o.getOrderId(),
                o.getStaffName(),
                o.getCustomerName(),
                df.format(o.getTotalAmount()), // ✅ SỬA HIỂN THỊ
                o.getPaymentMethod(),
                o.getStatus() == 1 ? "Đã TT" : "Hủy",
                o.getOrderDate()
            });
        }
    }

    private void cancelOrder() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        orderDAO.cancelOrder(id);
        loadData();
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        new OrderDetailDialog(id).setVisible(true);
    }
}


