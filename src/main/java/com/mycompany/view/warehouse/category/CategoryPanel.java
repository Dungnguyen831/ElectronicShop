package com.mycompany.view.warehouse.category;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.model.Category;
import com.mycompany.util.Style;
import com.mycompany.view.warehouse.category.CategoryDialog;
import com.mycompany.view.warehouse.category.ProductListDialog;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CategoryPanel extends JPanel {
    private JTable tblCategories;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private CategoryDAO dao = new CategoryDAO();

    public CategoryPanel() {
        initComponents();
        fillTable(); // Tự động load dữ liệu
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Style.COLOR_BG_RIGHT);

        // --- TOP: Tìm kiếm (Giống SupplierPanel) ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Tìm kiếm");
        
        pnlTop.add(new JLabel("Tên Danh Mục:"));
        pnlTop.add(txtSearch);
        pnlTop.add(btnSearch);
        add(pnlTop, BorderLayout.NORTH);

        // --- CENTER: Bảng hiển thị (Khớp database) ---
        String[] headers = {"ID", "Tên Danh Mục", "Mô tả"};
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCategories = new JTable(model);
        tblCategories.setRowHeight(30);
        add(new JScrollPane(tblCategories), BorderLayout.CENTER);

        // --- SOUTH: Cụm nút chức năng cao 45px ---
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = createBtn("Thêm");
        JButton btnEdit = createBtn("Sửa");
        JButton btnDelete = createBtn("Xóa");
        JButton btnRefresh = createBtn("Làm mới");

        // Sự kiện Tìm kiếm
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            fillDataToModel(dao.selectByKeyword(keyword));
        });

        // Sự kiện Làm mới
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            fillTable();
        });

        // Sự kiện Thêm
        btnAdd.addActionListener(e -> {
            CategoryDialog d = new CategoryDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
            d.setVisible(true);
            if (d.isConfirmed() && dao.insert(d.getCategory())) {
                fillTable();
                JOptionPane.showMessageDialog(this, "Thêm Danh Mục sản phẩm thành công!");
            }
        });

        // Sự kiện Sửa
        btnEdit.addActionListener(e -> {
            int row = tblCategories.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblCategories.getValueAt(row, 0);
                Category cat = dao.selectAll().stream()
                                  .filter(x -> x.getCategoryId() == id)
                                  .findFirst().orElse(null);
                
                CategoryDialog d = new CategoryDialog((Frame) SwingUtilities.getWindowAncestor(this), cat);
                d.setVisible(true);
                if (d.isConfirmed() && dao.update(d.getCategory())) {
                    fillTable();
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một Danh Mục để sửa!");
            }
        });

        // Sự kiện Xóa
        btnDelete.addActionListener(e -> {
            int row = tblCategories.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblCategories.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa Danh Mục ID: " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dao.delete(id);
                    fillTable();
                }
            }
        });

        // Thêm sự kiện Double Click để xem danh sách sản phẩm thuộc Danh mục này
        tblCategories.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tblCategories.getSelectedRow();
                    if (row >= 0) {
                        Category c = new Category();
                        c.setCategoryId((int) tblCategories.getValueAt(row, 0));
                        c.setCategoryName((String) tblCategories.getValueAt(row, 1));
                        new ProductListDialog((Frame) SwingUtilities.getWindowAncestor(CategoryPanel.this), c).setVisible(true);
                    }
                }
            }
        });

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnEdit);
        pnlBtns.add(btnDelete);
        pnlBtns.add(btnRefresh);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private JButton createBtn(String t) {
        JButton b = new JButton(t);
        b.setPreferredSize(new Dimension(120, 45)); // Đồng bộ chiều cao 45px
        return b;
    }

    public void fillTable() {
        fillDataToModel(dao.selectAll());
    }

    private void fillDataToModel(List<Category> list) {
        model.setRowCount(0);
        if (list != null) {
            for (Category c : list) {
                model.addRow(new Object[]{
                    c.getCategoryId(), 
                    c.getCategoryName(), 
                    c.getDescription()
                });
            }
        }
    }
}