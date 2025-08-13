package com.example.appbanquanao;

public class TaiKhoan {
    String tdn,mk,quyen;

    public TaiKhoan(String tdn, String hoten, String quyen) {
        this.tdn = tdn;
        this.mk = hoten;
        this.quyen = quyen;
    }

    public String getTdn() {
        return tdn;
    }

    public void setTdn(String tdn) {
        this.tdn = tdn;
    }

    public String getMk() {
        return mk;
    }

    public void setMk(String hoten) {
        this.mk = hoten;
    }

    public String getQuyen() {
        return quyen;
    }

    public void setQuyen(String quyen) {
        this.quyen = quyen;
    }
}
