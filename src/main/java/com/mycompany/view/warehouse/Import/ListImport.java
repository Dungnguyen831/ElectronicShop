package com.mycompany.view.warehouse.Import;

import com.mycompany.dao.ImportDAO;
import com.mycompany.util.Style;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ListImport extends JPanel {

    private CardLayout cardLayout = new CardLayout();
    private JPanel containerPanel;

    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh, btnFilter;
    private JSpinner dateStart, dateEnd;

    private JTable tblImports;
    private DefaultTableModel modelImports;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTable tblDetails;
    private DefaultTableModel modelDetails;
    private JLabel lblDetailHeader;

    private ImportDAO importDAO = new ImportDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private DecimalFormat df = new DecimalFormat("#,###");

    public ListImport() {
        setLayout(new BorderLayout());
        containerPanel = new JPanel(cardLayout);

        initListView();
        initDetailView();

        add(containerPanel, BorderLayout.CENTER);
        loadData();
    }

    private void initListView() {
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBackground(Style.COLOR_BG_RIGHT);
        listPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- 1. THANH CÔNG CỤ (GIỮ NGUYÊN) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Style.COLOR_BG_RIGHT);

        topPanel.add(new JLabel("Từ khóa:"));
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 30));
        topPanel.add(txtSearch);

        btnSearch = new JButton("Tìm");
        btnSearch.setBackground(Style.COLOR_PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        topPanel.add(btnSearch);

        topPanel.add(new JLabel("|  Từ ngày:"));
        dateStart = new JSpinner(new SpinnerDateModel());
        dateStart.setEditor(new JSpinner.DateEditor(dateStart, "dd/MM/yyyy"));
        topPanel.add(dateStart);

        topPanel.add(new JLabel("Đến:"));
        dateEnd = new JSpinner(new SpinnerDateModel());
        dateEnd.setEditor(new JSpinner.DateEditor(dateEnd, "dd/MM/yyyy"));
        dateEnd.setValue(new Date());
        topPanel.add(dateEnd);

        btnFilter = new JButton("Lọc Ngày");
        btnFilter.setBackground(Style.COLOR_PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        topPanel.add(btnFilter);

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);
        topPanel.add(btnRefresh);

        // --- 2. BẢNG DỮ LIỆU ---
        String[] columns = {"ID Phiếu", "Thời Gian", "Nhà Cung Cấp", "Người Nhập","Tổng Tiền"};
        modelImports = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tblImports = new JTable(modelImports);
        tblImports.setRowHeight(35);
        tblImports.getTableHeader().setFont(Style.FONT_BOLD);
        sorter = new TableRowSorter<>(modelImports);
        tblImports.setRowSorter(sorter);

        // XỬ LÝ SỰ KIỆN CHUỘT: CLICK 1 LẦN & CLICK 2 LẦN
        tblImports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblImports.getSelectedRow();
                if (row < 0) return;
                
                int modelRow = tblImports.convertRowIndexToModel(row);
                
                // 1 Click: Điền tên nhà cung cấp vào ô tìm kiếm (Cột 2)
                String supplierName = modelImports.getValueAt(modelRow, 2).toString();
                txtSearch.setText(supplierName);

                // 2 Clicks: Mở bảng chi tiết
                if (e.getClickCount() == 2) {
                    showDetailView(modelRow);
                }
            }
        });

        listPanel.add(topPanel, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(tblImports), BorderLayout.CENTER);

        // --- 3. GÁN SỰ KIỆN NÚT BẤM ---
        btnSearch.addActionListener(e -> applySearchFilter());
        txtSearch.addActionListener(e -> applySearchFilter());

        btnFilter.addActionListener(e -> {
            Date start = (Date) dateStart.getValue();
            Date end = (Date) dateEnd.getValue();
            if (start.after(end)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
            loadDataByDate(start, end); // Gọi hàm lọc ngày mới
        });

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            dateEnd.setValue(new Date());
            loadData();
            sorter.setRowFilter(null);
        });

        containerPanel.add(listPanel, "LIST");
    }

    private void initDetailView() {
        JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblDetailHeader = new JLabel("CHI TIẾT PHIẾU");
        lblDetailHeader.setFont(Style.FONT_BOLD);
        
        JButton btnBack = new JButton("<< Quay lại");
        btnBack.addActionListener(e -> cardLayout.show(containerPanel, "LIST"));

        headerPanel.add(lblDetailHeader, BorderLayout.CENTER);
        headerPanel.add(btnBack, BorderLayout.WEST);

        modelDetails = new DefaultTableModel(new String[]{"Sản Phẩm", "Số Lượng", "Giá Nhập", "Thành Tiền"}, 0);
        tblDetails = new JTable(modelDetails);
        tblDetails.setRowHeight(30);

        detailPanel.add(headerPanel, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(tblDetails), BorderLayout.CENTER);

        containerPanel.add(detailPanel, "DETAIL");
    }

    private void loadData() {
        modelImports.setRowCount(0);
        List<Object[]> data = importDAO.selectAllImports();
        for (Object[] row : data) formatAndAddRow(row);
    }

    // SỬA LỖI LỌC NGÀY TẠI ĐÂY
    private void loadDataByDate(Date start, Date end) {
        modelImports.setRowCount(0);
        List<Object[]> data = importDAO.selectImportsByDate(start, end); // Dùng hàm mới trong DAO
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu trong khoảng thời gian này!");
        }
        for (Object[] row : data) formatAndAddRow(row);
    }

    private void formatAndAddRow(Object[] row) {
        if (row[1] instanceof Date) row[1] = sdf.format(row[1]);
        if (row[3] instanceof Number) row[3] = df.format(row[3]);
        modelImports.addRow(row);
    }

    private void showDetailView(int modelRow) {
        int id = (int) modelImports.getValueAt(modelRow, 0);
        String ncc = modelImports.getValueAt(modelRow, 2).toString();
        String total = modelImports.getValueAt(modelRow, 3).toString();

        lblDetailHeader.setText(" MÃ PHIẾU: #" + id + " | NCC: " + ncc + " | TỔNG: " + total);

        modelDetails.setRowCount(0);
        List<Object[]> details = importDAO.selectDetailsByImportId(id);
        for (Object[] d : details) {
            if (d[2] instanceof Number) d[2] = df.format(d[2]);
            if (d[3] instanceof Number) d[3] = df.format(d[3]);
            modelDetails.addRow(d);
        }
        cardLayout.show(containerPanel, "DETAIL");
    }

    private void applySearchFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Lọc trên cột Nhà CC (cột 2)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 2));
        }
    }
}