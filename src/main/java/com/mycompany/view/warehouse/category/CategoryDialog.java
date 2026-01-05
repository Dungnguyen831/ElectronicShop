package com.mycompany.view.warehouse.category;

import com.mycompany.model.Category;
import java.awt.*;
import javax.swing.*;

public class CategoryDialog extends JDialog {
    private JTextField txtName;
    private JTextArea txtDesc;
    private Category category;
    private boolean confirmed = false;

    public CategoryDialog(Frame parent, Category c) {
        super(parent, true);
        this.category = (c == null) ? new Category() : c;
        initComponents();
        if (c != null) {
            txtName.setText(c.getCategoryName());
            txtDesc.setText(c.getDescription());
        }
        setTitle(c == null ? "Thêm Danh Mục sản phẩm" : "Sửa Danh Mục sản phẩm");
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel pnlForm = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlForm.add(new JLabel("Tên Danh Mục:"));
        txtName = new JTextField(20);
        pnlForm.add(txtName);

        pnlForm.add(new JLabel("Mô tả:"));
        txtDesc = new JTextArea(2, 20);
        pnlForm.add(new JScrollPane(txtDesc));

        JPanel pnlBtns = new JPanel();
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        
        btnSave.addActionListener(e -> {
            category.setCategoryName(txtName.getText().trim());
            category.setDescription(txtDesc.getText().trim());
            confirmed = true;
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
        
        pnlBtns.add(btnSave); pnlBtns.add(btnCancel);
        add(pnlForm, BorderLayout.CENTER);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    public Category getCategory() { return category; }
    public boolean isConfirmed() { return confirmed; }
}