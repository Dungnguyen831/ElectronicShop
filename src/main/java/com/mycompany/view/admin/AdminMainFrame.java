package com.mycompany.view.admin;

import com.mycompany.util.Style;
import com.mycompany.model.User;
import com.mycompany.view.LoginFrame; // Đảm bảo đúng package của LoginFrame
import com.mycompany.view.admin.UserPanel; // Đảm bảo đúng package
import com.mycompany.view.admin.CustomerPanel; // Đảm bảo đúng package
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author Nguyen Anh Dung
 */
public class AdminMainFrame extends JFrame implements ActionListener {

    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private User currentUser;

    // Các nút menu
    private JButton btnHome, btnRevenue, btnVoucher, btnUser, btnCustomer, btnLogout;

    public AdminMainFrame(User user) {
        this.currentUser = user; 
        initComponents();
        this.setTitle("Hệ thống Quản lý - Xin chào: " + user.getFullName());
    }

    private void initComponents() {
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

        // Hàng 1: Tiêu đề menu
        JLabel lblMenu = new JLabel("QUẢN TRỊ VIÊN", JLabel.CENTER);
        lblMenu.setFont(Style.FONT_HEADER);
        lblMenu.setForeground(Color.WHITE);
        sidebarPanel.add(lblMenu);

        // Hàng 2-6: Các nút chức năng
        btnHome = createMenuButton("Trang Chủ");
        btnRevenue = createMenuButton("Quản Lý Doanh Thu");
        btnVoucher = createMenuButton("Quản Lý Voucher");
        btnUser = createMenuButton("Quản Lý Nhân Viên");
        btnCustomer = createMenuButton("Quản Lý Khách Hàng");

        sidebarPanel.add(btnHome);
        sidebarPanel.add(btnRevenue);
        sidebarPanel.add(btnVoucher);
        sidebarPanel.add(btnUser);
        sidebarPanel.add(btnCustomer);

        // Hàng 7: Filler (Ô trống)
        sidebarPanel.add(new JLabel("")); 

        // Hàng 8: Hiển thị Tên người dùng
        JLabel lblName = new JLabel((currentUser != null ? currentUser.getFullName() : "Admin"));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);
        lblName.setBorder(new EmptyBorder(0, 15, 0, 0));
        sidebarPanel.add(lblName);

        // Hàng 9: Hiển thị Chức vụ (Admin có role_id = 1)
        JLabel lblRole = new JLabel("   Chức vụ: Quản trị viên");
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblRole.setForeground(new Color(200, 200, 200));
        lblRole.setBorder(new EmptyBorder(0, 15, 0, 0));
        sidebarPanel.add(lblRole);

        // Hàng 10: Nút Đăng xuất tạo riêng
        setupLogoutButton();
        sidebarPanel.add(btnLogout);

        this.add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createDummyPanel("Trang Chủ", Color.WHITE), "HOME");
        contentPanel.add(createDummyPanel("Màn hình báo cáo doanh thu", Color.LIGHT_GRAY), "REVENUE");
        contentPanel.add(createDummyPanel("Màn hình voucher", Color.LIGHT_GRAY), "VOUCHER");
        contentPanel.add(new UserPanel(), "USER");
        contentPanel.add(new CustomerPanel(), "CUSTOMER");

        this.add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Hàm tạo nút Đăng xuất riêng biệt (Màu đỏ, chữ trắng)
     */
    private void setupLogoutButton() {
        btnLogout = new JButton("ĐĂNG XUẤT");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(211, 47, 47)); // Màu đỏ
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setHorizontalAlignment(SwingConstants.CENTER);

        // Hiệu ứng hover cho nút Đăng xuất
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(183, 28, 28));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(211, 47, 47));
            }
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn đăng xuất không?", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // Đóng AdminMainFrame
                // Mở lại LoginFrame
                java.awt.EventQueue.invokeLater(() -> {
                    new LoginFrame().setVisible(true);
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
                btn.setBackground(Style.COLOR_BG_LEFT);
                btn.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.WHITE));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Style.COLOR_PRIMARY);
                btn.setBorder(new EmptyBorder(10, 20, 10, 10));
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
        if (e.getSource() == btnHome) cardLayout.show(contentPanel, "HOME");
        else if (e.getSource() == btnRevenue) cardLayout.show(contentPanel, "REVENUE");
        else if (e.getSource() == btnVoucher) cardLayout.show(contentPanel, "VOUCHER");
        else if (e.getSource() == btnUser) cardLayout.show(contentPanel, "USER");
        else if (e.getSource() == btnCustomer) cardLayout.show(contentPanel, "CUSTOMER");
    }
}