package com.example.appbanquanao;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SanPham_DanhMuc_Adapter extends BaseAdapter {

    private Context context;
    private Uri selectedImageUri; // Biến lưu trữ URI đã chọn
    private static final int REQUEST_CODE_PICK_IMAGE = 1; // Định nghĩa mã yêu cầu
    private ArrayList<SanPham> spList;
    private boolean showFullDetails; // Biến để xác định xem có hiển thị 7 thuộc tính hay không
    public SanPham_DanhMuc_Adapter(Context context, ArrayList<SanPham> bacsiList, boolean showFullDetails) {
        this.context = context;
        this.spList = bacsiList;
        this.showFullDetails = showFullDetails; // Khởi tạo biến
    }

    @Override
    public int getCount() {
        return spList.size();
    }

    @Override
    public Object getItem(int position) {
        return spList.get(position);
    }

    public void setSelectedImageUri(Uri uri) {
        this.selectedImageUri = uri; // Setter để cập nhật URI
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (showFullDetails) {
            return getViewWith8Properties(position, convertView, parent);
        } else {
            return getViewWith4Properties(position, convertView, parent);
        }
    }




    public View getViewWith8Properties(int i, View view, ViewGroup parent) {
        View viewtemp;
        if (view == null) {
            viewtemp = LayoutInflater.from(parent.getContext()).inflate(R.layout.ds_sanpham, parent, false);
        } else {
            viewtemp = view;
        }

        SanPham tt = spList.get(i);
        TextView masp = viewtemp.findViewById(R.id.masp);
        TextView tensp = viewtemp.findViewById(R.id.tensp);
        TextView dongia = viewtemp.findViewById(R.id.dongia);
        TextView mota = viewtemp.findViewById(R.id.mota);
        TextView ghichu = viewtemp.findViewById(R.id.ghichu);
        TextView soluongkho = viewtemp.findViewById(R.id.soluongkho);
        TextView manhomsanpham = viewtemp.findViewById(R.id.manhomsanpham);
        ImageView anh = viewtemp.findViewById(R.id.imgsp);
        ImageButton sua = viewtemp.findViewById(R.id.imgsua);
        ImageButton xoa = viewtemp.findViewById(R.id.imgxoa);

        // Hiển thị thông tin bác sĩ
        masp.setText(tt.getMasp());
        tensp.setText(tt.getTensp());
        dongia.setText(String.valueOf(tt.getDongia())); // Chuyển đổi Float thành String
        mota.setText(tt.getMota());
        ghichu.setText(tt.getGhichu());
        soluongkho.setText(String.valueOf(tt.getSoluongkho())); // Chuyển đổi Integer thành String
        manhomsanpham.setText(tt.getMansp());

        // Hiển thị ảnh bác sĩ
        byte[] anhByteArray = tt.getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            anh.setImageBitmap(imganhbs);
        } else {
            anh.setImageResource(R.drawable.vest);
        }

        // Sự kiện cho nút "Sửa"
        sua.setOnClickListener(view1 -> showEditDialog(tt));

        // Sự kiện cho nút "Xóa"
        xoa.setOnClickListener(v -> {
            new AlertDialog.Builder(parent.getContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", "");
                        ApiCaller.callApi("sanpham/" + tt.getMasp(), "DELETE", null, token, response -> {
                            ((Activity) context).runOnUiThread(() -> {
                                if (response != null) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.has("msg") && jsonResponse.getString("msg").equals("Sanpham removed")) {
                                            spList.remove(i);
                                            notifyDataSetChanged(); // Cập nhật giao diện
                                            Toast.makeText(parent.getContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Xóa sản phẩm thất bại";
                                            Toast.makeText(parent.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(parent.getContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(parent.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return viewtemp;
    }
    public View getViewWith4Properties(int i, View view, ViewGroup parent) {
        View viewtemp;
        if (view == null) {
            viewtemp = LayoutInflater.from(parent.getContext()).inflate(R.layout.ds_hienthi_gridview1_nguoidung, parent, false);
        } else {
            viewtemp = view;
        }

        SanPham tt = spList.get(i);
        TextView masp = viewtemp.findViewById(R.id.masp);
        TextView tensp = viewtemp.findViewById(R.id.tensp);
        TextView dongia = viewtemp.findViewById(R.id.dongia);
        ImageView anh = viewtemp.findViewById(R.id.imgsp);
        TextView tinhtrangkho = viewtemp.findViewById(R.id.tinhtrangkho);

        // Hiển thị thông tin sản phẩm
        masp.setText(tt.getMasp());
        tensp.setText(tt.getTensp());
        dongia.setText(String.valueOf(tt.getDongia())); // Chuyển đổi Float thành String


        // Hiển thị ảnh sản phẩm
        byte[] anhByteArray = tt.getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            anh.setImageBitmap(imganhbs);
        } else {
            anh.setImageResource(R.drawable.vest);
        }

        if (tt.getSoluongkho() == 0) {
            tinhtrangkho.setText("Hết hàng");
            tinhtrangkho.setTextColor(Color.RED);
        } else if (tt.getSoluongkho() < 10) {
            tinhtrangkho.setText("Sắp hết hàng");
            tinhtrangkho.setTextColor(Color.parseColor("#FFA500")); // Orange color
        } else {
            tinhtrangkho.setText("Còn hàng");
            tinhtrangkho.setTextColor(Color.GREEN);
        }

        viewtemp.setOnClickListener(v -> {
            if (tt.getSoluongkho() > 0) {
                Intent intent = new Intent(context, ChiTietSanPham_Activity.class);
                ChiTietSanPham chiTietSanPham = new ChiTietSanPham(
                        tt.getMasp(),
                        tt.getTensp(),
                        tt.getDongia(),
                        tt.getMota(),
                        tt.getGhichu(),
                        tt.getSoluongkho(),
                        tt.getMansp(),
                        tt.getAnh()
                );
                intent.putExtra("chitietsanpham", chiTietSanPham);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            }
        });

        return viewtemp;
    }
    // Hàm hiển thị dialog sửa thông tin bác sĩ
    private void showEditDialog(SanPham tt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_sua_san_pham, null);
        builder.setView(dialogView);

        // Các trường EditText và Spinner
        EditText editMasp = dialogView.findViewById(R.id.masp);
        EditText editTensp = dialogView.findViewById(R.id.tensp);
        EditText editDongia = dialogView.findViewById(R.id.dongia);
        EditText editMota = dialogView.findViewById(R.id.mota);
        EditText editGhichu = dialogView.findViewById(R.id.ghichu);
        EditText editSoluongkho = dialogView.findViewById(R.id.soluongkho);
        Spinner mansp = dialogView.findViewById(R.id.manhomsanpham);
        ImageView imgsp = dialogView.findViewById(R.id.imgsp);

        // Load danh sách nhóm sản phẩm
        // Điền dữ liệu hiện tại vào các trường
        editMasp.setText(tt.getMasp());
        editTensp.setText(tt.getTensp());
        editDongia.setText(String.valueOf(tt.getDongia()));
        editMota.setText(tt.getMota());
        editGhichu.setText(tt.getGhichu());
        editSoluongkho.setText(String.valueOf(tt.getSoluongkho()));

        // Chọn nhóm sản phẩm hiện tại (dựa trên maso)
        for (int i = 0; i < mangNSPList.size(); i++) {
            if (mangNSPList.get(i).getMa().equals(tt.getMansp())) {
                mansp.setSelection(i);
                break;
            }
        }

        // Hiển thị ảnh sản phẩm
        byte[] anhByteArray = tt.getAnh();
        if (anhByteArray != null && anhByteArray.length > 0) {
            Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
            imgsp.setImageBitmap(imganhbs);
        } else {
            imgsp.setImageResource(R.drawable.vest);
        }
        // Sự kiện chọn ảnh từ drawable
        Button imgAddanh = dialogView.findViewById(R.id.btnAddImg);
        imgAddanh.setOnClickListener(v1 -> openDrawableImagePicker(imgsp));
        // Sự kiện chọn ảnh từ bộ nhớ
        imgsp.setOnClickListener(imgView -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((Activity) context).startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            // Cập nhật thông tin sản phẩm
            updateSanPham(tt, editMasp, editTensp, editDongia, editMota, editGhichu, editSoluongkho, mansp);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Phương thức cập nhật thông tin sản phẩm
    private void updateSanPham(SanPham tt, EditText editMasp, EditText editTensp, EditText editDongia, EditText editMota, EditText editGhichu, EditText editSoluongkho, Spinner editMansp) {
        String newMasp = editMasp.getText().toString().trim();
        String newTensp = editTensp.getText().toString().trim();
        float newDongia = Float.parseFloat(editDongia.getText().toString().trim());
        String newMota = editMota.getText().toString().trim();
        String newGhichu = editGhichu.getText().toString().trim();
        int newSoluongkho = Integer.parseInt(editSoluongkho.getText().toString().trim());

        // Lấy maso từ spinner
        String newMansp = ((NhomSanPham) editMansp.getSelectedItem()).getMa(); // Lấy maso thay vì tennhom

        // Cập nhật ảnh nếu có
        byte[] newAnh = selectedImageUri != null ? getBytesFromUri(selectedImageUri) : null;

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("masp", newMasp);
            updateData.put("tensp", newTensp);
            updateData.put("dongia", newDongia);
            updateData.put("mota", newMota);
            updateData.put("ghichu", newGhichu);
            updateData.put("soluongkho", newSoluongkho);
            updateData.put("maso", newMansp);
            if (newAnh != null) {
                updateData.put("anh", Base64.encodeToString(newAnh, Base64.DEFAULT));
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", "");

            ApiCaller.callApi("sanpham/" + tt.getMasp(), "PUT", updateData, token, response -> {
                ((Activity) context).runOnUiThread(() -> {
                    if (response != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("_id")) {
                                // Cập nhật đối tượng SanPham
                                tt.setMasp(newMasp);
                                tt.setTensp(newTensp);
                                tt.setDongia(newDongia);
                                tt.setMota(newMota);
                                tt.setGhichu(newGhichu);
                                tt.setSoluongkho(newSoluongkho);
                                tt.setMansp(newMansp);
                                if (newAnh != null) {
                                    tt.setAnh(newAnh); // Cập nhật ảnh nếu có
                                }
                                notifyDataSetChanged(); // Cập nhật giao diện
                                Toast.makeText(context, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Cập nhật sản phẩm thất bại";
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để mở hộp thoại chọn ảnh từ drawable
    private void openDrawableImagePicker(ImageView imgBacSi) {
        final String[] imageNames = {"vest1","vest2","vest3", "aococ1","aococ2","aococ3", "len1","len2","len3", "dahoi1","dahoi2","dahoi3", "giay1","giay2","giay3","giay4","giay5", "giaythethao", "aosomi1","aosomi2","aosomi3", "quan1","quan2", "quan3",  "vay1","vay2","vay3"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn ảnh từ drawable");
        builder.setItems(imageNames, (dialog, which) -> {
            // Lấy tên hình ảnh đã chọn
            String selectedImageName = imageNames[which];

            // Lấy ID tài nguyên drawable
            int resourceId = context.getResources().getIdentifier(selectedImageName, "drawable", context.getPackageName());

            // Cập nhật ImageView
            imgBacSi.setImageResource(resourceId);

            // Cập nhật URI
            selectedImageUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resourceId);
        });
        builder.show();
    }

    private ArrayList<NhomSanPham> mangNSPList;

    private void loadTenNhomSanPham(Spinner mansp) {
        mangNSPList = new ArrayList<>();
        ApiCaller.callApi("nhomsanpham", "GET", null, response -> {
            ((Activity) context).runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangNSPList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String ma = jsonObject.getString("maso");
                            String ten = jsonObject.getString("tennsp");
                            mangNSPList.add(new NhomSanPham(ma, ten, null));
                        }
                        // Tạo adapter cho Spinner
                        ArrayAdapter<NhomSanPham> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, mangNSPList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mansp.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Chuyển đổi URI thành mảng byte
    private byte[] getBytesFromUri(Uri uri) {
        if (uri == null) {
            return null; // Trả về null nếu URI không hợp lệ
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray(); // Trả về mảng byte của ảnh
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
