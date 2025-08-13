package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import android.util.Base64;

public class Sanpham_admin_Activity extends BaseRealtimeActivity {

    private ListView lv;
    private FloatingActionButton addButton;
    private ArrayList<SanPham> mangSP;
    private SanPhamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanpham_admin);

        // Setup menu highlighting - Sản phẩm được highlight
        setupMenuHighlighting();

        initializeViews();
        loadData();

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ThemSanPham_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        ImageButton btntrangchu=findViewById(R.id.btntrangchu);
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),TrangchuAdmin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btncanhan=findViewById(R.id.btncanhan);
        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    // Chưa đăng nhập, chuyển đến trang login
                    Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    // Đã đăng nhập, chuyển đến trang 2
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_admin_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        ImageButton btndonhang=findViewById(R.id.btndonhang);
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),DonHang_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnsanpham    =findViewById(R.id.btnsanpham);
        btnsanpham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Sanpham_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnnhomsp   =findViewById(R.id.btnnhomsp);
        btnnhomsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Nhomsanpham_admin_Actvity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btntaikhoan    =findViewById(R.id.btntaikhoan);
        btntaikhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Taikhoan_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        loadData();
    }

    private void initializeViews() {
        lv = findViewById(R.id.listtk);
        addButton = findViewById(R.id.btnthem);
        mangSP = new ArrayList<>();

        adapter = new SanPhamAdapter(Sanpham_admin_Activity.this, mangSP, true);

        lv.setAdapter(adapter);
    }

    private void loadData() {
        ApiCaller.callApi("sanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangSP.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int masp = jsonObject.getInt("masp");
                            String tensp = jsonObject.getString("tensp");
                            float dongia = (float) jsonObject.getDouble("dongia");
                            String mota = jsonObject.getString("mota");
                            String ghichu = jsonObject.getString("ghichu");
                            int soluongkho = jsonObject.getInt("soluongkho");
                            String maso = "";
                            if (jsonObject.has("maso") && !jsonObject.isNull("maso")) {
                                maso = jsonObject.getJSONObject("maso").getString("tennsp");
                            }
                            byte[] blob = null;
                            if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                String anhBase64 = jsonObject.getString("anh");
                                try {
                                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    blob = null; // Set blob to null if decoding fails
                                }
                            }
                            mangSP.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, blob));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Sanpham_admin_Activity.this, "Lỗi xử lý dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Sanpham_admin_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // Refresh data when real-time updates are received
        loadData();
    }
    
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
                    try {
                        blob = Base64.decode(anhBase64, Base64.DEFAULT);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        blob = null;
                    }
                }
                
                SanPham newProduct = new SanPham(String.valueOf(masp), tensp, (float)dongia, mota, ghichu, soluongkho, String.valueOf(maso), blob);
                mangSP.add(0, newProduct); // Add to beginning of list
                adapter.notifyDataSetChanged();

                Toast.makeText(this, "Đã thêm sản phẩm: " + tensp, Toast.LENGTH_SHORT).show();
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
                    try {
                        blob = Base64.decode(anhBase64, Base64.DEFAULT);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        blob = null;
                    }
                }
                
                // Find and update the product in the list
                for (int i = 0; i < mangSP.size(); i++) {
                    if (mangSP.get(i).getMasp().equals(String.valueOf(updatedMasp))) {
                        SanPham updatedProduct = new SanPham(String.valueOf(updatedMasp), tensp, (float)dongia, mota, ghichu, soluongkho, String.valueOf(maso), blob);
                        mangSP.set(i, updatedProduct);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Đã cập nhật sản phẩm: " + tensp, Toast.LENGTH_SHORT).show();
                        // Handle success
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
                String deletedMasp = data.getString("masp");
                
                // Remove the product from the list
                for (int i = 0; i < mangSP.size(); i++) {
                    if (mangSP.get(i).getMasp().equals(deletedMasp)) {
                        String productName = mangSP.get(i).getTensp();
                        mangSP.remove(i);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Đã xóa sản phẩm: " + productName, Toast.LENGTH_SHORT).show();
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
            resetMenuButton(R.id.btnnhomsp);
            resetMenuButton(R.id.btnsanpham);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btntaikhoan);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu sản phẩm (vì đây là Sanpham_admin_Activity)
            highlightMenuButton(R.id.btnsanpham);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void resetMenuButton(int buttonId) {
        try {
            android.widget.ImageButton button = findViewById(buttonId);
            if (button != null) {
                button.setBackgroundColor(android.graphics.Color.WHITE);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void highlightMenuButton(int buttonId) {
        try {
            android.widget.ImageButton button = findViewById(buttonId);
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