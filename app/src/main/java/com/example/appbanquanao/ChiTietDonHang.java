package com.example.appbanquanao;

import android.os.Parcel;
import android.os.Parcelable;

public class ChiTietDonHang implements Parcelable {
    private int id_chitiet; // ID chi tiết đơn hàng
    private long id_dathang; // ID đơn hàng
    private int masp; // Mã sản phẩm
    private int soLuong; // Số lượng
    private float donGia; // Đơn giá
    private byte[] anh; // Hình ảnh (dưới dạng byte array)
    private int trangthai; // Trạng thái đơn hàng
    private String tenSanPham; // Thêm tên sản phẩm
    private int trangthaithanhtoan; // Trạng thái thanh toán (0: chưa thanh toán, 1: đã thanh toán)
    private int phuongthucthanhtoan; // Phương thức thanh toán (0: tiền mặt, 1: chuyển khoản)
    private String ngaycapnhat; // Ngày cập nhật đơn hàng (chỉ hiển thị cho admin)

    // Constructor với tất cả các tham số
    public ChiTietDonHang(int id_chitiet, long id_dathang, int masp, String tenSanPham, int soLuong, float donGia, byte[] anh, int trangthai) {
        this.id_chitiet = id_chitiet;
        this.id_dathang = id_dathang;
        this.masp = masp;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.anh = anh; // Đặt thuộc tính ảnh là kiểu byte[]
        this.trangthai = trangthai;
        this.trangthaithanhtoan = 0; // Default chưa thanh toán
        this.phuongthucthanhtoan = 0; // Default tiền mặt
    }

    public int getId_chitiet() {
        return id_chitiet;
    }

    public void setId_chitiet(int id_chitiet) {
        this.id_chitiet = id_chitiet;
    }

    public long getId_dathang() {
        return id_dathang;
    }

    public void setId_dathang(long id_dathang) {
        this.id_dathang = id_dathang;
    }

    public int getMasp() {
        return masp;
    }

    public void setMasp(int masp) {
        this.masp = masp;
    }

    public int getSoLuong() {
        return soLuong; // Getter cho số lượng
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public float getDonGia() {
        return donGia; // Getter cho đơn giá
    }

    public void setDonGia(float donGia) {
        this.donGia = donGia;
    }

    public byte[] getAnh() {
        return anh; // Getter cho ảnh (byte array)
    }

    public void setAnh(byte[] anh) {
        this.anh = anh; // Setter cho ảnh
    }

    public int getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(int trangthai) {
        this.trangthai = trangthai;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public int getTrangthaithanhtoan() {
        return trangthaithanhtoan;
    }

    public void setTrangthaithanhtoan(int trangthaithanhtoan) {
        this.trangthaithanhtoan = trangthaithanhtoan;
    }

    public int getPhuongthucthanhtoan() {
        return phuongthucthanhtoan;
    }

    public void setPhuongthucthanhtoan(int phuongthucthanhtoan) {
        this.phuongthucthanhtoan = phuongthucthanhtoan;
    }

    public String getNgaycapnhat() {
        return ngaycapnhat;
    }

    public void setNgaycapnhat(String ngaycapnhat) {
        this.ngaycapnhat = ngaycapnhat;
    }

    // Constructor từ Parcel
    protected ChiTietDonHang(Parcel in) {
        id_chitiet = in.readInt();
        masp = in.readInt();
        tenSanPham = in.readString();
        soLuong = in.readInt();
        donGia = in.readFloat();
        anh = in.createByteArray(); // Đọc ảnh từ Parcel dưới dạng byte array
        id_dathang = in.readLong(); // Đọc ID đơn hàng từ Parcel
        trangthai = in.readInt(); // Đọc trạng thái đơn hàng từ Parcel
        trangthaithanhtoan = in.readInt(); // Đọc trạng thái thanh toán từ Parcel
        phuongthucthanhtoan = in.readInt(); // Đọc phương thức thanh toán từ Parcel
        ngaycapnhat = in.readString(); // Đọc ngày cập nhật từ Parcel
    }

    // Creator cho Parcelable
    public static final Creator<ChiTietDonHang> CREATOR = new Creator<ChiTietDonHang>() {
        @Override
        public ChiTietDonHang createFromParcel(Parcel in) {
            return new ChiTietDonHang(in);
        }

        @Override
        public ChiTietDonHang[] newArray(int size) {
            return new ChiTietDonHang[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id_chitiet);
        dest.writeInt(masp);
        dest.writeString(tenSanPham);
        dest.writeInt(soLuong);
        dest.writeFloat(donGia);
        dest.writeByteArray(anh); // Ghi ảnh vào Parcel dưới dạng byte array
        dest.writeLong(id_dathang); // Ghi ID đơn hàng vào Parcel
        dest.writeInt(trangthai); // Ghi trạng thái đơn hàng vào Parcel
        dest.writeInt(trangthaithanhtoan); // Ghi trạng thái thanh toán vào Parcel
        dest.writeInt(phuongthucthanhtoan); // Ghi phương thức thanh toán vào Parcel
        dest.writeString(ngaycapnhat); // Ghi ngày cập nhật vào Parcel
    }
}
