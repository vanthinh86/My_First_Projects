package com.example.appbanquanao;

import android.os.Parcel;
import android.os.Parcelable;

public class ChiTietSanPham implements Parcelable {
    String masp;
    String tensp;
    Float dongia;
    String mota,ghichu;
    int soluongkho;

    String mansp;
    byte[] anh;

    public ChiTietSanPham(String masp, String tensp, Float dongia, String mota, String ghichu, int soluongkho, String mansp, byte[] anh) {
        this.masp = masp;
        this.tensp = tensp;
        this.dongia = dongia;
        this.mota = mota;
        this.ghichu = ghichu;
        this.soluongkho = soluongkho;
        this.mansp = mansp;
        this.anh = anh;
    }

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public Float getDongia() {
        return dongia;
    }

    public void setDongia(Float dongia) {
        this.dongia = dongia;
    }

    public String getMota() {
        return mota;
    }

    public void setMota(String mota) {
        this.mota = mota;
    }

    public String getGhichu() {
        return ghichu;
    }

    public void setGhichu(String ghichu) {
        this.ghichu = ghichu;
    }

    public int getSoluongkho() {
        return soluongkho;
    }

    public void setSoluongkho(int soluongkho) {
        this.soluongkho = soluongkho;
    }

    public String getMansp() {
        return mansp;
    }

    public void setMansp(String ma) {
        this.mansp = ma;
    }

    public byte[] getAnh() {
        return anh;
    }

    public void setAnh(byte[] anh) {
        this.anh = anh;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    protected ChiTietSanPham(Parcel in) {
        masp = in.readString();
        tensp = in.readString();
        dongia = in.readFloat();
        mota = in.readString();
        ghichu = in.readString();
        soluongkho = in.readInt();
       mansp = in.readString();
        anh = in.createByteArray();
    }
    public static final Creator<ChiTietSanPham> CREATOR = new Creator<ChiTietSanPham>   () {
        @Override
        public ChiTietSanPham createFromParcel(Parcel in) {
            return new ChiTietSanPham(in);
        }

        @Override
        public ChiTietSanPham[] newArray(int size) {
            return new ChiTietSanPham[size];
        }
    };
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(masp);
        dest.writeString(tensp);
        dest.writeFloat(dongia);
        dest.writeString(mota);
        dest.writeString(ghichu);
        dest.writeInt(soluongkho);
        dest.writeString(mansp);
        dest.writeByteArray(anh);
    }

}
