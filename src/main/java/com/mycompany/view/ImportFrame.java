package com.mycompany.view;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.view.warehouse.product.ProductPanel;
import com.mycompany.view.warehouse.supplier.SupplierDialog;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ImportFrame extends JFrame {
    private JTextField txtProductId, txtName, txtQuantity, txtPrice;
    private JComboBox<String> cbSupplier;
    private JButton btnAdd, btnConfirm, btnQuickAddSup;
    private JTable tblImport;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;

    private List<ImportDetail> listDetails = new ArrayList<>();
    private ImportDAO importDao = new ImportDAO();
    private ProductDAO productDao = new ProductDAO();
    private SupplierDAO supplierDao = new SupplierDAO();
    private ProductPanel productPanel; 
    
    private HashMap<String, Integer> supplierMap = new HashMap<>();
    private DecimalFormat formatter = new DecimalFormat("#,###");

    public ImportFrame(ProductPanel panel) {
        this.productPanel = panel;
        initComponents();
        initEvents();
        loadSuppliersToCombo(""); // Nạp danh sách ban đầu
        setTitle("Nhập Kho Sản Phẩm");
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Nhập liệu ---
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBorder(BorderFactory.createTitledBorder(" Thông tin hàng hóa "));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // HÀNG 0: NHÀ CUNG CẤP (Searchable + Button +)
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pnlInput.add(new JLabel("Nhà Cung Cấp:"), g);
        
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1.0;
        JPanel pnlSupRow = new JPanel(new BorderLayout(5, 0));
        cbSupplier = new JComboBox<>();
        cbSupplier.setEditable(true); // Cho phép gõ tìm kiếm
        btnQuickAddSup = new JButton("+");
        btnQuickAddSup.setPreferredSize(new Dimension(45, 25));
        pnlSupRow.add(cbSupplier, BorderLayout.CENTER); 
        pnlSupRow.add(btnQuickAddSup, BorderLayout.EAST);
        pnlInput.add(pnlSupRow, g);

        // HÀNG 1: MÃ SP & TÊN SP (Bỏ nút + ở Tên SP)
        g.gridwidth = 1; g.gridy = 1;
        g.gridx = 0; g.weightx = 0;
        pnlInput.add(new JLabel("Mã Sản Phẩm:"), g);
        
        g.gridx = 1; g.weightx = 0.3;
        txtProductId = new JTextField();
        pnlInput.add(txtProductId, g);
        
        g.gridx = 2; g.weightx = 0;
        pnlInput.add(new JLabel("Tên Sản Phẩm:"), g);
        
        g.gridx = 3; g.weightx = 0.7;
        txtName = new JTextField();
        txtName.setEditable(false);
        txtName.setBackground(new Color(245, 245, 245));
        pnlInput.add(txtName, g);

        // HÀNG 2: SỐ LƯỢNG & GIÁ
        g.gridy = 2; 
        g.gridx = 0; g.weightx = 0;
        pnlInput.add(new JLabel("Số Lượng:"), g);
        
        g.gridx = 1; g.weightx = 0.3;
        txtQuantity = new JTextField(); 
        pnlInput.add(txtQuantity, g);
        
        g.gridx = 2; g.weightx = 0;
        pnlInput.add(new JLabel("Giá Nhập:"), g);
        
        g.gridx = 3; g.weightx = 0.7;
        txtPrice = new JTextField(); 
        pnlInput.add(txtPrice, g);

        // NÚT THÊM
        btnAdd = new JButton("Thêm vào phiếu");
        btnAdd.setBackground(new Color(40, 167, 69)); 
        btnAdd.setForeground(Color.WHITE);
        g.gridy = 3; g.gridx = 3; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.EAST;
        pnlInput.add(btnAdd, g);

        add(pnlInput, BorderLayout.NORTH);

        // Bảng
        tableModel = new DefaultTableModel(new String[]{"Mã SP", "Tên SP", "Số lượng", "Giá nhập", "Thành tiền"}, 0);
        tblImport = new JTable(tableModel);
        add(new JScrollPane(tblImport), BorderLayout.CENTER);

        // Tổng tiền & Xác nhận
        JPanel pnlSouth = new JPanel(new BorderLayout(20, 0));
        pnlSouth.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblTotal = new JLabel("TỔNG TIỀN: 0 VNĐ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        btnConfirm = new JButton("XÁC NHẬN NHẬP KHO");
        btnConfirm.setPreferredSize(new Dimension(250, 50));
        btnConfirm.setBackground(new Color(0, 123, 255)); 
        btnConfirm.setForeground(Color.WHITE);
        
        pnlSouth.add(lblTotal, BorderLayout.WEST); 
        pnlSouth.add(btnConfirm, BorderLayout.EAST);
        add(pnlSouth, BorderLayout.SOUTH);
    }

    private void initEvents() {
        // 1. Logic tìm kiếm NCC khi gõ (Searchable)
        JTextField editor = (JTextField) cbSupplier.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Tránh các phím điều hướng
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    return;
                }
                String input = editor.getText();
                filterSupplier(input);
            }
        });

        // 2. Tra cứu sản phẩm khi nhập mã (Liên kết Tên & NCC)
        txtProductId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupProduct(txtProductId.getText());
            }
        });

        // 3. Nút thêm NCC nhanh
        btnQuickAddSup.addActionListener(e -> {
            SupplierDialog dialog = new SupplierDialog(this, null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Supplier s = dialog.getSupplier();
                if (supplierDao.insert(s)) { // Lưu vào DB
                    loadSuppliersToCombo(""); // Load lại toàn bộ Map
                    cbSupplier.setSelectedItem(s.getSupplierName()); // Chọn luôn NCC mới
                }
            }
        });

        // 4. Nút thêm vào bảng tạm
        btnAdd.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtProductId.getText());
                String name = txtName.getText();
                int qty = Integer.parseInt(txtQuantity.getText());
                double price = Double.parseDouble(txtPrice.getText());
                
                tableModel.addRow(new Object[]{id, name, qty, formatter.format(price), formatter.format(qty * price)});
                listDetails.add(new ImportDetail(id, qty, price));
                updateTotal();
                clearInput();
                txtProductId.requestFocus();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số liệu hợp lệ!");
            }
        });

        // 5. Nút xác nhận lưu phiếu
                btnConfirm.addActionListener(e -> {
            // 1. Kiểm tra đầu vào
            String selectedSupName = (String) cbSupplier.getSelectedItem();
            if (listDetails.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Danh sách nhập đang trống!");
                return;
            }
            if (selectedSupName == null || !supplierMap.containsKey(selectedSupName)) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà cung cấp hợp lệ!");
                return;
            }

            // 2. Tạo đối tượng Import
            Import imp = new Import();
            imp.setSupplierId(supplierMap.get(selectedSupName));
            imp.setUserId(1); // Tạm thời để mặc định, có thể thay bằng user đăng nhập
            imp.setTotalAmount(listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum());

            // 3. Gọi DAO lưu vào Database
            if (importDao.saveImportOrder(imp, listDetails)) {
                JOptionPane.showMessageDialog(this, "Nhập kho thành công! Số lượng tồn đã được cập nhật.");

        // --- QUAN TRỌNG: CẬP NHẬT LẠI BẢNG Ở PRODUCTPANEL ---
        if (productPanel != null) {
            // Gọi hàm đổ lại dữ liệu từ Database lên bảng ở màn hình chính
            productPanel.fillTable(); 
        }
        
        // Xóa sạch bảng tạm và đóng Frame hoặc reset
        resetAll();
        // this.dispose(); // Bỏ comment nếu muốn đóng cửa sổ sau khi nhập xong
    } else {
        JOptionPane.showMessageDialog(this, "Lỗi: Không thể lưu phiếu nhập vào cơ sở dữ liệu!");
    }
});
    }

    // HÀM QUAN TRỌNG: Khớp với selectByKeyword trong SupplierDAO của bạn
    private void filterSupplier(String input) {
        List<Supplier> sups = supplierDao.selectByKeyword(input); // Dùng đúng tên hàm của bạn
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        
        // Lưu vào Map để lấy ID sau này
        for (Supplier s : sups) {
            model.addElement(s.getSupplierName());
            supplierMap.put(s.getSupplierName(), s.getSupplierId());
        }
        
        cbSupplier.setModel(model);
        cbSupplier.getEditor().setItem(input); // Giữ lại chữ đang gõ
        if (model.getSize() > 0) {
            cbSupplier.showPopup();
        }
    }

    private void lookupProduct(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            int pid = Integer.parseInt(idStr.trim());
            Product p = productDao.getProductById(pid);
            if (p != null) {
                txtName.setText(p.getProductName());
                txtPrice.setText(String.valueOf(p.getImportPrice()));
                // Đổ GIÁ NHẬP từ database vào ô nhập liệu
                txtPrice.setText(String.valueOf(p.getImportPrice()));
                // Tự động nhảy ComboBox nhà cung cấp
                int idNCC = p.getSupplierId();
                for (Map.Entry<String, Integer> entry : supplierMap.entrySet()) {
                    if (entry.getValue().equals(idNCC)) {
                        cbSupplier.setSelectedItem(entry.getKey());
                        break;
                    }
                }
            }
        } catch (Exception e) {}
    }

    public void loadSuppliersToCombo(String filter) {
        List<Supplier> sups = supplierDao.selectAll();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        supplierMap.clear();
        for (Supplier s : sups) {
            supplierMap.put(s.getSupplierName(), s.getSupplierId());
            model.addElement(s.getSupplierName());
        }
        cbSupplier.setModel(model);
    }

    private void updateTotal() {
        double total = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
        lblTotal.setText("TỔNG TIỀN: " + formatter.format(total) + " VNĐ");
    }

    private void clearInput() {
        txtProductId.setText(""); txtName.setText(""); txtQuantity.setText(""); txtPrice.setText("");
    }

    private void resetAll() {
        listDetails.clear();
        tableModel.setRowCount(0);
        lblTotal.setText("TỔNG TIỀN: 0 VNĐ");
    }
}