package com.mycompany.view.warehouse;

import com.mycompany.model.User;
import com.mycompany.view.warehouse.supplier.SupplierPanel;
import com.mycompany.view.warehouse.product.ProductPanel;
import com.mycompany.view.warehouse.category.CategoryPanel;
import com.mycompany.util.Style;
import com.mycompany.view.warehouse.Import.ListImport;
import com.mycompany.view.ImportFrame; // Import file ImportFrame của bạn
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WarehouseMainFrame extends JFrame implements ActionListener {

    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JButton btnListImport, btnImportAction, btnProduct, btnSupplier, btnCategorie, btnLogout;
    private final User userLogged; 
    private JButton currentSelectedButton = null; 

    public WarehouseMainFrame(User u) {
        this.userLogged = u;
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ Thống Quản Lý Kho Điện Tử");
        setSize(1300, 800); // Tăng nhẹ kích thước để form nhập liệu thoải mái
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR ---
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setBackground(Style.COLOR_PRIMARY);
        sidebarPanel.setLayout(new java.awt.GridLayout(11, 1, 0, 5)); // Tăng số hàng lên 11
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel lblMenu = new JLabel("MENU QUẢN KHO", JLabel.CENTER);
        lblMenu.setFont(Style.FONT_HEADER);
        lblMenu.setForeground(Color.WHITE);
        sidebarPanel.add(lblMenu);

        // Khởi tạo các nút Menu
        btnListImport = createMenuButton("Lịch Sử Nhập Kho");
        btnImportAction = createMenuButton("Thực Hiện Nhập Kho"); // MỤC MỚI
        btnProduct = createMenuButton("Quản Lý Sản Phẩm");
        btnSupplier = createMenuButton("Quản Lý Nhà Cung Cấp");
        btnCategorie = createMenuButton("Danh Mục Sản Phẩm");

        sidebarPanel.add(btnListImport);
        sidebarPanel.add(btnImportAction); // Thêm vào Sidebar
        sidebarPanel.add(btnProduct);
        sidebarPanel.add(btnSupplier);
        sidebarPanel.add(btnCategorie);

        // Filler
        sidebarPanel.add(new JLabel("")); 
        sidebarPanel.add(new JLabel("")); 

        // Thông tin User
        JLabel lblName = new JLabel(userLogged != null ? userLogged.getFullName() : "Admin", JLabel.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(Color.WHITE);
        sidebarPanel.add(lblName);

        String roleName = "Nhân viên thủ kho";
        if (userLogged != null) {
            int role = userLogged.getRoleId();
            if (role == 1) roleName = "Quản trị viên";
        }
        JLabel lblRole = new JLabel(roleName, JLabel.CENTER);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(new Color(220, 220, 220));
        sidebarPanel.add(lblRole);

        setupLogoutButton();
        sidebarPanel.add(btnLogout);

        add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // KHỞI TẠO CÁC TRANG (PANEL)
        ProductPanel pnlProduct = new ProductPanel();
        
        // Nhúng ImportFrame vào CardLayout thông qua getContentPane()
        ImportFrame frmImport = new ImportFrame(pnlProduct);
        Container importContent = frmImport.getContentPane();

        contentPanel.add(new ListImport(), "ListImport");
        contentPanel.add(importContent, "IMPORT_ACTION"); // Nhúng ruột ImportFrame
        contentPanel.add(pnlProduct, "PRODUCT");        
        contentPanel.add(new SupplierPanel(), "SUPPLIER");
        contentPanel.add(new CategoryPanel(), "CATEGORIE");

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();

        // Highlight nút được chọn
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(Style.COLOR_PRIMARY);
            currentSelectedButton.setBorder(new EmptyBorder(10, 20, 10, 10));
        }
        clickedButton.setBackground(Style.COLOR_BG_LEFT);
        clickedButton.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.WHITE));
        currentSelectedButton = clickedButton;

        // Chuyển màn hình theo Card Name
        if (clickedButton == btnListImport) cardLayout.show(contentPanel, "ListImport");
        else if (clickedButton == btnImportAction) cardLayout.show(contentPanel, "IMPORT_ACTION");
        else if (clickedButton == btnProduct) cardLayout.show(contentPanel, "PRODUCT");
        else if (clickedButton == btnSupplier) cardLayout.show(contentPanel, "SUPPLIER");
        else if (clickedButton == btnCategorie) cardLayout.show(contentPanel, "CATEGORIE");
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
                btn.setBackground(Style.COLOR_BG_LEFT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != currentSelectedButton) {
                    btn.setBackground(Style.COLOR_PRIMARY);
                }
            }
        });
        btn.addActionListener(this);
        return btn;
    }

    private void setupLogoutButton() {
        btnLogout = new JButton("ĐĂNG XUẤT");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(211, 47, 47));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Đăng xuất hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new com.mycompany.view.LoginFrame().setVisible(true);
            }
        });
    }
}