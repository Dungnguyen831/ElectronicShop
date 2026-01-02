package com.mycompany.view.warehouse.product;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.dao.SupplierDAO;
import com.mycompany.model.Category;
import com.mycompany.model.Product;
import com.mycompany.model.Supplier;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class ProductDialog extends JDialog {
    private JTextField txtName, txtBarcode, txtImport, txtSale, txtQty, txtImage;
    private JComboBox<Category> cbCategory;
    private JComboBox<Supplier> cbSupplier;
    private JComboBox<String> cbStatus; // Thay đổi từ JCheckBox sang JComboBox cho kiểu int
    
    private JButton btnSave, btnCancel;
    private Product product;
    private boolean isEdit = false;
    private boolean confirmed = false;

    private CategoryDAO catDAO = new CategoryDAO();
    private SupplierDAO supDAO = new SupplierDAO();

    public ProductDialog(Frame parent, Product p) {
        super(parent, true);
        this.product = (p == null) ? new Product() : p;
        this.isEdit = (p != null);
        
        initComponents();
        loadComboBoxData(); 
        
        if (isEdit) {
            fillForm();
        } else {
            // Thiết lập mặc định cho sản phẩm mới
            cbStatus.setSelectedIndex(1); // Mặc định là "Đang bán" (int = 1)
        }
        
        setTitle(isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm mới");
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new GridLayout(11, 2, 10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel(" Tên sản phẩm:")); 
        txtName = new JTextField(20); 
        add(txtName);
        
        add(new JLabel(" Loại sản phẩm:")); 
        cbCategory = new JComboBox<>();
        add(cbCategory);
        
        add(new JLabel(" Nhà cung cấp:")); 
        cbSupplier = new JComboBox<>();
        add(cbSupplier);

        add(new JLabel(" Barcode:")); 
        txtBarcode = new JTextField(); 
        add(txtBarcode);

        add(new JLabel(" Giá nhập:")); 
        txtImport = new JTextField(); 
        add(txtImport);

        add(new JLabel(" Giá bán:")); 
        txtSale = new JTextField(); 
        add(txtSale);

        add(new JLabel(" Số lượng:")); 
        txtQty = new JTextField(); 
        add(txtQty);

        add(new JLabel(" Tên file ảnh:")); 
        txtImage = new JTextField(); 
        add(txtImage);

        add(new JLabel(" Trạng thái:")); 
        // 0: Ngừng bán, 1: Đang bán
        cbStatus = new JComboBox<>(new String[]{"Ngừng bán", "Đang bán"}); 
        add(cbStatus);

        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        
        // Style cho nút
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        
        add(btnSave); 
        add(btnCancel);

        // Sự kiện nút Lưu
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                captureData();
                this.confirmed = true; 
                this.setVisible(false); // CHỈ ẩn đi để lớp gọi xử lý tiếp
            }
        });

        // Sự kiện nút Hủy
        btnCancel.addActionListener(e -> {
            this.confirmed = false;
            this.dispose(); 
        });
    }

    private void loadComboBoxData() {
        cbCategory.removeAllItems();
        cbSupplier.removeAllItems();

        List<Category> cats = catDAO.selectAll();
        for (Category c : cats) cbCategory.addItem(c);

        List<Supplier> sups = supDAO.selectAll();
        for (Supplier s : sups) cbSupplier.addItem(s);
    }

    private void fillForm() {
        txtName.setText(product.getProductName());
        txtBarcode.setText(product.getBarcode());
        txtImport.setText(String.valueOf(product.getImportPrice()));
        txtSale.setText(String.valueOf(product.getSalePrice()));
        txtQty.setText(String.valueOf(product.getQuantity()));
        txtImage.setText(product.getImage());
        
        // Gán trạng thái int vào ComboBox
        cbStatus.setSelectedIndex(product.getStatus());

        // Chọn đúng Item trong ComboBox dựa trên ID
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getCategoryId() == product.getCategoryId()) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
        
        for (int i = 0; i < cbSupplier.getItemCount(); i++) {
            if (cbSupplier.getItemAt(i).getSupplierId() == product.getSupplierId()) {
                cbSupplier.setSelectedIndex(i);
                break;
            }
        }
    }

    private void captureData() {
        product.setProductName(txtName.getText().trim());
        product.setBarcode(txtBarcode.getText().trim());
        product.setImage(txtImage.getText().trim());
        
        Category selectedCat = (Category) cbCategory.getSelectedItem();
        Supplier selectedSup = (Supplier) cbSupplier.getSelectedItem();
        
        if (selectedCat != null) product.setCategoryId(selectedCat.getCategoryId());
        if (selectedSup != null) product.setSupplierId(selectedSup.getSupplierId());

        product.setQuantity(parseIntOrZero(txtQty.getText()));
        product.setImportPrice(parseDoubleOrZero(txtImport.getText()));
        product.setSalePrice(parseDoubleOrZero(txtSale.getText()));
        
        // Lấy giá trị int từ ComboBox (0 hoặc 1)
        product.setStatus(cbStatus.getSelectedIndex());
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!");
            return false;
        }
        if (cbCategory.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Loại sản phẩm!");
            return false;
        }
        if (cbSupplier.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp!");
            return false;
        }
        return true;
    }

    // Getter & Setter hỗ trợ vòng lặp ở Panel
    public Product getProduct() { return product; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
    
    private int parseIntOrZero(String text) {
        try { return (text == null || text.trim().isEmpty()) ? 0 : Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private double parseDoubleOrZero(String text) {
        try { return (text == null || text.trim().isEmpty()) ? 0.0 : Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}