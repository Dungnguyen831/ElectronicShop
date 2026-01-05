package com.mycompany.view.warehouse.Import;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.view.warehouse.product.ProductPanel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ImportFrame extends JFrame {
    private JTextField txtProductId, txtName, txtQuantity, txtPrice;
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
        
        // Vô hiệu hóa nút xác nhận khi bảng trống
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

        add(pnlInput, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        tableModel = new DefaultTableModel(new String[]{"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Giá Nhập", "Thành Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblImport = new JTable(tableModel);
        tblImport.setRowHeight(35);
        tblImport.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblImport.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(tblImport);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

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
        pnlButtons.add(Box.createRigidArea(new Dimension(15, 0)));
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void initEvents() {
        // Tự động tìm tên sản phẩm khi gõ mã
        txtProductId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { lookupProduct(txtProductId.getText()); }
        });

        // 1. THÊM MỚI VÀO BẢNG TẠM
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

        // 2. CẬP NHẬT DÒNG ĐANG CHỌN (Sửa lỗi không cập nhật)
        btnEdit.addActionListener(e -> {
            int row = tblImport.getSelectedRow();
            if (row != -1) {
                try {
                    int qty = Integer.parseInt(txtQuantity.getText().trim());
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    
                    // Cập nhật vào danh sách chờ
                    listDetails.get(row).setQuantity(qty);
                    listDetails.get(row).setInputPrice(price);
                    
                    // Cập nhật lên giao diện bảng
                    tableModel.setValueAt(qty, row, 2);
                    tableModel.setValueAt(formatter.format(price), row, 3);
                    tableModel.setValueAt(formatter.format(qty * price), row, 4);
                    
                    updateTotal();
                    clearInput();
                    JOptionPane.showMessageDialog(this, "Đã cập nhật dòng chọn!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu sửa không hợp lệ!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hãy chọn một dòng để sửa!");
            }
        });

        // 3. XÓA DÒNG
        btnDelete.addActionListener(e -> {
            int row = tblImport.getSelectedRow();
            if (row != -1) {
                listDetails.remove(row);
                tableModel.removeRow(row);
                updateTotal();
                clearInput();
            }
        });

        // 4. CLICK BẢNG ĐỔ LÊN Ô NHẬP
        tblImport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblImport.getSelectedRow();
                if (row != -1) {
                    txtProductId.setText(tableModel.getValueAt(row, 0).toString());
txtName.setText(tableModel.getValueAt(row, 1).toString());
                    txtQuantity.setText(tableModel.getValueAt(row, 2).toString());
                    String priceStr = tableModel.getValueAt(row, 3).toString().replace(",", "");
                    txtPrice.setText(priceStr);
                }
            }
        });

        // 5. XÁC NHẬN LƯU VÀO DATABASE
        btnConfirm.addActionListener(e -> {
            if (listDetails.isEmpty()) return;

            int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận nhập kho phiếu này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            Import imp = new Import();
            imp.setUserId(1); // Mặc định Admin ID = 1
            double total = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
            imp.setTotalAmount(total);
            
            try {
                Product p = productDao.getProductById(listDetails.get(0).getProductId());
                imp.setSupplierId(p != null ? p.getSupplierId() : 1);
            } catch(Exception ex) { imp.setSupplierId(1); }

            if (importDao.saveImportOrder(imp, listDetails)) {
                JOptionPane.showMessageDialog(this, "NHẬP KHO THÀNH CÔNG!");
                if (productPanel != null) productPanel.fillTable(); // Load lại bảng SP ở màn hình chính
                resetAll();
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu vào Database! Vui lòng kiểm tra Console.");
            }
        });
    }

    private void lookupProduct(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            Product p = productDao.getProductById(Integer.parseInt(idStr.trim()));
            if (p != null) {
                txtName.setText(p.getProductName());
                txtPrice.setText(String.valueOf(p.getImportPrice()));
            } else {
                txtName.setText("Sản phẩm không tồn tại!");
            }
        } catch (Exception e) {}
    }

    private void updateTotal() {
        double total = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
        lblTotal.setText("TỔNG TIỀN: " + formatter.format(total) + " VNĐ");
        btnConfirm.setEnabled(!listDetails.isEmpty());
    }

    private void clearInput() {
        txtProductId.setText(""); txtName.setText(""); txtQuantity.setText(""); txtPrice.setText("");
        tblImport.clearSelection();
    }

    private void resetAll() {
        listDetails.clear();
        tableModel.setRowCount(0);
        updateTotal();
    }
}