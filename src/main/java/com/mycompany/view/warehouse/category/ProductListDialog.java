package com.mycompany.view.warehouse.category;

import com.mycompany.dao.ProductDAO;
import com.mycompany.model.Category;
import com.mycompany.model.Product;
import com.mycompany.util.Style;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductListDialog extends JDialog {
    private JTable tblProducts;
    private DefaultTableModel model;
    private ProductDAO productDAO = new ProductDAO();
    private Category category;
    private JLabel lblTitle; 

    public ProductListDialog(Frame parent, Category category) {
        super(parent, true);
        this.category = category;
        initComponents();
        loadProducts(category.getCategoryId());
        
        setTitle("Danh sách sản phẩm thuộc loại: " + category.getCategoryName());
        setSize(1100, 650); 
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Style.COLOR_BG_RIGHT);

        // --- TOP: Header hiển thị tên LOẠI và SỐ LƯỢNG ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(new Color(52, 73, 94)); // Màu xanh đậm chuyên nghiệp
        pnlHeader.setPreferredSize(new Dimension(0, 60));
        
        lblTitle = new JLabel("LOẠI: " + category.getCategoryName().toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // --- CENTER: Bảng hiển thị thông tin ---
        // Sửa lại header cột cho chính xác
        String[] columns = {
            "ID", "Nhà cung cấp", "Tên sản phẩm", "Mã vạch", 
            "Giá nhập", "Giá bán", "Số lượng", "Trạng thái", "Ngày tạo"
        };
        
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tblProducts = new JTable(model);
        tblProducts.setRowHeight(35); // Tăng chiều cao dòng cho dễ nhìn
        
        // Tùy chỉnh độ rộng cột
        tblProducts.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tblProducts.getColumnModel().getColumn(1).setPreferredWidth(150); // NCC
        tblProducts.getColumnModel().getColumn(2).setPreferredWidth(250); // Tên SP
        
        JScrollPane scrollPane = new JScrollPane(tblProducts);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: Nút đóng ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnlBottom.setBackground(Style.COLOR_BG_RIGHT);
        
        JButton btnClose = new JButton("Đóng cửa sổ");
        btnClose.setPreferredSize(new Dimension(130, 45)); 
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        
        pnlBottom.add(btnClose);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void loadProducts(int categoryId) {
        model.setRowCount(0);
        // Lưu ý: Hàm selectByCategoryId trong ProductDAO cần có JOIN để lấy supplier_name
        List<Product> list = productDAO.selectByCategoryId(categoryId);
        
        for (Product p : list) {
            model.addRow(new Object[]{
                p.getProductId(),
                p.getSupplierName() != null ? p.getSupplierName() : "N/A", // Hiện tên NCC
                p.getProductName(), // Tên sản phẩm
                p.getBarcode(),
                String.format("%,.0f VNĐ", p.getImportPrice()), 
                String.format("%,.0f VNĐ", p.getSalePrice()),
                p.getQuantity(),
                // Sửa logic trạng thái theo kiểu int
                p.getStatus() == 1 ? "Đang bán" : "Ngừng bán", 
                p.getCreatedAt()
            });
        }
        
        // Cập nhật tiêu đề kèm tổng số lượng sản phẩm
        lblTitle.setText("LOẠI: " + category.getCategoryName().toUpperCase() + " (" + list.size() + " sản phẩm)");
    }
}