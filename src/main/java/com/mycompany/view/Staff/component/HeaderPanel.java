package com.mycompany.view.Staff.component;

import com.mycompany.model.User;
import com.mycompany.util.Style; // Nếu có
import java.awt.*;
import javax.swing.*;

public class HeaderPanel extends JPanel {
    public HeaderPanel(User user, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80)); // Màu xanh đậm
        setPreferredSize(new Dimension(0, 60));
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblLogo = new JLabel("POS SYSTEM");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlUser.setOpaque(false);
        
        JLabel lblInfo = new JLabel("<html><div style='text-align: right; color: white'>NV: <b>" + user.getFullName() + "</b></div></html>");
        
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> onLogout.run());

        pnlUser.add(lblInfo);
        pnlUser.add(btnLogout);
        
        add(lblLogo, BorderLayout.WEST);
        add(pnlUser, BorderLayout.EAST);
    }
}