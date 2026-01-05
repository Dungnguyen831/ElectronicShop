package com.mycompany.view.Staff;

import com.mycompany.view.Staff.component.CartPanel;
import com.mycompany.view.Staff.component.HeaderPanel;
import com.mycompany.view.Staff.component.ProductListPanel;
import com.mycompany.view.Staff.component.CustomerPaymentPanel;
import com.mycompany.dao.*;
import com.mycompany.model.*;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SaleFrame extends JFrame {

    private List<OrderDetail> cartItems = new ArrayList<>();
    private User currentUser;
    
    private HeaderPanel pnlHeader;
    private CartPanel pnlCart;
    private ProductListPanel pnlProductList;
    private CustomerPaymentPanel pnlPayment;
    
    private CardLayout cardLayoutRight;
    private JPanel pnlRightContainer;

    private Customer selectedCustomer = null;
    private double subTotalAmount = 0;
    private double discountAmount = 0;
    private double pointsDiscount = 0;
    private Integer appliedVoucherId = null;
    private int currentCustomerPoints = 0;
    
    private CustomerDAO customerDAO = new CustomerDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ProductDAO productDAO = new ProductDAO();

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

        pnlHeader = new HeaderPanel(currentUser, () -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn đăng xuất?", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                java.awt.EventQueue.invokeLater(() -> {
                    new com.mycompany.view.LoginFrame().setVisible(true); 
                });
            }
        });
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMain.setBackground(new Color(240, 240, 240));

        pnlCart = new CartPanel(cartItems, 
            () -> switchToPayment(), 
            (item, delta) -> updateQty(item, delta), 
            (item) -> removeItem(item)
        );
        pnlMain.add(pnlCart);

        cardLayoutRight = new CardLayout();
        pnlRightContainer = new JPanel(cardLayoutRight);

        pnlProductList = new ProductListPanel((product) -> addToCart(product));
        
        pnlPayment = new CustomerPaymentPanel(
            () -> findCustomer(),
            () -> applyVoucher(),
            () -> processOrder(),
            () -> showProductScreen()
        );
        pnlPayment.chkUsePoints.addActionListener(e -> calculateTotals());

        pnlRightContainer.add(pnlProductList, "PRODUCT_VIEW");
        pnlRightContainer.add(new JScrollPane(pnlPayment), "PAYMENT_VIEW");

        pnlMain.add(pnlRightContainer);
        add(pnlMain, BorderLayout.CENTER);
    }

    // Logic xử lý khi tìm khách hàng (Phần bạn yêu cầu)
    private void findCustomer() {
        String ph = pnlPayment.txtPhone.getText().trim();
        if(ph.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập SĐT!"); return; }
        if(!isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT sai!"); return; }
        
        Customer c = customerDAO.findByPhone(ph);
        if(c != null) {
            // KHÁCH HÀNG CŨ: Hiện thông tin và KHÓA nhập liệu
            this.selectedCustomer = c;
            pnlPayment.txtName.setText(c.getFullName());
            pnlPayment.txtEmail.setText(c.getEmail());
            pnlPayment.txtAddress.setText(c.getAddress());
            currentCustomerPoints = c.getPoints();
            pnlPayment.lblPointInfo.setText("Có: " + currentCustomerPoints + " điểm");
            
            pnlPayment.setFieldsEditable(false); // Khóa các ô nhập
            pnlPayment.chkUsePoints.setEnabled(currentCustomerPoints > 0);
            
            JOptionPane.showMessageDialog(this, "Khách hàng cũ: " + c.getFullName());
        } else {
            // KHÁCH HÀNG MỚI: Xóa trắng và MỞ KHÓA nhập liệu
            this.selectedCustomer = new Customer();
            this.selectedCustomer.setCustomerId(-1);
            this.selectedCustomer.setPhone(ph);
            this.selectedCustomer.setPoints(0);
            
            pnlPayment.txtName.setText("");
            pnlPayment.txtEmail.setText("");
            pnlPayment.txtAddress.setText("");
            
            pnlPayment.setFieldsEditable(true); // Mở khóa cho nhập tên/email/địa chỉ
            
            currentCustomerPoints = 0; 
            pnlPayment.chkUsePoints.setEnabled(false);
            pnlPayment.lblPointInfo.setText("");
            JOptionPane.showMessageDialog(this, "Khách hàng mới. Vui lòng nhập thông tin!");
        }
    }

    // Cần Reset trạng thái khóa khi dọn dẹp form
    private void resetAll() {
        cartItems.clear(); discountAmount = 0; pointsDiscount = 0; appliedVoucherId = null; currentCustomerPoints = 0;
        pnlPayment.txtPhone.setText(""); pnlPayment.txtName.setText(""); 
        pnlPayment.txtEmail.setText(""); pnlPayment.txtAddress.setText(""); 
        pnlPayment.txtVoucherCode.setText(""); pnlPayment.lblVoucherMsg.setText("");
        pnlCart.setVoucherName("Chưa áp dụng");
        
        pnlPayment.chkUsePoints.setSelected(false);
        pnlPayment.chkUsePoints.setEnabled(false);
        pnlPayment.lblPointInfo.setText("");
        
        pnlPayment.setFieldsEditable(false); // Đưa về trạng thái khóa mặc định
        
        calculateTotals();
        cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
    }

    // --- Các hàm Logic kho và tính toán khác giữ nguyên ---
    private void addToCart(Product p_ui) {
        Product p_db = productDAO.getProductById(p_ui.getProductId());
        int stockInDB = (p_db != null) ? p_db.getQuantity() : 0;
        if (stockInDB <= 0) {
            JOptionPane.showMessageDialog(this, "Sản phẩm đã hết hàng!", "Kho", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean exists = false;
        for (OrderDetail item : cartItems) {
            if (item.getProductId() == p_ui.getProductId()) {
                if (item.getQuantity() + 1 > stockInDB) {
                    JOptionPane.showMessageDialog(this, "Kho chỉ còn: " + stockInDB, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                exists = true; break;
            }
        }
        if (!exists) cartItems.add(new OrderDetail(p_ui.getProductId(), 1, p_ui.getSalePrice()));
        calculateTotals();
    }

    private void updateQty(OrderDetail item, int delta) {
        if (delta < 0) {
            int newQty = item.getQuantity() + delta;
            if (newQty > 0) item.setQuantity(newQty); else removeItem(item);
        } else {
            Product p_db = productDAO.getProductById(item.getProductId());
            int stockInDB = (p_db != null) ? p_db.getQuantity() : 0;
            if (item.getQuantity() + delta > stockInDB) {
                JOptionPane.showMessageDialog(this, "Kho chỉ còn: " + stockInDB);
                return;
            }
            item.setQuantity(item.getQuantity() + delta);
        }
        calculateTotals();
    }

    private void removeItem(OrderDetail item) {
        if(JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == 0) {
            cartItems.remove(item);
            calculateTotals();
        }
    }

    private void calculateTotals() {
        subTotalAmount = 0;
        for(OrderDetail item : cartItems) {
            item.setSubtotal(item.getQuantity() * item.getUnitPrice());
            subTotalAmount += item.getSubtotal();
        }
        if (pnlPayment.chkUsePoints.isSelected()) {
            double max = currentCustomerPoints * 1000;
            double remain = subTotalAmount - discountAmount;
            pointsDiscount = (max > remain) ? remain : max;
        } else pointsDiscount = 0;
        double finalTotal = Math.max(0, subTotalAmount - discountAmount - pointsDiscount);
        pnlCart.render(subTotalAmount, discountAmount + pointsDiscount, finalTotal);
        pnlPayment.lblFinalTotal.setText("THANH TOÁN: " + new DecimalFormat("#,### đ").format(finalTotal));
    }

    private void processOrder() {
        String ph = pnlPayment.txtPhone.getText().trim();
        if(ph.isEmpty() || !isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT không hợp lệ!"); return; }
        
        Customer c = customerDAO.findByPhone(ph);
        if(c == null) {
            c = new Customer(); c.setPhone(ph); c.setFullName(pnlPayment.txtName.getText());
            c.setEmail(pnlPayment.txtEmail.getText()); c.setAddress(pnlPayment.txtAddress.getText());
            customerDAO.addCustomer(c); c = customerDAO.findByPhone(ph);
        }
        calculateTotals();
        double finalTotal = Math.max(0, subTotalAmount - discountAmount - pointsDiscount);
        int ptsUsed = (pnlPayment.chkUsePoints.isSelected()) ? (int)(pointsDiscount/1000) : 0;
        if(orderDAO.createOrder(currentUser.getUserId(), c.getCustomerId(), appliedVoucherId, finalTotal, cartItems, ptsUsed)) {
            pnlProductList.loadData(null, 0,0);
            JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG!");
            resetAll();
        }
    }

    private void applyVoucher() {
        String code = pnlPayment.txtVoucherCode.getText().trim();
        if(code.isEmpty() || selectedCustomer == null) return;
        Voucher v = voucherDAO.findByCode(code);
        if(v != null) {
            if (selectedCustomer.getCustomerId() > 0 && voucherDAO.checkVoucherUsed(selectedCustomer.getCustomerId(), v.getVoucherId())) {
                JOptionPane.showMessageDialog(this, "Mã đã được khách này sử dụng!");
                return;
            }
            discountAmount = Math.min(v.getMaxDiscount() > 0 ? v.getMaxDiscount() : Double.MAX_VALUE, (subTotalAmount * v.getDiscountPercent()) / 100.0);
            appliedVoucherId = v.getVoucherId();
            pnlCart.setVoucherName(code);
        }
        calculateTotals();
    }

    private void switchToPayment() { cardLayoutRight.show(pnlRightContainer, "PAYMENT_VIEW"); calculateTotals(); }
    private void showProductScreen() { cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW"); }
    private boolean isValidPhone(String s) { return Pattern.matches("^0\\d{9}$", s); }
    private boolean isValidEmail(String s) { return s == null || s.trim().isEmpty() || Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", s); }
    private User createDefaultUser() { User u = new User(); u.setUserId(2); u.setUsername("sales"); return u; }
}