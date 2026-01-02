package com.mycompany.view.Staff;

import com.mycompany.dao.*;
import com.mycompany.model.*;
import com.mycompany.view.component.*; 

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern; // Import thêm để dùng Regex

public class SaleFrame extends JFrame {

    // --- Dữ liệu Logic ---
    private List<OrderDetail> cartItems = new ArrayList<>();
    private User currentUser;
    
    // --- Components ---
    private HeaderPanel pnlHeader;
    private CartPanel pnlCart;
    private ProductListPanel pnlProductList;
    private CustomerPaymentPanel pnlPayment;
    
    private CardLayout cardLayoutRight;
    private JPanel pnlRightContainer;

    // --- Biến tính toán ---
    private double subTotalAmount = 0;
    private double discountAmount = 0;
    private double pointsDiscount = 0;
    private Integer appliedVoucherId = null;
    private int currentCustomerPoints = 0;
    
    // --- DAOs ---
    private CustomerDAO customerDAO = new CustomerDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private OrderDAO orderDAO = new OrderDAO();

    public SaleFrame(User user) {
        this.currentUser = (user != null) ? user : createDefaultUser();
        initUI();
    }
    
    public SaleFrame() { this(null); }

    private void initUI() {
        setTitle("HỆ THỐNG POS - MAIZEN SHOP");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Header
        pnlHeader = new HeaderPanel(currentUser, () -> dispose());
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Main Body
        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMain.setBackground(new Color(240, 240, 240));

        // --- CỘT TRÁI ---
        pnlCart = new CartPanel(cartItems, () -> switchToPayment());
        pnlMain.add(pnlCart);

        // --- CỘT PHẢI ---
        cardLayoutRight = new CardLayout();
        pnlRightContainer = new JPanel(cardLayoutRight);

        // A. Product List
        pnlProductList = new ProductListPanel((product) -> addToCart(product));
        
        // B. Payment
        pnlPayment = new CustomerPaymentPanel(
            () -> findCustomer(),   // Tìm khách
            () -> applyVoucher(),   // Voucher
            () -> processOrder(),   // Hoàn tất
            () -> showProductScreen() // Back
        );
        pnlPayment.chkUsePoints.addActionListener(e -> calculateTotals());

        pnlRightContainer.add(pnlProductList, "PRODUCT_VIEW");
        pnlRightContainer.add(new JScrollPane(pnlPayment), "PAYMENT_VIEW");

        pnlMain.add(pnlRightContainer);
        add(pnlMain, BorderLayout.CENTER);
    }

    // ================= HELPER VALIDATION (MỚI THÊM) =================

    // Kiểm tra SĐT: 10 số, bắt đầu bằng 0
    private boolean isValidPhone(String phone) {
        // Regex: Bắt đầu bằng 0, theo sau là 9 chữ số bất kỳ
        return Pattern.matches("^0\\d{9}$", phone);
    }

    // Kiểm tra Email: Cho phép rỗng (nếu khách k có), nếu nhập thì phải đúng
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Không bắt buộc
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailRegex, email);
    }

    // ================= LOGIC NGHIỆP VỤ =================

    private void addToCart(Product p) {
        boolean exists = false;
        for (OrderDetail item : cartItems) {
            if (item.getProductId() == p.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true; break;
            }
        }
        if (!exists) {
            cartItems.add(new OrderDetail(p.getProductId(), 1, p.getSalePrice()));
        }
        calculateTotals();
    }

    private void switchToPayment() {
        if(cartItems.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Giỏ hàng trống!"); 
            return; 
        }
        pnlPayment.lblVoucherMsg.setText("");
        pnlPayment.txtVoucherCode.setText("");
        calculateTotals(); 
        cardLayoutRight.show(pnlRightContainer, "PAYMENT_VIEW");
    }
    
    private void showProductScreen() {
        cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
    }

    private void calculateTotals() {
        subTotalAmount = 0;
        for(OrderDetail item : cartItems) {
            item.setSubtotal(item.getQuantity() * item.getUnitPrice());
            subTotalAmount += item.getSubtotal();
        }

        if (pnlPayment.chkUsePoints.isSelected()) {
            double maxDiscount = currentCustomerPoints * 1000;
            double remain = subTotalAmount - discountAmount;
            pointsDiscount = (maxDiscount > remain) ? remain : maxDiscount;
        } else {
            pointsDiscount = 0;
        }

        double finalTotal = subTotalAmount - discountAmount - pointsDiscount;
        if(finalTotal < 0) finalTotal = 0;
        double totalDiscount = discountAmount + pointsDiscount;

        pnlCart.render(subTotalAmount, totalDiscount, finalTotal);

        DecimalFormat df = new DecimalFormat("#,### đ");
        pnlPayment.lblFinalTotal.setText("THANH TOÁN: " + df.format(finalTotal));
        
        if (totalDiscount > 0) {
            pnlPayment.lblVoucherMsg.setText("Đã giảm: -" + df.format(totalDiscount));
            pnlPayment.lblVoucherMsg.setForeground(new Color(39, 174, 96));
        } else {
            pnlPayment.lblVoucherMsg.setText("");
        }
    }

    // --- VALIDATE Ở NÚT TÌM KHÁCH ---
    private void findCustomer() {
        String ph = pnlPayment.txtPhone.getText().trim();
        
        // 1. Kiểm tra rỗng
        if(ph.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 2. Kiểm tra định dạng (Validate)
        if (!isValidPhone(ph)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! (Phải là 10 số, bắt đầu bằng 0)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Customer c = customerDAO.findByPhone(ph);
        if(c != null) {
            pnlPayment.txtName.setText(c.getFullName());
            pnlPayment.txtEmail.setText(c.getEmail());
            pnlPayment.txtAddress.setText(c.getAddress());
            
            currentCustomerPoints = c.getPoints();
            pnlPayment.lblPointInfo.setText("Có: " + currentCustomerPoints + " điểm (= " + new DecimalFormat("#,###").format(currentCustomerPoints*1000) + "đ)");
            pnlPayment.chkUsePoints.setEnabled(currentCustomerPoints > 0);
            
            JOptionPane.showMessageDialog(this, "Tìm thấy khách hàng: " + c.getFullName());
        } else {
            resetCustomerInfo();
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu. Mời nhập thông tin khách mới.");
            pnlPayment.txtName.requestFocus();
        }
    }

    private void applyVoucher() {
        String code = pnlPayment.txtVoucherCode.getText().trim();
        if(code.isEmpty()) return;
        
        Voucher v = voucherDAO.findByCode(code);
        if(v != null) {
            discountAmount = (subTotalAmount * v.getDiscountPercent()) / 100.0;
            if(v.getMaxDiscount() > 0 && discountAmount > v.getMaxDiscount()) 
                discountAmount = v.getMaxDiscount();
            
            appliedVoucherId = v.getVoucherId();
            pnlCart.setVoucherName(code + " (-" + v.getDiscountPercent() + "%)");
            JOptionPane.showMessageDialog(this, "Áp dụng thành công!");
        } else {
            discountAmount = 0; 
            appliedVoucherId = null; 
            pnlCart.setVoucherName("Mã lỗi");
            JOptionPane.showMessageDialog(this, "Mã không hợp lệ!");
        }
        calculateTotals();
    }

    // --- VALIDATE Ở NÚT THANH TOÁN ---
    private void processOrder() {
        String ph = pnlPayment.txtPhone.getText().trim();
        String em = pnlPayment.txtEmail.getText().trim();
        
        // 1. Validate SĐT
        if(ph.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT!"); return; 
        }
        if (!isValidPhone(ph)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không đúng định dạng!"); return;
        }

        // 2. Validate Email (Nếu có nhập)
        if (!em.isEmpty() && !isValidEmail(em)) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng! (vd: abc@gmail.com)"); 
            pnlPayment.txtEmail.requestFocus();
            return;
        }
        
        // Logic tạo khách hàng & đơn hàng
        Customer c = customerDAO.findByPhone(ph);
        if(c == null) {
            c = new Customer();
            c.setFullName(pnlPayment.txtName.getText()); 
            c.setPhone(ph);
            c.setEmail(em); 
            c.setAddress(pnlPayment.txtAddress.getText());
            c.setPoints(0);
            customerDAO.addCustomer(c);
            c = customerDAO.findByPhone(ph);
        } else {
            // Nếu khách cũ, có thể cập nhật lại email/địa chỉ nếu họ đổi ý (Optional)
            // c.setEmail(em); customerDAO.update(c); 
        }

        calculateTotals(); 
        
        double finalTotal = subTotalAmount - discountAmount - pointsDiscount;
        if(finalTotal < 0) finalTotal = 0;
        
        int pointsUsed = (pnlPayment.chkUsePoints.isSelected() && pointsDiscount > 0) ? (int)(pointsDiscount/1000) : 0;

        boolean success = orderDAO.createOrder(
            currentUser.getUserId(), 
            c.getCustomerId(), 
            appliedVoucherId, 
            finalTotal, 
            cartItems, 
            pointsUsed
        );
        
        if(success) {
            JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG! \nĐã tích điểm và trừ kho.");
            resetAll(); 
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán! Vui lòng thử lại.");
        }
    }

    private void resetAll() {
        cartItems.clear();
        discountAmount = 0; 
        pointsDiscount = 0; 
        appliedVoucherId = null; 
        currentCustomerPoints = 0;
        
        pnlPayment.txtPhone.setText(""); 
        pnlPayment.txtName.setText(""); 
        pnlPayment.txtEmail.setText("");
        pnlPayment.txtAddress.setText("");
        pnlPayment.txtVoucherCode.setText(""); 
        pnlPayment.lblVoucherMsg.setText("");
        
        pnlCart.setVoucherName("Chưa áp dụng");
        resetCustomerInfo();
        calculateTotals(); 
        
        cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
    }
    
    private void resetCustomerInfo() {
        currentCustomerPoints = 0;
        pnlPayment.chkUsePoints.setSelected(false);
        pnlPayment.chkUsePoints.setEnabled(false);
        pnlPayment.lblPointInfo.setText("");
    }

    private User createDefaultUser() {
        User u = new User(); u.setUserId(2); u.setUsername("sales"); u.setFullName("Tran Ban Hang"); u.setRoleId(2); return u;
    }
}