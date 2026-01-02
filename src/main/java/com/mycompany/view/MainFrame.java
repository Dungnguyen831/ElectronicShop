/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view;

import com.mycompany.util.Style;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Nguyen Anh Dung
 */
public class MainFrame extends JFrame implements ActionListener {
    
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Các nút menu
    private JButton btnHome, btnProduct, btnSale, btnUser;

    public MainFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ Thống Quản Lý Điện Tử");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (Menu bên trái) ---
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBackground(Style.COLOR_PRIMARY);
        sidebarPanel.setLayout(new java.awt.GridLayout(10, 1, 0, 10)); // 10 dòng
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Tiêu đề menu
        JLabel lblMenu = new JLabel("MENU", JLabel.CENTER);
        lblMenu.setFont(Style.FONT_HEADER);
        lblMenu.setForeground(Color.WHITE);
        sidebarPanel.add(lblMenu);

        // Tạo các nút menu
        btnHome = createMenuButton("Trang Chủ");
        btnProduct = createMenuButton("Quản Lý Sản Phẩm");
        btnSale = createMenuButton("Bán Hàng");
        btnUser = createMenuButton("Quản Lý Nhân Viên");

        sidebarPanel.add(btnHome);
        sidebarPanel.add(btnProduct);
        sidebarPanel.add(btnSale);
        sidebarPanel.add(btnUser);

        this.add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL (Phần nội dung chính - Quan trọng nhất) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Thêm các Panel con vào đây (Các thành viên khác sẽ code phần này)
        // Tạm thời ta add các Panel rỗng có màu để test
        contentPanel.add(createDummyPanel("Trang Chủ", Color.WHITE), "HOME");
        contentPanel.add(createDummyPanel("Màn hình Sản Phẩm (Đang code...)", Color.LIGHT_GRAY), "PRODUCT");
        contentPanel.add(createDummyPanel("Màn hình Bán Hàng (Đang code...)", Color.CYAN), "SALE");
        
        // Ví dụ sau này: contentPanel.add(new ProductPanel(), "PRODUCT");

        this.add(contentPanel, BorderLayout.CENTER);
    }

    // Hàm tạo nút menu nhanh cho đẹp
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Style.FONT_BOLD);

        // Màu mặc định
        btn.setBackground(Style.COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);

        // Căn lề trái cho chữ (thay vì căn giữa mặc định) -> Nhìn giống menu hơn
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10)); // Padding trong nút

        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Bỏ viền nổi
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng Hover (Di chuột vào đổi màu)
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Style.COLOR_BG_LEFT); // Màu tối hơn
                btn.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, Color.WHITE)); // Thêm vạch trắng bên trái
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Style.COLOR_PRIMARY); // Trả về màu cũ
                btn.setBorder(new EmptyBorder(10, 20, 10, 10)); // Bỏ vạch trắng
            }
        });

        btn.addActionListener(this);
        return btn;
    }

    // Hàm tạo panel giả để test (Xóa sau khi ghép code thật)
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
        // Logic chuyển màn hình
        if (e.getSource() == btnHome) {
            cardLayout.show(contentPanel, "HOME");
        } else if (e.getSource() == btnProduct) {
            cardLayout.show(contentPanel, "PRODUCT");
        } else if (e.getSource() == btnSale) {
            cardLayout.show(contentPanel, "SALE");
        }
    }
}
