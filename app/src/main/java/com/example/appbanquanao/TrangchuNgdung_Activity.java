package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrangchuNgdung_Activity extends BaseRealtimeActivity {
    RecyclerView grv2;
    GridView grv1;
    ArrayList<SanPham> mangSPgrv1; // Danh sách cho GridView
    ArrayList<NhomSanPham> mangNSPgrv2; // Danh sách cho ListView
    NhomSanPhamHorizontalAdapter adapterGrv2;
    SanPhamAdapter adapterGrv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu_ngdung);

        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);
        EditText timkiem = findViewById(R.id.timkiem);
        TextView textTendn = findViewById(R.id.tendn); // TextView hiển thị tên đăng nhập
        grv2 = findViewById(R.id.grv2);
        grv1 = findViewById(R.id.grv1);

        // Lấy tên đăng nhập từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String tendn = sharedPreferences.getString("tendn", "");

        if (isLoggedIn) {
            textTendn.setText(tendn);
        } else {
            Intent intent = new Intent(TrangchuNgdung_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }

        // Gửi tên đăng nhập qua Intent trong sự kiện click
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reload the current activity to refresh data
                finish();
                startActivity(getIntent());
            }
        });
        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrangchuNgdung_Activity.this, TimKiemSanPham_Activity.class);

                // Gửi tên đăng nhập qua Intent
                intent.putExtra("tendn", tendn); // Sử dụng biến tendn đã được lấy từ SharedPreferences

                // Truyền từ khóa tìm kiếm nếu có
                String searchQuery = timkiem.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    intent.putExtra("searchQuery", searchQuery);
                }

                startActivity(intent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrangchuNgdung_Activity.this, DonHang_User_Activity.class);
                intent.putExtra("tendn", tendn); // Gửi tên đăng nhập
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        btncard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrangchuNgdung_Activity.this, GioHang_Activity.class);
                intent.putExtra("tendn", tendn); // Gửi tên đăng nhập
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrangchuNgdung_Activity.this, TrangCaNhan_nguoidung_Activity.class);
                intent.putExtra("tendn", tendn); // Gửi tên đăng nhập
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        // Các sự kiện khác
        timkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TrangchuNgdung_Activity.this, TimKiemSanPham_Activity.class);

                // Gửi tên đăng nhập qua Intent
                intent.putExtra("tendn", tendn); // Sử dụng biến tendn đã được lấy từ SharedPreferences

                // Truyền từ khóa tìm kiếm nếu có
                String searchQuery = timkiem.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    intent.putExtra("searchQuery", searchQuery);
                }

                startActivity(intent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Khởi tạo danh sách và adapter
        mangNSPgrv2 = new ArrayList<>();
        mangSPgrv1 = new ArrayList<>();
        adapterGrv2 = new NhomSanPhamHorizontalAdapter(this, mangNSPgrv2);
        grv2.setAdapter(adapterGrv2);
        grv2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterGrv1 = new SanPhamAdapter(this, mangSPgrv1, false);
        grv1.setAdapter(adapterGrv1);

        Loaddulieubacsigridview2();
        Loaddulieubacsigridview1();
        
        // Setup menu highlighting
        setupMenuHighlighting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity to reflect any changes
        Loaddulieubacsigridview2();
        Loaddulieubacsigridview1();
    }

    private void Loaddulieubacsigridview2() {
        ApiCaller.callApi("nhomsanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangNSPgrv2.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String ma = jsonObject.getString("maso");
                            String ten = jsonObject.getString("tennsp");
                            // Assuming 'anh' is a base64 string in the response
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
                            mangNSPgrv2.add(new NhomSanPham(ma, ten, blob));
                        }
                        adapterGrv2.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TrangchuNgdung_Activity.this, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TrangchuNgdung_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void Loaddulieubacsigridview1() {
        ApiCaller.callApi("sanpham?sort=sales", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray;
                        // Check if the response is a JSON object or a JSON array
                        if (response.trim().startsWith("{")) {
                            JSONObject responseObj = new JSONObject(response);
                            // Check if response has SanPhams array
                            if (responseObj.has("SanPhams")) {
                                jsonArray = responseObj.getJSONArray("SanPhams");
                            } else {
                                // If it's an object but doesn't have SanPhams, we can't proceed
                                throw new JSONException("Response is an object but does not contain SanPhams array");
                            }
                        } else {
                            // The response is a JSON array
                            jsonArray = new JSONArray(response);
                        }

                        mangSPgrv1.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int masp = jsonObject.getInt("masp");
                            String tensp = jsonObject.getString("tensp");
                            float dongia = (float) jsonObject.getDouble("dongia");
                            String mota = jsonObject.getString("mota");
                            String ghichu = jsonObject.getString("ghichu");
                            int soluongkho = jsonObject.getInt("soluongkho");

                            // Get maso from nested nhomSanPham object
                            String maso = "";
                            if (jsonObject.has("nhomSanPham") && !jsonObject.isNull("nhomSanPham")) {
                                JSONObject nhomSanPham = jsonObject.getJSONObject("nhomSanPham");
                                maso = nhomSanPham.getString("maso");
                            }

                            // Assuming 'anh' is a base64 string in the response
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
                            mangSPgrv1.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, blob));
                        }
                        adapterGrv1.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TrangchuNgdung_Activity.this, "Lỗi xử lý dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                        mangSPgrv1.clear();
                    }
                } else {
                    Toast.makeText(TrangchuNgdung_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // Refresh data when real-time updates are received
        loadSanPham();
        loadNhomSanPham();
    }
    
    // Override specific real-time event handlers for user-specific behavior
    @Override
    public void onSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                // Add the new product to the list
                int masp = data.getInt("masp");
                String tensp = data.getString("tensp");
                double dongia = data.getDouble("dongia");
                String mota = data.optString("mota", "");
                String ghichu = data.optString("ghichu", "");
                int soluongkho = data.getInt("soluongkho");
                int maso = data.getInt("maso");
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                SanPham newProduct = new SanPham(String.valueOf(masp), tensp, (float)dongia, mota, ghichu, soluongkho, String.valueOf(maso), blob);
                mangSPgrv1.add(0, newProduct); // Add to beginning of list
                adapterGrv1.notifyDataSetChanged();

                Toast.makeText(this, "Sản phẩm mới có sẵn: " + tensp, Toast.LENGTH_SHORT).show();
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
                int maso = data.getInt("maso");
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                // Find and update the product in the list
                for (int i = 0; i < mangSPgrv1.size(); i++) {
                    if (mangSPgrv1.get(i).getMasp().equals(String.valueOf(updatedMasp))) {
                        SanPham updatedProduct = new SanPham(String.valueOf(updatedMasp), tensp, (float)dongia, mota, ghichu, soluongkho, String.valueOf(maso), blob);
                        mangSPgrv1.set(i, updatedProduct);
                        adapterGrv1.notifyDataSetChanged();
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
    public void onNhomSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                // Add the new category to the list
                int maso = data.getInt("maso");
                String tennsp = data.getString("tennsp");
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                NhomSanPham newCategory = new NhomSanPham(String.valueOf(maso), tennsp, blob);
                mangNSPgrv2.add(0, newCategory); // Add to beginning of list
                adapterGrv2.notifyDataSetChanged();
                Toast.makeText(this, "Danh mục mới: " + tennsp, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadSanPham() {
        Loaddulieubacsigridview1();
    }

    private void loadNhomSanPham() {
        Loaddulieubacsigridview2();
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btncart);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu trang chủ (vì đây là TrangchuNgdung_Activity)
            highlightMenuButton(R.id.btntrangchu);
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