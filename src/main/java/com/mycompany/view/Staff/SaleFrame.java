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

                // Trong lớp SaleFrame.java
        pnlHeader = new HeaderPanel(currentUser, () -> {
            // Đây là hành động "onLogout" sẽ được thực hiện khi bấm nút
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc chắn muốn đăng xuất?", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // Bước 1: Đóng cửa sổ SaleFrame hiện tại

                // Bước 2: Khởi tạo và hiển thị lại LoginFrame
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

    // ================= LOGIC CHECK KHO CHUẨN (KHÔNG CHO VƯỢT QUÁ DB) =================

    // 1. Logic thêm từ danh sách
    private void addToCart(Product p_ui) {
        // Lấy số lượng thực tế trong kho từ DB
        Product p_db = productDAO.getProductById(p_ui.getProductId());
        int stockInDB = (p_db != null) ? p_db.getQuantity() : 0;

        if (stockInDB <= 0) {
            JOptionPane.showMessageDialog(this, "Sản phẩm đã hết hàng!", "Kho", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exists = false;
        for (OrderDetail item : cartItems) {
            if (item.getProductId() == p_ui.getProductId()) {
                // Nếu (Số lượng trong giỏ + 1) > (Số lượng trong kho) -> CHẶN
                if (item.getQuantity() + 1 > stockInDB) {
                    JOptionPane.showMessageDialog(this, 
                        "Kho chỉ còn: " + stockInDB + " sản phẩm. Không thể thêm!", 
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                exists = true; break;
            }
        }
        
        if (!exists) {
            cartItems.add(new OrderDetail(p_ui.getProductId(), 1, p_ui.getSalePrice()));
        }
        calculateTotals();
    }

    // 2. Logic cập nhật số lượng (+/-)
    private void updateQty(OrderDetail item, int delta) {
        // Nếu giảm -> Luôn OK
        if (delta < 0) {
            int newQty = item.getQuantity() + delta;
            if (newQty > 0) {
                item.setQuantity(newQty);
                calculateTotals();
            } else {
                removeItem(item);
            }
            return;
        }

        // Nếu tăng -> Check kỹ với DB
        if (delta > 0) {
            Product p_db = productDAO.getProductById(item.getProductId());
            int stockInDB = (p_db != null) ? p_db.getQuantity() : 0;

            // Kiểm tra: Nếu (Số lượng hiện tại trong giỏ + 1) > (Số lượng trong kho) -> CHẶN NGAY
            if (item.getQuantity() + delta > stockInDB) {
                JOptionPane.showMessageDialog(this,
"Không thể tăng! Kho chỉ còn: " + stockInDB, 
                    "Hết hàng", JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại, không tăng
            }
            
            // Nếu chưa vượt quá kho -> Cho tăng
            item.setQuantity(item.getQuantity() + delta);
            calculateTotals();
        }
    }

    private void removeItem(OrderDetail item) {
        if(JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == 0) {
            cartItems.remove(item);
            calculateTotals();
        }
    }

    // ================= CÁC PHẦN KHÁC (GIỮ NGUYÊN) =================

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

    private void switchToPayment() {
        if(cartItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Giỏ trống!"); return; }
        pnlPayment.lblVoucherMsg.setText("");
        pnlPayment.txtVoucherCode.setText("");
        calculateTotals();
        cardLayoutRight.show(pnlRightContainer, "PAYMENT_VIEW");
    }
    
    private void showProductScreen() {
        cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
    }

    private boolean isValidPhone(String s) { return Pattern.matches("^0\\d{9}$", s); }
    private boolean isValidEmail(String s) { return s == null || s.trim().isEmpty() || Pattern.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", s); }

    private void findCustomer() {
    String ph = pnlPayment.txtPhone.getText().trim();
    if(ph.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập SĐT!"); return; }
    if(!isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT sai!"); return; }
    
    Customer c = customerDAO.findByPhone(ph);
    if(c != null) {
this.selectedCustomer = c;
        pnlPayment.txtName.setText(c.getFullName());
        pnlPayment.txtEmail.setText(c.getEmail());
        pnlPayment.txtAddress.setText(c.getAddress());
        currentCustomerPoints = c.getPoints();
        pnlPayment.lblPointInfo.setText("Có: " + currentCustomerPoints + " điểm");
        pnlPayment.chkUsePoints.setEnabled(currentCustomerPoints > 0);
        JOptionPane.showMessageDialog(this, "Khách hàng cũ: " + c.getFullName());
    } else {
        // --- SỬA TẠI ĐÂY: Tạo đối tượng khách hàng tạm cho khách mới ---
        this.selectedCustomer = new Customer();
        this.selectedCustomer.setCustomerId(-1); // Đánh dấu là khách chưa có trong DB
        this.selectedCustomer.setPhone(ph);
        this.selectedCustomer.setPoints(0);
        
        currentCustomerPoints = 0; 
        pnlPayment.chkUsePoints.setEnabled(false);
        pnlPayment.lblPointInfo.setText("");
        JOptionPane.showMessageDialog(this, "Khách hàng mới. Bạn có thể nhập tên và áp dụng mã.");
    }
}

    private void applyVoucher() {
    String code = pnlPayment.txtVoucherCode.getText().trim();
    if(code.isEmpty()) return;

    if (selectedCustomer == null) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT và bấm 'Tìm' để xác định khách hàng trước!");
        return;
    }

    Voucher v = voucherDAO.findByCode(code);
    if(v != null) {
        // Nếu là khách cũ (ID > 0), mới kiểm tra trong DB xem đã dùng mã chưa
        if (selectedCustomer.getCustomerId() > 0) {
            if (voucherDAO.checkVoucherUsed(selectedCustomer.getCustomerId(), v.getVoucherId())) {
                JOptionPane.showMessageDialog(this, "Khách hàng này đã sử dụng mã " + code + " trước đó!");
                resetVoucherState();
                return;
            }
        }
        // Nếu ID là -1 (khách mới), bỏ qua kiểm tra checkVoucherUsed vì họ chắc chắn chưa dùng

        discountAmount = (subTotalAmount * v.getDiscountPercent()) / 100.0;
        if(v.getMaxDiscount() > 0 && discountAmount > v.getMaxDiscount()) {
            discountAmount = v.getMaxDiscount();
        }
        
        appliedVoucherId = v.getVoucherId();
        pnlCart.setVoucherName(code + " (-" + v.getDiscountPercent() + "%)");
        JOptionPane.showMessageDialog(this, "Áp dụng mã giảm giá thành công!");
    } else {
        resetVoucherState();
        JOptionPane.showMessageDialog(this, "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
    }
    calculateTotals();
}

// Hàm bổ trợ để xóa trạng thái voucher khi lỗi
private void resetVoucherState() {
    discountAmount = 0;
    appliedVoucherId = null;
    pnlCart.setVoucherName("Mã lỗi/Không áp dụng");
}

    private void processOrder() {
        String ph = pnlPayment.txtPhone.getText().trim();
        String em = pnlPayment.txtEmail.getText().trim();
if(ph.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập SĐT!"); return; }
        if(!isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT sai!"); return; }
        if(!isValidEmail(em)) { JOptionPane.showMessageDialog(this, "Email sai!"); return; }
        
        Customer c = customerDAO.findByPhone(ph);
        if(c == null) {
            c = new Customer(); c.setPhone(ph); c.setFullName(pnlPayment.txtName.getText());
            c.setEmail(em); c.setAddress(pnlPayment.txtAddress.getText()); c.setPoints(0);
            customerDAO.addCustomer(c); c = customerDAO.findByPhone(ph);
        }

        calculateTotals();
        double finalTotal = subTotalAmount - discountAmount - pointsDiscount;
        if(finalTotal < 0) finalTotal = 0;
        int ptsUsed = (pnlPayment.chkUsePoints.isSelected() && pointsDiscount > 0) ? (int)(pointsDiscount/1000) : 0;

        boolean ok = orderDAO.createOrder(currentUser.getUserId(), c.getCustomerId(), appliedVoucherId, finalTotal, cartItems, ptsUsed);
        if(ok) {
            pnlProductList.loadData(null, 0);
            JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG!");
            resetAll();
            
        } else JOptionPane.showMessageDialog(this, "Lỗi!");
        
    }

    private void resetAll() {
        cartItems.clear(); discountAmount = 0; pointsDiscount = 0; appliedVoucherId = null; currentCustomerPoints = 0;
        pnlPayment.txtPhone.setText(""); pnlPayment.txtName.setText(""); 
        pnlPayment.txtEmail.setText(""); pnlPayment.txtAddress.setText(""); 
        pnlPayment.txtVoucherCode.setText(""); pnlPayment.lblVoucherMsg.setText("");
        pnlCart.setVoucherName("Chưa áp dụng");
        
        pnlPayment.chkUsePoints.setSelected(false);
        pnlPayment.chkUsePoints.setEnabled(false);
        pnlPayment.lblPointInfo.setText("");
        
        calculateTotals();
        cardLayoutRight.show(pnlRightContainer, "PRODUCT_VIEW");
    }

    private User createDefaultUser() {
        User u = new User(); u.setUserId(2); u.setUsername("sales"); u.setFullName("Tran Ban Hang"); u.setRoleId(2); return u;
    }
}