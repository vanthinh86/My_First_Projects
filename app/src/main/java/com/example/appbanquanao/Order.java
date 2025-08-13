package com.example.appbanquanao;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private long id; // ID đơn hàng - đổi thành long để tránh overflow
    private String tenKh;
    private String diaChi;
    private String sdt;
    private float tongTien;
    private String ngayDatHang;
    private int trangThai;
    private List<ChiTietDonHang> chiTietList; // Danh sách chi tiết đơn hàng
    private byte[] firstProductImage; // Ảnh sản phẩm đầu tiên trong đơn hàng

    public static final String[] TRANG_THAI = {
      "Đã đặt hàng", "Đang vận chuyển", "Vận chuyển thành công", "Hoàn hàng"
    };
    public static final int DA_DAT_HANG = 0;
    public static final int DANG_VAN_CHUYEN = 1;
    public static final int VAN_CHUYEN_THANH_CONG = 2;
    public static final int HOAN_HANG = 3;

    public Order(long id, String tenKh, String diaChi, String sdt, float tongTien, String ngayDatHang) {
        this.id = id;
        this.tenKh = tenKh;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.tongTien = tongTien;
        this.ngayDatHang = ngayDatHang;
        this.chiTietList = new ArrayList<>(); // Khởi tạo danh sách
    }

    public Order(long id, String tenKh, String diaChi, String sdt, float tongTien, String ngayDatHang, int trangThai) {
        this.id = id;
        this.tenKh = tenKh;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.tongTien = tongTien;
        this.ngayDatHang = ngayDatHang;
        this.chiTietList = new ArrayList<>(); // Khởi tạo danh sách
        this.trangThai = trangThai;
    }

    // Getter methods
    public long getId() {
        return id;
    }

    public int getIdDatHang() {
        return (int) id;
    }

    public String getTenKh() {
        return tenKh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public float getTongTien() {
        return tongTien;
    }

    public String getNgayDatHang() {
        return ngayDatHang;
    }

    public int getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(int trangThai) {
        this.trangThai = trangThai;

    }

    public List<ChiTietDonHang> getChiTietList() {
        return chiTietList; // Getter cho danh sách chi tiết
    }

    public void setChiTietList(List<ChiTietDonHang> chiTietList) {
        this.chiTietList = chiTietList; // Setter cho danh sách chi tiết
    }
    
    public byte[] getFirstProductImage() {
        return firstProductImage;
    }
    
    public void setFirstProductImage(byte[] firstProductImage) {
        this.firstProductImage = firstProductImage;
    }
}