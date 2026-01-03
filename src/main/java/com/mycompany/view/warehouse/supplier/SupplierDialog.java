package com.mycompany.view.warehouse.supplier;

import com.mycompany.model.Supplier;
import java.awt.*;
import javax.swing.*;

public class SupplierDialog extends JDialog {
    // Sửa các trường theo đúng cấu trúc database
    private JTextField txtName, txtContact, txtPhone, txtAddress;
    private JCheckBox chkActive; // Thay cho Email bằng trạng thái is_active
    private Supplier supplier;
    private boolean confirmed = false;

    public SupplierDialog(Frame parent, Supplier s) {
        super(parent, true);
        this.supplier = (s == null) ? new Supplier() : s;
        initComponents();
        if (s != null) fillForm();
        setTitle(s == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp");
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Sử dụng GridLayout để căn chỉnh form nhập liệu
        setLayout(new GridLayout(6, 2, 10, 10));

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
        chkActive = new JCheckBox("Hoạt động", true); // Cột is_active
        add(chkActive);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        // Sự kiện Lưu dữ liệu
       // Sự kiện Lưu dữ liệu
        btnSave.addActionListener(e -> {
            // 1. Lấy dữ liệu và trim khoảng trắng
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();

            // 2. Kiểm tra ràng buộc không được để trống
            if (name.isEmpty() || contact.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tất cả các trường thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Ràng buộc số điện thoại: Phải là số, bắt đầu bằng 0 và đúng 10 chữ số
            // Regex: ^0 (bắt đầu bằng 0), \\d{9} (9 chữ số tiếp theo), $ (kết thúc)
            if (!phone.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!\n(Phải bắt đầu bằng số 0 và có đúng 10 chữ số)", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                txtPhone.requestFocus(); // Đưa con trỏ về ô số điện thoại để sửa
                return;
            }

            // 4. Nếu vượt qua các kiểm tra trên, tiến hành gán dữ liệu vào model
            supplier.setSupplierName(name);
            supplier.setContactPerson(contact); 
            supplier.setPhone(phone);
            supplier.setAddress(address);
            supplier.setIsActive(chkActive.isSelected()); 

            confirmed = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());
        
        add(btnSave); 
        add(btnCancel);
    }

    private void fillForm() {
        // Đổ dữ liệu từ object vào các ô nhập khi thực hiện Sửa
        txtName.setText(supplier.getSupplierName());
        txtContact.setText(supplier.getContactPerson()); // Khớp với database
        txtPhone.setText(supplier.getPhone());
        txtAddress.setText(supplier.getAddress());
        chkActive.setSelected(supplier.isIsActive());
    }

    public Supplier getSupplier() { return supplier; }
    public boolean isConfirmed() { return confirmed; }
}