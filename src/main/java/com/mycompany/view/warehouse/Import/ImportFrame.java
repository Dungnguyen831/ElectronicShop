package com.mycompany.view.warehouse.Import;

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
    private JButton btnAdd, btnConfirm;
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
    private int currentSupplierId = -1; // -1 nghĩa là chưa có NCC nào
    
    public ImportFrame(ProductPanel panel) {
        this.productPanel = panel;
        initComponents();
        initEvents();
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
        

        // 2. Tra cứu sản phẩm khi nhập mã (Liên kết Tên & NCC)
        txtProductId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupProduct(txtProductId.getText());
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
         // 5. Nút xác nhận lưu phiếu
        btnConfirm.addActionListener(e -> {
            // 1. Kiểm tra danh sách
            if (listDetails.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Danh sách nhập đang trống!");
                return;
            }
            
            // [QUAN TRỌNG] Kiểm tra xem đã có SupplierID chưa
            // (Giả sử lấy theo NCC của sản phẩm đầu tiên trong danh sách)
            if (this.currentSupplierId == -1) {
                 // Fallback: Nếu lỗi logic, thử lấy lại từ DB dựa trên sp đầu tiên
                 int firstProductId = listDetails.get(0).getProductId();
                 Product p = productDao.getProductById(firstProductId);
                 if(p != null) this.currentSupplierId = p.getSupplierId();
            }

            // 2. Tạo đối tượng Import
            Import imp = new Import();
            imp.setSupplierId(this.currentSupplierId); // <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY
            imp.setUserId(1); // Tạm thời set cứng user admin (ID 1)
            
            // Tính tổng tiền
            double totalAmt = listDetails.stream().mapToDouble(d -> d.getQuantity() * d.getInputPrice()).sum();
            imp.setTotalAmount(totalAmt);

            // 3. Gọi DAO lưu
            if (importDao.saveImportOrder(imp, listDetails)) {
                JOptionPane.showMessageDialog(this, "Nhập kho thành công!");
                
                // Cập nhật lại bảng kho bên ngoài nếu có
                if (productPanel != null) {
                    productPanel.fillTable(); 
                }
                
                resetAll();
                // this.dispose(); // Đóng form nếu muốn
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể lưu phiếu nhập!");
            }
        });   
    }

    // HÀM QUAN TRỌNG: Khớp với selectByKeyword trong SupplierDAO của bạn
    

    private void lookupProduct(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return;
        try {
            int pid = Integer.parseInt(idStr.trim());
            Product p = productDao.getProductById(pid);
            
            if (p != null) {
                txtName.setText(p.getProductName());
                // Format giá cho đẹp (bỏ số thập phân thừa .0)
                txtPrice.setText(new DecimalFormat("#").format(p.getImportPrice())); 
                
                // [QUAN TRỌNG] Lấy ID nhà cung cấp từ sản phẩm
                this.currentSupplierId = p.getSupplierId(); 
            } else {
                // Reset nếu không tìm thấy
                txtName.setText("");
                txtPrice.setText("");
                this.currentSupplierId = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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