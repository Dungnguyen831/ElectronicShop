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
    private JComboBox<String> cboFilter;
    
    private List<Voucher> originalList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public VoucherPanel() {
        this.dao = new VoucherDAO();
        // Cài đặt strict mode cho ngày tháng (nhập 2025-02-30 sẽ báo lỗi thay vì tự nhảy sang tháng 3)
        sdf.setLenient(false); 
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP BAR ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm mã:");
        lblSearch.setFont(Style.FONT_BOLD);
        txtSearch = new JTextField(10);
        txtSearch.setPreferredSize(new Dimension(100, 30));
        JButton btnSearch = createButton("Tìm", Style.COLOR_PRIMARY);
        
        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(Style.FONT_BOLD);
        String[] filters = {"Tất cả", "Đang hoạt động", "Đã khóa", "Còn hạn dùng", "Đã hết hạn", "Hết số lượng"};
        cboFilter = new JComboBox<>(filters);
        cboFilter.setPreferredSize(new Dimension(130, 30));

        JButton btnAdd = createButton("Thêm", Style.COLOR_SUCCESS);
        JButton btnEdit = createButton("Sửa", Style.COLOR_PRIMARY);
        JButton btnDelete = createButton("Xóa", Style.COLOR_DANGER);
        JButton btnRefresh = createButton("Reset", Color.GRAY);

        pnlTop.add(lblSearch); pnlTop.add(txtSearch); pnlTop.add(btnSearch);
        pnlTop.add(Box.createHorizontalStrut(10));
        pnlTop.add(lblFilter); pnlTop.add(cboFilter);
        pnlTop.add(Box.createHorizontalStrut(10));
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

        // --- EVENTS ---
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            originalList = dao.search(keyword);
            cboFilter.setSelectedIndex(0);
            fillTable(originalList);
        });

        cboFilter.addActionListener(e -> applyFilter());
        btnAdd.addActionListener(e -> showDialog(null));
        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Chọn voucher cần sửa!"); return; }
            int id = (int) table.getValueAt(row, 0);
            try {
                String code = (String) table.getValueAt(row, 1);
                int percent = Integer.parseInt(table.getValueAt(row, 2).toString().replace("%", ""));
                double max = Double.parseDouble(table.getValueAt(row, 3).toString().replace(",", "").replace(" đ", ""));
                Date start = sdf.parse((String) table.getValueAt(row, 4));
                Date end = sdf.parse((String) table.getValueAt(row, 5));
                int qty = Integer.parseInt(table.getValueAt(row, 6).toString());
                String statusStr = (String) table.getValueAt(row, 7);
                int status = statusStr.equals("Hoạt động") ? 1 : 0;
                
                Voucher v = new Voucher(id, code, percent, max, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), qty, status);
                showDialog(v);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnDelete.addActionListener(e -> deleteVoucher());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); cboFilter.setSelectedIndex(0); refreshData(); });
    }

    // === LOGIC NGHIỆP VỤ ===

    private void refreshData() {
        originalList = dao.getAll();
        applyFilter();
    }

    private void applyFilter() {
        String criterion = (String) cboFilter.getSelectedItem();
        if (criterion == null) return;
        List<Voucher> filteredList = new ArrayList<>();
        Date now = new Date();

        for (Voucher v : originalList) {
            boolean match = false;
            switch (criterion) {
                case "Tất cả": match = true; break;
                case "Đang hoạt động": if (v.getStatus() == 1) match = true; break;
                case "Đã khóa": if (v.getStatus() == 0) match = true; break;
                case "Còn hạn dùng": if (v.getEndDate() != null && !now.after(v.getEndDate())) match = true; break;
                case "Đã hết hạn": if (v.getEndDate() != null && now.after(v.getEndDate())) match = true; break;
                case "Hết số lượng": if (v.getQuantity() <= 0) match = true; break;
            }
            if (match) filteredList.add(v);
        }
        fillTable(filteredList);
    }

    private void fillTable(List<Voucher> list) {
        tableModel.setRowCount(0);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        for (Voucher v : list) {
            tableModel.addRow(new Object[]{
                v.getVoucherId(), v.getCode(), v.getDiscountPercent() + "%", df.format(v.getMaxDiscount()) + " đ",
                sdf.format(v.getStartDate()), sdf.format(v.getEndDate()), v.getQuantity(), v.getStatus() == 1 ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    // === FORM VALIDATION (QUAN TRỌNG: MỚI THÊM) ===
    private String validateInput(String code, String percentStr, String maxStr, String startStr, String endStr, String qtyStr) {
        // 1. Check rỗng
        if (code.isEmpty() || percentStr.isEmpty() || maxStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty() || qtyStr.isEmpty()) {
            return "Vui lòng nhập đầy đủ tất cả các trường!";
        }
        
        // 2. Check Mã Code
        if (code.contains(" ")) return "Mã Voucher không được chứa khoảng trắng!";
        if (code.length() < 3) return "Mã Voucher phải từ 3 ký tự trở lên!";

        try {
            // 3. Check Phần trăm giảm
            int percent = Integer.parseInt(percentStr);
            if (percent <= 0 || percent > 100) return "Phần trăm giảm phải từ 1 đến 100!";

            // 4. Check Giảm tối đa
            double max = Double.parseDouble(maxStr);
            if (max < 0) return "Số tiền giảm tối đa không được âm!";

            // 5. Check Số lượng
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) return "Số lượng phát hành phải lớn hơn 0!";

            // 6. Check Ngày tháng
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);
            
            if (end.before(start)) return "Ngày kết thúc phải SAU ngày bắt đầu!";
            
        } catch (NumberFormatException e) {
            return "Vui lòng nhập đúng định dạng số cho: %, Tiền giảm, Số lượng!";
        } catch (ParseException e) {
            return "Ngày tháng phải đúng định dạng yyyy-MM-dd (Ví dụ: 2025-01-30)!";
        }

        return null; // Không có lỗi
    }

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

        if (isEdit) txtCode.setEditable(false); // Không cho sửa mã khi edit

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Mã Code (VD: SALE50):")); panel.add(txtCode);
        panel.add(new JLabel("Giảm (%):")); panel.add(txtPercent);
        panel.add(new JLabel("Giảm tối đa (VNĐ):")); panel.add(txtMax);
        panel.add(new JLabel("Ngày BĐ (yyyy-MM-dd):")); panel.add(txtStart);
        panel.add(new JLabel("Ngày KT (yyyy-MM-dd):")); panel.add(txtEnd);
        panel.add(new JLabel("Số lượng:")); panel.add(txtQty);
        panel.add(new JLabel("Trạng thái:")); panel.add(chkActive);

        int opt = JOptionPane.showConfirmDialog(this, panel, isEdit ? "Sửa Voucher" : "Thêm Voucher", JOptionPane.OK_CANCEL_OPTION);
        
        if (opt == JOptionPane.OK_OPTION) {
            // --- GỌI HÀM VALIDATE ---
            String error = validateInput(
                txtCode.getText().trim(), 
                txtPercent.getText().trim(), 
                txtMax.getText().trim(), 
                txtStart.getText().trim(), 
                txtEnd.getText().trim(), 
                txtQty.getText().trim()
            );

            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return; // Dừng lại, không lưu
            }
            // ------------------------

            try {
                Voucher newV = new Voucher();
                newV.setCode(txtCode.getText().trim().toUpperCase());
                newV.setDiscountPercent(Integer.parseInt(txtPercent.getText().trim()));
                newV.setMaxDiscount(Double.parseDouble(txtMax.getText().trim()));
                newV.setStartDate(new java.sql.Date(sdf.parse(txtStart.getText().trim()).getTime()));
                newV.setEndDate(new java.sql.Date(sdf.parse(txtEnd.getText().trim()).getTime()));
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
                        JOptionPane.showMessageDialog(this, "Lỗi: Mã code đã tồn tại!");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(Style.FONT_BOLD); btn.setFocusPainted(false);
        return btn;
    }
}