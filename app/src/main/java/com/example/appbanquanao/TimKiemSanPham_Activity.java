package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TimKiemSanPham_Activity extends BaseRealtimeActivity {

    private GridView grv;
    private ArrayList<SanPham> productList;
    private SanPham_TimKiem_Adapter productAdapter;
    String tendn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem_san_pham);
        EditText timkiem = findViewById(R.id.timkiem);
        timkiem.requestFocus();

        // Thêm xử lý Enter key cho EditText
        timkiem.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            String query = timkiem.getText().toString().trim();
                            if (!query.isEmpty()) {
                                searchSanPham(query);
                            } else {
                                // Nếu thanh tìm kiếm trống, hiển thị tất cả sản phẩm
                                loadAllSanPham();
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        grv = findViewById(R.id.grv);
        productList = new ArrayList<>();
        productAdapter = new SanPham_TimKiem_Adapter(this, productList, false);
        grv.setAdapter(productAdapter);

        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        tendn = sharedPreferences.getString("tendn", null);

        if (tendn == null) {
            tendn = getIntent().getStringExtra("tendn");
        }

        TextView textTendn = findViewById(R.id.tendn);
        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            Intent intent = new Intent(TimKiemSanPham_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
            return;
        }

        // Kiểm tra xem có từ khóa tìm kiếm được truyền từ Intent không
        String searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            timkiem.setText(searchQuery);
            searchSanPham(searchQuery);
        } else {
            // Nếu không có từ khóa tìm kiếm, hiển thị tất cả sản phẩm
            loadAllSanPham();
        }

        btncard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);
                intent.putExtra("tendn", tendn);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);
                    intent.putExtra("tendn", tendn);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Luôn chuyển đến trang tìm kiếm mới khi nhấn nút tìm kiếm
                Intent intent = new Intent(TimKiemSanPham_Activity.this, TimKiemSanPham_Activity.class);
                intent.putExtra("tendn", tendn);

                // Truyền từ khóa tìm kiếm nếu có
                String query = timkiem.getText().toString().trim();
                if (!query.isEmpty()) {
                    intent.putExtra("searchQuery", query);
                }
                
                startActivity(intent);
                
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        
        // Setup menu highlighting - search page resets all menus 
        setupMenuHighlighting();
    }

    private void loadAllSanPham() {
        // Tải tất cả sản phẩm khi tìm kiếm trống
        ApiCaller.callApi("sanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        productList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() == 0) {
                            Toast.makeText(TimKiemSanPham_Activity.this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int masp = jsonObject.getInt("masp");
                                String tensp = jsonObject.getString("tensp");
                                float dongia = (float) jsonObject.getDouble("dongia");
                                String mota = jsonObject.getString("mota");
                                String ghichu = jsonObject.getString("ghichu");
                                int soluongkho = jsonObject.getInt("soluongkho");
                                String anhBase64 = jsonObject.getString("anh");
                                byte[] anh = android.util.Base64.decode(anhBase64, android.util.Base64.DEFAULT);
                                
                                // Get maso from nested object
                                String maso = "";
                                if (jsonObject.has("maso")) {
                                    maso = jsonObject.getString("maso");
                                }
                                
                                productList.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, anh));
                            }
                            Toast.makeText(TimKiemSanPham_Activity.this, "Hiển thị tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                        productAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TimKiemSanPham_Activity.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TimKiemSanPham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void searchSanPham(String query) {
        // Tìm kiếm sản phẩm theo tên sử dụng API search mới
        ApiCaller.callApi("sanpham/search/" + query, "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        productList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        
                        // Log Ä‘á»ƒ debug
                        System.out.println("Search API Response: " + response);
                        System.out.println("Search query: " + query);
                        
                        if (jsonArray.length() == 0) {
                            Toast.makeText(TimKiemSanPham_Activity.this, "Không tìm thấy sản phẩm phù hợp với \"" + query + "\"", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int masp = jsonObject.getInt("masp");
                                String tensp = jsonObject.getString("tensp");
                                float dongia = (float) jsonObject.getDouble("dongia");
                                String mota = jsonObject.getString("mota");
                                String ghichu = jsonObject.getString("ghichu");
                                int soluongkho = jsonObject.getInt("soluongkho");
                                String anhBase64 = jsonObject.getString("anh");
                                byte[] anh = android.util.Base64.decode(anhBase64, android.util.Base64.DEFAULT);
                                
                                // Get maso from nested object
                                String maso = "";
                                if (jsonObject.has("maso")) {
                                    if (jsonObject.get("maso") instanceof JSONObject) {
                                        JSONObject nhomSanPham = jsonObject.getJSONObject("maso");
                                        maso = nhomSanPham.getString("maso");
                                    } else {
                                        maso = jsonObject.getString("maso");
                                    }
                                }
                                
                                productList.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, anh));
                            }
                            Toast.makeText(TimKiemSanPham_Activity.this, "Tìm thấy " + jsonArray.length() + " sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                        productAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TimKiemSanPham_Activity.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TimKiemSanPham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // Refresh search results when real-time data changes
        EditText timkiem = findViewById(R.id.timkiem);
        String searchQuery = timkiem.getText().toString().trim();
        if (!searchQuery.isEmpty()) {
            searchSanPham(searchQuery);
        } else {
            loadAllSanPham();
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
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                SanPham newProduct = new SanPham(String.valueOf(masp), tensp, (float)dongia, mota, ghichu, soluongkho, maso, blob);
                
                // Check if product matches current search query
                EditText timkiem = findViewById(R.id.timkiem);
                String searchQuery = timkiem.getText().toString().trim();
                
                if (searchQuery.isEmpty() || tensp.toLowerCase().contains(searchQuery.toLowerCase())) {
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
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                // Find and update the product in the list
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).getMasp().equals(String.valueOf(updatedMasp))) {
                        SanPham updatedProduct = new SanPham(String.valueOf(updatedMasp), tensp, (float)dongia, mota, ghichu, soluongkho, maso, blob);
                        productList.set(i, updatedProduct);
                        productAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Sáº£n pháº©m Ä‘Ã£ cáº­p nháº­t: " + tensp, Toast.LENGTH_SHORT).show();
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
            resetMenuButton(R.id.btntimkiem);

            // Highlight button tìm kiếm vì đây là trang tìm kiếm sản phẩm
            highlightMenuButton(R.id.btntimkiem);
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