package com.mycompany.view.warehouse.product;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.dao.SupplierDAO;
import com.mycompany.model.Category;
import com.mycompany.model.Product;
import com.mycompany.model.Supplier;
import com.mycompany.util.Style;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import javax.swing.*;

public class ProductDialog extends JDialog {

    private JTextField txtName, txtBarcode, txtImport, txtSale, txtQty, txtImage;
    private JComboBox<Category> cbCategory;
    private JComboBox<Supplier> cbSupplier;
    private JComboBox<String> cbStatus;
    private JButton btnBrowse, btnSave, btnCancel;
    
    private Product product;
    private boolean isEdit = false;
    private boolean confirmed = false;

    private CategoryDAO catDAO = new CategoryDAO();
    private SupplierDAO supDAO = new SupplierDAO();

    // Đường dẫn lưu file ảnh
    private final String UPLOAD_DIR = "D:\\Desktop\\java\\ElectronicShop\\src\\main\\java\\com\\mycompany\\util\\upload";

    public ProductDialog(Frame parent, Product p) {
        super(parent, true);
        this.product = (p == null) ? new Product() : p;
        this.isEdit = (p != null);

        initComponents();
        loadComboBoxData();

        if (isEdit) {
            fillForm();
        } else {
            cbStatus.setSelectedIndex(0); // Mặc định Đang bán
            txtImport.setText("0");
            txtQty.setText("0");
        }

        setTitle(isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm mới");
        this.setResizable(false);
       
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Sử dụng Style từ file Style.java của bạn
        getContentPane().setBackground(Style.COLOR_BG_RIGHT);
        setLayout(new GridLayout(11, 2, 10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tên sản phẩm
        add(new JLabel(" Tên sản phẩm:"));
        txtName = new JTextField();
        add(txtName);

        // Danh mục
        add(new JLabel(" Danh mục sản phẩm:"));
        cbCategory = new JComboBox<>();
        add(cbCategory);

        // Nhà cung cấp
        add(new JLabel(" Nhà cung cấp:"));
        cbSupplier = new JComboBox<>();
        add(cbSupplier);

        // Barcode
        add(new JLabel(" Barcode:"));
        txtBarcode = new JTextField();
        add(txtBarcode);

        // Giá nhập (KHÔNG CHO SỬA)
        add(new JLabel(" Giá nhập:"));
        txtImport = new JTextField();
        txtImport.setEditable(false);
        txtImport.setBackground(new Color(230, 230, 230));
        add(txtImport);

        // Giá bán
        add(new JLabel(" Giá bán:"));
        txtSale = new JTextField();
        add(txtSale);

        // Số lượng (KHÔNG CHO SỬA)
        add(new JLabel(" Số lượng:"));
        txtQty = new JTextField();
        txtQty.setEditable(false);
        txtQty.setBackground(new Color(230, 230, 230));
        add(txtQty);

        // Hình ảnh (Chọn File)
        add(new JLabel(" Hình ảnh:"));
        JPanel pnlImage = new JPanel(new BorderLayout(5, 0));
        txtImage = new JTextField();
        txtImage.setEditable(false);
        // Thiết lập chiều rộng cố định (ví dụ 150 pixel), chiều cao để tự động
        txtImage.setPreferredSize(new Dimension(150, 25));
        btnBrowse = new JButton("Chọn ảnh");
        btnBrowse.addActionListener(e -> chooseImage());
        pnlImage.add(txtImage, BorderLayout.CENTER);
        pnlImage.add(btnBrowse, BorderLayout.EAST);
        add(pnlImage);

        // Trạng thái
        add(new JLabel(" Trạng thái:"));
        cbStatus = new JComboBox<>(new String[]{ "Ngừng bán","Đang bán"});
        add(cbStatus);

        // Nút bấm
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        // Áp dụng Style cho nút Lưu
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(btnSave);
        add(btnCancel);

        // Sự kiện
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                captureData();
                this.confirmed = true;
                this.setVisible(false);
            }
        });

        btnCancel.addActionListener(e -> {
            this.confirmed = false;
            this.dispose();
        });
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh", "jpg", "png", "gif", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtImage.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private String saveImage(File sourceFile) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = sourceFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            String newFileName = UUID.randomUUID().toString() + extension;

            File destFile = new File(dir, newFileName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void captureData() {
        product.setProductName(txtName.getText().trim());
        product.setBarcode(txtBarcode.getText().trim());
        
        // Xử lý ảnh: Nếu txtImage chứa đường dẫn tuyệt đối thì copy
        String path = txtImage.getText().trim();
        File file = new File(path);
        if (file.isAbsolute() && file.exists()) {
            String newName = saveImage(file);
            product.setImage(newName);
        } else {
            product.setImage(path); // Giữ tên cũ nếu đang sửa
        }

        Category cat = (Category) cbCategory.getSelectedItem();
        Supplier sup = (Supplier) cbSupplier.getSelectedItem();
        if (cat != null) product.setCategoryId(cat.getCategoryId());
        if (sup != null) product.setSupplierId(sup.getSupplierId());

        product.setImportPrice(Double.parseDouble(txtImport.getText().isEmpty() ? "0" : txtImport.getText()));
        product.setSalePrice(Double.parseDouble(txtSale.getText().isEmpty() ? "0" : txtSale.getText()));
        product.setQuantity(Integer.parseInt(txtQty.getText().isEmpty() ? "0" : txtQty.getText()));
        product.setStatus(cbStatus.getSelectedIndex());
    }

    private void loadComboBoxData() {
        cbCategory.removeAllItems();
        cbSupplier.removeAllItems();
        catDAO.selectAll().forEach(cbCategory::addItem);
        supDAO.selectAll().forEach(cbSupplier::addItem);
    }

    private void fillForm() {
        txtName.setText(product.getProductName());
        txtBarcode.setText(product.getBarcode());
        txtImport.setText(String.valueOf(product.getImportPrice()));
        txtSale.setText(String.valueOf(product.getSalePrice()));
        txtQty.setText(String.valueOf(product.getQuantity()));
        txtImage.setText(product.getImage());
        cbStatus.setSelectedIndex(product.getStatus());

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

    private boolean validateForm() {
        if (txtName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên không được để trống!");
            return false;
        }
        try {
            Double.parseDouble(txtSale.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là số!");
            return false;
        }
        // --- LỆNH MỚI: KIỂM TRA SỐ LƯỢNG VÀ TRẠNG THÁI ---
        // Lấy số lượng hiện tại từ ô text (đã set ở fillForm hoặc mặc định là 0)
        int currentQty = Integer.parseInt(txtQty.getText().isEmpty() ? "0" : txtQty.getText());

        // Trong ComboBox của bạn: index 0 là "Ngừng bán", index 1 là "Đang bán"
        int selectedStatus = cbStatus.getSelectedIndex();

        // Nếu số lượng <= 0 mà người dùng chọn "Đang bán" (index 1)
        if (currentQty <= 0 && selectedStatus == 1) {
            JOptionPane.showMessageDialog(this, 
                "Không thể chuyển sang trạng thái 'Đang bán' vì sản phẩm này đã hết hàng!", 
                "Thông báo lỗi", 
                JOptionPane.ERROR_MESSAGE);

            // Tự động đưa về "Ngừng bán" (index 0) để người dùng không bấm nhầm
            cbStatus.setSelectedIndex(0); 
        return false;
    }
        return true;
    }

    public Product getProduct() { return product; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}