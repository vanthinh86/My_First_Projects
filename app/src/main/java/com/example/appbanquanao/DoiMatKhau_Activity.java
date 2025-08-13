package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class DoiMatKhau_Activity extends AppCompatActivity {

    private EditText tendn, matkhau, nhaplaimatkhau;
    private Button btnDoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doi_mat_khau);

        tendn = findViewById(R.id.tdn);
        matkhau = findViewById(R.id.mk);
        nhaplaimatkhau = findViewById(R.id.mk2);
        btnDoi = findViewById(R.id.btnDoi);
        TextView ql = findViewById(R.id.ql);
        TextView togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        TextView toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);

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

        // Confirm password visibility toggle
        final boolean[] isConfirmPasswordVisible = {false};
        toggleConfirmPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible[0]) {
                    nhaplaimatkhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleConfirmPasswordVisibility.setText("👁");
                    isConfirmPasswordVisible[0] = false;
                } else {
                    nhaplaimatkhau.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleConfirmPasswordVisibility.setText("🙈");
                    isConfirmPasswordVisible[0] = true;
                }
                nhaplaimatkhau.setSelection(nhaplaimatkhau.getText().length());
            }
        });

        ql.setOnClickListener(v -> {
            Intent intent = new Intent(DoiMatKhau_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnDoi.setOnClickListener(v -> {
            changePassword();
        });
    }

    private void changePassword() {
        String username = tendn.getText().toString().trim();
        String password = matkhau.getText().toString().trim();
        String confirmPassword = nhaplaimatkhau.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền vào tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("tendn", username);
            requestData.put("matkhau", password);

            ApiCaller.callApi("auth/change-password", "POST", requestData, response -> {
                runOnUiThread(() -> {
                    if (response != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String msg = jsonResponse.optString("msg", "Có lỗi xảy ra.");
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            if (jsonResponse.has("msg") && jsonResponse.getString("msg").equals("Password updated successfully")) {
                                Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
