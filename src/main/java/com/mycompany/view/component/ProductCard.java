package com.mycompany.view.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class ProductCard extends JPanel {

    private JLabel lblImage;
    private JLabel lblName;
    private JLabel lblPrice;
    private JButton btnAdd; // Nút thêm vào giỏ (nếu cần)

    public ProductCard(String name, double price, String imagePath) {
        initComponents(name, price, imagePath);
        addHoverEffect();
    }

    private void initComponents(String name, double price, String imagePath) {
        // 1. Setup Panel chính
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(220, 320)); // Kích thước cố định 1 ô
        // Viền mặc định màu xám nhạt
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // 2. Phần Ảnh (Image)
        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(JLabel.CENTER);
        lblImage.setPreferredSize(new Dimension(200, 180));
        // Xử lý load ảnh an toàn
        ImageIcon icon = resizeImage(imagePath, 180, 160);
        if (icon != null) {
            lblImage.setIcon(icon);
        } else {
            lblImage.setText("No Image");
            lblImage.setForeground(Color.GRAY);
        }
        add(lblImage, BorderLayout.CENTER);

        // 3. Phần Thông tin (Tên + Giá) - Đặt ở phía dưới (SOUTH)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        // Tên sản phẩm (Dùng HTML để tự xuống dòng nếu tên dài)
        lblName = new JLabel("<html><div style='text-align: center; width: 180px;'>" + name + "</div></html>");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Giá sản phẩm (Màu đỏ, to hơn)
        lblPrice = new JLabel(String.format("%,.0f VNĐ", price)); // Format tiền tệ
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblPrice.setForeground(new Color(220, 53, 69)); // Màu đỏ đẹp
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Khoảng cách giữa tên và giá
        infoPanel.add(Box.createVerticalStrut(10)); 
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblPrice);
        infoPanel.add(Box.createVerticalStrut(10));

        add(infoPanel, BorderLayout.SOUTH);
    }

    // Hiệu ứng khi di chuột vào ô sản phẩm
    private void addHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Đổi màu viền sang xanh khi hover
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0, 123, 255), 2, true),
                        BorderFactory.createEmptyBorder(9, 9, 9, 9) // Giảm padding 1px để bù viền dày
                ));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Trả về viền xám
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(230, 230, 230), 1, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
        });
    }

    // Hàm tiện ích resize ảnh
    private ImageIcon resizeImage(String path, int targetW, int targetH) {
        try {
            // Logic tìm ảnh: Thử load từ resource hoặc file
            URL imgUrl = getClass().getResource(path);
            ImageIcon imageIcon;
            if (imgUrl != null) {
                imageIcon = new ImageIcon(imgUrl);
            } else {
                imageIcon = new ImageIcon(path); // Thử đường dẫn tuyệt đối nếu không thấy trong resource
            }
            
            if (imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE) return null;

            Image image = imageIcon.getImage(); 
            Image newimg = image.getScaledInstance(targetW, targetH,  java.awt.Image.SCALE_SMOOTH); 
            return new ImageIcon(newimg);  
        } catch (Exception e) {
            return null;
        }
    }
}