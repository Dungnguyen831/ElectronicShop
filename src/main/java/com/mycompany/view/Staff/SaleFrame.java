package com.mycompany.view.Staff;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SaleFrame extends JFrame {

    // --- Components Toàn Cục ---
    private DefaultTableModel cartModel;
    private JTable tblCart;
    private final String IMAGE_DIR = "D:\\Documents\\NetBeansProjects\\ElectronicShop\\src\\main\\java\\com\\mycompany\\util\\upload\\";
    // Label Chi tiết thanh toán
    private JLabel lblSubTotal;    // Tổng tiền hàng
    private JLabel lblVoucherName; // Tên Voucher
    private JLabel lblDiscount;    // Số tiền giảm
    private JLabel lblFinalTotal;  // Khách cần trả
    private JComboBox<String> cboPaymentMethod;

    // Quản lý giao diện bên phải (CardLayout)
    private CardLayout cardLayoutRight;
    private JPanel pnlRightContainer;
    
    // Components phần Sản phẩm
    private JPanel pnlProductGrid;
    private JTextField txtSearch;
    private JComboBox<Category> cboCategory;

    // Components phần Khách hàng
    private JTextField txtCustName, txtCustPhone, txtCustEmail, txtCustAddress, txtVoucherCode;
    
    // --- DATA & LOGIC ---
    private List<OrderDetail> cartItems = new ArrayList<>();
    
    // Khởi tạo các DAO
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private OrderDAO orderDAO = new OrderDAO();
    
    private double subTotalAmount = 0;
    private double discountAmount = 0;
    private Integer appliedVoucherId = null;
    
    // User hiện tại (Giả lập hoặc lấy từ Login)
    private User currentUser = new User("Tran la luot", "admin", "Admin System", 1); 

    // Constructor nhận User từ LoginFrame
    public SaleFrame(User user) {
        if(user != null) this.currentUser = user;
        initComponents();
        loadCategoriesToCombo();
        loadProducts(null, 0);
    }
    
    // Constructor mặc định (Test)
    public SaleFrame() {
        initComponents();
        loadCategoriesToCombo();
        loadProducts(null, 0);
    }

    private void initComponents() {
        setTitle("HỆ THỐNG POS - MAIZEN SHOP");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ================= 1. HEADER (Logo & User Info) =================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Style.COLOR_BG_LEFT);
        pnlHeader.setPreferredSize(new Dimension(0, 60));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblLogo = new JLabel("BÁN HÀNG TẠI QUẦY");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        // Panel thông tin nhân viên & Đăng xuất
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlUser.setOpaque(false);
        
        String role = (currentUser.getRoleId() == 1) ? "Quản trị viên" : "Nhân viên";
        JLabel lblInfo = new JLabel("<html><div style='text-align: right;'>Xin chào: <b>" + currentUser.getFullName() + "</b><br><span style='font-size:10px'>" + role + "</span></div></html>");
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60)); // Màu đỏ
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) this.dispose();
        });

        pnlUser.add(lblInfo);
        pnlUser.add(btnLogout);
        pnlHeader.add(lblLogo, BorderLayout.WEST);
        pnlHeader.add(pnlUser, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // ================= 2. MAIN CONTAINER =================
        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding khung chính
        
        // --- TRÁI: GIỎ HÀNG ---
        pnlMain.add(createLeftPanel());

        // --- PHẢI: CONTAINER (SẢN PHẨM / KHÁCH HÀNG) ---
        cardLayoutRight = new CardLayout();
        pnlRightContainer = new JPanel(cardLayoutRight);
        
        pnlRightContainer.add(createProductView(), "PRODUCT_VIEW");
        pnlRightContainer.add(createCustomerForm(), "CUSTOMER_VIEW");
        
        pnlMain.add(pnlRightContainer);
        add(pnlMain, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    //                          PANEL TRÁI (GIỎ HÀNG)
    // -------------------------------------------------------------------------
    private JPanel createLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBorder(BorderFactory.createTitledBorder(null, "GIỎ HÀNG", 0, 0, Style.FONT_BOLD));

        // Bảng giỏ hàng
        String[] cols = {"Tên sản phẩm", "SL", "Giá", "Tổng"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setRowHeight(35);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(200); // Cột Tên rộng hơn
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnl.add(new JScrollPane(tblCart), BorderLayout.CENTER);

        // Khu vực Chi tiết hóa đơn
        JPanel pnlBill = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlBill.setBackground(new Color(245, 245, 245));
        pnlBill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, Style.COLOR_PRIMARY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 1. Tổng tiền hàng
        pnlBill.add(new JLabel("Tổng tiền hàng:"));
        lblSubTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblSubTotal.setFont(Style.FONT_BOLD);
        pnlBill.add(lblSubTotal);

        // 2. Voucher
        pnlBill.add(new JLabel("Mã giảm giá:"));
        lblVoucherName = new JLabel("Chưa áp dụng", SwingConstants.RIGHT);
        pnlBill.add(lblVoucherName);

        // 3. Tiền giảm
        pnlBill.add(new JLabel("Tiền giảm:"));
        lblDiscount = new JLabel("- 0 đ", SwingConstants.RIGHT);
        lblDiscount.setForeground(new Color(39, 174, 96)); // Xanh lá
        lblDiscount.setFont(Style.FONT_BOLD);
        pnlBill.add(lblDiscount);

        // 4. Phương thức thanh toán
        pnlBill.add(new JLabel("Phương thức TT:"));
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt (CASH)", "Chuyển khoản (BANK)", "Thẻ (CARD)"});
        ((JLabel)cboPaymentMethod.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        pnlBill.add(cboPaymentMethod);

        // 5. Tổng thanh toán
        JLabel lblTotalTxt = new JLabel("KHÁCH CẦN TRẢ:");
        lblTotalTxt.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalTxt.setForeground(Style.COLOR_PRIMARY);
        pnlBill.add(lblTotalTxt);

        lblFinalTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFinalTotal.setForeground(Color.RED);
        pnlBill.add(lblFinalTotal);

        // Nút Thanh toán
        JButton btnPay = new JButton("THANH TOÁN NGAY");
        btnPay.setPreferredSize(new Dimension(0, 60));
        btnPay.setBackground(Style.COLOR_PRIMARY);
        btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnPay.addActionListener(e -> {
            if (cartItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cardLayoutRight.show(pnlRightContainer, "CUSTOMER_VIEW");
        });

        JPanel pnlBottom = new JPanel(new BorderLayout(0, 10));
        pnlBottom.add(pnlBill, BorderLayout.CENTER);
        pnlBottom.add(btnPay, BorderLayout.SOUTH);
        pnl.add(pnlBottom, BorderLayout.SOUTH);

        return pnl;
    }

    // -------------------------------------------------------------------------
    //                  PANEL PHẢI 1: DANH SÁCH SẢN PHẨM (ĐÃ SỬA LỖI DÀI)
    // -------------------------------------------------------------------------
    private JPanel createProductView() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // --- Thanh Tìm kiếm & Lọc ---
        JPanel pnlFilter = new JPanel(new BorderLayout(10, 0));
        
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên sản phẩm, mã vạch...");
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 5)
        ));
        
        cboCategory = new JComboBox<>();
        cboCategory.setPreferredSize(new Dimension(200, 40));
        cboCategory.setBackground(Color.WHITE);
        
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setBackground(Style.COLOR_PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        
        // Logic tìm kiếm
        ActionListener searchAction = e -> {
            String kw = txtSearch.getText();
            Category cat = (Category) cboCategory.getSelectedItem();
            int catId = (cat != null) ? cat.getCategoryId() : 0;
            loadProducts(kw, catId);
        };
        btnSearch.addActionListener(searchAction);
        cboCategory.addActionListener(searchAction);

        pnlFilter.add(txtSearch, BorderLayout.CENTER);
        pnlFilter.add(cboCategory, BorderLayout.EAST);
        pnlFilter.add(btnSearch, BorderLayout.WEST);

        // --- Lưới Sản phẩm (ĐÃ SỬA LỖI KÉO DÀI) ---
        // Sử dụng Wrapper Panel để chặn GridLayout giãn chiều cao
        pnlProductGrid = new JPanel(new GridLayout(0, 3, 15, 15)); // 3 cột, khoảng cách 15px
        pnlProductGrid.setBackground(Color.WHITE);

        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.add(pnlProductGrid, BorderLayout.NORTH); // QUAN TRỌNG: Đẩy lưới lên trên cùng

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        pnl.add(pnlFilter, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        
        return pnl;
    }

    // Hàm tạo thẻ sản phẩm (Card) với kích thước cố định
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(180, 260)); 
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // Tạo Label chứa ảnh
        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(180, 140)); // Kích thước cố định
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setBackground(new Color(245, 245, 245));
        lblImg.setOpaque(true);

        // --- XỬ LÝ HIỂN THỊ ẢNH ---
        String imageName = p.getImage(); // Lấy tên file từ DB (VD: iphone15.jpg)
        
        if (imageName != null && !imageName.isEmpty()) {
            try {
                // 1. Tạo đường dẫn đầy đủ: Thư mục gốc + Tên file
                String fullPath = IMAGE_DIR + imageName;
                
                // 2. Load ảnh từ đường dẫn
                ImageIcon originalIcon = new ImageIcon(fullPath);
                
                // Kiểm tra xem file có thực sự tồn tại không (tránh lỗi width=-1)
                if (originalIcon.getIconWidth() > 0) {
                    // 3. Resize ảnh cho vừa với khung (180x140)
                    Image img = originalIcon.getImage();
                    Image scaledImg = img.getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                    
                    // 4. Gán ảnh đã resize vào Label
                    lblImg.setIcon(new ImageIcon(scaledImg));
                } else {
                    lblImg.setText("Ảnh lỗi"); // File không tồn tại trên ổ cứng
                }
            } catch (Exception e) {
                e.printStackTrace();
                lblImg.setText("Lỗi Load");
            }
        } else {
            lblImg.setText("No Image"); // Trong DB là null
        }
        // ---------------------------

        // Phần thông tin bên dưới (Giữ nguyên)
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

        card.add(lblImg, BorderLayout.CENTER);
        card.add(pnlInfo, BorderLayout.SOUTH);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(Style.COLOR_PRIMARY, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
            @Override
            public void mouseClicked(MouseEvent e) { addToCart(p); }
        });

        return card;
    }

    // -------------------------------------------------------------------------
    //                  PANEL PHẢI 2: FORM KHÁCH HÀNG & THANH TOÁN
    // -------------------------------------------------------------------------
    private JScrollPane createCustomerForm() {
        JPanel pnl = new JPanel(null);
        pnl.setPreferredSize(new Dimension(600, 750));
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("THÔNG TIN KHÁCH MUA HÀNG");
        lblTitle.setFont(Style.FONT_HEADER);
        lblTitle.setForeground(Style.COLOR_PRIMARY);
        lblTitle.setBounds(30, 20, 400, 40);
        pnl.add(lblTitle);

        int y = 80;
        
        // Tìm khách hàng
        createLabel(pnl, "Số điện thoại (*):", 30, y);
        txtCustPhone = new JTextField();
        txtCustPhone.setBounds(30, y+30, 400, 35);
        pnl.add(txtCustPhone);
        
        JButton btnCheck = new JButton("Tìm");
        btnCheck.setBounds(440, y+30, 80, 35);
        btnCheck.setBackground(Style.COLOR_PRIMARY);
        btnCheck.setForeground(Color.WHITE);
        pnl.add(btnCheck);
        
        btnCheck.addActionListener(e -> {
            String phone = txtCustPhone.getText().trim();
            if(phone.isEmpty()) return;
            Customer c = customerDAO.findByPhone(phone);
            if(c != null) {
                txtCustName.setText(c.getFullName());
                txtCustEmail.setText(c.getEmail());
                txtCustAddress.setText(c.getAddress());
                JOptionPane.showMessageDialog(this, "Khách hàng thân thiết: " + c.getFullName() + "\nĐiểm: " + c.getPoints());
            } else {
                JOptionPane.showMessageDialog(this, "Khách hàng mới. Mời nhập thông tin.");
                txtCustName.requestFocus();
            }
        });

        // Các ô nhập liệu
        createLabelInput(pnl, "Họ và tên:", txtCustName = new JTextField(), y+=80);
        createLabelInput(pnl, "Email:", txtCustEmail = new JTextField(), y+=80);
        createLabelInput(pnl, "Địa chỉ:", txtCustAddress = new JTextField(), y+=80);

        // Voucher
        JLabel lblVou = new JLabel("Mã Voucher (nếu có):");
        lblVou.setFont(Style.FONT_BOLD);
        lblVou.setBounds(30, y+=80, 200, 25);
        pnl.add(lblVou);
        
        txtVoucherCode = new JTextField();
        txtVoucherCode.setBounds(30, y+30, 300, 35);
        pnl.add(txtVoucherCode);
        
        JButton btnApply = new JButton("Áp dụng");
        btnApply.setBounds(340, y+30, 100, 35);
        btnApply.setBackground(new Color(39, 174, 96));
        btnApply.setForeground(Color.WHITE);
        pnl.add(btnApply);
        btnApply.addActionListener(e -> applyVoucher());

        // Nút Xác nhận
        JButton btnConfirm = new JButton("XÁC NHẬN & HOÀN TẤT");
        btnConfirm.setBackground(new Color(230, 126, 34));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.setBounds(30, y+100, 250, 50);
        btnConfirm.addActionListener(e -> processOrder());

        JButton btnBack = new JButton("QUAY LẠI");
        btnBack.setBounds(300, y+100, 150, 50);
        btnBack.addActionListener(e -> cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW"));

        pnl.add(btnConfirm);
        pnl.add(btnBack);

        return new JScrollPane(pnl);
    }

    private void createLabel(JPanel p, String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setFont(Style.FONT_BOLD);
        l.setBounds(x, y, 200, 25);
        p.add(l);
    }
    private void createLabelInput(JPanel p, String t, JTextField txt, int y) {
        createLabel(p, t, 30, y);
        txt.setBounds(30, y+30, 490, 35);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        p.add(txt);
    }

    // -------------------------------------------------------------------------
    //                              LOGIC NGHIỆP VỤ
    // -------------------------------------------------------------------------

    private void loadCategoriesToCombo() {
        cboCategory.removeAllItems();
        for(Category c : categoryDAO.getAllCategories()) cboCategory.addItem(c);
    }

    private void loadProducts(String kw, int catId) {
        pnlProductGrid.removeAll();
        // Gọi DAO lấy danh sách sản phẩm
        List<Product> list = productDAO.searchProducts(kw, catId);
        
        for(Product p : list) {
            pnlProductGrid.add(createProductCard(p));
        }
        // Vẽ lại giao diện
        pnlProductGrid.revalidate();
        pnlProductGrid.repaint();
    }

    private void addToCart(Product p) {
        boolean exists = false;
        for(OrderDetail item : cartItems) {
            if(item.getProductId() == p.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true; break;
            }
        }
        if(!exists) cartItems.add(new OrderDetail(p.getProductId(), 1, p.getSalePrice()));
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        subTotalAmount = 0;
        DecimalFormat df = new DecimalFormat("#,###");

        for(OrderDetail item : cartItems) {
            subTotalAmount += item.getSubtotal();
            Product p = productDAO.getProductById(item.getProductId());
            String name = (p != null) ? p.getProductName() : "SP-" + item.getProductId();
            cartModel.addRow(new Object[]{ name, item.getQuantity(), df.format(item.getUnitPrice()), df.format(item.getSubtotal()) });
        }
        
        double finalTotal = subTotalAmount - discountAmount;
        if(finalTotal < 0) finalTotal = 0;

        lblSubTotal.setText(df.format(subTotalAmount) + " đ");
        lblDiscount.setText("- " + df.format(discountAmount) + " đ");
        lblFinalTotal.setText(df.format(finalTotal) + " đ");
    }

    private void applyVoucher() {
        String code = txtVoucherCode.getText().trim();
        if(code.isEmpty()) return;
        Voucher v = voucherDAO.findByCode(code);
        
        if(v != null) {
            discountAmount = (subTotalAmount * v.getDiscountPercent()) / 100.0;
            if(v.getMaxDiscount() > 0 && discountAmount > v.getMaxDiscount()) 
                discountAmount = v.getMaxDiscount();
            appliedVoucherId = v.getVoucherId();
            lblVoucherName.setText(code + " (-" + v.getDiscountPercent() + "%)");
            JOptionPane.showMessageDialog(this, "Áp dụng mã giảm giá thành công!");
        } else {
            discountAmount = 0;
            appliedVoucherId = null;
            lblVoucherName.setText("Không hợp lệ");
            JOptionPane.showMessageDialog(this, "Mã Voucher không đúng hoặc hết hạn!");
        }
        updateCartDisplay();
    }

    private void processOrder() {
        if(cartItems.isEmpty()) return;
        String phone = txtCustPhone.getText().trim();
        if(phone.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT Khách hàng!"); return; }

        // B1: Xử lý Khách hàng
        Customer c = customerDAO.findByPhone(phone);
        if(c == null) {
            c = new Customer();
            c.setFullName(txtCustName.getText());
            c.setPhone(phone);
            c.setEmail(txtCustEmail.getText());
            c.setAddress(txtCustAddress.getText());
            c.setPoints(0);
            customerDAO.addCustomer(c);
            c = customerDAO.findByPhone(phone); // Lấy lại để có ID
        }

        // B2: Gọi DAO tạo đơn
        double finalTotal = subTotalAmount - discountAmount;
        if(finalTotal < 0) finalTotal = 0;

        boolean success = orderDAO.createOrder(
            currentUser.getUserId(), 
            c.getCustomerId(), 
            appliedVoucherId, 
            finalTotal, 
            cartItems
        );

        if(success) {
            JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG! \nĐiểm tích lũy: +" + (int)(finalTotal/100000));
            // Reset
            cartItems.clear();
            appliedVoucherId = null;
            discountAmount = 0;
            lblVoucherName.setText("Chưa áp dụng");
            updateCartDisplay();
            txtCustPhone.setText("");
            txtCustName.setText("");
            txtVoucherCode.setText("");
            cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán! Vui lòng thử lại.");
        }
    }
}