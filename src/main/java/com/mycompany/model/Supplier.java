package com.mycompany.model;

public class Supplier {
    private int supplierId;
    private String supplierName;
    private String contactPerson; // Khớp với database
    private String phone;
    private String address;
    private boolean isActive;     // Khớp với database
   
    public Supplier() {}

    // Getters và Setters
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
     @Override
    public String toString() {
        return this.supplierName; // Trả về tên nhà cung cấp
    }
}