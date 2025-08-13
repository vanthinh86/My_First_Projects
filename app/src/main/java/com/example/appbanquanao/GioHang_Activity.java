package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Base64;

import java.util.List;

public class GioHang_Activity extends AppCompatActivity {
    private ListView listView;
    private GioHangAdapter adapter;
    private GioHangManager gioHangManager;
    private Button thanhtoan;
    private TextView txtTongTien; // Khai báo TextView cho tổng tiền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gio_hang);
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);
        thanhtoan = findViewById(R.id.btnthanhtoan);
        listView = findViewById(R.id.listtk);
        TextView textTendn = findViewById(R.id.tendn);

        ImageView ql = (ImageView) findViewById(R.id.back);
        ql.setOnClickListener(v-> finish());
        // Lấy tendn từ SharedPreferences
        SharedPreferences sharedPre = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String tendn = sharedPre.getString("tendn", null);

        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            Intent intent = new Intent(GioHang_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }

        txtTongTien = findViewById(R.id.tongtien); // Khởi tạo TextView cho tổng tiền

        gioHangManager = GioHangManager.getInstance();

        // Lấy danh sách giỏ hàng và cập nhật giao diện
        List<GioHang> gioHangList = gioHangManager.getGioHangList();
        adapter = new GioHangAdapter(this, gioHangList, txtTongTien);
        listView.setAdapter(adapter);

        // Cập nhật tổng tiền ngay từ giá hàng
        txtTongTien.setText(String.valueOf(gioHangManager.getTongTien()));
        
        // Setup menu highlighting
        setupMenuHighlighting();

        // Xử lý sự kiện click thanh toán
        thanhtoan.setOnClickListener(v -> showPaymentDialog());
        btncard.setOnClickListener(new View.OnClickListener() {
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
                    Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đã đăng nhập, chuyển đến trang đơn hàng
                Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);

                startActivity(intent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra trạng thái đăng nhập của người dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (isLoggedIn) {
                    // Đã đăng nhập, chuyển đến trang đơn hàng
                    Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);

                    // Truyền tendn qua Intent
                    intent.putExtra("tendn", tendn);  // Thêm dòng này để truyền tendn

                    startActivity(intent);

                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    // Chưa đăng nhập, chuyển đến trang login
                    Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }

        });
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
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),TimKiemSanPham_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }

    private void showPaymentDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_thong_tin_thanh_toan);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        EditText edtTenKh = dialog.findViewById(R.id.tenkh);
        EditText edtDiaChi = dialog.findViewById(R.id.diachi);
        EditText edtSdt = dialog.findViewById(R.id.sdt);
        Button btnLuu = dialog.findViewById(R.id.btnxacnhandathang);
        TextView tvTongTien = dialog.findViewById(R.id.tienthanhtoan);

        String tongTien = txtTongTien.getText().toString();
        tvTongTien.setText(tongTien);

        btnLuu.setOnClickListener(v -> {
            String tenKh = edtTenKh.getText().toString().trim();
            String diaChi = edtDiaChi.getText().toString().trim();
            String sdt = edtSdt.getText().toString().trim();

            if (tenKh.isEmpty() || diaChi.isEmpty() || sdt.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null);

            if (token == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để đặt hàng!", Toast.LENGTH_SHORT).show();
                // Optional: Redirect to login
                // Intent intent = new Intent(GioHang_Activity.this, Login_Activity.class);
                // startActivity(intent); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return;
            }

            try {
                JSONObject orderData = new JSONObject();
                orderData.put("tenkh", tenKh);
                orderData.put("diachi", diaChi);
                orderData.put("sdt", sdt);
                orderData.put("tongthanhtoan", Float.parseFloat(tongTien.replace(",", "")));
                orderData.put("id_dathang", System.currentTimeMillis());
                orderData.put("trangthai", 0);

                JSONArray chiTietDonHangs = new JSONArray();
                List<GioHang> gioHangList = gioHangManager.getGioHangList();
                for (int i = 0; i < gioHangList.size(); i++) {
                    GioHang item = gioHangList.get(i);
                    JSONObject chiTiet = new JSONObject();
                    chiTiet.put("id_chitiet", System.currentTimeMillis() + i); // Unique ID for detail
                    chiTiet.put("masp", item.getSanPham().getMasp());
                    chiTiet.put("soluong", item.getSoLuong());
                    chiTiet.put("dongia", item.getSanPham().getDongia());

                    // Chuyển đổi ảnh thành base64 string để gửi qua API
                    if (item.getSanPham().getAnh() != null) {
                        String base64Image = Base64.encodeToString(item.getSanPham().getAnh(), Base64.NO_WRAP);
                        chiTiet.put("anh", base64Image);
                    } else {
                        chiTiet.put("anh", ""); // Empty string if no image
                    }
                    
                    chiTiet.put("trangthai", 0); // Default status
                    chiTietDonHangs.put(chiTiet);
                }
                orderData.put("chiTietDonHangs", chiTietDonHangs);

                ApiCaller.callApi("dathang", "POST", orderData, token, response -> {
                    runOnUiThread(() -> {
                        if (response != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                if (jsonResponse.has("_id")) {
                                    Toast.makeText(GioHang_Activity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                                    gioHangManager.clearGioHang();
                                    txtTongTien.setText("0");
                                    adapter.notifyDataSetChanged();
                                    Intent a = new Intent(GioHang_Activity.this, TrangchuNgdung_Activity.class);
                                    startActivity(a);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                } else {
                                    String message = jsonResponse.optString("msg", "Đặt hàng thất bại!");
                                    Toast.makeText(GioHang_Activity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(GioHang_Activity.this, "Lỗi xử lý phản hồi!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GioHang_Activity.this, "Đặt hàng thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    });
                });

            } catch (JSONException | NumberFormatException e) {
                Toast.makeText(this, "Có lỗi xảy ra khi tạo yêu cầu đặt hàng!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btncart);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu giỏ hàng (vì đây là GioHang_Activity)
            highlightMenuButton(R.id.btncart);
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

