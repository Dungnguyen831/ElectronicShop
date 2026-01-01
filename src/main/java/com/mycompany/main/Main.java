/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.main;

import com.mycompany.view.LoginFrame;
//import com.mycompany.view.warehouse.WarehouseMainFrame;

/**
 *
 * @author Nguyen Anh Dung
 */
public class Main {
    public static void main(String[] args) {
// Set giao diện đẹp (Nimbus LookAndFeel)
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

  //       Chạy form đăng nhập
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
        
    }
    
}//huu check
//package com.mycompany.main;
//
//
//import com.mycompany.view.warehouse.WarehouseMainFrame;
//import com.mycompany.model.User; // Đảm bảo import model User
//
//public class Main {
//    public static void main(String[] args) {
//        // --- Giữ nguyên phần Set Nimbus LookAndFeel của bạn ---
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception ex) {
//            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//
//        // --- Chạy riêng WarehouseMainFrame ---
//        java.awt.EventQueue.invokeLater(() -> {
//            // Khởi tạo User mẫu để tránh lỗi truyền tham số
//            User testUser = new User();
//            testUser.setUserName("Test_Warehouse");
//            
//            // Mở frame và truyền user vào
//            WarehouseMainFrame frame = new WarehouseMainFrame(testUser);
//            frame.setVisible(true);
//        });
//    }
//}