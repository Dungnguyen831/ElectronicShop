package com.mycompany.view.Staff;

import com.mycompany.util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SaleFrame extends JFrame {

    // CardLayout để tráo đổi giao diện bên phải
    private CardLayout rightCardLayout;
    private JPanel pnlRightContainer;

    // Components phần Khách hàng (CRM)
    private JTextField txtCustName, txtCustPhone, txtCustEmail, txtCustAddress;
    private JLabel lblCustPointsDisplay; // Dùng Label để nhân viên không tự sửa điểm được

    // Components phần Bán hàng
    private JTable tblCart;
    private DefaultTableModel cartModel;
    private JLabel lblTotalAmount, lblDiscountAmount, lblFinalPay;
    private JButton btnPay, btnConfirmOrder, btnBackToProducts;

    public SaleFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("HỆ THỐNG BÁN HÀNG POS - ELECTRONICS SHOP");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        // ================= 1. THANH MENU TRÊN CÙNG =================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Style.COLOR_BG_LEFT);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        JLabel lblTitle = new JLabel("  GIAO DIỆN BÁN HÀNG CHUYÊN NGHIỆP");
        lblTitle.setFont(Style.FONT_BOLD);
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // ================= 2. BÊN TRÁI: GIỎ HÀNG & TỔNG TIỀN =================
        JPanel pnlLeft = createCartSection();
        add(pnlLeft, BorderLayout.CENTER);

        // ================= 3. BÊN PHẢI: CONTAINER (CARD LAYOUT) =================
        rightCardLayout = new CardLayout();
        pnlRightContainer = new JPanel(rightCardLayout);
        pnlRightContainer.setPreferredSize(new Dimension(600, 0));

        // Card 1: Danh sách sản phẩm
        JPanel pnlProductList = createProductListPanel();
        
        // Card 2: Form nhập thông tin khách hàng (Phần bổ sung theo ảnh)
        JPanel pnlCustomerForm = createCustomerInputForm();

        pnlRightContainer.add(pnlProductList, "PRODUCT_CARD");
        pnlRightContainer.add(pnlCustomerForm, "CUSTOMER_CARD");

        add(pnlRightContainer, BorderLayout.EAST);

        // Xử lý sự kiện nút THANH TOÁN
        btnPay.addActionListener(e -> rightCardLayout.show(pnlRightContainer, "CUSTOMER_CARD"));
    }

    private JPanel createCartSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Bảng giỏ hàng
        String[] cols = {"Mã SP", "Tên sản phẩm", "SL", "Giá", "Tổng"};
        cartModel = new DefaultTableModel(cols, 0);
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(35);
        
        // Khu vực thanh toán bên dưới giỏ hàng
        JPanel pnlCheckout = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlCheckout.setBackground(Color.WHITE);
        pnlCheckout.add(new JLabel("Tổng tiền hàng:"));
        lblTotalAmount = new JLabel("0 đ", SwingConstants.RIGHT);
        pnlCheckout.add(lblTotalAmount);

        pnlCheckout.add(new JLabel("Giảm giá:"));
        lblDiscountAmount = new JLabel("0 đ", SwingConstants.RIGHT);
        lblDiscountAmount.setForeground(Color.RED);
        pnlCheckout.add(lblDiscountAmount);

        lblFinalPay = new JLabel("KHÁCH CẦN TRẢ:");
        lblFinalPay.setFont(Style.FONT_HEADER);
        lblFinalPay.setForeground(Style.COLOR_PRIMARY);
        pnlCheckout.add(lblFinalPay);

        btnPay = new JButton("THANH TOÁN");
        btnPay.setFont(Style.FONT_HEADER);
        btnPay.setBackground(new Color(230, 126, 34)); // Màu cam nhấn mạnh
        btnPay.setForeground(Color.WHITE);
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnl.add(new JScrollPane(tblCart), BorderLayout.CENTER);
        pnl.add(pnlCheckout, BorderLayout.SOUTH);
        pnl.add(btnPay, BorderLayout.SOUTH);
        
        return pnl;
    }

    private JPanel createProductListPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(new Color(245, 246, 250));

        JTextField txtSearch = new JTextField("  Tìm sản phẩm...");
        txtSearch.setPreferredSize(new Dimension(0, 40));
        pnl.add(txtSearch, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        // Thêm sản phẩm mẫu
        for(int i=0; i<9; i++) pnlGrid.add(createProductCard("Sản phẩm mẫu " + i, "1.000.000 đ"));
        
        pnl.add(new JScrollPane(pnlGrid), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createCustomerInputForm() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(Style.COLOR_BG_RIGHT);

        JLabel lblTitle = new JLabel("THÀNH VIÊN & THANH TOÁN");
        lblTitle.setFont(Style.FONT_HEADER);
        lblTitle.setForeground(Style.COLOR_PRIMARY);
        lblTitle.setBounds(50, 30, 400, 40);
        pnl.add(lblTitle);

        // Các trường nhập liệu (Dựa trên bảng customers trong DB)
        createInputField(pnl, "Tên khách hàng:", txtCustName = new JTextField(), 100);
        createInputField(pnl, "Số điện thoại:", txtCustPhone = new JTextField(), 180);
        createInputField(pnl, "Email:", txtCustEmail = new JTextField(), 260);
        createInputField(pnl, "Địa chỉ:", txtCustAddress = new JTextField(), 340);

        // Hiển thị điểm - KHÔNG CHO NHÂN VIÊN NHẬP (Dùng Label)
        JLabel lblPointsTitle = new JLabel("Điểm tích lũy:");
        lblPointsTitle.setFont(Style.FONT_BOLD);
        lblPointsTitle.setBounds(50, 420, 150, 30);
        pnl.add(lblPointsTitle);

        lblCustPointsDisplay = new JLabel("0"); // Mặc định là 0
        lblCustPointsDisplay.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblCustPointsDisplay.setForeground(new Color(39, 174, 96));
        lblCustPointsDisplay.setBounds(180, 420, 100, 30);
        pnl.add(lblCustPointsDisplay);

        // Nút bấm xác nhận
        btnConfirmOrder = new JButton("XÁC NHẬN");
        btnConfirmOrder.setBackground(Style.COLOR_PRIMARY);
        btnConfirmOrder.setForeground(Color.WHITE);
        btnConfirmOrder.setFont(Style.FONT_BOLD);
        btnConfirmOrder.setBounds(50, 500, 180, 45);
        pnl.add(btnConfirmOrder);

        btnBackToProducts = new JButton("QUAY LẠI");
        btnBackToProducts.setBounds(250, 500, 150, 45);
        btnBackToProducts.addActionListener(e -> rightCardLayout.show(pnlRightContainer, "PRODUCT_CARD"));
        pnl.add(btnBackToProducts);

        return pnl;
    }

    private void createInputField(JPanel pnl, String label, JTextField field, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Style.FONT_BOLD);
        lbl.setBounds(50, y, 200, 20);
        pnl.add(lbl);

        field.setBounds(50, y + 25, 450, 35);
        field.setBorder(Style.BORDER_BOTTOM_NORMAL);
        pnl.add(field);
        
        // Hiệu ứng focus từ Style.java
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { field.setBorder(Style.BORDER_BOTTOM_FOCUS); }
            public void focusLost(java.awt.event.FocusEvent e) { field.setBorder(Style.BORDER_BOTTOM_NORMAL); }
        });
    }

    private JPanel createProductCard(String name, String price) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        card.add(new JLabel("IMAGE", SwingConstants.CENTER), BorderLayout.CENTER);
        JLabel lblInfo = new JLabel("<html>" + name + "<br><font color='blue'>" + price + "</font></html>", SwingConstants.CENTER);
        card.add(lblInfo, BorderLayout.SOUTH);
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SaleFrame().setVisible(true));
    }
}