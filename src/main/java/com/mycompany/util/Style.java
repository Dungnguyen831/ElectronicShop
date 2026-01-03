/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 *
 * @author Nguyen Anh Dung
 */
public class Style {
    // 1. Bảng màu (Palette) - Flat Design
    public static final Color COLOR_PRIMARY = new Color(41, 128, 185);    // Xanh dương đậm
    public static final Color COLOR_PRIMARY_DARK = new Color(31, 97, 141); // Xanh khi hover
    public static final Color COLOR_BG_LEFT = new Color(52, 73, 94);      // Màu nền bên trái (Dark Blue)
    public static final Color COLOR_BG_RIGHT = new Color(255, 255, 255);  // Màu nền bên phải (Trắng)
    public static final Color COLOR_TEXT_HEADER = new Color(44, 62, 80);  // Màu chữ tiêu đề
    
    // 2. Font chữ
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    
    // 3. Border hiện đại (Chỉ gạch chân dưới)
    public static final Border BORDER_BOTTOM_FOCUS = BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_PRIMARY);
    public static final Border BORDER_BOTTOM_NORMAL = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

    
    public static final Color COLOR_SUCCESS = new Color(46, 204, 113); // Màu Xanh lá (Emerald) - Dùng cho nút Thêm/Lưu
    public static final Color COLOR_DANGER = new Color(231, 76, 60);   // Màu Đỏ (Alizarin) - Dùng cho nút Xóa/Hủy
}
