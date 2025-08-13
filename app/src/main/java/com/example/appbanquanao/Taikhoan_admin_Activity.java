package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Taikhoan_admin_Activity extends AppCompatActivity {

    ListView lv;
    ArrayList<TaiKhoan> mangTK;
    TaiKhoanAdapter adapter;
    FloatingActionButton dauconggocphai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taikhoan_admin);

        // Setup menu highlighting - Tài khoản được highlight
        setupMenuHighlighting();
        
        dauconggocphai = findViewById(R.id.btnthem);
        lv = findViewById(R.id.listtk);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), TrangchuAdmin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btncanhan = findViewById(R.id.btncanhan);
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
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_admin_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), DonHang_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnsanpham = findViewById(R.id.btnsanpham);
        btnsanpham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), Sanpham_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnnhomsp = findViewById(R.id.btnnhomsp);
        btnnhomsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), Nhomsanpham_admin_Actvity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btntaikhoan = findViewById(R.id.btntaikhoan);
        btntaikhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), Taikhoan_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        dauconggocphai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(getApplicationContext(), ThemTaiKhoan_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        mangTK = new ArrayList<>();
        adapter = new TaiKhoanAdapter(Taikhoan_admin_Activity.this, R.layout.ds_taikhoan, mangTK);
        lv.setAdapter(adapter);

        loadTaiKhoanData();
    }

    private void loadTaiKhoanData() {
        ApiCaller.callApi("auth/users", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    Log.d("API_RESPONSE", "Raw Taikhoan response: " + response);
                    try {
                        if (response.trim().startsWith("[")) {
                            mangTK.clear();
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String tdn = jsonObject.optString("tendn", "");
                                String mk = jsonObject.optString("matkhau", "");
                                String q = jsonObject.optString("quyen", "");
                                mangTK.add(new TaiKhoan(tdn, mk, q));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            JSONObject jsonObject = new JSONObject(response);
                            String errorMessage = jsonObject.optString("msg", "Lỗi không xác định từ API");
                            Toast.makeText(Taikhoan_admin_Activity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "Failed to parse Taikhoan JSON. Response: " + response, e);
                        Toast.makeText(Taikhoan_admin_Activity.this, "Lỗi xử lý dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Taikhoan API response is null");
                    Toast.makeText(Taikhoan_admin_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
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

            // Highlight menu tài khoản (vì đây là Taikhoan_admin_Activity)
            highlightMenuButton(R.id.btntaikhoan);
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
                button.setBackgroundColor(android.graphics.Color.parseColor("#D3D3D3")); // Green highlight
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