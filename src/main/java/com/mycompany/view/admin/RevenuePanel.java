package com.mycompany.view.admin;

import com.mycompany.dao.StatisticalDAO;
import com.mycompany.util.Style;
import com.toedter.calendar.JDateChooser; 
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class RevenuePanel extends JPanel {

    // Components
    private JDateChooser dateFrom, dateTo;
    private JButton btnFilter, btnRefresh;
    
    // 4 Thẻ chỉ số
    private JLabel lblRevenueVal, lblImportTotalVal, lblCOGSVal, lblProfitVal;
    
    // --- CÁC COMPONENT MỚI (BỘ LỌC) ---
    private JComboBox<String> cboQuickTime; // Chọn nhanh: Tuần này, Tháng này...
    private JComboBox<String> cboSort;      // Sắp xếp: Cao-Thấp...
    
    private JTable table;
    private DefaultTableModel model;
    
    // Dữ liệu và DAO
    private final DecimalFormat df = new DecimalFormat("#,##0 đ");
    private StatisticalDAO statsDAO = new StatisticalDAO();
    
    // Lưu danh sách hiện tại để phục vụ việc Sắp xếp (Sort) mà không cần query lại DB
    private List<Object[]> currentDataList = new ArrayList<>();

    public RevenuePanel() {
        initComponents();
        loadDefaultData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================== 1. TOP BAR (Chọn ngày tùy chỉnh) ==================
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new EmptyBorder(0, 0, 10, 0));

        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateFrom.setPreferredSize(new Dimension(150, 35));
        
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("dd/MM/yyyy");
        dateTo.setPreferredSize(new Dimension(150, 35));
        
        btnFilter = createButton("Thống kê", Style.COLOR_PRIMARY);
        btnRefresh = createButton("Làm mới", Style.COLOR_SUCCESS);

        pnlTop.add(new JLabel("Từ ngày:"));
        pnlTop.add(dateFrom);
        pnlTop.add(new JLabel("Đến ngày:"));
        pnlTop.add(dateTo);
        pnlTop.add(btnFilter);
        pnlTop.add(btnRefresh);

        add(pnlTop, BorderLayout.NORTH);

        // ================== 2. CENTER (Cards + Tools + Table) ==================
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 20));
        pnlCenter.setBackground(Color.WHITE);

        // --- 2A. CARDS (4 Thẻ chỉ số) ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlCards.setBackground(Color.WHITE);
        pnlCards.setPreferredSize(new Dimension(0, 120));

        lblRevenueVal = new JLabel("0 đ");
        lblImportTotalVal = new JLabel("0 đ");
        lblCOGSVal = new JLabel("0 đ");
        lblProfitVal = new JLabel("0 đ");

        pnlCards.add(createStatsCard("DOANH THU", lblRevenueVal, new Color(52, 152, 219))); 
        pnlCards.add(createStatsCard("TIỀN NHẬP KHO", lblImportTotalVal, new Color(230, 126, 34)));   
        pnlCards.add(createStatsCard("GIÁ VỐN BÁN", lblCOGSVal, new Color(231, 76, 60))); 
        pnlCards.add(createStatsCard("LỢI NHUẬN", lblProfitVal, new Color(46, 204, 113)));      

        pnlCenter.add(pnlCards, BorderLayout.NORTH);

        // --- 2B. TABLE CONTAINER (Chứa thanh công cụ + Bảng) ---
        JPanel pnlTableContainer = new JPanel(new BorderLayout(0, 10));
        pnlTableContainer.setBackground(Color.WHITE);

        // [MỚI] THANH CÔNG CỤ LỌC & SẮP XẾP
        JPanel pnlTableTools = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTableTools.setBackground(Color.WHITE);
        
        // Label tiêu đề
        JLabel lblListTitle = new JLabel("CHI TIẾT DOANH THU THEO NGÀY");
        lblListTitle.setFont(Style.FONT_BOLD);
        lblListTitle.setForeground(Style.COLOR_PRIMARY);
        
        // ComboBox Chọn nhanh thời gian
        cboQuickTime = new JComboBox<>(new String[]{"-- Chọn thời gian --", "7 ngày qua", "Tháng này", "Năm nay", "Tất cả"});
        styleComboBox(cboQuickTime);
        
        // ComboBox Sắp xếp
        cboSort = new JComboBox<>(new String[]{
            "Sắp xếp: Mới nhất", 
            "Sắp xếp: Cũ nhất", 
            "Doanh thu: Cao -> Thấp", 
            "Doanh thu: Thấp -> Cao",
            "Số đơn: Cao -> Thấp"
        });
        styleComboBox(cboSort);

        // Add vào panel tools (Tiêu đề bên trái, bộ lọc bên phải)
        JPanel pnlToolsWrapper = new JPanel(new BorderLayout());
        pnlToolsWrapper.setBackground(Color.WHITE);
        pnlToolsWrapper.add(lblListTitle, BorderLayout.WEST);
        
        JPanel pnlRightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightTools.setBackground(Color.WHITE);
        pnlRightTools.add(new JLabel("Lọc nhanh:"));
        pnlRightTools.add(cboQuickTime);
        pnlRightTools.add(new JLabel(" | "));
        pnlRightTools.add(cboSort);
        
        pnlToolsWrapper.add(pnlRightTools, BorderLayout.EAST);
        
        pnlTableContainer.add(pnlToolsWrapper, BorderLayout.NORTH);

        // BẢNG DỮ LIỆU
        String[] cols = {"Ngày", "Số hóa đơn", "Doanh thu ngày", "Ghi chú"};
        model = new DefaultTableModel(cols, 0) {
             @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(Style.FONT_BOLD);
        table.getTableHeader().setBackground(Style.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        
        pnlTableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        
        pnlCenter.add(pnlTableContainer, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // ================== 3. EVENTS ==================
        
        btnFilter.addActionListener(e -> filterData());
        btnRefresh.addActionListener(e -> loadDefaultData());
        
        // Sự kiện Lọc nhanh (Tháng/Năm)
        cboQuickTime.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handleQuickTimeFilter();
            }
        });

        // Sự kiện Sắp xếp
        cboSort.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sortData();
            }
        });
    }

    // ================== LOGIC XỬ LÝ ==================

    // 1. Xử lý Lọc nhanh thời gian
    private void handleQuickTimeFilter() {
        int index = cboQuickTime.getSelectedIndex();
        if (index == 0) return;

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        
        Date fromDate = null;

        switch (index) {
            case 1: // 7 ngày qua
                cal.add(Calendar.DAY_OF_YEAR, -7);
                fromDate = cal.getTime();
                break;
            case 2: // Tháng này (Từ ngày 1)
                cal.set(Calendar.DAY_OF_MONTH, 1);
                fromDate = cal.getTime();
                break;
            case 3: // Năm nay (Từ 1/1)
                cal.set(Calendar.DAY_OF_YEAR, 1);
                fromDate = cal.getTime();
                break;
            case 4: // Tất cả
                cal.set(2000, Calendar.JANUARY, 1);
                fromDate = cal.getTime();
                break;
        }

        if (fromDate != null) {
            dateFrom.setDate(fromDate);
            dateTo.setDate(now);
            // Tự động gọi lọc luôn cho tiện
            filterData();
        }
    }

    // 2. Xử lý Sắp xếp (Sort) trên List hiện tại
    private void sortData() {
        if (currentDataList == null || currentDataList.isEmpty()) return;

        int index = cboSort.getSelectedIndex();
        
        // Sử dụng Comparator để sắp xếp
        Collections.sort(currentDataList, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                // o1[0]: Date, o1[1]: Số đơn (int), o1[2]: Doanh thu (Double)
                
                switch (index) {
                    case 0: // Mới nhất (Date DESC)
                        return ((Date) o2[0]).compareTo((Date) o1[0]);
                    case 1: // Cũ nhất (Date ASC)
                        return ((Date) o1[0]).compareTo((Date) o2[0]);
                    case 2: // Doanh thu Cao -> Thấp
                        return Double.compare((Double) o2[2], (Double) o1[2]);
                    case 3: // Doanh thu Thấp -> Cao
                        return Double.compare((Double) o1[2], (Double) o2[2]);
                    case 4: // Số đơn Cao -> Thấp
                        return Integer.compare((Integer) o2[1], (Integer) o1[1]);
                    default:
                        return 0;
                }
            }
        });

        // Cập nhật lại bảng sau khi sort
        updateTable();
    }

    private void loadDefaultData() {
        cboQuickTime.setSelectedIndex(2); // Mặc định chọn "Tháng này"
        // (Sự kiện setSelectedIndex sẽ tự kích hoạt handleQuickTimeFilter)
    }
    
    private void filterData() {
        Date from = dateFrom.getDate();
        Date to = dateTo.getDate();
        
        if (from == null || to == null) return;

        // Fix lỗi giờ (00:00:00 -> 23:59:59)
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(from);
        calFrom.set(Calendar.HOUR_OF_DAY, 0); calFrom.set(Calendar.MINUTE, 0); calFrom.set(Calendar.SECOND, 0);
        
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(to);
        calTo.set(Calendar.HOUR_OF_DAY, 23); calTo.set(Calendar.MINUTE, 59); calTo.set(Calendar.SECOND, 59);

        processDataLoad(calFrom.getTime(), calTo.getTime());
    }

    private void processDataLoad(Date from, Date to) {
        // 1. Cập nhật 4 Card chỉ số (Luôn query mới)
        double[] stats = statsDAO.getRevenueAndProfit(from, to);
        double importTotal = statsDAO.getTotalImportCost(from, to);
        
        lblRevenueVal.setText(df.format(stats[0]));     
        lblImportTotalVal.setText(df.format(importTotal)); 
        lblCOGSVal.setText(df.format(stats[1]));        
        lblProfitVal.setText(df.format(stats[2]));      
        
        // 2. Lấy dữ liệu bảng -> LƯU VÀO BIẾN TOÀN CỤC currentDataList
        currentDataList = statsDAO.getRevenueByDate(from, to);
        
        // 3. Sắp xếp theo lựa chọn hiện tại của cboSort
        sortData(); 
        
        // (Hàm sortData sẽ gọi updateTable để hiển thị)
    }
    
    // Hàm đổ dữ liệu từ list ra bảng
    private void updateTable() {
        model.setRowCount(0);
        for (Object[] row : currentDataList) {
            model.addRow(new Object[]{
                row[0], 
                row[1], 
                df.format(row[2]), 
                "Xem chi tiết"
            });
        }
    }

    // --- HELPER UI ---
    
    private void styleComboBox(JComboBox box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(180, 30));
    }

    private JPanel createStatsCard(String title, JLabel lblVal, Color bg) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(255, 255, 255, 220));
        
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        lblVal.setForeground(Color.WHITE);
        
        card.add(lblTitle);
        card.add(lblVal);
        return card;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(Style.FONT_BOLD);
        btn.setFocusPainted(false);
        return btn;
    }
}