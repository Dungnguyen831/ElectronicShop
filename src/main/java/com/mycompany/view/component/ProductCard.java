package com.mycompany.view.component;

import com.mycompany.model.Product;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import javax.swing.*;

public class ProductCard extends JPanel {
    
    // Đường dẫn ảnh (Bạn tự sửa lại cho đúng máy bạn)
    private final String IMAGE_DIR = "D:\\Desktop\\java\\ElectronicShop\\src\\main\\java\\com\\mycompany\\util\\upload\\";

    public ProductCard(Product p, Consumer<Product> onSelect) {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(180, 260)); 
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // 1. Ảnh
        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(180, 140));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setBackground(new Color(245, 245, 245));
        lblImg.setOpaque(true);
        loadImage(lblImg, p.getImage());

        // 2. Thông tin
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel lblName = new JLabel("<html><center>" + p.getProductName() + "</center></html>", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel lblPrice = new JLabel(new DecimalFormat("#,### đ").format(p.getSalePrice()), SwingConstants.CENTER);
        lblPrice.setForeground(new Color(231, 76, 60));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));

        pnlInfo.add(lblName);
        pnlInfo.add(lblPrice);

        add(lblImg, BorderLayout.CENTER);
        add(pnlInfo, BorderLayout.SOUTH);

        // 3. Sự kiện Click
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(new Color(46, 204, 113), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.accept(p);
            }
        });
    }

    private void loadImage(JLabel lbl, String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            try {
                String fullPath = IMAGE_DIR + imageName;
                ImageIcon originalIcon = new ImageIcon(fullPath);
                if (originalIcon.getIconWidth() > 0) {
                    Image img = originalIcon.getImage();
                    Image scaledImg = img.getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                    lbl.setIcon(new ImageIcon(scaledImg));
                    return;
                }
            } catch (Exception e) { }
        }
        lbl.setText("No Image");
    }
}
