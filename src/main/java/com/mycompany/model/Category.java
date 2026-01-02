/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.model;

/**
 *
 * @author admin
 */
public class Category {
    private int categoryId;
    private String categoryName;
    private String description;
    
    public Category(int categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    // Quan trọng: Hàm này giúp ComboBox hiển thị tên danh mục thay vì địa chỉ bộ nhớ
    @Override
    public String toString() {
        return categoryName;
    }
}
