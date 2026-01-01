/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Nguyen Anh Dung
 */
public class DatabaseConnection {
    public static Connection getConnection(){
        Connection cons = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Sửa thành tài khoản database của mình
            cons = DriverManager.getConnection("jdbc:mysql://localhost:3306/electronics_shop", "root", "");
        } catch (ClassNotFoundException | SQLException e) {
        }
        
        return cons;
    }
    
    public static void main(String[] args) {
        System.out.println(getConnection());

        System.out.println("Test pull github");

    }
}
//ĐCM NETBEAN