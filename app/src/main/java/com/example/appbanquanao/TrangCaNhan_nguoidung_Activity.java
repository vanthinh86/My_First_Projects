package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class TrangCaNhan_nguoidung_Activity extends AppCompatActivity {
    String tendn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trang_ca_nhan_nguoidung);

        Button dangxuat = findViewById(R.id.btndangxuat);
        TextView textTendn = findViewById(R.id.tendn); // TextView hiển thị tên đăng nhập

        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        // Lấy giá trị tendn từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
       tendn = sharedPreferences.getString("tendn", null);

        // Nếu SharedPreferences không có, lấy từ Intent
        if (tendn == null) {
            tendn = getIntent().getStringExtra("tendn");
        }

        // Kiểm tra giá trị tendn
        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(TrangCaNhan_nguoidung_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }
        
        // Setup menu highlighting
        setupMenuHighlighting();

        btncard.setOnClickListener(view -> {
            // Kiểm tra trạng thái đăng nhập của người dùng
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
        });

        btntrangchu.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btndonhang.setOnClickListener(view -> {
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
            if (isLoggedIn) {
                Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);
                intent.putExtra("tendn", tendn);  // Truyá»n tendn qua Intent
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btncanhan.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);
            intent.putExtra("tendn", tendn);  // Truyền tendn qua Intent
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btntimkiem.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TimKiemSanPham_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        dangxuat.setOnClickListener(v -> {
            new AlertDialog.Builder(TrangCaNhan_nguoidung_Activity.this)
                    .setTitle("Đăng Xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Xóa trạng thái đăng nhập
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.putString("tendn", null);
                        editor.apply();

                        // Quay lại Activity chính
                        Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish(); // Kết thúc activity
                    })
                    .setNegativeButton("KhÃ´ng", null)
                    .show();
        });
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btncart);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu cá nhân (vì đây là TrangCaNhan_nguoidung_Activity)
            highlightMenuButton(R.id.btncanhan);
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