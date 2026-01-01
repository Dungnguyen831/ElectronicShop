/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view;

import com.mycompany.dao.UserDAO;
import com.mycompany.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Nguyen Anh Dung
 */
public class LoginFrame extends JFrame implements ActionListener {

    // 1. Khai báo các linh kiện (Components)
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;

    // Constructor
    public LoginFrame() {
        initComponents();
    }

    // 2. Hàm thiết lập giao diện (Thay vì NetBeans tự sinh, ta tự viết)
    private void initComponents() {
        // --- Thiết lập cửa sổ chính ---
        setTitle("Đăng Nhập Hệ Thống");
        setSize(400, 250); // Kích thước rộng x cao
        setLocationRelativeTo(null); // Căn giữa màn hình
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Layout chính

        // --- Tạo Tiêu đề (Phần trên - NORTH) ---
        JLabel lblTitle = new JLabel("ELECTRONIC SHOP", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 102, 204)); // Màu xanh
        lblTitle.setBorder(new EmptyBorder(20, 0, 20, 0)); // Tạo khoảng cách đệm
        this.add(lblTitle, BorderLayout.NORTH);

        // --- Tạo Form nhập liệu (Phần giữa - CENTER) ---
        JPanel panelCenter = new JPanel();
        // GridLayout: 2 dòng, 2 cột, khoảng cách ngang 10, dọc 10
        panelCenter.setLayout(new GridLayout(2, 2, 10, 10)); 
        panelCenter.setBorder(new EmptyBorder(10, 40, 10, 40)); // Lùi vào 2 bên

        // Dòng 1: Username
        panelCenter.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField();
        panelCenter.add(txtUsername);

        // Dòng 2: Password
        panelCenter.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panelCenter.add(txtPassword);

        this.add(panelCenter, BorderLayout.CENTER);

        // --- Tạo Nút bấm (Phần dưới - SOUTH) ---
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Căn giữa, cách nhau 20px

        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");

        // Trang trí nút chút xíu
        btnLogin.setBackground(new Color(0, 153, 51));
        btnLogin.setForeground(Color.WHITE);
        
        panelBottom.add(btnLogin);
        panelBottom.add(btnRegister);

        this.add(panelBottom, BorderLayout.SOUTH);

        // 3. Đăng ký sự kiện (Lắng nghe nút bấm)
        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);
    }

    // 4. Xử lý sự kiện khi bấm nút
    @Override
    public void actionPerformed(ActionEvent e) {
        // Nếu bấm nút Đăng nhập
        if (e.getSource() == btnLogin) {
            handleLogin();
        } 
        // Nếu bấm nút Đăng ký
        else if (e.getSource() == btnRegister) {
            handleRegister();
        }
    }

    // Logic xử lý Đăng nhập
    private void handleLogin() {
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Gọi DAO
        UserDAO dao = new UserDAO();
        User u = dao.checkLogin(user, pass);

        if (u != null) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!\nXin chào: " + u.getFullName());
            // TODO: Mở MainFrame tại đây
            // new MainFrame().setVisible(true);
            this.dispose(); // Đóng form login
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Logic xử lý Đăng ký (Demo)
    private void handleRegister() {
        JOptionPane.showMessageDialog(this, "Chức năng đang phát triển...");
    }
}
