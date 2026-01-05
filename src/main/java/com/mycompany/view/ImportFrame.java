package com.mycompany.view;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.view.warehouse.product.ProductPanel;
import com.mycompany.database.DatabaseConnection; // Thêm để kết nối DB lấy tên NCC
import java.awt.*;
import java.awt.event.*;
import java.sql.*; // QUAN TRỌNG: Thêm để xóa lỗi gạch đỏ SQL
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ImportFrame extends JFrame {
    private JTextField txtProductId, txtName, txtQuantity, txtPrice, txtSupplier; // Thêm txtSupplier
    private JButton btnAdd, btnConfirm, btnDelete, btnEdit;
    private JTable tblImport;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    private List<ImportDetail> listDetails = new ArrayList<>();
    private ImportDAO importDao = new ImportDAO();
    private ProductDAO productDao = new ProductDAO();
    private ProductPanel productPanel; 
    private DecimalFormat formatter = new DecimalFormat("#,###");

    public ImportFrame(ProductPanel panel) {
        this.productPanel = panel;
        initComponents();
        initEvents();
        setTitle("HỆ THỐNG NHẬP KHO");
        setSize(1150, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        btnConfirm.setEnabled(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        getContentPane().setBackground(new Color(245, 245, 250));

        // --- 1. PANEL NHẬP LIỆU ---
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(" Thông Tin Hàng Hóa ");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlInput.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 15, 10, 15);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pnlInput.add(new JLabel("Mã Sản Phẩm:"), g);
        g.gridx = 1; g.weightx = 0.5;
        txtProductId = new JTextField();
        txtProductId.setPreferredSize(new Dimension(0, 35));
        pnlInput.add(txtProductId, g);

        g.gridx = 2; g.gridy = 0; g.weightx = 0;
        pnlInput.add(new JLabel("Tên Sản Phẩm:"), g);
        g.gridx = 3; g.weightx = 0.5;
        txtName = new JTextField();
        txtName.setEditable(false);
        txtName.setBackground(new Color(240, 240, 240));
        txtName.setPreferredSize(new Dimension(0, 35));
        pnlInput.add(txtName, g);

        g.gridx = 0; g.gridy = 1;
        pnlInput.add(new JLabel("Số Lượng:"), g);
        g.gridx = 1;
        txtQuantity = new JTextField();
        txtQuantity.setPreferredSize(new Dimension(0, 35));
        pnlInput.add(txtQuantity, g);

        g.gridx = 2; g.gridy = 1;
        pnlInput.add(new JLabel("Giá Nhập:"), g);
        g.gridx = 3;
        txtPrice = new JTextField();
        txtPrice.setPreferredSize(new Dimension(0, 35));
        pnlInput.add(txtPrice, g);

        // THÊM Ô NHÀ CUNG CẤP VÀO GIAO DIỆN
        g.gridx = 0; g.gridy = 2;
        pnlInput.add(new JLabel("Nhà Cung Cấp:"), g);
        g.gridx = 1; g.gridwidth = 3;
        txtSupplier = new JTextField();
        txtSupplier.setEditable(false);
        txtSupplier.setBackground(new Color(240, 240, 240));
        txtSupplier.setPreferredSize(new Dimension(0, 35));
        pnlInput.add(txtSupplier, g);

        add(pnlInput, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        tableModel = new DefaultTableModel(new String[]{"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Giá Nhập", "Thành Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblImport = new JTable(tableModel);
        tblImport.setRowHeight(35);
        add(new JScrollPane(tblImport), BorderLayout.CENTER);

        // --- 3. SOUTH PANEL ---
        JPanel pnlSouth = new JPanel(new BorderLayout(10, 10));
        pnlSouth.setOpaque(false);

        lblTotal = new JLabel("TỔNG TIỀN: 0 VNĐ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(new Color(211, 47, 47));
        pnlSouth.add(lblTotal, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        btnAdd = createStyledButton("Thêm Mới", new Color(46, 125, 50));
        btnEdit = createStyledButton("Cập Nhật", new Color(255, 160, 0));
        btnDelete = createStyledButton("Xóa Dòng", new Color(117, 117, 117));
        btnConfirm = createStyledButton("XÁC NHẬN NHẬP KHO", new Color(25, 118, 210));
        btnConfirm.setPreferredSize(new Dimension(220, 50));

        pnlButtons.add(btnAdd);
        pnlButtons.add(btnEdit);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnConfirm);

        pnlSouth.add(pnlButtons, BorderLayout.EAST);
        add(pnlSouth, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void initEvents() {
        txtProductId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { lookupProduct(txtProductId.getText()); }
        });

        btnAdd.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtProductId.getText().trim());
                String name = txtName.getText();
                int qty = Integer.parseInt(txtQuantity.getText().trim());
                double price = Double.parseDouble(txtPrice.getText().trim());
                
                if(name.isEmpty() || name.equals("Sản phẩm không tồn tại!")) {
                    JOptionPane.showMessageDialog(this, "Mã sản phẩm không hợp lệ!");
                    return;
                }

                tableModel.addRow(new Object[]{id, name, qty, formatter.format(price), formatter.format(qty * price)});
                listDetails.add(new ImportDetail(id, qty, price));
                updateTotal();
                clearInput();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số lượng và Giá phải là số!");
            }
        });

        btnConfirm.addActionListener(e -> {
            if (listDetails.isEmpty()) return;

            int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận nhập kho phiếu này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            Import imp = new Import();
            // Đã lược bỏ imp.setUserId() để tránh lỗi DB như bạn yêu cầu
            
            try {
                Product p = productDao.getProductById(listDetails.get(0).getProductId());
                imp.setSupplierId(p != null ? p.getSupplierId() : 1);
            } catch(Exception ex) { imp.setSupplierId(1); }

            double total = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
            imp.setTotalAmount(total);
            
            if (importDao.saveImportOrder(imp, listDetails)) {
                JOptionPane.showMessageDialog(this, "NHẬP KHO THÀNH CÔNG!");
                resetAll();
                if (productPanel != null) productPanel.fillTable();
                
                // Đóng cửa sổ sau khi thành công
                this.setVisible(false);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi Database! Vui lòng kiểm tra Console.");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = tblImport.getSelectedRow();
            if (row != -1) {
                listDetails.remove(row);
                tableModel.removeRow(row);
                updateTotal();
            }
        });
    }

    private void lookupProduct(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            Product p = productDao.getProductById(Integer.parseInt(idStr.trim()));
            if (p != null) {
                txtName.setText(p.getProductName());
                txtPrice.setText(String.valueOf((int)p.getImportPrice()));
                // Đổ đúng tên nhà cung cấp từ database của bạn
                txtSupplier.setText(getSupplierNameFromDB(p.getSupplierId()));
            } else {
                txtName.setText("Sản phẩm không tồn tại!");
                txtSupplier.setText("N/A");
            }
        } catch (Exception e) {}
    }

    // HÀM LẤY TÊN NHÀ CUNG CẤP TRỰC TIẾP
    private String getSupplierNameFromDB(int id) {
        String name = "N/A";
        String query = "SELECT supplier_name FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) name = rs.getString("supplier_name");
        } catch (Exception e) { e.printStackTrace(); }
        return name;
    }

    private void updateTotal() {
        double total = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
        lblTotal.setText("TỔNG TIỀN: " + formatter.format(total) + " VNĐ");
        btnConfirm.setEnabled(!listDetails.isEmpty());
    }

    private void clearInput() {
        txtProductId.setText(""); txtName.setText(""); txtQuantity.setText(""); 
        txtPrice.setText(""); txtSupplier.setText("");
        tblImport.clearSelection();
    }
    private void resetAll() {
    listDetails.clear();           // Xóa danh sách chi tiết trong bộ nhớ
    tableModel.setRowCount(0);     // Xóa toàn bộ các dòng trên bảng giao diện
    updateTotal();                 // Đưa tổng tiền về 0 VNĐ
    clearInput();                  // Xóa trắng các ô nhập (Mã SP, Tên, Số lượng...)
    }
}