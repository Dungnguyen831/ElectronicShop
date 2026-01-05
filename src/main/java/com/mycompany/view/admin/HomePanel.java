package com.mycompany.view.admin;

import com.mycompany.dao.HomeDAO;
import com.mycompany.dao.StatisticalDAO;
import com.mycompany.util.Style;
import com.mycompany.util.UIHelper; // [MỚI] Import Helper vừa tạo
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class HomePanel extends JPanel {

    // Components
    private JLabel lblRevenueVal, lblOrderVal, lblCustomerVal, lblLowStockVal;
    private JTable tblTopProducts, tblTopStaff;
    private DefaultTableModel modelTopProducts, modelTopStaff;
    private SimpleLineChart pnlChart; 
    
    // Logic & Data
    private HomeDAO homeDAO = new HomeDAO();
    private StatisticalDAO statsDAO = new StatisticalDAO();
    private DecimalFormat df = new DecimalFormat("#,##0 đ");

    public HomePanel() {
        initComponents();
        loadDataFromDB();
    }

    private void initComponents() {
        // Layout chính
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // === 1. TOP CARDS ===
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setBackground(Color.WHITE);
        pnlCards.setPreferredSize(new Dimension(0, 130));

        lblRevenueVal = new JLabel("Loading...");
        lblOrderVal = new JLabel("Loading...");
        lblCustomerVal = new JLabel("Loading...");
        lblLowStockVal = new JLabel("Loading...");

        // [MỚI] Gọi hàm từ UIHelper
        pnlCards.add(UIHelper.createCard("DOANH THU HÔM NAY", lblRevenueVal, new Color(46, 204, 113)));
        pnlCards.add(UIHelper.createCard("ĐƠN HÀNG HÔM NAY", lblOrderVal, new Color(52, 152, 219)));
        pnlCards.add(UIHelper.createCard("TỔNG KHÁCH HÀNG", lblCustomerVal, new Color(155, 89, 182)));
        pnlCards.add(UIHelper.createCard("SẮP HẾT HÀNG", lblLowStockVal, new Color(231, 76, 60)));

        this.add(pnlCards, BorderLayout.NORTH);

        // === 2. BODY (Biểu đồ + Bảng Top) ===
        JPanel pnlBody = new JPanel(new BorderLayout(0, 20));
        pnlBody.setBackground(Color.WHITE);

        // --- PHẦN BIỂU ĐỒ ---
        JPanel pnlChartContainer = new JPanel(new BorderLayout());
        pnlChartContainer.setBackground(Color.WHITE);
        pnlChartContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Biểu Đồ Doanh Thu 7 Ngày Gần Nhất",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.DARK_GRAY
        ));
        pnlChart = new SimpleLineChart();
        pnlChartContainer.add(pnlChart, BorderLayout.CENTER);
        pnlBody.add(pnlChartContainer, BorderLayout.NORTH);


        // --- PHẦN DANH SÁCH TOP ---
        JPanel pnlLists = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlLists.setBackground(Color.WHITE);

        // Bảng Trái: Top SP
        // [MỚI] Gọi UIHelper.createTablePanel
        JPanel pnlLeft = UIHelper.createTablePanel("Top 5 Sản Phẩm Bán Chạy (Tháng)", Style.COLOR_PRIMARY);
        String[] colsProd = {"Tên Sản Phẩm", "Đã Bán"};
        modelTopProducts = new DefaultTableModel(colsProd, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        
        // [MỚI] Gọi UIHelper.createStyledTable
        tblTopProducts = UIHelper.createStyledTable(modelTopProducts);
        
        JScrollPane scrLeft = new JScrollPane(tblTopProducts);
        scrLeft.setBorder(null); scrLeft.getViewport().setBackground(Color.WHITE);
        pnlLeft.add(scrLeft, BorderLayout.CENTER); // Đã fix logic trong UIHelper nên để Center hay North đều ổn, nhưng giữ Center cho đẹp trong khung panel

        // Bảng Phải: Top NV
        JPanel pnlRight = UIHelper.createTablePanel("Top 5 Nhân Viên Xuất Sắc (Tháng)", new Color(230, 126, 34));
        String[] colsStaff = {"Tên Nhân Viên", "Số Đơn", "Doanh Số"};
        modelTopStaff = new DefaultTableModel(colsStaff, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        
        tblTopStaff = UIHelper.createStyledTable(modelTopStaff);
        
        JScrollPane scrRight = new JScrollPane(tblTopStaff);
        scrRight.setBorder(null); scrRight.getViewport().setBackground(Color.WHITE);
        pnlRight.add(scrRight, BorderLayout.CENTER);

        pnlLists.add(pnlLeft);
        pnlLists.add(pnlRight);

        pnlBody.add(pnlLists, BorderLayout.CENTER);

        this.add(pnlBody, BorderLayout.CENTER);
        
        // Nút Refresh
        JButton btnRefresh = new JButton("Cập nhật dữ liệu mới nhất");
        btnRefresh.setFont(Style.FONT_BOLD);
        btnRefresh.addActionListener(e -> loadDataFromDB());
        this.add(btnRefresh, BorderLayout.SOUTH);
    }

    public void loadDataFromDB() {
        // 1. Load Cards
        lblRevenueVal.setText(df.format(homeDAO.getTodayRevenue()));
        lblOrderVal.setText(homeDAO.getTodayOrderCount() + " Đơn");
        lblCustomerVal.setText(homeDAO.getTotalCustomers() + " Khách");
        lblLowStockVal.setText(homeDAO.getLowStockCount() + " SP");

        // 2. Load Chart
        pnlChart.setData(statsDAO.getRevenueLast7Days());

        // 3. Load Top Products
        modelTopProducts.setRowCount(0);
        List<Object[]> listProd = homeDAO.getTopSellingProducts();
        for (Object[] row : listProd) {
            modelTopProducts.addRow(new Object[]{row[0], row[1] + " cái"});
        }

        // 4. Load Top Staff
        modelTopStaff.setRowCount(0);
        List<Object[]> listStaff = homeDAO.getTopEmployees();
        for (Object[] row : listStaff) {
            modelTopStaff.addRow(new Object[]{row[0], row[1] + " đơn", df.format(row[2])});
        }
    }
}