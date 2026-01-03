/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view;

import com.mycompany.view.admin.AdminMainFrame;
import com.mycompany.dao.UserDAO;
import com.mycompany.model.User;
import com.mycompany.util.Style;
//import com.mycompany.view.Staff.SaleFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng Nhập Hệ Thống");
        setSize(850, 500); // Kích thước hình chữ nhật dài
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2)); // Chia đôi màn hình (1 dòng 2 cột)

        // --- PANEL TRÁI (Branding) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Style.COLOR_BG_LEFT);
        leftPanel.setLayout(new GridBagLayout()); // Căn giữa nội dung

        JLabel lblLogo = new JLabel("SHOP MANAGER");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLogo.setForeground(Color.WHITE);
        
        JLabel lblSlogan = new JLabel("Hệ thống quản lý bán hàng chuyên nghiệp");
        lblSlogan.setFont(Style.FONT_SUBTITLE);
        lblSlogan.setForeground(new Color(200, 200, 200));

        // Thêm vào panel trái
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(lblLogo, gbc);
        gbc.gridy = 1; 
        gbc.insets = new Insets(10, 0, 0, 0); // Cách dòng
        leftPanel.add(lblSlogan, gbc);

        this.add(leftPanel);

        // --- PANEL PHẢI (Form nhập liệu) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Style.COLOR_BG_RIGHT);
        rightPanel.setLayout(null); // Dùng Absolute Layout để chỉnh vị trí tự do cho đẹp

        // Tiêu đề form
        JLabel lblTitle = new JLabel("WELCOME BACK");
        lblTitle.setFont(Style.FONT_HEADER);
        lblTitle.setForeground(Style.COLOR_PRIMARY);
        lblTitle.setBounds(100, 50, 300, 40);
        rightPanel.add(lblTitle);

        // Ô nhập Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(Style.FONT_BOLD);
        lblUser.setBounds(50, 120, 100, 30);
        rightPanel.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(50, 150, 320, 35);
        txtUsername.setFont(Style.FONT_REGULAR);
        txtUsername.setBorder(Style.BORDER_BOTTOM_NORMAL); // Chỉ gạch chân
        txtUsername.setBackground(Style.COLOR_BG_RIGHT);
        rightPanel.add(txtUsername);

        // Ô nhập Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(Style.FONT_BOLD);
        lblPass.setBounds(50, 200, 100, 30);
        rightPanel.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(50, 230, 320, 35);
        txtPassword.setFont(Style.FONT_REGULAR);
        txtPassword.setBorder(Style.BORDER_BOTTOM_NORMAL);
        txtPassword.setBackground(Style.COLOR_BG_RIGHT);
        rightPanel.add(txtPassword);

        // Hiệu ứng focus cho ô nhập liệu (Đổi màu đường gạch chân)
        addFocusEffect(txtUsername);
        addFocusEffect(txtPassword);

        // Nút Đăng nhập
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBounds(50, 300, 150, 45);
        btnLogin.setBackground(Style.COLOR_PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(Style.FONT_BOLD);
        btnLogin.setFocusPainted(false); // Bỏ viền focus xấu
        btnLogin.setBorderPainted(false); // Bỏ viền lồi lõm
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi chuột thành bàn tay
        
        // Hiệu ứng hover nút
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(Style.COLOR_PRIMARY_DARK); }
            public void mouseExited(MouseEvent e) { btnLogin.setBackground(Style.COLOR_PRIMARY); }
        });
        
        // Xử lý sự kiện click
        btnLogin.addActionListener(e -> handleLogin());
        rightPanel.add(btnLogin);

        // Nút Đăng ký (Dạng link)
        btnRegister = new JButton("Đăng ký tài khoản?");
        btnRegister.setBounds(210, 300, 160, 45);
        btnRegister.setBackground(Color.WHITE);
        btnRegister.setForeground(Style.COLOR_PRIMARY);
        btnRegister.setFont(Style.FONT_REGULAR);
        btnRegister.setBorder(null);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> JOptionPane.showMessageDialog(this, "Tính năng đang phát triển"));
        rightPanel.add(btnRegister);

        this.add(rightPanel);
    }

    // Hàm tạo hiệu ứng đổi màu border khi click vào ô nhập
    private void addFocusEffect(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(Style.BORDER_BOTTOM_FOCUS);
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(Style.BORDER_BOTTOM_NORMAL);
            }
        });
    }

    private void handleLogin() {
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        UserDAO dao = new UserDAO();
        User u = dao.checkLogin(user, pass);

        if (u != null) {
           JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
    
    // Đóng form đăng nhập
    this.dispose();

    // Kiểm tra quyền để mở cửa sổ tương ứng
    switch (u.getRoleId()) {
       case 1: // ADMIN
        //    Mở giao diện Quản trị viên
            new com.mycompany.view.admin.AdminMainFrame(u).setVisible(true);
            break;

       case 2: // STAFF (Nhân viên bán hàng)
            // Mở giao diện Bán hàng (Full màn hình cho chuyên nghiệp)
//            SaleFrame salesFrame = new SaleFrame(u);
//            salesFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
//            salesFrame.setVisible(true);
//            break;

        case 3: // WAREHOUSE (Thủ kho)
      //       Mở giao diện Kho
            new com.mycompany.view.warehouse.WarehouseMainFrame(u).setVisible(true);
            break;
            
        default:
            JOptionPane.showMessageDialog(this, "Tài khoản không có quyền truy cập!");
            new LoginFrame().setVisible(true); // Mở lại đăng nhập
            break;
    }
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
