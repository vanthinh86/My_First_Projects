package com.example.appbanquanao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class NhomSanPhamHorizontalAdapter extends RecyclerView.Adapter<NhomSanPhamHorizontalAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NhomSanPham> mangNSP;

    public NhomSanPhamHorizontalAdapter(Context context, ArrayList<NhomSanPham> mangNSP) {
        this.context = context;
        this.mangNSP = mangNSP;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ds_nhomsanpham_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NhomSanPham nhomSanPham = mangNSP.get(position);
        holder.tenNhom.setText(nhomSanPham.getTennhom());

        if (nhomSanPham.getAnh() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(nhomSanPham.getAnh(), 0, nhomSanPham.getAnh().length);
            holder.imgNhom.setImageBitmap(bitmap);
        } else {
            holder.imgNhom.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DanhMucSanPham_Activity.class);
            intent.putExtra("nhomSpId", nhomSanPham.getMa());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mangNSP.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNhom;
        TextView tenNhom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNhom = itemView.findViewById(R.id.imgnsp);
            tenNhom = itemView.findViewById(R.id.ten);
        }
    }
}