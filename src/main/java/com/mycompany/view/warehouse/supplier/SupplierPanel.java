package com.mycompany.view.warehouse.supplier;

import com.mycompany.dao.SupplierDAO;
import com.mycompany.model.Supplier;
import com.mycompany.util.Style;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SupplierPanel extends JPanel {
    private JTable tblSuppliers;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private SupplierDAO dao = new SupplierDAO();

    public SupplierPanel() {
        initComponents();
        fillTable(); // Tự động load dữ liệu khi mở tab
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Style.COLOR_BG_RIGHT); // Áp dụng Style chuẩn

        // --- TOP: Tìm kiếm ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Tìm kiếm");
        
        pnlTop.add(new JLabel("Tên NCC:"));
        pnlTop.add(txtSearch);
        pnlTop.add(btnSearch);
        add(pnlTop, BorderLayout.NORTH);

        // --- CENTER: Bảng hiển thị (Khớp 6 cột trong DB) ---
        String[] headers = {"ID", "Tên NCC", "Người liên hệ", "SĐT", "Địa chỉ", "Trạng thái"};
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
       
        tblSuppliers = new JTable(model);
        tblSuppliers.setRowHeight(30);
        add(new JScrollPane(tblSuppliers), BorderLayout.CENTER);

        // --- SOUTH: Nút chức năng cao 45px ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = createBtn("Thêm");
        JButton btnEdit = createBtn("Sửa");
        JButton btnDelete = createBtn("Xóa");
        
        // SỬA LỖI: creatBtn -> createBtn (Thiếu chữ 'e')
        JButton btnRefresh = createBtn("Làm mới"); 

        // --- XỬ LÝ SỰ KIỆN ---

        // Thêm mới
        btnAdd.addActionListener(e -> {
            SupplierDialog dialog = new SupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                if (dao.insert(dialog.getSupplier())) {
                    fillTable();
                    JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại! Kiểm tra lại dữ liệu.");
                }
            }
        });

        // Sửa nhà cung cấp
        btnEdit.addActionListener(e -> {
            int row = tblSuppliers.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblSuppliers.getValueAt(row, 0);
                Supplier s = dao.selectAll().stream()
                                .filter(x -> x.getSupplierId() == id)
                                .findFirst().orElse(null);
                
                if (s != null) {
                    SupplierDialog dialog = new SupplierDialog((Frame) SwingUtilities.getWindowAncestor(this), s);
                    dialog.setVisible(true);
                    if (dialog.isConfirmed()) {
                        if (dao.update(dialog.getSupplier())) {
                            fillTable();
                            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                        } else {
                            // Khắc phục thông báo khi cập nhật thất bại
                            JOptionPane.showMessageDialog(this, "Cập nhật THẤT BẠI! Vui lòng kiểm tra dữ liệu."); 
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp để sửa!");
            }
        });

        // Xóa nhà cung cấp
        btnDelete.addActionListener(e -> {
            int row = tblSuppliers.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblSuppliers.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa NCC ID: " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.delete(id);
                    fillTable();
                }
            }
        });

        // Làm mới & Tìm kiếm
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            fillTable();
        });

        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            fillDataToModel(dao.selectByKeyword(keyword));
        });

        // Thêm các nút vào panel theo thứ tự logic
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnEdit);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnRefresh);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // Hàm tạo nút chuẩn hóa chiều cao 45px
    private JButton createBtn(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 45));
        return btn;
    }

    public void fillTable() {
        fillDataToModel(dao.selectAll());
    }

    private void fillDataToModel(List<Supplier> list) {
        model.setRowCount(0); 
        if (list != null) {
            for (Supplier s : list) {
                model.addRow(new Object[]{
                    s.getSupplierId(),
                    s.getSupplierName(),
                    s.getContactPerson(), 
                    s.getPhone(),
                    s.getAddress(),
                    s.isIsActive() ? "Hoạt động" : "Ngừng"
                });
            }
        }
    }
}