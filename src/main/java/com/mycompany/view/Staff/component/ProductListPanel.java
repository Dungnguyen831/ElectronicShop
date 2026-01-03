package com.mycompany.view.Staff.component;

import com.mycompany.dao.CategoryDAO;
import com.mycompany.dao.ProductDAO;
import com.mycompany.model.Category;
import com.mycompany.model.Product;
// import com.mycompany.util.Style; // <--- XÓA DÒNG NÀY NẾU BÁO ĐỎ (Vì có thể bạn chưa tạo class Style)

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class ProductListPanel extends JPanel {
    private JPanel pnlGrid;
    private JTextField txtSearch;
    private JComboBox<Category> cboCategory;
    
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private Consumer<Product> onProductSelect;

    // Constructor nhận vào hành động khi click sản phẩm
    public ProductListPanel(Consumer<Product> onProductSelect) {
        this.onProductSelect = onProductSelect;
        
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        initFilter();
        initGrid();
        loadData(null, 0); // Load dữ liệu ban đầu
    }

    private void initFilter() {
        JPanel pnl = new JPanel(new BorderLayout(5, 0));
        pnl.setBackground(Color.WHITE);
        
        txtSearch = new JTextField(); 
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm sản phẩm...");
        
        cboCategory = new JComboBox<>();
        cboCategory.setPreferredSize(new Dimension(150, 35));
        for(Category c : categoryDAO.getAllCategories()) {
            cboCategory.addItem(c);
        }
        
        JButton btn = new JButton("Tìm");
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        
        // Sự kiện tìm kiếm
        btn.addActionListener(e -> search());
        cboCategory.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search()); // Enter để tìm

        pnl.add(txtSearch, BorderLayout.CENTER);
        pnl.add(cboCategory, BorderLayout.EAST);
        pnl.add(btn, BorderLayout.WEST);
        add(pnl, BorderLayout.NORTH);
    }

    private void initGrid() {
        // GridLayout 0 hàng (tự động), 3 cột, khoảng cách 10px
        pnlGrid = new JPanel(new GridLayout(0, 3, 10, 10)); 
        pnlGrid.setBackground(Color.WHITE);
        
        // Wrapper đẩy lưới lên trên cùng (tránh bị giãn khi ít SP)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(pnlGrid, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void search() {
        String kw = txtSearch.getText();
        Category c = (Category) cboCategory.getSelectedItem();
        int catId = (c != null) ? c.getCategoryId() : 0;
        loadData(kw, catId);
    }

    public void loadData(String kw, int catId) {
        pnlGrid.removeAll();
        
        List<Product> list = productDAO.searchProducts(kw, catId);
        for(Product p : list) {
            // QUAN TRỌNG: Gọi ProductCard để hiển thị từng ô
            // Bạn phải đảm bảo file ProductCard.java đã không còn lỗi đỏ
            ProductCard card = new ProductCard(p, onProductSelect);
            pnlGrid.add(card);
        }
        
        pnlGrid.revalidate();
        pnlGrid.repaint();
    }
}