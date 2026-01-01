package com.mycompany.view.component;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class ProductListPanel extends JPanel {
    
    private JPanel gridPanel;

    public ProductListPanel() {
        setLayout(new BorderLayout());
        
        // 1. Tạo Grid Panel (Nơi chứa các ô vuông sản phẩm)
        // GridLayout(0, 4, 20, 20): 0 hàng (tự động), 4 cột, khoảng cách 20px
        gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setBackground(new Color(245, 245, 245)); // Màu nền xám nhẹ cho background
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Căn lề

        // 2. Đặt Grid vào trong ScrollPane (Để cuộn được)
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null); // Bỏ viền xấu của scrollpane
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn chuột
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Chỉ cuộn dọc
        
        add(scrollPane, BorderLayout.CENTER);
    }

    // Hàm thêm 1 sản phẩm vào lưới
    public void addProduct(String name, double price, String imgPath) {
        ProductCard card = new ProductCard(name, price, imgPath);
        gridPanel.add(card);
        revalidate(); // Vẽ lại giao diện
        repaint();
    }
    
    // Hàm xóa hết sản phẩm (để load lại hoặc tìm kiếm)
    public void clearProducts() {
        gridPanel.removeAll();
        revalidate();
        repaint();
    }
    // --- THÊM ĐOẠN NÀY VÀO ProductListPanel.java ---
    
    // Hàm này cho phép thêm 1 thẻ ProductCard đã được gắn sự kiện click từ bên ngoài
    public void addCard(JPanel card) {
        gridPanel.add(card);
        revalidate(); 
        repaint();
    }
}