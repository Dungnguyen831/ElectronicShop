package com.mycompany.view.Staff.component;

import com.mycompany.model.Product;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import javax.swing.*;

public class ProductCard extends JPanel {

    private final String IMAGE_DIR = "D:\\Documents\\NetBeansProjects\\ElectronicShop\\src\\main\\java\\com\\mycompany\\util\\upload\\";
    
    private final int FIXED_WIDTH = 220; 
    // GIẢM chiều cao ảnh xuống một chút (từ 180 -> 170 hoặc 165)
    private final int IMAGE_HEIGHT = 165; 

    public ProductCard(Product p, Consumer<Product> onSelect) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5)); 
        setBackground(Color.WHITE);
        
        // --- PANEL CHÍNH ---
        JPanel pnlMain = new JPanel(new BorderLayout(0, 0));
        // GIẢM tổng chiều cao thẻ (từ 300 -> 270 hoặc 275) để ép phần chữ lên gần ảnh hơn
        pnlMain.setPreferredSize(new Dimension(FIXED_WIDTH, 270)); 
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        // 1. PHẦN TRÊN: ẢNH
        JLayeredPane lpImage = new JLayeredPane();
        lpImage.setPreferredSize(new Dimension(FIXED_WIDTH, IMAGE_HEIGHT));

        JLabel lblImg = new JLabel();
        lblImg.setBounds(0, 0, FIXED_WIDTH, IMAGE_HEIGHT); 
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setBackground(new Color(252, 252, 252));
        lblImg.setOpaque(true);
        loadImage(lblImg, p.getImage());

        // Badge số lượng
        int stock = p.getQuantity();
        JLabel lblBadge = new JLabel(stock <= 0 ? "Hết" : String.valueOf(stock), SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(stock <= 0 ? new Color(231, 76, 60) : (stock <= 5 ? new Color(243, 156, 18) : new Color(46, 204, 113)));
        lblBadge.setBounds(FIXED_WIDTH - 50, 0, 50, 28); 

        lpImage.add(lblImg, JLayeredPane.DEFAULT_LAYER);
        lpImage.add(lblBadge, JLayeredPane.PALETTE_LAYER);

        // 2. PHẦN DƯỚI: THÔNG TIN (SỬA LẠI PADDING)
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 2)); // Giảm vgap giữa 2 dòng chữ
        pnlInfo.setBackground(Color.WHITE);
        
        // THAY ĐỔI PADDING: Giảm khoảng cách phía trên (Top) từ 10 xuống 2 hoặc 5
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JLabel lblName = new JLabel("<html><center>" + p.getProductName() + "</center></html>", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblPrice = new JLabel(new DecimalFormat("#,### đ").format(p.getSalePrice()), SwingConstants.CENTER);
        lblPrice.setForeground(new Color(231, 76, 60));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 17));

        pnlInfo.add(lblName);
        pnlInfo.add(lblPrice);

        pnlMain.add(lpImage, BorderLayout.CENTER);
        pnlMain.add(pnlInfo, BorderLayout.SOUTH);

        add(pnlMain);

        // Sự kiện Hover & Click... (Giữ nguyên phần MouseListener của bạn)
        setupEvents(pnlMain, onSelect, p);
    }

    private void setupEvents(JPanel pnl, Consumer<Product> onSelect, Product p) {
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { 
                pnl.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2)); 
            }
            @Override
            public void mouseExited(MouseEvent e) { 
                pnl.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1)); 
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
                ImageIcon icon = new ImageIcon(IMAGE_DIR + imageName);
                if (icon.getIconWidth() > 0) {
                    Image scaled = icon.getImage().getScaledInstance(FIXED_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                    lbl.setIcon(new ImageIcon(scaled));
                    lbl.setText("");
                    return;
                }
            } catch (Exception e) {}
        }
        lbl.setText("Không có ảnh");
    }
}
