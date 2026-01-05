package com.mycompany.view.admin;

import com.mycompany.dao.OrderDAO;
import com.mycompany.model.Order;
import com.mycompany.util.Style;
import com.toedter.calendar.JDateChooser; // [QUAN TRỌNG] Import thư viện lịch
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class OrderPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchStaff; // Đổi tên biến cho rõ nghĩa
    private JComboBox<String> cboTimeFilter;
    
    // Panel chứa 2 ô chọn ngày (để ẩn/hiện)
    private JPanel pnlCustomDate;
    private JDateChooser dateFrom, dateTo;
    
    private OrderDAO orderDAO = new OrderDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0");

    public OrderPanel() {
        initComponents();
        loadData(); // Load mặc định (Tất cả)
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. TOP PANEL (Thanh công cụ lọc) ---
        // Dùng FlowLayout có wrap nếu màn hình nhỏ, hoặc GridBagLayout nếu cần ngay ngắn
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(BorderFactory.createTitledBorder("Bộ lọc & Tìm kiếm"));

        // A. Tìm kiếm nhân viên
        JLabel lblSearch = new JLabel("Nhân viên:");
        lblSearch.setFont(Style.FONT_BOLD);
        txtSearchStaff = new JTextField(15);
        txtSearchStaff.setPreferredSize(new Dimension(150, 30));
        txtSearchStaff.setToolTipText("Nhập tên nhân viên...");

        // B. Bộ lọc thời gian
        JLabel lblTime = new JLabel("Thời gian:");
        lblTime.setFont(Style.FONT_BOLD);
        String[] filters = {"Tất cả", "Hôm nay", "Tuần này", "Tháng này", "Năm nay", "Tùy chọn ngày..."};
        cboTimeFilter = new JComboBox<>(filters);
        cboTimeFilter.setPreferredSize(new Dimension(130, 30));
        cboTimeFilter.setBackground(Color.WHITE);

        // C. Panel chọn ngày tùy chỉnh (Mặc định ẩn)
        pnlCustomDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlCustomDate.setBackground(Color.WHITE);
        pnlCustomDate.setVisible(false); // Ẩn lúc đầu

        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateFrom.setPreferredSize(new Dimension(120, 30));
        
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("dd/MM/yyyy");
        dateTo.setPreferredSize(new Dimension(120, 30));
        
        pnlCustomDate.add(new JLabel("Từ:"));
        pnlCustomDate.add(dateFrom);
        pnlCustomDate.add(new JLabel("Đến:"));
        pnlCustomDate.add(dateTo);

        // D. Các nút bấm
        JButton btnFilter = createButton("Lọc", Style.COLOR_PRIMARY);
        JButton btnReset = createButton("Làm mới", Style.COLOR_SUCCESS);
        JButton btnDetail = createButton("Xem CT", new Color(230, 126, 34)); // Màu cam

        // Add component vào Top
        pnlTop.add(lblSearch);
        pnlTop.add(txtSearchStaff);
        pnlTop.add(lblTime);
        pnlTop.add(cboTimeFilter);
        pnlTop.add(pnlCustomDate); // Add panel ẩn này vào
        pnlTop.add(btnFilter);
        pnlTop.add(btnReset);
        pnlTop.add(btnDetail);

        add(pnlTop, BorderLayout.NORTH);

        // --- 2. TABLE ---
        String[] columns = {"ID", "Nhân viên", "Khách hàng", "Tổng tiền", "Thanh toán", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(Style.FONT_REGULAR);
        table.getTableHeader().setFont(Style.FONT_BOLD);
        table.getTableHeader().setBackground(Style.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Căn giữa cột ID và Ngày
        // DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        // centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        // table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. EVENTS ---
        
        // Sự kiện khi chọn Combobox
        cboTimeFilter.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) cboTimeFilter.getSelectedItem();
                // Nếu chọn "Tùy chọn ngày" thì hiện 2 ô chọn ngày, ngược lại thì ẩn
                pnlCustomDate.setVisible("Tùy chọn ngày...".equals(selected));
                pnlTop.revalidate(); // Vẽ lại layout để tránh bị vỡ
                pnlTop.repaint();
            }
        });

        btnFilter.addActionListener(e -> processFilter());
        
        btnReset.addActionListener(e -> {
            txtSearchStaff.setText("");
            cboTimeFilter.setSelectedIndex(0); // Về "Tất cả"
            dateFrom.setDate(null);
            dateTo.setDate(null);
            processFilter();
        });
        
        btnDetail.addActionListener(e -> showDetail());
    }

    // --- HÀM XỬ LÝ LOGIC ---

    private void processFilter() {
        String staffName = txtSearchStaff.getText().trim();
        int timeIndex = cboTimeFilter.getSelectedIndex();
        
        Date from = null;
        Date to = null;
        
        // Tính toán ngày tháng dựa trên lựa chọn
        if (timeIndex != 0) { // Không phải "Tất cả"
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            
            switch (timeIndex) {
                case 1: // Hôm nay
                    from = now;
                    to = now;
                    break;
                case 2: // Tuần này
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Thứ 2 đầu tuần
                    from = cal.getTime();
                    to = now;
                    break;
                case 3: // Tháng này
                    cal.set(Calendar.DAY_OF_MONTH, 1); // Ngày 1 đầu tháng
                    from = cal.getTime();
                    to = now;
                    break;
                case 4: // Năm nay
                    cal.set(Calendar.DAY_OF_YEAR, 1); // Ngày 1/1
                    from = cal.getTime();
                    to = now;
                    break;
                case 5: // Tùy chọn ngày
                    from = dateFrom.getDate();
                    to = dateTo.getDate();
                    // Validate
                    if (from == null || to == null) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng ngày!");
                        return;
                    }
                    if (from.after(to)) {
                        JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải nhỏ hơn ngày kết thúc!");
                        return;
                    }
                    break;
            }
        }

        // Gọi DAO để lọc
        List<Order> list = orderDAO.filterOrders(staffName, from, to);
        updateTable(list);
    }

    private void loadData() {
        // Load mặc định là lọc "Tất cả" (from=null, to=null)
        List<Order> list = orderDAO.filterOrders(null, null, null);
        updateTable(list);
    }

    private void updateTable(List<Order> list) {
        model.setRowCount(0);
        for (Order o : list) {
            model.addRow(new Object[]{
                o.getOrderId(),
                o.getStaffName(),
                o.getCustomerName(),
                df.format(o.getTotalAmount()) + " đ",
                o.getPaymentMethod(),
                o.getStatus() == 1 ? "Hoàn thành" : "Đã hủy",
                o.getOrderDate()
            });
        }
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng để xem chi tiết!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        new OrderDetailDialog(id).setVisible(true);
    }
    
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(Style.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 30));
        return btn;
    }
}