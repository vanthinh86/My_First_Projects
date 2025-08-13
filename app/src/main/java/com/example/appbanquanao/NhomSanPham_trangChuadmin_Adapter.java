package com.example.appbanquanao;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NhomSanPham_trangChuadmin_Adapter  extends BaseAdapter {
    private Context context;
    private ArrayList<NhomSanPham> nhomSanPhamList;
    private boolean showFullDetails;

    public NhomSanPham_trangChuadmin_Adapter(Activity context, ArrayList<NhomSanPham> nhomSanPhamList, boolean showFullDetails) {
        this.context = context;
        this.nhomSanPhamList = nhomSanPhamList;
        this.showFullDetails = showFullDetails;
    }

    @Override
    public int getCount() {
        return nhomSanPhamList.size();
    }

    @Override
    public Object getItem(int position) {
        return nhomSanPhamList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (showFullDetails) {
            return getViewTrue(position, convertView, parent);
        } else {
            return getViewFalse(position, convertView, parent);
        }
    }

    private View getViewTrue(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.ds_nhomsanpham, parent, false);
        }

        NhomSanPham nhomSanPham = nhomSanPhamList.get(position);
        TextView ten = view.findViewById(R.id.ten);
        TextView id = view.findViewById(R.id.idma);
        ImageView anh = view.findViewById(R.id.imgnsp);
        ImageButton xoa = view.findViewById(R.id.imgxoa);
        ImageButton sua = view.findViewById(R.id.imgsua);

        Button back = view.findViewById(R.id.btnback);
        back.setOnClickListener(v -> {
            ((Activity) context).finish();
        });

        id.setText(nhomSanPham.getMa());
        ten.setText(nhomSanPham.getTennhom());

        byte[] anhByteArray = nhomSanPham.getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            anh.setImageBitmap(imganhbs);
        } else {
            anh.setImageResource(R.drawable.vest);
        }



        return view;
    }

    private View getViewFalse(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.ds_hienthi_gridview2_nguoidung, parent, false);
        }

        NhomSanPham nhomSanPham = nhomSanPhamList.get(position);
        TextView ten = view.findViewById(R.id.ten);
        TextView id = view.findViewById(R.id.idma);
        ImageView anh = view.findViewById(R.id.imgnsp);

        id.setText(nhomSanPham.getMa());
        ten.setText(nhomSanPham.getTennhom());

        byte[] anhByteArray = nhomSanPham.getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            anh.setImageBitmap(imganhbs);
        } else {
            anh.setImageResource(R.drawable.vest);
        }

        return view;
    }


}
