/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.view.admin;

/**
 *
 * @author Administrator
 */
import com.mycompany.dao.OrderDetailDAO;
import com.mycompany.model.OrderDetail;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailDialog extends JDialog {

    // 👉 THÊM: formatter tiền
    private final DecimalFormat df = new DecimalFormat("#,##0");

    public OrderDetailDialog(int orderId) {
        setTitle("Chi tiết đơn hàng #" + orderId);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setModal(true);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}, 0
        );
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        OrderDetailDAO dao = new OrderDetailDAO();
        List<OrderDetail> list = dao.getDetailsByOrderId(orderId);

        for (OrderDetail d : list) {
            model.addRow(new Object[]{
                d.getProductName(),
                d.getQuantity(),
                df.format(d.getUnitPrice()),          // ✅ SỬA 2.5E7
                df.format(d.getSubtotal())            // ✅ SỬA 4.925E7
            });
        }
    }
}


