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
    
}