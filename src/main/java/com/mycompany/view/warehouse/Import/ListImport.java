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
        setBackground(Color.WHITE); // Sử dụng màu trắng mặc định nếu Style lỗi
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- 1. THANH CÔNG CỤ ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);

        topPanel.add(new JLabel("Từ khóa:"));
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 30));
        topPanel.add(txtSearch);

        btnSearch = new JButton("Tìm");
        btnSearch.setBackground(new Color(0, 123, 255));
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
        btnFilter.setBackground(new Color(40, 167, 69));
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
            
            // Định nghĩa kiểu dữ liệu để Sorter hoạt động chính xác với số
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4) return Integer.class;
                return Object.class;
            }
        };
        
        tblImports = new JTable(model);
        tblImports.setRowHeight(35);
        
        sorter = new TableRowSorter<>(model);
        tblImports.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tblImports);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. SỰ KIỆN ---
        btnSearch.addActionListener(e -> applySearchFilter());
        txtSearch.addActionListener(e -> applySearchFilter());

        btnFilter.addActionListener(e -> {
            Date start = (Date) dateStart.getValue();
            Date end = (Date) dateEnd.getValue();
            if (start.after(end)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                return;
            }
            loadDataByDate(start, end);
        });

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            sorter.setRowFilter(null);
            loadData();
        });

        // Click dòng đổ vào ô tìm kiếm
        tblImports.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblImports.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tblImports.convertRowIndexToModel(row);
                    txtSearch.setText(model.getValueAt(modelRow, 3).toString());
                }
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<Object[]> data = importDAO.selectAllJoined();
            for (Object[] row : data) {
                processRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDataByDate(Date start, Date end) {
        model.setRowCount(0);
        try {
            List<Object[]> data = importDAO.selectByDateRange(start, end);
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu!");
            }
            for (Object[] row : data) {
                processRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm xử lý dữ liệu dòng để tránh lỗi ép kiểu (Cast Exception)
    private void processRow(Object[] row) {
        Object[] formattedRow = new Object[row.length];
        
        // ID
        formattedRow[0] = row[0];
        
        // Thời gian
        if (row[1] != null) {
            formattedRow[1] = sdf.format(row[1]);
        }
        
        // Nhà CC & Sản phẩm
        formattedRow[2] = row[2];
        formattedRow[3] = row[3];
        
        // Số lượng
        formattedRow[4] = row[4];
        
        // Giá & Tổng tiền (Định dạng số có dấu phẩy)
        try {
            formattedRow[5] = df.format(Double.parseDouble(row[5].toString()));
            formattedRow[6] = df.format(Double.parseDouble(row[6].toString()));
        } catch (Exception e) {
            formattedRow[5] = row[5];
            formattedRow[6] = row[6];
        }
        
        model.addRow(formattedRow);
    }

    private void applySearchFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Lọc không phân biệt hoa thường trên tất cả các cột
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }
}