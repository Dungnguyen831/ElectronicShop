package com.mycompany.view.warehouse.product;

import com.mycompany.dao.ProductDAO;
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
    private boolean confirmed = false; // Biến xác nhận trạng thái nhấn nút Lưu
    
    private ProductDAO proDAO = new ProductDAO();
    private CategoryDAO catDAO = new CategoryDAO();
    private SupplierDAO supDAO = new SupplierDAO();

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
            cbStatus.setSelectedIndex(1); // Mặc định 'Đang bán'
            txtImport.setText("0");
            txtQty.setText("0");
        }

        setTitle(isEdit ? "Sửa sản phẩm" : "Thêm sản phẩm mới");
        this.setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        getContentPane().setBackground(Style.COLOR_BG_RIGHT);
        setLayout(new GridLayout(15, 2, 10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        add(new JLabel(" Tên sản phẩm:"));
        txtName = new JTextField();
        add(txtName);
        
        add(new JLabel(" Danh mục sản phẩm:"));
        cbCategory = new JComboBox<>();
        add(cbCategory);

        add(new JLabel(" Nhà cung cấp:"));
        cbSupplier = new JComboBox<>();
        add(cbSupplier);

        add(new JLabel(" Barcode:"));
        txtBarcode = new JTextField();
        add(txtBarcode);

        add(new JLabel(" Giá nhập:"));
        txtImport = new JTextField("0");
        txtImport.setEditable(false);
        txtImport.setBackground(new Color(230, 230, 230));
        add(txtImport);

        add(new JLabel(" Giá bán:"));
        txtSale = new JTextField();
        add(txtSale);

        add(new JLabel(" Số lượng:"));
        txtQty = new JTextField("0");
        txtQty.setEditable(false);
        txtQty.setBackground(new Color(230, 230, 230));
        add(txtQty);

        add(new JLabel(" Hình ảnh:"));
        JPanel pnlImage = new JPanel(new BorderLayout(5, 0));
        txtImage = new JTextField();
        txtImage.setEditable(false);
        btnBrowse = new JButton("Chọn ảnh");
        btnBrowse.addActionListener(e -> chooseImage());
        pnlImage.add(txtImage, BorderLayout.CENTER);
        pnlImage.add(btnBrowse, BorderLayout.EAST);
        add(pnlImage);

        add(new JLabel(" Trạng thái:"));
        cbStatus = new JComboBox<>(new String[]{ "Ngừng bán", "Đang bán" });
        add(cbStatus);

        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(btnSave);
        add(btnCancel);

        // Sự kiện nút Lưu
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                captureData();
                this.confirmed = true; // Đánh dấu thành công
                this.setVisible(false); // Ẩn dialog để quay lại panel chính xử lý SQL
            }
        });

        // Sự kiện nút Hủy
        btnCancel.addActionListener(e -> {
            this.confirmed = false;
            this.dispose();
        });
    }

    private boolean validateForm() {
        // Lấy text và loại bỏ khoảng trắng (Sửa lỗi image_b6e83f.png)
        String name = txtName.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!");
            return false;
        }

        // KIỂM TRA TRÙNG TÊN (Chỉ check nếu là thêm mới hoặc đổi tên khác tên cũ)
        if (!isEdit || !name.equalsIgnoreCase(product.getProductName())) {
            if (proDAO.isProductExists(name)) {
                JOptionPane.showMessageDialog(this, "Tên sản phẩm '" + name + "' đã tồn tại!");
                return false;
            }
        }

        try {
            double salePrice = Double.parseDouble(txtSale.getText().trim());
            if (salePrice < 0) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là số dương hợp lệ!");
            return false;
        }

        // Kiểm tra logic trạng thái theo số lượng
        int qty = Integer.parseInt(txtQty.getText());
        if (qty <= 0 && cbStatus.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(this, "Không thể chọn 'Đang bán' khi hết hàng!");
            cbStatus.setSelectedIndex(0);
            return false;
        }

        return true;
    }

    private void captureData() {
        product.setProductName(txtName.getText().trim());
        product.setBarcode(txtBarcode.getText().trim());
        
        // Xử lý lưu ảnh
        String path = txtImage.getText().trim();
        File file = new File(path);
        if (file.isAbsolute() && file.exists()) {
            product.setImage(saveImage(file));
        }

        Category cat = (Category) cbCategory.getSelectedItem();
        Supplier sup = (Supplier) cbSupplier.getSelectedItem();
        if (cat != null) product.setCategoryId(cat.getCategoryId());
        if (sup != null) product.setSupplierId(sup.getSupplierId());

        product.setImportPrice(Double.parseDouble(txtImport.getText()));
        product.setSalePrice(Double.parseDouble(txtSale.getText()));
        product.setQuantity(Integer.parseInt(txtQty.getText()));
        product.setStatus(cbStatus.getSelectedIndex());
    }

    // Các phương thức getter/setter bổ sung (Sửa lỗi image_b6fee9.png)
    public boolean isConfirmed() { return confirmed; }
    
    public void setConfirmed(boolean confirmed) { 
        this.confirmed = confirmed; 
    }

    public Product getProduct() { return product; }

    // --- Các hàm hỗ trợ load dữ liệu ---
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
        // ... (Logic chọn index cho ComboBox giữ nguyên của bạn)
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtImage.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private String saveImage(File src) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();
            String name = UUID.randomUUID().toString() + src.getName().substring(src.getName().lastIndexOf('.'));
            Files.copy(src.toPath(), new File(dir, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return name;
        } catch (Exception e) { return null; }
    }
}