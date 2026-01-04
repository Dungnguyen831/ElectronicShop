package com.mycompany.view.warehouse;

import com.mycompany.model.User;
import com.mycompany.view.warehouse.supplier.SupplierPanel;
import com.mycompany.view.warehouse.product.ProductPanel;
import com.mycompany.view.warehouse.category.CategoryPanel;
import com.mycompany.util.Style;
import com.mycompany.view.warehouse.Import.ListImport;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WarehouseMainFrame extends JFrame implements ActionListener {

    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnListImport, btnProduct, btnSupplier, btnCategorie, btnLogout;
    private final User userLogged; // Đối tượng người dùng từ DB
    private JButton currentSelectedButton = null; // Lưu nút đang được chọn
    public WarehouseMainFrame(User u) {
        this.userLogged = u;
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ Thống Quản Lý Điện Tử");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (GridLayout 10 hàng) ---
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(Style.COLOR_PRIMARY);
        sidebarPanel.setLayout(new java.awt.GridLayout(10, 1, 0, 10)); 
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Hàng 1: Tiêu đề
        JLabel lblMenu = new JLabel("MENU", JLabel.CENTER);
        lblMenu.setFont(Style.FONT_HEADER);
        lblMenu.setForeground(Color.WHITE);
        sidebarPanel.add(lblMenu);

        // Hàng 2-5: Menu chính
        btnListImport = createMenuButton("Danh Sách Nhập Kho");
        btnProduct = createMenuButton("Quản Lý Sản Phẩm");
        btnSupplier = createMenuButton("Quản Lý Nhà Cung Cấp");
        btnCategorie = createMenuButton("Danh Mục Sản Phẩm");

        sidebarPanel.add(btnListImport);
        sidebarPanel.add(btnProduct);
        sidebarPanel.add(btnSupplier);
        sidebarPanel.add(btnCategorie);

        // Hàng 6-7: Filler
        sidebarPanel.add(new JLabel("")); 
        sidebarPanel.add(new JLabel("")); 

        // Hàng 8: Tên người dùng (full_name)
        // Bỏ hoàn toàn Border, dùng JLabel.CENTER để căn giữa tuyệt đối
        JLabel lblName = new JLabel(userLogged != null ? userLogged.getFullName() : "Admin", JLabel.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(Color.WHITE);
        sidebarPanel.add(lblName);

        // Hàng 9: Chức vụ dựa trên role_id
        String roleName = "Nhân viên"; // Giá trị mặc định
        if (userLogged != null) {
            int role = userLogged.getRoleId();
            if (role == 1) roleName = "Quản trị viên";
            else if (role == 2) roleName = "Nhân viên bán hàng";
            else if (role == 3) roleName = "Nhân viên thủ kho";
        }
        
        JLabel lblRole = new JLabel("Chức vụ: " + roleName, JLabel.CENTER);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(new Color(220, 220, 220));
        sidebarPanel.add(lblRole);

        // Hàng 10: Nút Đăng xuất (đã được setupLogoutButton định dạng riêng)
        setupLogoutButton();
        sidebarPanel.add(btnLogout);

        add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(new ListImport(), "ListImport");
        contentPanel.add(new ProductPanel(), "PRODUCT");        
        contentPanel.add(new SupplierPanel(), "SUPPLIER");
        contentPanel.add(new CategoryPanel(), "CATEGORIE");
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Hàm tạo nút Đăng xuất riêng biệt với màu đỏ và chữ trắng
     */
    private void setupLogoutButton() {
        btnLogout = new JButton("ĐĂNG XUẤT");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE); // Chữ trắng
        btnLogout.setBackground(new Color(211, 47, 47)); // Màu đỏ (Material Red)
        
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Căn chỉnh nội dung nút vào giữa để khác biệt với menu
        btnLogout.setHorizontalAlignment(SwingConstants.CENTER);

        // Hiệu ứng hover riêng cho nút đỏ
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(183, 28, 28)); // Đỏ đậm hơn khi hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(211, 47, 47));
            }
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                java.awt.EventQueue.invokeLater(() -> {
                    new com.mycompany.view.LoginFrame().setVisible(true); 
                });
            }
        });
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Style.FONT_BOLD);
        btn.setBackground(Style.COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
         @Override
            public void mouseEntered(MouseEvent e) {
                // Luôn đổi màu khi di chuột vào
                btn.setBackground(Style.COLOR_BG_LEFT);
                btn.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.WHITE));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // CHỈ reset màu nếu nút này KHÔNG PHẢI là nút đang được chọn
                if (btn != currentSelectedButton) {
                    btn.setBackground(Style.COLOR_PRIMARY);
                    btn.setBorder(new EmptyBorder(10, 20, 10, 10));
                }
            }
        });
        btn.addActionListener(this);
        return btn;
    }

    private JPanel createDummyPanel(String text, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(color);
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 30));
        p.add(l);
        return p;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();

        // 1. Reset màu cho nút cũ (nếu có)
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(Style.COLOR_PRIMARY);
            currentSelectedButton.setBorder(new EmptyBorder(10, 20, 10, 10));
        }

        // 2. Thiết lập màu sẫm cho nút mới được chọn
        clickedButton.setBackground(Style.COLOR_BG_LEFT); // Màu sẫm khi active
        clickedButton.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.WHITE));
        currentSelectedButton = clickedButton;

        // 3. Chuyển đổi CardLayout
        if (clickedButton == btnListImport) cardLayout.show(contentPanel, "ListImport");
        else if (clickedButton == btnProduct) cardLayout.show(contentPanel, "PRODUCT");
        else if (clickedButton == btnSupplier) cardLayout.show(contentPanel, "SUPPLIER");
        else if (clickedButton == btnCategorie) cardLayout.show(contentPanel, "CATEGORIE");
    }
}