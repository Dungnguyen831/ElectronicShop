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

    private JTextField txtSearch;
    private JButton btnSearch;
    private JTable tblImports;
    private DefaultTableModel model;
    private JSpinner dateStart, dateEnd;
    private JButton btnRefresh, btnFilter;
    
    // --- KHAI BÁO SORTER TOÀN CỤC ---
    private TableRowSorter<DefaultTableModel> sorter;

    private ImportDAO importDAO = new ImportDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private DecimalFormat df = new DecimalFormat("#,###");

    public ListImport() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Style.COLOR_BG_RIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- 1. THANH CÔNG CỤ ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(Style.COLOR_BG_RIGHT);

        // a. Ô tìm kiếm & Nút tìm
        topPanel.add(new JLabel("Từ khóa:"));
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 30));
        topPanel.add(txtSearch);

        btnSearch = new JButton("Tìm");
        btnSearch.setBackground(Style.COLOR_PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topPanel.add(btnSearch);

        // b. Bộ lọc ngày
        topPanel.add(new JLabel("|  Từ ngày:"));
        dateStart = new JSpinner(new SpinnerDateModel());
        dateStart.setEditor(new JSpinner.DateEditor(dateStart, "dd/MM/yyyy"));
        topPanel.add(dateStart);

        topPanel.add(new JLabel("Đến:"));
        dateEnd = new JSpinner(new SpinnerDateModel());
        dateEnd.setEditor(new JSpinner.DateEditor(dateEnd, "dd/MM/yyyy"));
        dateEnd.setValue(new Date());
        topPanel.add(dateEnd);

        // c. Nút chức năng
        btnFilter = new JButton("Lọc Ngày");
        btnFilter.setBackground(Style.COLOR_PRIMARY);
        btnFilter.setForeground(Color.WHITE);
        topPanel.add(btnFilter);
        
        btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. BẢNG DỮ LIỆU ---
        String[] columns = {"ID", "Thời Gian", "Nhà Cung Cấp", "Sản Phẩm", "Số Lượng", "Giá Nhập", "Tổng Tiền"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tblImports = new JTable(model);
        tblImports.setRowHeight(30);
        tblImports.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblImports.getTableHeader().setFont(Style.FONT_BOLD);

        // --- QUAN TRỌNG: KHỞI TẠO SORTER MỘT LẦN ---
        sorter = new TableRowSorter<>(model);
        tblImports.setRowSorter(sorter);
        
        // Sự kiện click dòng -> Điền tên vào ô tìm kiếm
        tblImports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblImports.getSelectedRow();
                if (row >= 0) {
                    // Cần convertRowIndexToModel vì khi lọc thứ tự dòng sẽ thay đổi
                    int modelRow = tblImports.convertRowIndexToModel(row);
                    String productName = model.getValueAt(modelRow, 3).toString();
                    txtSearch.setText(productName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblImports);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. XỬ LÝ SỰ KIỆN ---

        // A. Xử lý tìm kiếm (Nút bấm + Phím Enter)
        ActionListener searchAction = e -> applySearchFilter();
        btnSearch.addActionListener(searchAction);
        txtSearch.addActionListener(searchAction);

        // B. Lọc ngày
        btnFilter.addActionListener(e -> {
            Date start = (Date) dateStart.getValue();
            Date end = (Date) dateEnd.getValue();
            if (start.after(end)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
            loadDataByDate(start, end);
        });

        // C. Làm mới
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            dateEnd.setValue(new Date());
            loadData();
            // Reset bộ lọc
            sorter.setRowFilter(null);
          //  JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu mới nhất!");
        });
    }

    // --- CÁC HÀM HỖ TRỢ ---

    private void loadData() {
        model.setRowCount(0);
        List<Object[]> data = importDAO.selectAllJoined();
        for (Object[] row : data) formatAndAddRow(row);
    }

    private void loadDataByDate(Date start, Date end) {
        model.setRowCount(0);
        List<Object[]> data = importDAO.selectByDateRange(start, end);
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu trong khoảng thời gian này!");
        }
        for (Object[] row : data) formatAndAddRow(row);
    }

    private void formatAndAddRow(Object[] row) {
        if (row[1] != null && row[1] instanceof Date) {
            row[1] = sdf.format(row[1]);
        }
        // Format tiền tệ để dễ nhìn
        if (row[5] instanceof Number) row[5] = df.format(row[5]);
        if (row[6] instanceof Number) row[6] = df.format(row[6]);
        
        model.addRow(row);
    }

    // --- HÀM TÌM KIẾM ĐÃ SỬA LỖI ---
    private void applySearchFilter() {
        String text = txtSearch.getText().trim();
        
        // Debug: In ra console để biết nút đã hoạt động chưa
        System.out.println("Đang tìm kiếm từ khóa: " + text);

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                // (?i) nghĩa là không phân biệt hoa thường
                // Lọc trên cột 2 (Nhà CC) và cột 3 (Sản phẩm)
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 2, 3));
            } catch (java.util.regex.PatternSyntaxException e) {
                JOptionPane.showMessageDialog(this, "Từ khóa tìm kiếm chứa ký tự đặc biệt không hợp lệ!");
            }
        }
    }
}