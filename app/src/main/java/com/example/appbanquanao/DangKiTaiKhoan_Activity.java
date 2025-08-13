package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DangKiTaiKhoan_Activity extends AppCompatActivity {

    String spn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ki_tai_khoan);

        Button btnadd = findViewById(R.id.btnDangki);
        EditText tendn = findViewById(R.id.tdn);
        EditText matkhau = findViewById(R.id.mk);
        EditText nhaplaimatkhau = findViewById(R.id.nhaplaimk);
        Spinner spinner = findViewById(R.id.quyen);
        TextView ql=findViewById(R.id.ql);
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
        ql.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(DangKiTaiKhoan_Activity.this,Login_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ArrayList<String> ar = new ArrayList<>();
        ar.add("user");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, ar);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spn = ar.get(i);
            }


            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = tendn.getText().toString().trim();
                String password = matkhau.getText().toString().trim();
                String nhaplaimk = nhaplaimatkhau.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || nhaplaimk.isEmpty()) {
                    Toast.makeText(DangKiTaiKhoan_Activity.this, "Tên đăng nhập và mật khẩu không được để trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(nhaplaimk)) {
                    Toast.makeText(DangKiTaiKhoan_Activity.this, "Mật khẩu không khớp, vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject registerData = new JSONObject();
                    registerData.put("tendn", username);
                    registerData.put("matkhau", password);
                    registerData.put("quyen", spn);

                    ApiCaller.callApi("auth/register", "POST", registerData, response -> {
                        runOnUiThread(() -> {
                            if (response != null) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    if (jsonResponse.has("token")) {
                                        Toast.makeText(DangKiTaiKhoan_Activity.this, "Đăng kí tài khoản thành công", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    } else {
                                        String msg = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Đăng ký thất bại";
                                        Toast.makeText(DangKiTaiKhoan_Activity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(DangKiTaiKhoan_Activity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(DangKiTaiKhoan_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
