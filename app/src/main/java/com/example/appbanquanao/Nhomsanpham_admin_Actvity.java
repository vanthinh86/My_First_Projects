package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import android.util.Base64;

public class Nhomsanpham_admin_Actvity extends AppCompatActivity {
    private ListView lv;
    private FloatingActionButton addButton;
    private ArrayList<NhomSanPham> mangNSP;
    private NhomSanPhamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhomsanpham_admin_actvity);

        // Setup menu highlighting - Nhóm sản phẩm được highlight
        setupMenuHighlighting();

        initializeViews();
        loadData();
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
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ThemNhomSanPham_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void initializeViews() {
        lv = findViewById(R.id.listtk);
        addButton = findViewById(R.id.btnthem);
        mangNSP = new ArrayList<>();

        adapter = new NhomSanPhamAdapter(Nhomsanpham_admin_Actvity.this, mangNSP, true);

        lv.setAdapter(adapter);
    }

    private void loadData() {
        ApiCaller.callApi("nhomsanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangNSP.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String ma = jsonObject.getString("maso");
                            String ten = jsonObject.getString("tennsp");
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
                            mangNSP.add(new NhomSanPham(ma, ten, blob));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Nhomsanpham_admin_Actvity.this, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Nhomsanpham_admin_Actvity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    private byte[] convertBitmapToByteArray(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
            // Highlight menu nhóm sản phẩm (vì đây là Nhomsanpham_admin_Actvity)
            highlightMenuButton(R.id.btnnhomsp);
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