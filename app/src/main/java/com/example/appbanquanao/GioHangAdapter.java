package com.example.appbanquanao;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GioHangAdapter extends ArrayAdapter<GioHang> {
    private Context context;
    private List<GioHang> items;
    private TextView txtTongTien; // Tham chiếu tới TextView tổng tiền
    private GioHangManager gioHangManager;

    public GioHangAdapter(Context context, List<GioHang> items, TextView txtTongTien) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.txtTongTien = txtTongTien;
        this.gioHangManager = GioHangManager.getInstance(); // Khởi tạo GioHangManager
        updateTongTien(); // Cập nhật tổng tiền ban đầu
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ds_giohang, parent, false);
        }

        TextView tensp = convertView.findViewById(R.id.tensp);
        ImageView imgSanPham = convertView.findViewById(R.id.imgsp);
        TextView masp = convertView.findViewById(R.id.masp);
        TextView dongia = convertView.findViewById(R.id.dongia);
        TextView soLuong = convertView.findViewById(R.id.soluongdat);
        ImageButton btnGiam = convertView.findViewById(R.id.btnTru);
        ImageButton btnTang = convertView.findViewById(R.id.btnCong);
        TextView xoasp = convertView.findViewById(R.id.xoasp);

        GioHang item = items.get(position);
        tensp.setText(item.getSanPham().getTensp());
        dongia.setText(String.valueOf(item.getSanPham().getDongia()));
        soLuong.setText(String.valueOf(item.getSoLuong()));
        masp.setText(item.getSanPham().getMasp());

        byte[] anhByteArray = item.getSanPham().getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganh = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            imgSanPham.setImageBitmap(imganh);
        } else {
            imgSanPham.setImageResource(R.drawable.vest);
        }

        // Thiết lập sự kiện cho nút tăng số lượng
        btnTang.setOnClickListener(v -> {
            gioHangManager.addItem(item.getSanPham());
            notifyDataSetChanged();
            updateTongTien();
        });

        // Thiết lập sự kiện cho nút giảm số lượng
        btnGiam.setOnClickListener(v -> {
            if (item.getSoLuong() > 1) {
                item.setSoLuong(item.getSoLuong() - 1);
            } else {
                gioHangManager.removeItem(position);
                items.remove(position);
            }
            notifyDataSetChanged();
            updateTongTien();
        });

        // Thiết lập sự kiện cho TextView xoasp
        xoasp.setOnClickListener(v -> {
            gioHangManager.removeItem(position); // Gọi phương thức xóa sản phẩm trong giỏ hàng
            items.remove(position); // Xóa sản phẩm khỏi danh sách hiện tại
            notifyDataSetChanged(); // Cập nhật giao diện
            updateTongTien(); // Cập nhật tổng tiền
            Toast.makeText(context, "Sản phẩm đã được xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo
        });



        return convertView;
    }

    private void updateTongTien() {
        float tongTien = gioHangManager.getTongTien(); // Lấy tổng tiền từ GioHangManager
        txtTongTien.setText(String.valueOf(tongTien)); // Cập nhật TextView
    }








}
