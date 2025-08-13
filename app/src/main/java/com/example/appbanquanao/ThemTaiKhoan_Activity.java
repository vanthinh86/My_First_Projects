package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ThemTaiKhoan_Activity extends AppCompatActivity {
    RadioButton admin;
    RadioButton user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_tai_khoan);

        Button btnadd = findViewById(R.id.btnadd);
        Button btnback = findViewById(R.id.btnback);
        EditText tendn = findViewById(R.id.tdn);
        EditText matkhau = findViewById(R.id.mk);
        admin = findViewById(R.id.admin);
        user = findViewById(R.id.user);
        TextView togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);

        btnback.setOnClickListener(v -> finish());

        // Password visibility toggle
        final boolean[] isPasswordVisible = {false};
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    matkhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setText("👁");
                    isPasswordVisible[0] = false;
                } else {
                    matkhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setText("🙈");
                    isPasswordVisible[0] = true;
                }
                matkhau.setSelection(matkhau.getText().length());
            }
        });

        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = tendn.getText().toString().trim();
                String password = matkhau.getText().toString().trim();
                String quyen = "";

                if (admin.isChecked()) {
                    quyen = "admin";
                } else if (user.isChecked()) {
                    quyen = "user";
                }

                if (username.isEmpty() || password.isEmpty() || quyen.isEmpty()) {
                    Toast.makeText(ThemTaiKhoan_Activity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject postData = new JSONObject();
                try {
                    postData.put("tendn", username);
                    postData.put("matkhau", password);
                    postData.put("quyen", quyen);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ThemTaiKhoan_Activity.this, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                ApiCaller.callApi("auth/register", "POST", postData, token, response -> {
                    runOnUiThread(() -> {
                        if (response != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.has("msg") && jsonResponse.getString("msg").equals("TaiKhoan created successfully")) {
                                    Toast.makeText(ThemTaiKhoan_Activity.this, "Thêm tài khoản thành công!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ThemTaiKhoan_Activity.this, Taikhoan_admin_Activity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                } else {
                                    String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Thêm tài khoản thất bại";
                                    Toast.makeText(ThemTaiKhoan_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ThemTaiKhoan_Activity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ThemTaiKhoan_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}