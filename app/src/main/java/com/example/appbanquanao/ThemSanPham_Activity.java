package com.example.appbanquanao;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.util.Base64;

public class ThemSanPham_Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Mã yêu cầu cho việc chọn ảnh
    EditText masp, tensp, dongia, mota, ghichu, soluongkho;
    Spinner mansp;
    ImageView imgsp;
    ArrayList<SanPham> mangSP;
    ArrayList<NhomSanPham> mangNSPList;
    SanPhamAdapter adapter;
    ImageButton back;
    private Uri imageUri; // Biến để lưu trữ URI của ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_san_pham);

        // Khởi tạo các view
        masp = findViewById(R.id.masp);
        tensp = findViewById(R.id.tensp);
        imgsp = findViewById(R.id.imgsp);
        mota = findViewById(R.id.mota);
        ghichu = findViewById(R.id.ghichu);
        dongia = findViewById(R.id.dongia);
        soluongkho = findViewById(R.id.soluongkho);
        mansp = findViewById(R.id.spn);
        Button chonimgbs = findViewById(R.id.btnAddImg);
        Button btnthem = findViewById(R.id.btnadd);
        Button btnback = findViewById(R.id.btnback);
        btnback.setOnClickListener(v -> finish());

        // Tải danh sách nhóm sản phẩm
        loadTenNhomSanPham();

        // Thiết lập sự kiện cho nút chọn ảnh
        chonimgbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawableImagePicker(); // Gọi hàm mở hình ảnh từ drawable
            }
        });

        // Thiết lập sự kiện cho nút thêm sản phẩm
        btnthem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSanPham();
            }
        });
    }

    private void loadTenNhomSanPham() {
        mangNSPList = new ArrayList<>();
        ApiCaller.callApi("nhomsanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String maso = jsonObject.getString("maso");
                            String tennhom = jsonObject.getString("tennsp");
                            mangNSPList.add(new NhomSanPham(maso, tennhom, null)); // null vì không cần ảnh ở đây
                        }
                        // Tạo adapter cho Spinner
                        ArrayAdapter<NhomSanPham> adapter = new ArrayAdapter<>(ThemSanPham_Activity.this, android.R.layout.simple_spinner_item, mangNSPList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mansp.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ThemSanPham_Activity.this, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ThemSanPham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addSanPham() {
        // Lấy dữ liệu từ các trường
        String maspStr = masp.getText().toString().trim();
        String tenSpStr = tensp.getText().toString().trim();
        String motaStr = mota.getText().toString().trim();
        String ghichuStr = ghichu.getText().toString().trim();
        String dongiaStr = dongia.getText().toString().trim();
        String soluongStr = soluongkho.getText().toString().trim();

        if (mansp.getSelectedItem() == null) {
            Toast.makeText(ThemSanPham_Activity.this, "Vui lòng chọn nhóm sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }
        String maso = ((NhomSanPham) mansp.getSelectedItem()).getMa();

        // Kiểm tra dữ liệu không rỗng
        if (maspStr.isEmpty() || tenSpStr.isEmpty() || motaStr.isEmpty() || ghichuStr.isEmpty() || dongiaStr.isEmpty() || soluongStr.isEmpty()) {
            Toast.makeText(ThemSanPham_Activity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khởi tạo giá trị cho imageBytes
        byte[] imageBytes = null;
        if (imageUri != null) {
            imageBytes = getBytesFromUri(imageUri);
            if (imageBytes == null) {
                Toast.makeText(ThemSanPham_Activity.this, "Lỗi khi lấy ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("masp", Integer.parseInt(maspStr));
            postData.put("tensp", tenSpStr);
            postData.put("dongia", Float.parseFloat(dongiaStr));
            postData.put("mota", motaStr);
            postData.put("ghichu", ghichuStr);
            postData.put("soluongkho", Integer.parseInt(soluongStr));
            postData.put("maso", maso);
            if (imageBytes != null) {
                String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                JSONObject anhObject = new JSONObject();
                anhObject.put("data", imageBase64);
                postData.put("anh", anhObject);
            } else {
                postData.put("anh", new JSONObject());
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(ThemSanPham_Activity.this, "Lỗi tạo dữ liệu JSON hoặc định dạng số không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        ApiCaller.callApi("sanpham", "POST", postData, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("_id")) { // Check for a field that exists on successful creation
                            Toast.makeText(ThemSanPham_Activity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), Sanpham_admin_Activity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Thêm sản phẩm thất bại";
                            Toast.makeText(ThemSanPham_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ThemSanPham_Activity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ThemSanPham_Activity.this, "Lỗi kết nối hoặc không nhận được phản hồi", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Mở dialog chọn hình ảnh từ drawable
    private void openDrawableImagePicker() {
        final String[] imageNames = {"vest1","vest2","vest3", "aococ1","aococ2","aococ3", "len1","len2","len3", "dahoi1","dahoi2","dahoi3", "giay1","giay2","giay3","giay4","giay5", "giaythethao", "aosomi1","aosomi2","aosomi3", "quan1","quan2", "quan3",  "vay1","vay2","vay3"};

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
                imgsp.setImageResource(resourceId);
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