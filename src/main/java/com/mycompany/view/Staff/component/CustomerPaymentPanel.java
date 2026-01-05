package com.mycompany.view.Staff.component;

import java.awt.*;
import javax.swing.*;

public class CustomerPaymentPanel extends JPanel {
    public JTextField txtPhone, txtName, txtEmail, txtAddress, txtVoucherCode;
    public JCheckBox chkUsePoints;
    public JLabel lblPointInfo;
    public JLabel lblVoucherMsg; 
    public JLabel lblFinalTotal; 
    
    private Runnable onFindCustomer;
    private Runnable onApplyVoucher;
    private Runnable onProcessOrder;
    private Runnable onBack;

    public CustomerPaymentPanel(Runnable onFind, Runnable onApply, Runnable onProcess, Runnable onBack) {
        this.onFindCustomer = onFind;
        this.onApplyVoucher = onApply;
        this.onProcessOrder = onProcess;
        this.onBack = onBack;
        
        setLayout(null);
        setPreferredSize(new Dimension(600, 750));
        setBackground(Color.WHITE);
        
        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("THÔNG TIN KHÁCH MUA HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(52, 152, 219));
        lblTitle.setBounds(30, 20, 400, 40);
        add(lblTitle);

        int y = 80;
        createLabel("Số điện thoại (*):", 30, y);
        txtPhone = createTextField(30, y+30, 400);
        
        JButton btnCheck = new JButton("Tìm");
        btnCheck.setBounds(440, y+30, 80, 35);
        btnCheck.setBackground(new Color(52, 152, 219));
        btnCheck.setForeground(Color.WHITE);
        btnCheck.addActionListener(e -> onFindCustomer.run());
        add(btnCheck);

        createLabel("Họ và tên:", 30, y+=80); txtName = createTextField(30, y+30, 490);
        createLabel("Email:", 30, y+=80); txtEmail = createTextField(30, y+30, 490);
        createLabel("Địa chỉ:", 30, y+=80); txtAddress = createTextField(30, y+30, 490);

        JLabel lblVou = new JLabel("Mã Voucher (nếu có):");
        lblVou.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVou.setBounds(30, y+=80, 200, 25);
        add(lblVou);
        
        txtVoucherCode = createTextField(30, y+30, 300);
        JButton btnApply = new JButton("Áp dụng");
        btnApply.setBounds(340, y+30, 100, 35);
        btnApply.setBackground(new Color(39, 174, 96));
        btnApply.setForeground(Color.WHITE);
        btnApply.addActionListener(e -> onApplyVoucher.run());
        add(btnApply);
        
        lblVoucherMsg = new JLabel("");
        lblVoucherMsg.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblVoucherMsg.setForeground(Color.RED);
        lblVoucherMsg.setBounds(30, y+65, 400, 20);
        add(lblVoucherMsg);

        y += 80;
        chkUsePoints = new JCheckBox("Đổi điểm tích lũy?");
        chkUsePoints.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkUsePoints.setBounds(26, y, 180, 30);
        chkUsePoints.setEnabled(false);
        add(chkUsePoints);
        
        lblPointInfo = new JLabel("");
        lblPointInfo.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblPointInfo.setForeground(Color.BLUE);
        lblPointInfo.setBounds(210, y, 250, 30);
        add(lblPointInfo);
        
        y += 50;
        lblFinalTotal = new JLabel("THANH TOÁN: 0 đ");
        lblFinalTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblFinalTotal.setForeground(Color.RED);
        lblFinalTotal.setBounds(30, y, 400, 30);
        add(lblFinalTotal);

        JButton btnConfirm = new JButton("THANH TOÁN & IN HÓA ĐƠN");
        btnConfirm.setBackground(new Color(230, 126, 34)); 
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.setBounds(30, y+50, 280, 50); 
        btnConfirm.addActionListener(e -> onProcessOrder.run());
        add(btnConfirm);

        JButton btnBack = new JButton("QUAY LẠI");
        btnBack.setBounds(330, y+50, 150, 50); 
        btnBack.addActionListener(e -> onBack.run());
        add(btnBack);
    }

    private void createLabel(String t, int x, int y) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBounds(x, y, 200, 25); add(l);
    }
    private JTextField createTextField(int x, int y, int w) {
        JTextField t = new JTextField(); t.setBounds(x, y, w, 35);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(t); return t;
    }
}
