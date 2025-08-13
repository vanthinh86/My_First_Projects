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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class SuaSanPham_Activity extends AppCompatActivity {

    private EditText tensp, dongia, mota, ghichu, soluongkho, masp;
    private Spinner manhomsanpham;
    private ImageView imgsp;
    private Button btnAddImg, btnsua;
    private Uri imageUri;
    private byte[] currentImageBytes;
    private ArrayList<NhomSanPham> mangNSPList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_san_pham);

        tensp = findViewById(R.id.tensp);
        dongia = findViewById(R.id.dongia);
        mota = findViewById(R.id.mota);
        ghichu = findViewById(R.id.ghichu);
        soluongkho = findViewById(R.id.soluongkho);
        manhomsanpham = findViewById(R.id.manhomsanpham);
        imgsp = findViewById(R.id.imgsp);
        btnAddImg = findViewById(R.id.btnAddImg);
        btnsua = findViewById(R.id.btnsua);
        masp = findViewById(R.id.masp);

        Intent intent = getIntent();
        masp.setText(intent.getStringExtra("masp"));
        tensp.setText(intent.getStringExtra("tensp"));
        dongia.setText(String.valueOf(intent.getFloatExtra("dongia", 0)));
        mota.setText(intent.getStringExtra("mota"));
        ghichu.setText(intent.getStringExtra("ghichu"));
        soluongkho.setText(String.valueOf(intent.getIntExtra("soluongkho", 0)));
        currentImageBytes = intent.getByteArrayExtra("anh");
        if (currentImageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(currentImageBytes, 0, currentImageBytes.length);
            imgsp.setImageBitmap(bitmap);
        }

        loadTenNhomSanPham();

        btnAddImg.setOnClickListener(v -> openDrawableImagePicker());
        btnsua.setOnClickListener(v -> updateSanPham());
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
                            mangNSPList.add(new NhomSanPham(maso, tennhom, null));
                        }
                        ArrayAdapter<NhomSanPham> adapter = new ArrayAdapter<>(SuaSanPham_Activity.this, android.R.layout.simple_spinner_item, mangNSPList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        manhomsanpham.setAdapter(adapter);

                        // Set selected item
                        String currentMaso = getIntent().getStringExtra("maso");
                        for (int i = 0; i < mangNSPList.size(); i++) {
                            if (mangNSPList.get(i).getMa().equals(currentMaso)) {
                                manhomsanpham.setSelection(i);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SuaSanPham_Activity.this, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SuaSanPham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateSanPham() {
        String maSpStr = masp.getText().toString();
        String tenSpStr = tensp.getText().toString().trim();
        String dongiaStr = dongia.getText().toString().trim();
        String motaStr = mota.getText().toString().trim();
        String ghichuStr = ghichu.getText().toString().trim();
        String soluongkhoStr = soluongkho.getText().toString().trim();
        String maNhomSp = ((NhomSanPham) manhomsanpham.getSelectedItem()).getMa();

        if (tenSpStr.isEmpty() || dongiaStr.isEmpty() || motaStr.isEmpty() || ghichuStr.isEmpty() || soluongkhoStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageBytes = null;
        if (imageUri != null) {
            imageBytes = getBytesFromUri(imageUri);
        } else {
            imageBytes = currentImageBytes;
        }

        JSONObject putData = new JSONObject();
        try {
            putData.put("tensp", tenSpStr);
            putData.put("dongia", Float.parseFloat(dongiaStr));
            putData.put("mota", motaStr);
            putData.put("ghichu", ghichuStr);
            putData.put("soluongkho", Integer.parseInt(soluongkhoStr));
            putData.put("maso", maNhomSp);
            if (imageBytes != null) {
                String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                JSONObject anhObject = new JSONObject();
                anhObject.put("data", imageBase64);
                putData.put("anh", anhObject);
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();

            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        ApiCaller.callApi("sanpham/" + maSpStr, "PUT", putData, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("_id")) {
                            Toast.makeText(SuaSanPham_Activity.this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_LONG).show();
                            currentImageBytes = null;
                            Intent intent = new Intent(getApplicationContext(), Sanpham_admin_Activity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Cập nhật thất bại";
                            Toast.makeText(SuaSanPham_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SuaSanPham_Activity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SuaSanPham_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void openDrawableImagePicker() {
        final String[] imageNames = {"vest1","vest2","vest3", "aococ1","aococ2","aococ3", "len1","len2","len3", "dahoi1","dahoi2","dahoi3", "giay1","giay2","giay3","giay4","giay5", "giaythethao", "aosomi1","aosomi2","aosomi3", "quan1","quan2", "quan3",  "vay1","vay2","vay3"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chá»n áº£nh tá»« drawable");
        builder.setItems(imageNames, (dialog, which) -> {
            String selectedImageName = imageNames[which];
            int resourceId = getResources().getIdentifier(selectedImageName, "drawable", getPackageName());
            imgsp.setImageResource(resourceId);
            imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            currentImageBytes = null;
            currentImageBytes = null;
        });
        builder.show();
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