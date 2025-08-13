package com.example.appbanquanao;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GioHang implements Parcelable {
    private ChiTietSanPham sanPham;
    private int soLuong;

    public GioHang(ChiTietSanPham sanPham, int soLuong) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }

    protected GioHang(Parcel in) {
        sanPham = in.readParcelable(ChiTietSanPham.class.getClassLoader());
        soLuong = in.readInt();
    }

    public static final Creator<GioHang> CREATOR = new Creator<GioHang>() {
        @Override
        public GioHang createFromParcel(Parcel in) {
            return new GioHang(in);
        }

        @Override
        public GioHang[] newArray(int size) {
            return new GioHang[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(sanPham, flags);
        dest.writeInt(soLuong);
    }

    // Getter và Setter
    public ChiTietSanPham getSanPham() {
        return sanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
    // Phương thức tính tổng giá
    public float getTongGia() {
        return sanPham.getDongia() * soLuong; // Giả sử ChiTietSanPham có phương thức getDongia
    }

}