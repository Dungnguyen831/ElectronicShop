package com.mycompany.view.Staff.component;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.dao.ProductDAO;
import com.mycompany.model.Category;
import com.mycompany.model.Product;

import com.mycompany.view.Staff.component.ProductCard;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class ProductListPanel extends JPanel {
    private JPanel pnlGrid;
    private JTextField txtSearch;
    private JComboBox<Category> cboCategory;
    private JComboBox<String> cboStock; 
    
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private Consumer<Product> onProductSelect;

    public ProductListPanel(Consumer<Product> onProductSelect) {
        this.onProductSelect = onProductSelect;
        
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        initFilter();
        initGrid();
        
        // Load mặc định: Không từ khóa, Tất cả danh mục (0), Tất cả kho (0)
        loadData("", 0, 0); 
    }

    private void initFilter() {
        // Thanh bộ lọc dùng FlowLayout để xếp hàng ngang
        JPanel pnlFilterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlFilterBar.setBackground(Color.WHITE);
        pnlFilterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        
        // --- Ô TÌM KIẾM ---
        txtSearch = new JTextField(); 
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên hoặc mã vạch...");
        
        // --- COMBOBOX LỌC KHO ---
        cboStock = new JComboBox<>(new String[]{"Tất cả kho", "Còn hàng", "Hết hàng"});
        cboStock.setPreferredSize(new Dimension(120, 35));

        // --- COMBOBOX DANH MỤC ---
        cboCategory = new JComboBox<>();
        cboCategory.setPreferredSize(new Dimension(150, 35));
        cboCategory.addItem(new Category(0, "Tất cả danh mục", "")); 
        for(Category c : categoryDAO.getAllCategories()) {
            cboCategory.addItem(c);
        }
        
        // --- NÚT TÌM ---
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setPreferredSize(new Dimension(70, 35));
        
        // --- THÊM VÀO PANEL ---
        pnlFilterBar.add(txtSearch);
        pnlFilterBar.add(new JLabel("Kho:"));
        pnlFilterBar.add(cboStock);
        pnlFilterBar.add(new JLabel("Loại:"));
        pnlFilterBar.add(cboCategory);
        pnlFilterBar.add(btnSearch);
        
        // --- SỰ KIỆN ---
        btnSearch.addActionListener(e -> search());
        cboCategory.addActionListener(e -> search());
        cboStock.addActionListener(e -> search()); 
        txtSearch.addActionListener(e -> search());

        add(pnlFilterBar, BorderLayout.NORTH);
    }

    private void initGrid() {
        // --- SỬA QUAN TRỌNG: DÙNG GRIDLAYOUT ---
        // GridLayout(0, 3): 0 nghĩa là số hàng tự động sinh ra, 3 là cố định 3 cột
        // Điều này đảm bảo nếu có nhiều sản phẩm nó sẽ tự xuống dòng, không bị trôi ngang
        pnlGrid = new JPanel(new GridLayout(0, 3, 15, 15)); 
        pnlGrid.setBackground(Color.WHITE);
        
        // Wrapper đẩy nội dung lên trên cùng
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(pnlGrid, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        // Chỉ hiện thanh cuộn dọc
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scroll, BorderLayout.CENTER);
    }

    private void search() {
        String kw = txtSearch.getText().trim();
        Category c = (Category) cboCategory.getSelectedItem();
        int catId = (c != null) ? c.getCategoryId() : 0;
        int stockStatus = cboStock.getSelectedIndex(); 
        
        loadData(kw, catId, stockStatus);
    }

    public void loadData(String kw, int catId, int stockStatus) {
        pnlGrid.removeAll();
        
        List<Product> list = productDAO.searchProducts(kw, catId, stockStatus);
        
        if (list.isEmpty()) {
            // Hiển thị thông báo nếu không tìm thấy
            JLabel lbl = new JLabel("Không tìm thấy sản phẩm nào!", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lbl.setForeground(Color.GRAY);
            // Với GridLayout, ta cần add vào wrapper hoặc chỉnh lại, 
            // nhưng add tạm vào grid cũng hiện được ở ô đầu tiên
            pnlGrid.add(lbl); 
        } else {
            for(Product p : list) {
                ProductCard card = new ProductCard(p, onProductSelect);
                pnlGrid.add(card);
            }
        }
        
        pnlGrid.revalidate();
        pnlGrid.repaint();
    }
    
    // Hàm phụ trợ để reset bộ lọc về mặc định (dùng sau khi thanh toán xong)
    public void resetFilters() {
        txtSearch.setText("");
        cboCategory.setSelectedIndex(0);
        cboStock.setSelectedIndex(0);
        loadData("", 0, 0);
    }
}
