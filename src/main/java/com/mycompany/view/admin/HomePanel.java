/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view.admin;

import com.mycompany.util.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nguyen Anh Dung
 */
public class HomePanel extends JPanel {

    public HomePanel() {
        initComponents();
        // Sau này bạn sẽ gọi hàm loadDataFromDB() ở đây
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20)); // Khoảng cách giữa các phần
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Căn lề 4 phía

        // --- PHẦN 1: CÁC THẺ THỐNG KÊ (TOP) ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0)); // 1 hàng, 4 cột, cách nhau 20px
        pnlCards.setBackground(Color.WHITE);
        pnlCards.setPreferredSize(new Dimension(0, 140)); // Chiều cao cố định cho thẻ

        // Thêm 4 thẻ với màu sắc khác nhau
        // Lưu ý: Số liệu đang là giả định (Hard-code), sau này thay bằng biến từ DAO
        pnlCards.add(createCard("DOANH THU NGÀY", "15,500,000 đ", new Color(46, 204, 113))); // Màu Xanh lá
        pnlCards.add(createCard("ĐƠN HÀNG MỚI", "24 Đơn", new Color(52, 152, 219)));     // Màu Xanh dương
        pnlCards.add(createCard("KHÁCH HÀNG", "150 Khách", new Color(155, 89, 182)));     // Màu Tím
        pnlCards.add(createCard("SẮP HẾT HÀNG", "05 SP", new Color(231, 76, 60)));      // Màu Đỏ (Cảnh báo)

        this.add(pnlCards, BorderLayout.NORTH);

        // --- PHẦN 2: BẢNG ĐƠN HÀNG GẦN ĐÂY (CENTER) ---
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Style.COLOR_PRIMARY), 
                "Đơn Hàng Gần Đây", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 14), 
                Style.COLOR_PRIMARY
        ));

        // Tạo bảng dữ liệu
        String[] columns = {"Mã Đơn", "Khách Hàng", "Ngày Mua", "Tổng Tiền", "Trạng Thái"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng Dashboard
            }
        };

        // Dữ liệu mẫu (Dummy Data) - Sau này lấy từ OrderDAO
        model.addRow(new Object[]{"#ORD001", "Nguyễn Văn A", "03/01/2026", "5,000,000", "Hoàn thành"});
        model.addRow(new Object[]{"#ORD002", "Trần Thị B", "03/01/2026", "12,500,000", "Hoàn thành"});
        model.addRow(new Object[]{"#ORD003", "Lê Văn C", "02/01/2026", "500,000", "Đã hủy"});
        model.addRow(new Object[]{"#ORD004", "Phạm Văn D", "02/01/2026", "2,100,000", "Hoàn thành"});

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        
        // Thêm bảng vào ScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null); // Bỏ viền thừa
        pnlTable.add(scrollPane, BorderLayout.CENTER);

        this.add(pnlTable, BorderLayout.CENTER);
    }

    // Hàm tạo giao diện cho 1 thẻ thống kê
    private JPanel createCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(new EmptyBorder(15, 20, 15, 20)); // Padding bên trong thẻ

        // Tiêu đề nhỏ phía trên
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(255, 255, 255, 200)); // Trắng mờ

        // Giá trị lớn ở giữa
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);

        // Icon tượng trưng (Dùng text emoji cho đơn giản, nếu có ảnh thì dùng ImageIcon)
        JLabel lblIcon = new JLabel("📊"); 
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        lblIcon.setForeground(new Color(255, 255, 255, 100)); // Rất mờ
        lblIcon.setHorizontalAlignment(SwingConstants.RIGHT);

        // Layout text bên trái
        JPanel pnlText = new JPanel(new GridLayout(2, 1));
        pnlText.setOpaque(false); // Trong suốt
        pnlText.add(lblTitle);
        pnlText.add(lblValue);

        card.add(pnlText, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);

        return card;
    }
}
