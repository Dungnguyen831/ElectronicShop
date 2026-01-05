package com.mycompany.view.warehouse.category;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.model.Category;
import com.mycompany.util.Style;
import java.awt.*;
import javax.swing.*;

public class CategoryDialog extends JDialog {
    private JTextField txtName;
    private JTextArea txtDesc;
    private Category category;
    private boolean confirmed = false;
    private CategoryDAO catDAO = new CategoryDAO(); // Khởi tạo DAO để check trùng

    public CategoryDialog(Frame parent, Category c) {
        super(parent, true);
        this.category = (c == null) ? new Category() : c;
        
        initComponents();
        
        if (c != null) {
            txtName.setText(c.getCategoryName());
            txtDesc.setText(c.getDescription());
        }
        
        setTitle(c == null ? "Thêm Danh Mục sản phẩm" : "Sửa Danh Mục sản phẩm");
        this.setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Áp dụng Style cho nền Dialog
        getContentPane().setBackground(Style.COLOR_BG_RIGHT);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlForm.setBackground(Style.COLOR_BG_RIGHT);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        pnlForm.add(new JLabel("Tên Danh Mục:"));
        txtName = new JTextField(20);
        pnlForm.add(txtName);

        pnlForm.add(new JLabel("Mô tả:"));
        txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        pnlForm.add(new JScrollPane(txtDesc));

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.setBackground(Style.COLOR_BG_RIGHT);

        JButton btnSave = new JButton("Lưu");
        // Áp dụng Style cho nút Lưu tương tự ProductDialog
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnCancel = new JButton("Hủy");
        
        // Sự kiện nút Lưu với logic validate
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                category.setCategoryName(txtName.getText().trim());
                category.setDescription(txtDesc.getText().trim());
                confirmed = true;
                this.setVisible(false);
            }
        });

        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        pnlBtns.add(btnSave); 
        pnlBtns.add(btnCancel);
        
        add(pnlForm, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private boolean validateForm() {
        String name = txtName.getText().trim();
        
        // 1. Kiểm tra rỗng
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên danh mục không được để trống!");
            return false;
        }

        // 2. Kiểm tra trùng (Logic: Nếu thêm mới hoặc sửa mà đổi tên khác tên cũ)
        boolean isEdit = (category.getCategoryId() > 0);
        if (!isEdit || !name.equalsIgnoreCase(category.getCategoryName())) {
            if (catDAO.isCategoryExists(name)) {
                JOptionPane.showMessageDialog(this, "Tên danh mục '" + name + "' đã tồn tại!");
                return false;
            }
        }
        
        return true;
    }

    // Bổ sung các phương thức để đồng bộ với Panel
    public Category getCategory() { return category; }
    
    public boolean isConfirmed() { return confirmed; }
    
    public void setConfirmed(boolean confirmed) { 
        this.confirmed = confirmed; 
    }
}