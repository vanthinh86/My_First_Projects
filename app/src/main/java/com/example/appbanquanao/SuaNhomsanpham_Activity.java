package com.example.appbanquanao;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SuaNhomsanpham_Activity extends AppCompatActivity {
    private EditText tennsp;
    private ImageView imgnsp;
    private Button btnAddImg, btnsua;
    private TextView id;
    private Uri imageUri;
    private byte[] currentImageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_nhomsanpham);

        tennsp = findViewById(R.id.ten);
        imgnsp = findViewById(R.id.imgnsp);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnsua = findViewById(R.id.btnsua);
        id = findViewById(R.id.id);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String maso = intent.getStringExtra("maso");
        String ten = intent.getStringExtra("tennsp");
        byte[] anh = intent.getByteArrayExtra("anh");

        // Hiển thị dữ liệu
        id.setText(maso);
        tennsp.setText(ten);
        if (anh != null) {
            currentImageBytes = anh;
            Bitmap bitmap = BitmapFactory.decodeByteArray(anh, 0, anh.length);
            imgnsp.setImageBitmap(bitmap);
        }

        btnAddImg.setOnClickListener(v -> openDrawableImagePicker());

        btnsua.setOnClickListener(v -> updateNhomSanPham());
    }

    private void openDrawableImagePicker() {
        final String[] imageNames = {"vest", "aococtay", "aolen", "dahoi", "giaydong", "giaythethao", "khoac1", "quanau", "quantat", "vay", "somi"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ drawable");
        builder.setItems(imageNames, (dialog, which) -> {
            String selectedImageName = imageNames[which];
            int resourceId = getResources().getIdentifier(selectedImageName, "drawable", getPackageName());
            imgnsp.setImageResource(resourceId);
            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            // Reset currentImageBytes since a new image is selected from drawable
            currentImageBytes = null;
        });
        builder.show();
    }

    private void updateNhomSanPham() {
        String maso = id.getText().toString();
        String ten = tennsp.getText().toString().trim();

        if (ten.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên nhóm sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageBytes = null;
        if (imageUri != null) {
            imageBytes = getBytesFromUri(imageUri);
        } else {
            imageBytes = currentImageBytes; // Keep the old image if no new one is selected
        }

        JSONObject putData = new JSONObject();
        try {
            putData.put("tennsp", ten);
            if (imageBytes != null) {
                String imageBase64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                JSONObject anhObject = new JSONObject();
                anhObject.put("data", imageBase64);
                putData.put("anh", anhObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        ApiCaller.callApi("nhomsanpham/" + maso, "PUT", putData, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        // Use a more generic success check, e.g., by checking for the updated object
                        if (jsonResponse.has("maso")) {
                            Toast.makeText(SuaNhomsanpham_Activity.this, "Cập nhật thành công!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), Nhomsanpham_admin_Actvity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Cập nhật thất bại";
                            Toast.makeText(SuaNhomsanpham_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SuaNhomsanpham_Activity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SuaNhomsanpham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private byte[] getBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}