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
        btnSave.addActionListener(e -> {
            // Gán dữ liệu vào model Supplier (Lưu ý: dùng đúng tên method set đã sửa)
            supplier.setSupplierName(txtName.getText().trim());
            supplier.setContactPerson(txtContact.getText().trim()); // Đổi thành contactPerson
            supplier.setPhone(txtPhone.getText().trim());
            supplier.setAddress(txtAddress.getText().trim());
            supplier.setIsActive(chkActive.isSelected()); // Gán giá trị boolean
            
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