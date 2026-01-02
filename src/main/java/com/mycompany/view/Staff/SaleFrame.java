package com.mycompany.view.Staff;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.util.Style;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SaleFrame extends JFrame {

    // --- Components Toàn Cục ---
    private JPanel pnlCartList; // Panel chứa danh sách sản phẩm
    
    // Đường dẫn ảnh (Bạn chỉnh lại cho đúng máy bạn)
    private final String IMAGE_DIR = "D:\\Documents\\NetBeansProjects\\ElectronicShop\\src\\main\\java\\com\\mycompany\\util\\upload\\";
    
    // Label Chi tiết thanh toán
    private JLabel lblSubTotal;    // Tổng tiền hàng
    private JLabel lblVoucherName; // Tên Voucher
    private JLabel lblDiscount;    // Số tiền giảm (Voucher + Điểm)
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
    
    // --- COMPONENT MỚI CHO PHẦN ĐỔI ĐIỂM ---
    private JCheckBox chkUsePoints; // Checkbox hỏi đổi điểm
    private JLabel lblPointInfo;    // Label hiện thông tin điểm (VD: Có 50 điểm = 50k)
    
    // --- DATA & LOGIC ---
    private List<OrderDetail> cartItems = new ArrayList<>();
    
    // Khởi tạo các DAO
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private OrderDAO orderDAO = new OrderDAO();
    
    private double subTotalAmount = 0;
    private double discountAmount = 0; // Tiền giảm từ Voucher
    private double pointsDiscount = 0; // Tiền giảm từ Điểm (MỚI)
    private Integer appliedVoucherId = null;
    private int currentCustomerPoints = 0; // Lưu điểm của khách đang tìm thấy
    
    // User hiện tại
    private User currentUser; 

    // Constructor
    public SaleFrame(User user) {
        if(user != null) {
            this.currentUser = user;
        } else {
            initDefaultUser();
        }
        initComponents();
        loadCategoriesToCombo();
        loadProducts(null, 0);
    }
    
    public SaleFrame() {
        initDefaultUser();
        initComponents();
        loadCategoriesToCombo();
        loadProducts(null, 0);
    }
    
    private void initDefaultUser() {
        this.currentUser = new User();
        this.currentUser.setUserId(2); 
        this.currentUser.setUsername("sales");
        this.currentUser.setFullName("Tran Ban Hang");
        this.currentUser.setRoleId(2);
    }

    private void initComponents() {
        setTitle("HỆ THỐNG POS - MAIZEN SHOP");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ================= 1. HEADER =================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Style.COLOR_BG_LEFT);
        pnlHeader.setPreferredSize(new Dimension(0, 60));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblLogo = new JLabel("BÁN HÀNG TẠI QUẦY");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlUser.setOpaque(false);
        String role = (currentUser.getRoleId() == 1) ? "Quản trị viên" : "Nhân viên";
        JLabel lblInfo = new JLabel("<html><div style='text-align: right;'>Xin chào: <b>" + currentUser.getFullName() + "</b><br><span style='font-size:10px'>" + role + "</span></div></html>");
        lblInfo.setForeground(Color.WHITE);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60)); 
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
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // --- TRÁI: GIỎ HÀNG ---
        pnlMain.add(createLeftPanel());

        // --- PHẢI: CONTAINER ---
        cardLayoutRight = new CardLayout();
        pnlRightContainer = new JPanel(cardLayoutRight);
        
        pnlRightContainer.add(createProductView(), "PRODUCT_VIEW");
        pnlRightContainer.add(createCustomerForm(), "CUSTOMER_VIEW");
        
        pnlMain.add(pnlRightContainer);
        add(pnlMain, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    //                  PANEL TRÁI (GIỎ HÀNG)
    // -------------------------------------------------------------------------
    private JPanel createLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBorder(BorderFactory.createTitledBorder(null, "GIỎ HÀNG", 0, 0, Style.FONT_BOLD));

        // 1. Header giả của danh sách
        JPanel pnlHeaderList = new JPanel(new BorderLayout());
        pnlHeaderList.setBackground(new Color(230, 230, 230));
        pnlHeaderList.setPreferredSize(new Dimension(0, 30));
        pnlHeaderList.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JLabel lblH1 = new JLabel("Sản phẩm");
        lblH1.setFont(Style.FONT_BOLD);
        JLabel lblH2 = new JLabel("SL             "); 
        lblH2.setFont(Style.FONT_BOLD);
        
        pnlHeaderList.add(lblH1, BorderLayout.WEST);
        pnlHeaderList.add(lblH2, BorderLayout.EAST);
        pnl.add(pnlHeaderList, BorderLayout.NORTH);

        // 2. Danh sách sản phẩm (Dùng JPanel cuộn dọc)
        pnlCartList = new JPanel();
        pnlCartList.setLayout(new BoxLayout(pnlCartList, BoxLayout.Y_AXIS));
        pnlCartList.setBackground(Color.WHITE);

        // Wrapper để đẩy items lên trên cùng
        JPanel pnlListWrapper = new JPanel(new BorderLayout());
        pnlListWrapper.setBackground(Color.WHITE);
        pnlListWrapper.add(pnlCartList, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pnlListWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pnl.add(scroll, BorderLayout.CENTER);

        // 3. Khu vực Chi tiết hóa đơn
        JPanel pnlBill = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlBill.setBackground(new Color(245, 245, 245));
        pnlBill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, Style.COLOR_PRIMARY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        pnlBill.add(new JLabel("Tổng tiền hàng:"));
        lblSubTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblSubTotal.setFont(Style.FONT_BOLD);
        pnlBill.add(lblSubTotal);

        pnlBill.add(new JLabel("Mã giảm giá:"));
        lblVoucherName = new JLabel("Chưa áp dụng", SwingConstants.RIGHT);
        pnlBill.add(lblVoucherName);

        pnlBill.add(new JLabel("Tiền giảm:"));
        lblDiscount = new JLabel("- 0 đ", SwingConstants.RIGHT);
        lblDiscount.setForeground(new Color(39, 174, 96));
        lblDiscount.setFont(Style.FONT_BOLD);
        pnlBill.add(lblDiscount);

        pnlBill.add(new JLabel("Phương thức TT:"));
        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt (CASH)", "Chuyển khoản (BANK)", "Thẻ (CARD)"});
        ((JLabel)cboPaymentMethod.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        pnlBill.add(cboPaymentMethod);

        JLabel lblTotalTxt = new JLabel("KHÁCH CẦN TRẢ:");
        lblTotalTxt.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalTxt.setForeground(Style.COLOR_PRIMARY);
        pnlBill.add(lblTotalTxt);

        lblFinalTotal = new JLabel("0 đ", SwingConstants.RIGHT);
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFinalTotal.setForeground(Color.RED);
        pnlBill.add(lblFinalTotal);

        JButton btnPay = new JButton("Xác nhận đơn & Thông tin khách hàng");
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

    // --- HÀM RENDER GIỎ HÀNG ---
    private void renderCartItems() {
        pnlCartList.removeAll(); 
        subTotalAmount = 0;
        DecimalFormat df = new DecimalFormat("#,### đ");

        for (OrderDetail item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            String pName = (p != null) ? p.getProductName() : "SP-" + item.getProductId();
            
            // Tính lại subtotal ( SL * Giá )
            double currentItemTotal = item.getQuantity() * item.getUnitPrice();
            item.setSubtotal(currentItemTotal); 
            
            subTotalAmount += currentItemTotal;

            // --- TẠO 1 DÒNG (ROW) ---
            JPanel row = new JPanel(new BorderLayout(5, 5));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            // Tăng chiều cao để chứa đủ 3 dòng thông tin
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95)); 

            // Thông tin (Trái)
            JPanel pnlInfo = new JPanel(new GridLayout(3, 1)); 
            pnlInfo.setOpaque(false);
            
            JLabel lblName = new JLabel(pName);
            lblName.setFont(Style.FONT_BOLD);
            
            JLabel lblPrice = new JLabel("Đơn giá: " + df.format(item.getUnitPrice()));
            lblPrice.setForeground(Color.GRAY);
            lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JLabel lblItemTotal = new JLabel("Thành tiền: " + df.format(currentItemTotal));
            lblItemTotal.setForeground(new Color(231, 76, 60)); 
            lblItemTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
            
            pnlInfo.add(lblName);
            pnlInfo.add(lblPrice);
            pnlInfo.add(lblItemTotal);

            // Nút bấm (Phải): [ - ] [ Qty ] [ + ] [ X ]
            JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 20)); 
            pnlControl.setOpaque(false);

            JButton btnDec = createSmallButton("-", new Color(240, 240, 240));
            btnDec.addActionListener(e -> changeQty(item, -1));

            JLabel lblQty = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.CENTER);
            lblQty.setPreferredSize(new Dimension(30, 25));
            lblQty.setFont(Style.FONT_BOLD);

            JButton btnInc = createSmallButton("+", new Color(240, 240, 240));
            btnInc.addActionListener(e -> changeQty(item, 1));

            JButton btnDel = createSmallButton("X", new Color(231, 76, 60));
            btnDel.setForeground(Color.WHITE);
            btnDel.addActionListener(e -> {
                int cf = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if(cf == JOptionPane.YES_OPTION) {
                    cartItems.remove(item);
                    renderCartItems();
                }
            });

            pnlControl.add(btnDec);
            pnlControl.add(lblQty);
            pnlControl.add(btnInc);
            pnlControl.add(Box.createHorizontalStrut(10));
            pnlControl.add(btnDel);

            row.add(pnlInfo, BorderLayout.CENTER);
            row.add(pnlControl, BorderLayout.EAST);
            pnlCartList.add(row);
        }

        pnlCartList.revalidate();
        pnlCartList.repaint();
        updateBillTotals(); // Cập nhật tổng tiền hóa đơn
    }

    private JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(35, 28));
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    private void changeQty(OrderDetail item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty > 0) {
            item.setQuantity(newQty); 
            renderCartItems();
        } else {
            int cf = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if(cf == JOptionPane.YES_OPTION) {
                cartItems.remove(item);
                renderCartItems();
            }
        }
    }

    // --- CẬP NHẬT TỔNG TIỀN (BAO GỒM CẢ ĐIỂM) ---
    private void updateBillTotals() {
        DecimalFormat df = new DecimalFormat("#,### đ");
        
        // Công thức: Tổng - Voucher - Điểm
        double finalTotal = subTotalAmount - discountAmount - pointsDiscount;
        if(finalTotal < 0) finalTotal = 0;

        lblSubTotal.setText(df.format(subTotalAmount));
        
        // Hiển thị tổng tiền giảm (Voucher + Điểm)
        double totalDiscount = discountAmount + pointsDiscount;
        lblDiscount.setText("- " + df.format(totalDiscount));
        
        lblFinalTotal.setText(df.format(finalTotal));
    }

    // -------------------------------------------------------------------------
    //                  PANEL PHẢI 1: DANH SÁCH SẢN PHẨM
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

        // --- Lưới Sản phẩm ---
        pnlProductGrid = new JPanel(new GridLayout(0, 3, 15, 15)); 
        pnlProductGrid.setBackground(Color.WHITE);

        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.add(pnlProductGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        pnl.add(pnlFilter, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);
        
        return pnl;
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(180, 260)); 
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(180, 140));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setBackground(new Color(245, 245, 245));
        lblImg.setOpaque(true);

        String imageName = p.getImage();
        if (imageName != null && !imageName.isEmpty()) {
            try {
                String fullPath = IMAGE_DIR + imageName;
                ImageIcon originalIcon = new ImageIcon(fullPath);
                if (originalIcon.getIconWidth() > 0) {
                    Image img = originalIcon.getImage();
                    Image scaledImg = img.getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                    lblImg.setIcon(new ImageIcon(scaledImg));
                } else {
                    lblImg.setText("Ảnh lỗi");
                }
            } catch (Exception e) {
                lblImg.setText("Lỗi Load");
            }
        } else {
            lblImg.setText("No Image");
        }

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
        lblTitle.setBounds(30, 20, 500, 40);
        pnl.add(lblTitle);

        int y = 80;
        
        createLabel(pnl, "Số điện thoại (*):", 30, y);
        txtCustPhone = new JTextField();
        txtCustPhone.setBounds(30, y+30, 400, 35);
        pnl.add(txtCustPhone);
        
        JButton btnCheck = new JButton("Tìm");
        btnCheck.setBounds(440, y+30, 80, 35);
        btnCheck.setBackground(Style.COLOR_PRIMARY);
        btnCheck.setForeground(Color.WHITE);
        pnl.add(btnCheck);
        
        // --- LOGIC TÌM KHÁCH (CÓ LẤY ĐIỂM) ---
        btnCheck.addActionListener(e -> {
            String phone = txtCustPhone.getText().trim();
            if(phone.isEmpty()) return;
            Customer c = customerDAO.findByPhone(phone);
            if(c != null) {
                txtCustName.setText(c.getFullName());
                txtCustEmail.setText(c.getEmail());
                txtCustAddress.setText(c.getAddress());
                
                // Lấy điểm từ DB
                currentCustomerPoints = c.getPoints();
                lblPointInfo.setText("Có: " + currentCustomerPoints + " điểm (= " + new DecimalFormat("#,###").format(currentCustomerPoints * 1000) + "đ)");
                
                // Bật Checkbox nếu có điểm
                if(currentCustomerPoints > 0) chkUsePoints.setEnabled(true);
                else chkUsePoints.setEnabled(false);
                
                JOptionPane.showMessageDialog(this, "Khách hàng thân thiết: " + c.getFullName());
            } else {
                // Reset điểm
                currentCustomerPoints = 0;
                pointsDiscount = 0;
                chkUsePoints.setSelected(false);
                chkUsePoints.setEnabled(false);
                lblPointInfo.setText("0 điểm");
                updateBillTotals();
                
                JOptionPane.showMessageDialog(this, "Khách hàng mới. Mời nhập thông tin.");
                txtCustName.requestFocus();
            }
        });

        createLabelInput(pnl, "Họ và tên:", txtCustName = new JTextField(), y+=80);
        createLabelInput(pnl, "Email:", txtCustEmail = new JTextField(), y+=80);
        createLabelInput(pnl, "Địa chỉ:", txtCustAddress = new JTextField(), y+=80);

        // --- VOUCHER ---
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

        // --- PHẦN ĐỔI ĐIỂM TÍCH LŨY (MỚI) ---
        y += 80;
        chkUsePoints = new JCheckBox("Đổi điểm tích lũy?");
        chkUsePoints.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkUsePoints.setBounds(26, y, 180, 30);
        chkUsePoints.setEnabled(false); // Mặc định ẩn
        pnl.add(chkUsePoints);
        
        lblPointInfo = new JLabel("");
        lblPointInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblPointInfo.setForeground(new Color(0, 102, 204)); // Xanh đậm
        lblPointInfo.setBounds(210, y, 250, 30);
        pnl.add(lblPointInfo);
        
        // Logic khi tick vào đổi điểm
        chkUsePoints.addActionListener(e -> {
            if (chkUsePoints.isSelected()) {
                // 1 điểm = 1000 VND
                double maxDiscount = currentCustomerPoints * 1000;
                double currentTotal = subTotalAmount - discountAmount;
                
                // Không giảm quá số tiền phải trả
                pointsDiscount = (maxDiscount > currentTotal) ? currentTotal : maxDiscount;
            } else {
                pointsDiscount = 0;
            }
            updateBillTotals();
        });

        // --- BUTTONS ---
        JButton btnConfirm = new JButton("Thanh Toán & in Hóa Đơn");
        btnConfirm.setBackground(new Color(46, 204, 113));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.setBounds(30, y+60, 250, 50);
        btnConfirm.addActionListener(e -> processOrder());

        JButton btnBack = new JButton("QUAY LẠI");
        btnBack.setBounds(300, y+60, 150, 50);
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
        List<Product> list = productDAO.searchProducts(kw, catId);
        for(Product p : list) pnlProductGrid.add(createProductCard(p));
        pnlProductGrid.revalidate();
        pnlProductGrid.repaint();
    }

    private void addToCart(Product p) {
        boolean exists = false;
        for(OrderDetail item : cartItems) {
            if(item.getProductId() == p.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true; 
                break;
            }
        }
        if(!exists) {
            cartItems.add(new OrderDetail(p.getProductId(), 1, p.getSalePrice()));
        }
        renderCartItems(); 
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
        // Tính lại tiền sau khi áp mã (có thể ảnh hưởng đến số điểm được dùng)
        if(chkUsePoints.isSelected()) {
             // Trigger lại sự kiện tính điểm
             for(ActionListener a : chkUsePoints.getActionListeners()) {
                 a.actionPerformed(null);
             }
        } else {
             updateBillTotals();
        }
    }

    private void processOrder() {
        if(cartItems.isEmpty()) return;
        String phone = txtCustPhone.getText().trim();
        if(phone.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT Khách hàng!"); return; }

        Customer c = customerDAO.findByPhone(phone);
        if(c == null) {
            c = new Customer();
            c.setFullName(txtCustName.getText());
            c.setPhone(phone);
            c.setEmail(txtCustEmail.getText());
            c.setAddress(txtCustAddress.getText());
            c.setPoints(0);
            customerDAO.addCustomer(c);
            c = customerDAO.findByPhone(phone); 
        }

        // Tính tiền cuối
        double finalTotal = subTotalAmount - discountAmount - pointsDiscount;
        if(finalTotal < 0) finalTotal = 0;

        // Xác định số điểm thực tế dùng để trừ trong DB
        int pointsUsed = 0;
        if (chkUsePoints.isSelected() && pointsDiscount > 0) {
            pointsUsed = (int) (pointsDiscount / 1000);
        }

        // GỌI DAO VỚI 6 THAM SỐ
        boolean success = orderDAO.createOrder(
            currentUser.getUserId(), 
            c.getCustomerId(), 
            appliedVoucherId, 
            finalTotal, 
            cartItems,
            pointsUsed // Tham số mới
        );

        if(success) {
            JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG! \nĐiểm tích lũy thêm: +" + (int)(finalTotal/100000));
            // Reset
            cartItems.clear();
            appliedVoucherId = null;
            discountAmount = 0;
            pointsDiscount = 0;
            currentCustomerPoints = 0;
            
            lblVoucherName.setText("Chưa áp dụng");
            chkUsePoints.setSelected(false);
            chkUsePoints.setEnabled(false);
            lblPointInfo.setText("");
            
            renderCartItems(); 
            txtCustPhone.setText("");
            txtCustName.setText("");
            txtVoucherCode.setText("");
            cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán! Vui lòng thử lại.");
        }
    }
}