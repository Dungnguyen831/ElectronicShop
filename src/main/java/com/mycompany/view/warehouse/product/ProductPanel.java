package com.mycompany.view.warehouse.product;

import com.mycompany.view.warehouse.product.ProductDialog;
import com.mycompany.dao.ProductDAO;
import com.mycompany.model.Product;
import com.mycompany.util.Style;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ProductPanel extends JPanel {
    private JTable tblProducts;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private ProductDAO dao = new ProductDAO();
    private JComboBox<String> cbQtyFilter;

    public ProductPanel() {
        initComponents();
        fillTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Style.COLOR_BG_RIGHT);

        // --- TOP: Tìm kiếm ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> fillDataToModel(dao.selectByKeyword(txtSearch.getText())));
        
        pnlTop.add(new JLabel("Tên sản phẩm:"));
        pnlTop.add(txtSearch);
        pnlTop.add(btnSearch);
        add(pnlTop, BorderLayout.NORTH);
        
        //bộ lọc số lượng
        cbQtyFilter = new JComboBox<>(new String[]{"Tất cả số lượng", "Đã hết hàng (= 0)", "Còn hàng (> 0)"});
        cbQtyFilter.setPreferredSize(new Dimension(150, 30));
        // Thêm vào panel phía trên (cùng hàng với txtSearch và btnSearch)
        pnlTop.add(new JLabel("Bộ lọc kho:"));
        pnlTop.add(cbQtyFilter);
        cbQtyFilter.addActionListener(e -> applyFilters());
        // --- CENTER: Bảng ---
        String[] headers = {"ID", "Danh Mục", "Nhà Cung Cấp", "Tên Sản Phẩm", "Mã Vạch", "Giá Nhập", "Giá Bán", "Số Lượng", "Trạng Thái", "Ngày Tạo"};
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Không cho sửa trực tiếp trên bảng
        };
        tblProducts = new JTable(model);
        tblProducts.setRowHeight(30);
        
        // Sự kiện: Click vào bảng hiện tên lên ô tìm kiếm
        tblProducts.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tblProducts.getSelectedRow();
                if (row >= 0) txtSearch.setText(tblProducts.getValueAt(row, 3).toString());
            }
        });
        add(new JScrollPane(tblProducts), BorderLayout.CENTER);

        // --- SOUTH: Nút chức năng cao 45px ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnAdd = createBtn("Thêm");
        JButton btnEdit = createBtn("Sửa");
        JButton btnDelete = createBtn("Xóa");
        JButton btnRefresh = createBtn("Làm mới");

        // Xử lý THÊM
      btnAdd.addActionListener(e -> {
        ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), null);

        while (true) {
            dialog.setVisible(true); // Mở và dừng lại đợi người dùng nhấn Lưu/Hủy

            if (dialog.isConfirmed()) {
                boolean success = dao.insert(dialog.getProduct()); 

                if (success) {
                    fillTable(); // Cập nhật bảng ngay khi thành công
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                    dialog.dispose(); // Thành công mới hủy Dialog khỏi bộ nhớ
                    break; // Thoát vòng lặp
                } else {
                    // THẤT BẠI: Thông báo lỗi và KHÔNG break, vòng lặp sẽ chạy lại setVisible(true)
                    JOptionPane.showMessageDialog(this, "Thêm THẤT BẠI! Vui lòng kiểm tra lại dữ liệu.");
                    dialog.setConfirmed(false); // Reset trạng thái để chờ nhấn Lưu lần nữa
                }
            } else {
                // Người dùng nhấn Hủy hoặc đóng bằng dấu X
                dialog.dispose();
                break; 
            }
        }
    });

        // Xử lý SỬA (Đã bổ sung logic)
        btnEdit.addActionListener(e -> {
            int row = tblProducts.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblProducts.getValueAt(row, 0);

                // Lấy dữ liệu mới nhất từ DB để đảm bảo tính chính xác
                Product p = dao.selectAll().stream()
                               .filter(x -> x.getProductId() == id)
                               .findFirst()
                               .orElse(null);

                if (p != null) {
                    ProductDialog dialog = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), p);
                    dialog.setVisible(true);

                    if (dialog.isConfirmed()) {
                        // GỌI HÀM UPDATE VÀ KIỂM TRA KẾT QUẢ TRẢ VỀ
                        boolean success = dao.update(dialog.getProduct());

                        if (success) {
                            fillTable(); // Chỉ load lại bảng nếu thành công
                            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Cập nhật THẤT BẠI! Vui lòng kiểm tra dữ liệu hoặc kết nối mạng.");
                        }
                    }
                } else {
                     JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm trong dữ liệu gốc!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!");
            }
        });

        // Xử lý XÓA
        btnDelete.addActionListener(e -> {
            int row = tblProducts.getSelectedRow();
            if (row >= 0) {
                int id = (int) tblProducts.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa sản phẩm ID: " + id + "?") == JOptionPane.YES_OPTION) {
                    dao.delete(id);
                    fillTable();
                }
            } else { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng để xóa!"); }
        });

        // Xử lý LÀM MỚI
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            fillTable();
        });

        pnlButtons.add(btnAdd); pnlButtons.add(btnEdit); pnlButtons.add(btnDelete); pnlButtons.add(btnRefresh);
        add(pnlButtons, BorderLayout.SOUTH);
        
    }

    private JButton createBtn(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 45)); // Chiều cao 45px
        return btn;
    }

    public void fillTable() { fillDataToModel(dao.selectAll()); }

   private void fillDataToModel(java.util.List<Product> list) {
        model.setRowCount(0);
       for (Product p : list) {
            String statusText;
                statusText = (p.getStatus() == 1) ? "Đang bán" : "Ngừng bán";
           
            // Thêm vào model để hiển thị
           model.addRow(new Object[]{
                p.getProductId(),
                p.getCategoryName(), 
                p.getSupplierName(), 
                p.getProductName(),
                p.getBarcode(),
                String.format("%,.0f", p.getImportPrice()), 
                String.format("%,.0f", p.getSalePrice()),
                p.getQuantity(),
                statusText, // Sử dụng biến statusText đã kiểm tra ràng buộc
                p.getCreatedAt()
            });
        }
    }
    private void applyFilters() {
        DefaultTableModel model = (DefaultTableModel) tblProducts.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tblProducts.setRowSorter(sorter);

        int selectedIdx = cbQtyFilter.getSelectedIndex();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Cột "Số Lượng" trong bảng của bạn là cột index 7
                int qty = Integer.parseInt(entry.getStringValue(7));

                if (selectedIdx == 1) return qty <= 0; // Lọc sản phẩm hết hàng
                if (selectedIdx == 2) return qty > 0;  // Lọc sản phẩm còn hàng
                return true; // "Tất cả số lượng"
            }
        });
    }
    
}