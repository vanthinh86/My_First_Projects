package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.util.Base64;
import java.util.List;

public class DanhMucSanPham_Activity extends BaseRealtimeActivity {
    private GridView grv;
    private ArrayList<SanPham> productList;
    private SanPham_DanhMuc_Adapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_muc_san_pham);
        
        // Initialize navigation buttons
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);
        
        // Retrieve nhomSpId and tendn from the Intent
        String nhomSpId = getIntent().getStringExtra("nhomSpId");
        String tendn = getIntent().getStringExtra("tendn");

        // Set click listeners for navigation buttons
        btntrangchu.setOnClickListener(v -> {
            Intent intent = new Intent(DanhMucSanPham_Activity.this, TrangchuNgdung_Activity.class);
            if (tendn != null) {
                intent.putExtra("tendn", tendn);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        btntimkiem.setOnClickListener(v -> {
            Intent intent = new Intent(DanhMucSanPham_Activity.this, TimKiemSanPham_Activity.class);
            if (tendn != null) {
                intent.putExtra("tendn", tendn);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        btncard.setOnClickListener(v -> {
            Intent intent = new Intent(DanhMucSanPham_Activity.this, GioHang_Activity.class);
            if (tendn != null) {
                intent.putExtra("tendn", tendn);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        btndonhang.setOnClickListener(v -> {
            Intent intent = new Intent(DanhMucSanPham_Activity.this, DonHang_User_Activity.class);
            if (tendn != null) {
                intent.putExtra("tendn", tendn);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        btncanhan.setOnClickListener(v -> {
            Intent intent = new Intent(DanhMucSanPham_Activity.this, TrangCaNhan_nguoidung_Activity.class);
            if (tendn != null) {
                intent.putExtra("tendn", tendn);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Initialize the GridView
        grv = findViewById(R.id.grv);
        productList = new ArrayList<>();
        productAdapter = new SanPham_DanhMuc_Adapter(this, productList, false);
        grv.setAdapter(productAdapter);

        // Check if nhomSpId is not null
        if (nhomSpId != null) {
            loadProductsByNhomSpId(nhomSpId);
        } else {
            Toast.makeText(this, "ID nhóm sản phẩm không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
        
        // Setup menu highlighting - this is a user page but doesn't specifically map to a menu
        setupMenuHighlighting();


        grv.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy sản phẩm tại vị trí được click
            SanPham sanPham = productList.get(position);

            // Tạo Intent để chuyển sang ChiTietSanPham_Activity
            Intent intent = new Intent(DanhMucSanPham_Activity.this, ChiTietSanPham_Activity.class);

            // Truyền dữ liệu sản phẩm qua Intent
            intent.putExtra("masp", sanPham.getMasp());
            intent.putExtra("tensp", sanPham.getTensp());
            intent.putExtra("dongia", sanPham.getDongia());
            intent.putExtra("mota", sanPham.getMota());
            intent.putExtra("ghichu", sanPham.getGhichu());
            intent.putExtra("soluongkho", sanPham.getSoluongkho());
            intent.putExtra("maso", sanPham.getMansp());
            intent.putExtra("anh", sanPham.getAnh());

            // Chuyển đến trang ChiTietSanPham_Activity
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

    }

    private void loadProductsByNhomSpId(String nhomSpId) {
        ApiCaller.callApi("sanpham/nhom/" + nhomSpId, "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null && !response.trim().isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        productList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int masp = jsonObject.getInt("masp");
                            String tensp = jsonObject.getString("tensp");
                            float dongia = (float) jsonObject.getDouble("dongia");
                            String mota = jsonObject.getString("mota");
                            String ghichu = jsonObject.getString("ghichu");
                            int soluongkho = jsonObject.getInt("soluongkho");
                            String maso = String.valueOf(jsonObject.getInt("maso"));
                            byte[] blob = null;
                            if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                // 'anh' is a direct Base64 string
                                String anhBase64 = jsonObject.getString("anh");
                                try {
                                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    blob = null;
                                }
                            }
                            productList.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, blob));
                        }
                        productAdapter.notifyDataSetChanged();
                        if (productList.isEmpty()) {
                            Toast.makeText(DanhMucSanPham_Activity.this, "Không có sản phẩm nào trong danh mục này", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DanhMucSanPham_Activity.this, "Lỗi xử lý dữ liệu sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(DanhMucSanPham_Activity.this, "Lỗi kết nối hoặc không có dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onRealtimeDataChanged() {
        // Refresh product list when real-time events are received
        String nhomSpId = getIntent().getStringExtra("nhomSpId");
        if (nhomSpId != null) {
            loadProductsByNhomSpId(nhomSpId);
        }
    }

    @Override
    public void onSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                int masp = data.getInt("masp");
                String tensp = data.getString("tensp");
                double dongia = data.getDouble("dongia");
                String mota = data.optString("mota", "");
                String ghichu = data.optString("ghichu", "");
                int soluongkho = data.getInt("soluongkho");
                String maso = data.optString("maso", "");
                
                // Check if the new product belongs to current category
                String currentNhomSpId = getIntent().getStringExtra("nhomSpId");
                if (currentNhomSpId != null && currentNhomSpId.equals(maso)) {
                    byte[] blob = null;
                    String anhBase64 = data.optString("anh");
                    if (!anhBase64.isEmpty()) {
                        blob = Base64.decode(anhBase64, Base64.DEFAULT);
                    }
                    
                    SanPham newProduct = new SanPham(String.valueOf(masp), tensp, (float)dongia, mota, ghichu, soluongkho, maso, blob);
                    productList.add(0, newProduct);
                    productAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Sản phẩm mới: " + tensp, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onSanPhamUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                int updatedMasp = data.getInt("masp");
                String tensp = data.getString("tensp");
                double dongia = data.getDouble("dongia");
                String mota = data.optString("mota", "");
                String ghichu = data.optString("ghichu", "");
                int soluongkho = data.getInt("soluongkho");
                String maso = data.optString("maso", "");
                
                // Find and update the product in the list
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).getMasp().equals(String.valueOf(updatedMasp))) {
                        byte[] blob = null;
                        String anhBase64 = data.optString("anh");
                        if (!anhBase64.isEmpty()) {
                            blob = Base64.decode(anhBase64, Base64.DEFAULT);
                        }
                        
                        SanPham updatedProduct = new SanPham(String.valueOf(updatedMasp), tensp, (float)dongia, mota, ghichu, soluongkho, maso, blob);
                        productList.set(i, updatedProduct);
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Sản phẩm đã cập nhật: " + tensp, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onSanPhamDeleted(JSONObject data) {
        runOnUiThread(() -> {
            try {
                int deletedMasp = data.getInt("masp");
                String tensp = data.optString("tensp", "");
                
                // Find and remove the product from the list
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).getMasp().equals(String.valueOf(deletedMasp))) {
                        productList.remove(i);
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Sản phẩm đã bị xóa: " + tensp, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường  
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btncart);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btncanhan);
            
            // Không highlight menu nào vì đây là trang danh mục sản phẩm
            // (không có menu tương ứng trực tiếp)
        } catch (Exception e) {
            // Ignore
        }
    }

    private void resetMenuButton(int buttonId) {
        try {
            ImageButton button = findViewById(buttonId);
            if (button != null) {
                button.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void highlightMenuButton(int buttonId) {
        try {
            ImageButton button = findViewById(buttonId);
            if (button != null) {
                button.setBackgroundColor(android.graphics.Color.parseColor("#D3D3D3"));
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

