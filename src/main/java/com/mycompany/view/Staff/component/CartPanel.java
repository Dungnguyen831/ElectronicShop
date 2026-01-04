package com.mycompany.view.Staff.component;

import com.mycompany.dao.ProductDAO;
import com.mycompany.model.OrderDetail;
import com.mycompany.model.Product;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;

public class CartPanel extends JPanel {
    private JPanel pnlList;
    private JLabel lblSubTotal, lblVoucherName, lblDiscount, lblFinalTotal;
    private JComboBox<String> cboPaymentMethod;
    
    private List<OrderDetail> cartItems;
    private ProductDAO productDAO = new ProductDAO();
    
    private Runnable onCheckout;
    private BiConsumer<OrderDetail, Integer> onQtyChange;
    private Consumer<OrderDetail> onDelete;

    public CartPanel(List<OrderDetail> cartItems, Runnable onCheckout, BiConsumer<OrderDetail, Integer> onQtyChange, Consumer<OrderDetail> onDelete) {
        this.cartItems = cartItems;
        this.onCheckout = onCheckout;
        this.onQtyChange = onQtyChange;
        this.onDelete = onDelete;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(null, "GIỎ HÀNG", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));
        
        initHeader();
        initList();
        initBillSection();
    }

    private void initHeader() {
        JPanel pnlH = new JPanel(new BorderLayout());
        pnlH.setBackground(new Color(230, 230, 230));
        pnlH.setPreferredSize(new Dimension(0, 30));
        pnlH.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JLabel l1 = new JLabel("Sản phẩm"); l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel l2 = new JLabel("SL             "); l2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlH.add(l1, BorderLayout.WEST);
        pnlH.add(l2, BorderLayout.EAST);
        add(pnlH, BorderLayout.NORTH);
    }

    private void initList() {
        pnlList = new JPanel();
        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
        pnlList.setBackground(Color.WHITE);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(pnlList, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void initBillSection() {
        JPanel pnlBill = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlBill.setBackground(new Color(245, 245, 245));
        pnlBill.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        pnlBill.add(new JLabel("Tổng tiền hàng:"));
        lblSubTotal = new JLabel("0 đ", SwingConstants.RIGHT); 
        lblSubTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBill.add(lblSubTotal);

        pnlBill.add(new JLabel("Mã giảm giá:"));
        lblVoucherName = new JLabel("Chưa áp dụng", SwingConstants.RIGHT);
        pnlBill.add(lblVoucherName);

        pnlBill.add(new JLabel("Tiền giảm:"));
        lblDiscount = new JLabel("- 0 đ", SwingConstants.RIGHT);
        lblDiscount.setForeground(new Color(39, 174, 96)); 
        lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBill.add(lblDiscount);

        pnlBill.add(new JLabel("Phương thức TT:"));
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt (CASH)", "Chuyển khoản", "Thẻ"});
        ((JLabel)cboPaymentMethod.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        pnlBill.add(cboPaymentMethod);

        JLabel lTotal = new JLabel("KHÁCH CẦN TRẢ:"); 
        lTotal.setForeground(new Color(52, 152, 219)); 
        lTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlBill.add(lTotal);
        
        lblFinalTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblFinalTotal.setForeground(Color.RED); 
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pnlBill.add(lblFinalTotal);

        JButton btnPay = new JButton("Xác nhận & Nhập thông tin");
        btnPay.setPreferredSize(new Dimension(0, 60));
        btnPay.setBackground(new Color(52, 152, 219));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnPay.addActionListener(e -> onCheckout.run());

        JPanel pnlBot = new JPanel(new BorderLayout(0, 10));
        pnlBot.add(pnlBill, BorderLayout.CENTER);
        pnlBot.add(btnPay, BorderLayout.SOUTH);
        add(pnlBot, BorderLayout.SOUTH);
    }

    public void render(double subTotal, double discount, double finalTotal) {
        pnlList.removeAll();
        for (OrderDetail item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            String name = (p != null) ? p.getProductName() : "SP-" + item.getProductId();
            pnlList.add(new CartItemPanel(item, name, onQtyChange, onDelete));
        }
        
        DecimalFormat df = new DecimalFormat("#,### đ");
        lblSubTotal.setText(df.format(subTotal));
        lblDiscount.setText("- " + df.format(discount));
        lblFinalTotal.setText(df.format(finalTotal));
        
        pnlList.revalidate();
        pnlList.repaint();
    }
    
    public void setVoucherName(String name) {
        lblVoucherName.setText(name);
    }
}