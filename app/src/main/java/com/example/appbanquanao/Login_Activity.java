package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class Login_Activity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable timeoutRunnable;
    private static final long TIMEOUT_DURATION = 86400000; // 24 hours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        EditText tdn = findViewById(R.id.tdn);
        EditText mk = findViewById(R.id.mk);
        TextView dangki = findViewById(R.id.dangki);
        TextView qmk = findViewById(R.id.qmk);
        TextView togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);

        // Password visibility toggle
        final boolean[] isPasswordVisible = {false};
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible[0]) {
                    mk.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setText("👁");
                    isPasswordVisible[0] = false;
                } else {
                    mk.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setText("🙈");
                    isPasswordVisible[0] = true;
                }
                mk.setSelection(mk.getText().length());
            }
        });

        qmk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoiMatKhau_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        
        dangki.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), DangKiTaiKhoan_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLogin.setOnClickListener(v -> {
            String username = tdn.getText().toString();
            String password = mk.getText().toString();

            try {
                JSONObject loginData = new JSONObject();
                loginData.put("tendn", username);
                loginData.put("matkhau", password);

                ApiCaller.callApi("auth/login", "POST", loginData, response -> {
                    runOnUiThread(() -> {
                        if (response != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.has("token")) {
                                    String token = jsonResponse.getString("token");
                                    String quyen = jsonResponse.getString("quyen");
                                    String tendnValue = jsonResponse.getString("tendn");

                                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("token", token);
                                    editor.putString("tendn", tendnValue);
                                    editor.putString("quyen", quyen);
                                    editor.putBoolean("isLoggedIn", true);
                                    
                                    // Store user type and user ID for Socket.io
                                    String userType = "admin".equals(quyen) ? "admin" : "user";
                                    editor.putString("userType", userType);
                                    editor.putString("userId", tendnValue); // Use username as user ID
                                    
                                    editor.apply();

                                    // startAutoLogoutTimer(); // Disabled auto-logout

                                    Intent intent;
                                    if ("admin".equals(quyen)) {
                                        intent = new Intent(Login_Activity.this, TrangchuAdmin_Activity.class);
                                        Toast.makeText(Login_Activity.this, "Đăng nhập với quyền Admin", Toast.LENGTH_SHORT).show();
                                    } else {
                                        intent = new Intent(Login_Activity.this, TrangchuNgdung_Activity.class);
                                        intent.putExtra("tendn", tendnValue);
                                        Toast.makeText(Login_Activity.this, "Đăng nhập với quyền User", Toast.LENGTH_SHORT).show();
                                    }
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                } else {
                                    Toast.makeText(Login_Activity.this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Login_Activity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void startAutoLogoutTimer() {
        handler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = () -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Login_Activity.this, Login_Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        };

        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        // startAutoLogoutTimer(); // Disabled auto-logout
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}