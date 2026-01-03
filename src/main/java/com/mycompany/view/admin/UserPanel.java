/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view.admin;

import com.mycompany.dao.UserDAO;
import com.mycompany.model.User;
import com.mycompany.util.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nguyen Anh Dung
 */
public class UserPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private UserDAO dao;

    public UserPanel() {
        this.dao = new UserDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP BAR ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTop.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Thêm Nhân Viên", Style.COLOR_SUCCESS);
        JButton btnDelete = createButton("Xóa", Style.COLOR_DANGER);
        JButton btnRefresh = createButton("Làm mới", Style.COLOR_PRIMARY);

        pnlTop.add(btnAdd);
        pnlTop.add(btnDelete);
        pnlTop.add(btnRefresh);
        
        this.add(pnlTop, BorderLayout.NORTH);

        // --- TABLE ---
        String[] columns = {"ID", "Tài khoản", "Họ Tên", "Vai Trò", "Mật khẩu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(Style.FONT_REGULAR);
        table.getTableHeader().setFont(Style.FONT_BOLD);
        table.getTableHeader().setBackground(Style.COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- EVENTS ---
        btnAdd.addActionListener(e -> showAddDialog());
        btnDelete.addActionListener(e -> deleteUser());
        btnRefresh.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<User> list = dao.getAll();
        for (User u : list) {
            // --- SỬA ĐOẠN NÀY ---
            String roleName = "";
            switch (u.getRoleId()) {
                case 1:
                    roleName = "Quản Trị Viên";
                    break;
                case 2:
                    roleName = "Nhân Viên Bán Hàng";
                    break;
                case 3:
                    roleName = "Nhân Viên Thủ Kho"; // <--- Thêm case 3
                    break;
                default:
                    roleName = "Không xác định";
                    break;
            }
            // --------------------

            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                roleName, // Biến này giờ đã hiển thị đúng 3 loại
                "******"
            });
        }
    }
    
    private void showAddDialog() {
        // Tạo form nhập liệu
        JTextField txtUser = new JTextField();
        JTextField txtFullname = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        
        // ComboBox chọn quyền
        String[] roles = {"Quản Trị Viên (Admin)", "Nhân Viên Bán Hàng (Staff)", "Nhân viên thủ kho (Warehouse)"};
        JComboBox<String> cboRole = new JComboBox<>(roles);
        cboRole.setSelectedIndex(1); // Mặc định chọn Staff

        Object[] message = {
            "Tài khoản:", txtUser,
            "Mật khẩu:", txtPass,
            "Họ tên:", txtFullname,
            "Chức vụ:", cboRole
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Nhân Viên Mới", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            if(txtUser.getText().isEmpty() || new String(txtPass.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không được để trống tài khoản/mật khẩu!");
                return;
            }

            User u = new User();
            u.setUsername(txtUser.getText());
            u.setPassword(new String(txtPass.getPassword()));
            u.setFullName(txtFullname.getText());
            // Lấy role: Index 0 là Admin (id 1), Index 1 là Staff (id 2)
            u.setRoleId(cboRole.getSelectedIndex() + 1); 

            if (dao.add(u)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại (Trùng tên tài khoản)!");
            }
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên cần xóa!");
            return;
        }
        
        int id = (int) table.getValueAt(selectedRow, 0);
        String username = (String) table.getValueAt(selectedRow, 1);
        
        // Chặn không cho xóa chính mình (Logic an toàn)
        // Bạn có thể lấy User hiện tại từ MainFrame truyền sang nếu muốn kỹ hơn
        if (username.equals("admin")) { 
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản Admin gốc!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa nhân viên " + username + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                loadData();
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
