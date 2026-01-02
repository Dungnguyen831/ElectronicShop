package com.mycompany.view.component;

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
    // Components UI
    private JPanel pnlList;
    private JLabel lblSubTotal;    // Tổng tiền hàng
    private JLabel lblVoucherName; // Tên Voucher hiện tại
    private JLabel lblDiscount;    // Số tiền giảm
    private JLabel lblFinalTotal;  // Khách cần trả
    private JComboBox<String> cboPaymentMethod;
    
    // Data & Callbacks
    private List<OrderDetail> cartItems;
    private ProductDAO productDAO = new ProductDAO();
    private Runnable onCheckout;

    // Constructor 2 tham số (Khớp với SaleFrame)
    public CartPanel(List<OrderDetail> cartItems, Runnable onCheckout) {
        this.cartItems = cartItems;
        this.onCheckout = onCheckout;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(null, "GIỎ HÀNG", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));
        
        // 1. Header (Tiêu đề cột)
        initHeader();
        
        // 2. List (Danh sách sản phẩm cuộn được)
        initList();

        // 3. Footer (Khu vực tính tiền chi tiết)
        initBillSection();
    }

    // --- Tạo Header: "Sản phẩm" --- "Số lượng" ---
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

    // --- Tạo List chứa các CartItemPanel ---
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

    // --- Tạo Khu vực hóa đơn chi tiết ---
    private void initBillSection() {
        JPanel pnlBill = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlBill.setBackground(new Color(245, 245, 245));
        pnlBill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(52, 152, 219)), 
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Dòng 1: Tổng tiền hàng
        pnlBill.add(new JLabel("Tổng tiền hàng:"));
        lblSubTotal = new JLabel("0 đ", SwingConstants.RIGHT); 
        lblSubTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBill.add(lblSubTotal);

        // Dòng 2: Voucher
        pnlBill.add(new JLabel("Mã giảm giá:"));
        lblVoucherName = new JLabel("Chưa áp dụng", SwingConstants.RIGHT);
        pnlBill.add(lblVoucherName);

        // Dòng 3: Tiền giảm
        pnlBill.add(new JLabel("Tiền giảm:"));
        lblDiscount = new JLabel("- 0 đ", SwingConstants.RIGHT);
        lblDiscount.setForeground(new Color(39, 174, 96)); 
        lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlBill.add(lblDiscount);

        // Dòng 4: Phương thức thanh toán
        pnlBill.add(new JLabel("Phương thức TT:"));
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt (CASH)", "Chuyển khoản", "Thẻ"});
        ((JLabel)cboPaymentMethod.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        pnlBill.add(cboPaymentMethod);

        // Dòng 5: Tổng thanh toán
        JLabel lTotal = new JLabel("KHÁCH CẦN TRẢ:"); 
        lTotal.setForeground(new Color(52, 152, 219)); 
        lTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlBill.add(lTotal);
        
        lblFinalTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblFinalTotal.setForeground(Color.RED); 
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pnlBill.add(lblFinalTotal);

        // Nút Thanh Toán
        JButton btnPay = new JButton("Xác nhận & Nhập thông tin");
        btnPay.setPreferredSize(new Dimension(0, 60));
        btnPay.setBackground(new Color(52, 152, 219));
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnPay.addActionListener(e -> {
            if(!cartItems.isEmpty()) onCheckout.run();
            else JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
        });

        JPanel pnlBot = new JPanel(new BorderLayout(0, 10));
        pnlBot.add(pnlBill, BorderLayout.CENTER);
        pnlBot.add(btnPay, BorderLayout.SOUTH);
        add(pnlBot, BorderLayout.SOUTH);
    }

    // --- Hàm vẽ lại giao diện ---
    public void refresh() {
        pnlList.removeAll();
        double total = 0;
        
        for (OrderDetail item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            String name = (p != null) ? p.getProductName() : "SP-" + item.getProductId();
            
            // Tính lại tiền từng món
            item.setSubtotal(item.getQuantity() * item.getUnitPrice());
            total += item.getSubtotal();

            // Thêm Panel con (CartItemPanel)
            pnlList.add(new CartItemPanel(item, name, 
                (i, d) -> changeQty(i, d), 
                (i) -> deleteItem(i)
            ));
        }
        
        // Cập nhật các số liệu cơ bản
        DecimalFormat df = new DecimalFormat("#,### đ");
        lblSubTotal.setText(df.format(total));
        lblFinalTotal.setText(df.format(total)); // Mặc định chưa trừ gì
        
        pnlList.revalidate();
        pnlList.repaint();
    }
    
    // --- Hàm để SaleFrame cập nhật thông số giảm giá từ bên ngoài ---
    public void render(double subTotal, double discount, double finalTotal) {
        refresh(); // Vẽ lại list trước
        
        DecimalFormat df = new DecimalFormat("#,### đ");
        lblSubTotal.setText(df.format(subTotal));
        lblDiscount.setText("- " + df.format(discount));
        lblFinalTotal.setText(df.format(finalTotal));
    }
    
    public void setVoucherName(String name) {
        lblVoucherName.setText(name);
    }

    // --- Logic nội bộ ---
    private void changeQty(OrderDetail item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty > 0) item.setQuantity(newQty);
        else deleteItem(item);
        refresh();
    }
    
    private void deleteItem(OrderDetail item) {
        if(JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == 0) {
            cartItems.remove(item);
            refresh();
        }
    }
    
    public double getSubTotal() {
        return cartItems.stream().mapToDouble(OrderDetail::getSubtotal).sum();
    }
}