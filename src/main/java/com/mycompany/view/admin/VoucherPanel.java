package com.mycompany.view.admin;

import com.mycompany.dao.VoucherDAO;
import com.mycompany.model.Voucher;
import com.mycompany.util.Style;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VoucherPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private VoucherDAO dao;
    private JTextField txtSearch;
    private JComboBox<String> cboFilter; // ComboBox bộ lọc
    
    // Lưu danh sách gốc để lọc không bị mất dữ liệu
    private List<Voucher> originalList = new ArrayList<>();
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public VoucherPanel() {
        this.dao = new VoucherDAO();
        initComponents();
        refreshData(); // Tải dữ liệu lần đầu
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP BAR ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTop.setBackground(Color.WHITE);

        // 1. Tìm kiếm
        JLabel lblSearch = new JLabel("Tìm mã:");
        lblSearch.setFont(Style.FONT_BOLD);
        txtSearch = new JTextField(10);
        txtSearch.setPreferredSize(new Dimension(100, 30));
        JButton btnSearch = createButton("Tìm", Style.COLOR_PRIMARY);
        
        // 2. Bộ lọc (3 Tiêu chí bạn cần)
        JLabel lblFilter = new JLabel("Lọc theo:");
        lblFilter.setFont(Style.FONT_BOLD);
        String[] filters = {
            "Tất cả", 
            "Đang hoạt động", 
            "Đã khóa", 
            "Còn hạn dùng", 
            "Đã hết hạn", 
            "Hết số lượng"
        };
        cboFilter = new JComboBox<>(filters);
        cboFilter.setPreferredSize(new Dimension(130, 30));

        // 3. Các nút chức năng
        JButton btnAdd = createButton("Thêm", Style.COLOR_SUCCESS);
        JButton btnEdit = createButton("Sửa", Style.COLOR_PRIMARY);
        JButton btnDelete = createButton("Xóa", Style.COLOR_DANGER);
        JButton btnRefresh = createButton("Reset", Color.GRAY);

        pnlTop.add(lblSearch); pnlTop.add(txtSearch); pnlTop.add(btnSearch);
        pnlTop.add(Box.createHorizontalStrut(15)); 
        pnlTop.add(lblFilter); pnlTop.add(cboFilter);
        pnlTop.add(Box.createHorizontalStrut(15));
        pnlTop.add(btnAdd); pnlTop.add(btnEdit); pnlTop.add(btnDelete); pnlTop.add(btnRefresh);

        this.add(pnlTop, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columns = {"ID", "Mã Code", "Giảm (%)", "Tối đa", "Ngày BĐ", "Ngày KT", "Số lượng", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(Style.FONT_REGULAR);
        table.getTableHeader().setFont(Style.FONT_BOLD);
        table.getTableHeader().setBackground(Style.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- SỰ KIỆN ---
        
        // Tìm kiếm
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            originalList = dao.search(keyword); // Cập nhật list gốc
            cboFilter.setSelectedIndex(0);      // Reset lọc
            fillTable(originalList);
        });

        // Chọn bộ lọc
        cboFilter.addActionListener(e -> applyFilter());

        // Thêm
        btnAdd.addActionListener(e -> showDialog(null));

        // Sửa
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Chọn dòng cần sửa!"); return; }
            int id = (int) table.getValueAt(row, 0);
            
            // Lấy dữ liệu từ bảng đổ lại vào form
            try {
                String code = (String) table.getValueAt(row, 1);
                int percent = Integer.parseInt(table.getValueAt(row, 2).toString().replace("%", ""));
                double max = Double.parseDouble(table.getValueAt(row, 3).toString().replace(",", "").replace(" đ", ""));
                Date start = sdf.parse((String) table.getValueAt(row, 4));
                Date end = sdf.parse((String) table.getValueAt(row, 5));
                int qty = Integer.parseInt(table.getValueAt(row, 6).toString());
                String statusStr = (String) table.getValueAt(row, 7);
                int status = statusStr.equals("Hoạt động") ? 1 : 0;
                
                // Convert util.Date -> sql.Date cho model
                Voucher v = new Voucher(id, code, percent, max, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), qty, status);
                showDialog(v);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // Xóa
        btnDelete.addActionListener(e -> deleteVoucher());

        // Reset
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboFilter.setSelectedIndex(0);
            refreshData();
        });
    }

    // === CÁC HÀM XỬ LÝ LOGIC ===

    // 1. Tải lại dữ liệu gốc từ DB
    private void refreshData() {
        originalList = dao.getAll();
        applyFilter(); // Gọi lọc để hiển thị đúng theo combobox hiện tại
    }

    // 2. Logic Lọc dữ liệu (Quan trọng)
    private void applyFilter() {
        String criterion = (String) cboFilter.getSelectedItem();
        if (criterion == null) return;
        
        List<Voucher> filteredList = new ArrayList<>();
        Date now = new Date(); // Ngày hiện tại

        for (Voucher v : originalList) {
            boolean match = false;
            switch (criterion) {
                case "Tất cả": 
                    match = true; break;
                case "Đang hoạt động": 
                    if (v.getStatus() == 1) match = true; break;
                case "Đã khóa": 
                    if (v.getStatus() == 0) match = true; break;
                case "Còn hạn dùng":
                    // Nếu có ngày kết thúc và chưa qua ngày đó
                    if (v.getEndDate() != null && !now.after(v.getEndDate())) match = true;
                    break;
                case "Đã hết hạn":
                    // Nếu có ngày kết thúc và đã qua ngày đó
                    if (v.getEndDate() != null && now.after(v.getEndDate())) match = true;
                    break;
                case "Hết số lượng":
                    if (v.getQuantity() <= 0) match = true; break;
            }
            
            if (match) filteredList.add(v);
        }
        fillTable(filteredList);
    }

    // 3. Đổ dữ liệu vào bảng
    private void fillTable(List<Voucher> list) {
        tableModel.setRowCount(0);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        
        for (Voucher v : list) {
            tableModel.addRow(new Object[]{
                v.getVoucherId(),
                v.getCode(),
                v.getDiscountPercent() + "%",
                df.format(v.getMaxDiscount()) + " đ",
                sdf.format(v.getStartDate()),
                sdf.format(v.getEndDate()),
                v.getQuantity(),
                v.getStatus() == 1 ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    // 4. Hộp thoại Thêm/Sửa
    private void showDialog(Voucher v) {
        boolean isEdit = (v != null);
        
        JTextField txtCode = new JTextField(isEdit ? v.getCode() : "");
        JTextField txtPercent = new JTextField(isEdit ? String.valueOf(v.getDiscountPercent()) : "");
        JTextField txtMax = new JTextField(isEdit ? String.valueOf((long)v.getMaxDiscount()) : "");
        JTextField txtStart = new JTextField(isEdit ? sdf.format(v.getStartDate()) : sdf.format(new Date()));
        JTextField txtEnd = new JTextField(isEdit ? sdf.format(v.getEndDate()) : "2025-12-31");
        JTextField txtQty = new JTextField(isEdit ? String.valueOf(v.getQuantity()) : "100");
        JCheckBox chkActive = new JCheckBox("Đang hoạt động");
        chkActive.setSelected(isEdit ? (v.getStatus() == 1) : true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Mã Code:")); panel.add(txtCode);
        panel.add(new JLabel("Giảm (%):")); panel.add(txtPercent);
        panel.add(new JLabel("Giảm tối đa:")); panel.add(txtMax);
        panel.add(new JLabel("Ngày BĐ (yyyy-MM-dd):")); panel.add(txtStart);
        panel.add(new JLabel("Ngày KT (yyyy-MM-dd):")); panel.add(txtEnd);
        panel.add(new JLabel("Số lượng:")); panel.add(txtQty);
        panel.add(new JLabel("Trạng thái:")); panel.add(chkActive);

        int opt = JOptionPane.showConfirmDialog(this, panel, isEdit ? "Sửa Voucher" : "Thêm Voucher", JOptionPane.OK_CANCEL_OPTION);
        
        if (opt == JOptionPane.OK_OPTION) {
            try {
                if(txtCode.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nhập mã code!"); return;
                }
                
                Voucher newV = new Voucher();
                newV.setCode(txtCode.getText().trim().toUpperCase());
                newV.setDiscountPercent(Integer.parseInt(txtPercent.getText().trim()));
                newV.setMaxDiscount(Double.parseDouble(txtMax.getText().trim()));
                
                // Chuyển String -> util.Date -> sql.Date
                java.util.Date dateStart = sdf.parse(txtStart.getText().trim());
                java.util.Date dateEnd = sdf.parse(txtEnd.getText().trim());
                newV.setStartDate(new java.sql.Date(dateStart.getTime()));
                newV.setEndDate(new java.sql.Date(dateEnd.getTime()));
                
                newV.setQuantity(Integer.parseInt(txtQty.getText().trim()));
                newV.setStatus(chkActive.isSelected() ? 1 : 0);

                if(isEdit) {
                    newV.setVoucherId(v.getVoucherId());
                    if(dao.update(newV)) {
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                        refreshData();
                    }
                } else {
                    if(dao.add(newV)) {
                        JOptionPane.showMessageDialog(this, "Thêm thành công!");
                        refreshData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi: Mã code có thể bị trùng!");
                    }
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Ngày tháng phải đúng định dạng yyyy-MM-dd");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: " + ex.getMessage());
            }
        }
    }

    private void deleteVoucher() {
        int row = table.getSelectedRow();
        if(row == -1) { JOptionPane.showMessageDialog(this, "Chọn dòng để xóa!"); return; }
        
        int id = (int) table.getValueAt(row, 0);
        String code = (String) table.getValueAt(row, 1);
        
        if(JOptionPane.showConfirmDialog(this, "Xóa mã " + code + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if(dao.delete(id)) {
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                refreshData();
            }
        }
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