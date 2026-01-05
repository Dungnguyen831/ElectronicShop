package com.mycompany.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class UIHelper {

    // 1. Tạo Card thống kê (4 ô màu trên cùng)
    public static JPanel createCard(String title, JLabel lblValue, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(255, 255, 255, 200));
        
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);
        
        JPanel pnlText = new JPanel(new GridLayout(2, 1));
        pnlText.setOpaque(false);
        pnlText.add(lblTitle);
        pnlText.add(lblValue);
        
        card.add(pnlText, BorderLayout.CENTER);
        
        // Icon trang trí
        JLabel lblIcon = new JLabel("*");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        lblIcon.setForeground(new Color(255, 255, 255, 60));
        card.add(lblIcon, BorderLayout.EAST);
        
        return card;
    }

    // 2. Tạo Panel chứa bảng có tiêu đề đẹp
    public static JPanel createTablePanel(String title, Color color) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 2), 
                title, 
                0, 0, new Font("Segoe UI", Font.BOLD, 14), color
        ));
        return pnl;
    }

    // 3. Tạo Bảng chuẩn (Style đẹp + Fix lỗi chiều cao 5 dòng)
    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        
        // Style cơ bản
        table.setRowHeight(40); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Style Header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 249, 250)); 
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30)); 
        
        // [QUAN TRỌNG] Logic tính chiều cao hiển thị (5 dòng)
        table.setPreferredScrollableViewportSize(new Dimension(
                table.getPreferredSize().width, 
                40 * 5 // 40px * 5 dòng = 200px
        ));
        
        // Các setting khác
        table.setFillsViewportHeight(true); 
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);
        table.setShowVerticalLines(false); 
        table.setIntercellSpacing(new Dimension(0, 0)); 
        
        return table;
    }
}