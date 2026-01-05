package com.mycompany.view.warehouse.supplier;

import com.mycompany.dao.SupplierDAO;
import com.mycompany.model.Supplier;
import com.mycompany.util.Style;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SupplierDialog extends JDialog {
    private JTextField txtName, txtContact, txtPhone, txtAddress;
    private JCheckBox chkActive;
    private Supplier supplier;
    private boolean confirmed = false;
    private SupplierDAO supDAO = new SupplierDAO(); // Khởi tạo DAO để kiểm tra trùng

    public SupplierDialog(Frame parent, Supplier s) {
        super(parent, true);
        this.supplier = (s == null) ? new Supplier() : s;
        
        initComponents();
        
        if (s != null) {
            fillForm();
        }
        
        setTitle(s == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
        this.setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Áp dụng Style từ Style.java
        getContentPane().setBackground(Style.COLOR_BG_RIGHT);
        setLayout(new GridLayout(6, 2, 15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        add(new JLabel(" Tên NCC:")); 
        txtName = new JTextField(20); 
        add(txtName);

        add(new JLabel(" Người liên hệ:")); 
        txtContact = new JTextField(); 
        add(txtContact);

        add(new JLabel(" Số điện thoại:")); 
        txtPhone = new JTextField(); 
        add(txtPhone);

        add(new JLabel(" Địa chỉ:")); 
        txtAddress = new JTextField(); 
        add(txtAddress);

        add(new JLabel(" Trạng thái:")); 
        chkActive = new JCheckBox("Hoạt động", true);
        chkActive.setBackground(Style.COLOR_BG_RIGHT);
        add(chkActive);

        JButton btnSave = new JButton("Lưu");
        // Áp dụng màu sắc từ Style.java cho nút Lưu
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnCancel = new JButton("Hủy");

        // Sự kiện Lưu dữ liệu với kiểm tra trùng lặp
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                captureData();
                confirmed = true;
                this.setVisible(false);
            }
        });

        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        add(btnSave); 
        add(btnCancel);
    }

    private boolean validateForm() {
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        // 1. Kiểm tra rỗng
        if (name.isEmpty() || contact.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. Kiểm tra trùng tên NCC (Logic: Thêm mới hoặc Sửa mà đổi tên khác tên cũ)
        boolean isEdit = (supplier.getSupplierId() > 0);
        if (!isEdit || !name.equalsIgnoreCase(supplier.getSupplierName())) {
            if (supDAO.isSupplierExists(name)) { // Gọi hàm check trùng từ DAO
                JOptionPane.showMessageDialog(this, "Nhà cung cấp '" + name + "' đã tồn tại!", "Cảnh báo trùng", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        // 3. Ràng buộc số điện thoại
        if (!phone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void captureData() {
        supplier.setSupplierName(txtName.getText().trim());
        supplier.setContactPerson(txtContact.getText().trim()); 
        supplier.setPhone(txtPhone.getText().trim());
        supplier.setAddress(txtAddress.getText().trim());
        supplier.setIsActive(chkActive.isSelected()); 
    }

    private void fillForm() {
        txtName.setText(supplier.getSupplierName());
        txtContact.setText(supplier.getContactPerson());
        txtPhone.setText(supplier.getPhone());
        txtAddress.setText(supplier.getAddress());
        chkActive.setSelected(supplier.isIsActive());
    }

    // Các phương thức getter/setter để đồng bộ với Panel
    public Supplier getSupplier() { return supplier; }
    
    public boolean isConfirmed() { return confirmed; }
    
    public void setConfirmed(boolean confirmed) { 
        this.confirmed = confirmed; 
    }
}