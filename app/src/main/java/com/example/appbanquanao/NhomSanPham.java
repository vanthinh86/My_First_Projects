package com.example.appbanquanao;

public class NhomSanPham {

    String ma;
    String tennhom;
    byte[] anh;

    public NhomSanPham(String ma, String tennhom, byte[] anh) {
        this.ma = ma;
        this.tennhom = tennhom;
        this.anh = anh;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getTennhom() {
        return tennhom;
    }

    public void setTennhom(String tennhom) {
        this.tennhom = tennhom;
    }

    public byte[] getAnh() {
        return anh;
    }

    public void setAnh(byte[] anh) {
        this.anh = anh;
    }
    public String toString() {
        return tennhom; // Hiển thị tên nhóm sản phẩm
    }
}
