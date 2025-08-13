package com.example.appbanquanao;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.util.Base64;

public class ThemNhomSanPham_Activity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu cho việc chọn ảnh
    EditText tennsp;
    ImageView imgnsp;
    ArrayList<NhomSanPham> mangNSP;
    NhomSanPhamAdapter adapter;
    ImageButton back;
    private Uri imageUri; // Biến để lưu trữ URI của ảnh
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nhom_san_pham);

        tennsp= findViewById(R.id.ten);
        imgnsp = findViewById(R.id.imgnsp);
        Button chonimgbs = findViewById(R.id.btnAddImg);
        Button btnthem = findViewById(R.id.btnadd);
        Button btnback = findViewById(R.id.btnback);
        btnback.setOnClickListener(v -> finish());


        // Thiết lập OnClickListener cho nút chọn ảnh
        chonimgbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawableImagePicker(); // Gọi hàm mở hình ảnh từ drawable
            }
        });
        mangNSP = new ArrayList<>();
        adapter = new NhomSanPhamAdapter(ThemNhomSanPham_Activity.this, mangNSP, true) {
        };

        // Thiết lập OnClickListener cho nút chọn ảnh
        chonimgbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawableImagePicker(); // Gọi hàm mở hình ảnh từ drawable
            }
        });

        btnthem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy dữ liệu từ các trường
                String tenNsp = tennsp.getText().toString().trim();

                // Kiểm tra dữ liệu không rỗng
                if (tenNsp.isEmpty()) {
                    Toast.makeText(ThemNhomSanPham_Activity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageUri == null) {
                    Toast.makeText(ThemNhomSanPham_Activity.this, "Vui lòng chọn một ảnh!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Khởi tạo biến imageBytes
                byte[] imageBytes = null;

                // Kiểm tra imageUri có khác null không
                if (imageUri != null) {
                    imageBytes = getBytesFromUri(imageUri);
                    if (imageBytes == null) {
                        Toast.makeText(ThemNhomSanPham_Activity.this, "Lỗi khi lấy ảnh!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                JSONObject postData = new JSONObject();
                try {
                    postData.put("tennsp", tenNsp);
                    if (imageBytes != null) {
                        String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        JSONObject anhObject = new JSONObject();
                        anhObject.put("data", imageBase64);
                        postData.put("anh", anhObject);
                    } else {
                        // Handle case where no image is selected, maybe send an empty string or null
                        postData.put("anh", new JSONObject());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ThemNhomSanPham_Activity.this, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                ApiCaller.callApi("nhomsanpham", "POST", postData, token, response -> {
                    runOnUiThread(() -> {
                        if (response != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.has("message")) {
                                    Toast.makeText(ThemNhomSanPham_Activity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), Nhomsanpham_admin_Actvity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish(); // Đóng Activity hiện tại
                                } else {
                                    // Log the error message from server if available
                                    String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Thêm nhóm sản phẩm thất bại";
                                    Toast.makeText(ThemNhomSanPham_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ThemNhomSanPham_Activity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ThemNhomSanPham_Activity.this, "Lỗi kết nối hoặc không nhận được phản hồi", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }
    // Mở dialog chọn hình ảnh từ drawable
    private void openDrawableImagePicker() {
        final String[] imageNames = {"vest", "aococtay", "aolen", "dahoi", "giaydong", "giaythethao", "khoac1", "quanau", "quantat", "vay", "somi"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ drawable");
        builder.setItems(imageNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lấy tên hình ảnh đã chọn
                String selectedImageName = imageNames[which];

                // Lấy ID tài nguyên drawable
                int resourceId = getResources().getIdentifier(selectedImageName, "drawable", getPackageName());

                // Cập nhật ImageView
                imgnsp.setImageResource(resourceId);
                imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId); // Cập nhật URI
            }
        });
        builder.show();
    }
    // Chuyển đổi URI thành mảng byte
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
            return byteBuffer.toByteArray(); // Trả về mảng byte của ảnh
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