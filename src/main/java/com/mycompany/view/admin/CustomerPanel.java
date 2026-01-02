/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view.admin;

import com.mycompany.dao.CustomerDAO;
import com.mycompany.model.Customer;
import com.mycompany.util.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nguyen Anh Dung
 */
public class CustomerPanel extends JPanel {

    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTable table;
    private DefaultTableModel tableModel;
    private CustomerDAO dao;

    public CustomerPanel() {
        this.dao = new CustomerDAO();
        initComponents();
        loadData(dao.getAll()); // Load dữ liệu ban đầu
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Khoảng cách giữa các vùng là 10px
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding 4 phía

        // --- 1. TOOLBAR (Tìm kiếm & Nút bấm) ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTop.setBackground(Color.WHITE);

        // Ô tìm kiếm
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(Style.FONT_BOLD);
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        
        JButton btnSearch = createButton("Tìm", Style.COLOR_PRIMARY);
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText();
            loadData(dao.search(keyword));
        });

        // Các nút chức năng
        btnAdd = createButton("Thêm Mới", Style.COLOR_SUCCESS);
        btnEdit = createButton("Sửa", Style.COLOR_PRIMARY);
        btnDelete = createButton("Xóa", Style.COLOR_DANGER);
        btnRefresh = createButton("Làm mới", Color.GRAY);

        pnlTop.add(lblSearch);
        pnlTop.add(txtSearch);
        pnlTop.add(btnSearch);
        pnlTop.add(Box.createHorizontalStrut(20)); // Khoảng cách
        pnlTop.add(btnAdd);
        pnlTop.add(btnEdit);
        pnlTop.add(btnDelete);
        pnlTop.add(btnRefresh);

        this.add(pnlTop, BorderLayout.NORTH);

        // --- 2. TABLE (Danh sách) ---
        String[] columns = {"ID", "Họ Tên", "SĐT", "Email", "Địa Chỉ", "Điểm Tích Lũy"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override // Không cho phép sửa trực tiếp trên bảng
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(Style.FONT_REGULAR);
        table.getTableHeader().setFont(Style.FONT_BOLD);
        table.getTableHeader().setBackground(Style.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane, BorderLayout.CENTER);

        // --- 3. SỰ KIỆN ---
        btnAdd.addActionListener(e -> showAddDialog());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData(dao.getAll());
        });
    }

    // Hàm load dữ liệu vào bảng
    private void loadData(List<Customer> list) {
        tableModel.setRowCount(0); // Xóa hết dữ liệu cũ
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getPhone(),
                c.getEmail(),
                c.getAddress(),
                c.getPoints()
            });
        }
    }

    // Hàm hiện hộp thoại thêm nhanh (Dùng JOptionPane cho gọn code)
    private void showAddDialog() {
        JTextField txtName = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtAddress = new JTextField();

        Object[] message = {
            "Họ tên:", txtName,
            "Số điện thoại:", txtPhone,
            "Email:", txtEmail,
            "Địa chỉ:", txtAddress
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Khách Hàng", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            // Validate sơ bộ
            if(txtName.getText().isEmpty() || txtPhone.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên và SĐT không được để trống!");
                return;
            }
            
            Customer c = new Customer();
            c.setFullName(txtName.getText());
            c.setPhone(txtPhone.getText());
            c.setEmail(txtEmail.getText());
            c.setAddress(txtAddress.getText());

            if (dao.add(c)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadData(dao.getAll());
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại (Có thể trùng SĐT)!");
            }
        }
    }

    // Hàm xóa
    private void deleteCustomer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!");
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                loadData(dao.getAll());
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa (Khách này đã có đơn hàng)!");
            }
        }
    }

    // Hàm tạo nút nhanh
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(Style.FONT_BOLD);
        return btn;
    }
}
