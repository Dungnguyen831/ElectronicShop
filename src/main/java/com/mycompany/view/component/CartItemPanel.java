package com.mycompany.view.component;

import com.mycompany.model.OrderDetail;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;

public class CartItemPanel extends JPanel {

    public CartItemPanel(OrderDetail item, String productName, BiConsumer<OrderDetail, Integer> onQtyChange, Consumer<OrderDetail> onDelete) {
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 95)); 

        DecimalFormat df = new DecimalFormat("#,### đ");

        // 1. Thông tin (Trái)
        JPanel pnlInfo = new JPanel(new GridLayout(3, 1));
        pnlInfo.setOpaque(false);
        
        JLabel lblName = new JLabel(productName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblPrice = new JLabel("Đơn giá: " + df.format(item.getUnitPrice()));
        lblPrice.setForeground(Color.GRAY);
        
        JLabel lblTotal = new JLabel("Thành tiền: " + df.format(item.getSubtotal()));
        lblTotal.setForeground(new Color(231, 76, 60));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        pnlInfo.add(lblName);
        pnlInfo.add(lblPrice);
        pnlInfo.add(lblTotal);

        // 2. Nút bấm (Phải)
        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 20));
        pnlControl.setOpaque(false);

        JButton btnDec = createBtn("-", new Color(240, 240, 240));
        btnDec.addActionListener(e -> onQtyChange.accept(item, -1));

        JLabel lblQty = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.CENTER);
        lblQty.setPreferredSize(new Dimension(30, 25));
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnInc = createBtn("+", new Color(240, 240, 240));
        btnInc.addActionListener(e -> onQtyChange.accept(item, 1));

        JButton btnDel = createBtn("X", new Color(231, 76, 60));
        btnDel.setForeground(Color.WHITE);
        btnDel.addActionListener(e -> onDelete.accept(item));

        pnlControl.add(btnDec);
        pnlControl.add(lblQty);
        pnlControl.add(btnInc);
        pnlControl.add(Box.createHorizontalStrut(10));
        pnlControl.add(btnDel);

        add(pnlInfo, BorderLayout.CENTER);
        add(pnlControl, BorderLayout.EAST);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(35, 28));
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }
}
