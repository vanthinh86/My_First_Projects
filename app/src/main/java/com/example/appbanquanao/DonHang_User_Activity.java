package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject;

public class DonHang_User_Activity extends BaseRealtimeActivity {
    private ListView listView;
    private DonHang_Adapter donHangAdapter;
    private Button btnDaDatHang, btnDangVanChuyen, btnVanChuyenThanhCong, btnHoanHang;

    private static final int DA_DAT_HANG = 0;
    private static final int DANG_VAN_CHUYEN = 1;
    private static final int VAN_CHUYEN_THANH_CONG = 2;
    private static final int HOAN_HANG = 3;
    private String tenDN = "";
    private static int TRANG_THAI = DA_DAT_HANG;

    private List<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang_user);
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        btnDaDatHang = findViewById(R.id.btn_da_dat_hang);
        btnDangVanChuyen = findViewById(R.id.btn_dang_van_chuyen);
        btnVanChuyenThanhCong = findViewById(R.id.btn_van_chuyen_thanh_cong);
        btnHoanHang = findViewById(R.id.btn_hoan_hang);
        resetBackgroundColor();
        btnDaDatHang.setBackgroundColor(Color.GRAY);

        btnDaDatHang.setOnClickListener(v -> {
            resetBackgroundColor();
            btnDaDatHang.setBackgroundColor(Color.GRAY);
            TRANG_THAI = DA_DAT_HANG;
            loadDonHang(tenDN, TRANG_THAI);
        } );
        btnDangVanChuyen.setOnClickListener(v -> {
            resetBackgroundColor();
            btnDangVanChuyen.setBackgroundColor(Color.GRAY);
            TRANG_THAI = DANG_VAN_CHUYEN;
            loadDonHang(tenDN, TRANG_THAI);

        });

        btnVanChuyenThanhCong.setOnClickListener(v -> {
            resetBackgroundColor();
            btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
            TRANG_THAI = VAN_CHUYEN_THANH_CONG;
            loadDonHang(tenDN, TRANG_THAI);

        });

        btnHoanHang.setOnClickListener(v -> {
            resetBackgroundColor();
            btnHoanHang.setBackgroundColor(Color.GRAY);
            TRANG_THAI = HOAN_HANG;
            loadDonHang(tenDN, TRANG_THAI);
        });

        // Khởi tạo các thành phần
        listView = findViewById(R.id.listViewChiTiet);
        orders = new ArrayList<>();
        donHangAdapter = new DonHang_Adapter(this, orders);
        listView.setAdapter(donHangAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = donHangAdapter.getItem(position);

                if (order != null) {
                    // Hiển thị Toast với ID đơn hàng
                    Toast.makeText(DonHang_User_Activity.this, "ID đơn hàng: " + order.getId(), Toast.LENGTH_SHORT).show();

                    // Gửi thông tin đơn hàng qua Intent
                    Intent intent = new Intent(DonHang_User_Activity.this, ChiTietDonHang_Activity.class);
                    intent.putExtra("donHangId", String.valueOf(order.getId())); // Đảm bảo rằng ID là chuỗi
                    intent.putExtra("tendn", tenDN); // Truyền tên đăng nhập
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }


        });

        // Lấy tên đăng nhập từ Intent hoặc SharedPreferences
        tenDN = getIntent().getStringExtra("tendn");

        // Nếu không có trong Intent, lấy từ SharedPreferences
        if (tenDN == null || tenDN.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            tenDN = sharedPreferences.getString("tendn", "");
        }

        // Kiểm tra giá trị tenDN
        if (tenDN == null || tenDN.isEmpty()) {
            Toast.makeText(this, "Tên đăng nhập không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish(); // Kết thúc activity nếu không có tên đăng nhập
            return;
        }

        loadDonHang(tenDN, DA_DAT_HANG); // Gọi phương thức loadDonHang với tenDN

        // Setup menu highlighting
        setupMenuHighlighting();
        
        // Setup menu highlighting
        setupMenuHighlighting();

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
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                // Đã đăng nhập, chuyển đến trang đơn hàng
                Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);
                intent.putExtra("tendn", tenDN);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

        // Load dữ liệu ban đầu - kiểm tra selectedStatus từ Intent
        if (tenDN != null && !tenDN.isEmpty()) {
            int selectedStatus = getIntent().getIntExtra("selectedStatus", DA_DAT_HANG);
            TRANG_THAI = selectedStatus;

            // Reset lại background color và highlight tab tương ứng
            resetBackgroundColor();
            if (selectedStatus == 1) { // DANG_VAN_CHUYEN
                btnDangVanChuyen.setBackgroundColor(Color.GRAY);
            } else if (selectedStatus == 2) { // VAN_CHUYEN_THANH_CONG
                btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
            } else if (selectedStatus == 3) { // HOAN_HANG
                btnHoanHang.setBackgroundColor(Color.GRAY);
            } else { // DA_DAT_HANG
                btnDaDatHang.setBackgroundColor(Color.GRAY);
            }

            // Load dữ liệu
            loadDonHang(tenDN, TRANG_THAI);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi activity này quay trở lại foreground
        if (tenDN != null && !tenDN.isEmpty()) {
            loadDonHang(tenDN, TRANG_THAI);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent hiện tại

        // Xử lý selectedStatus từ Intent mới
        int selectedStatus = intent.getIntExtra("selectedStatus", DA_DAT_HANG);
        TRANG_THAI = selectedStatus;

        // Reset lại background color và highlight tab tương ứng
        resetBackgroundColor();
        if (selectedStatus == 1) { // DANG_VAN_CHUYEN
            btnDangVanChuyen.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 2) { // VAN_CHUYEN_THANH_CONG
            btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 3) { // HOAN_HANG
            btnHoanHang.setBackgroundColor(Color.GRAY);
        } else { // DA_DAT_HANG
            btnDaDatHang.setBackgroundColor(Color.GRAY);
        }

        // Load dữ liệu với trạng thái mới
        if (tenDN != null && !tenDN.isEmpty()) {
            loadDonHang(tenDN, TRANG_THAI);
        }
    }

    private void resetBackgroundColor() {
        btnDaDatHang.setBackgroundColor(Color.TRANSPARENT);
        btnDangVanChuyen.setBackgroundColor(Color.TRANSPARENT);
        btnVanChuyenThanhCong.setBackgroundColor(Color.TRANSPARENT);
        btnHoanHang.setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadDonHang(String tenKh, int trangthai) {
        // Kiểm tra tên khách hàng trước khi truy vấn
        if (tenKh == null || tenKh.isEmpty()) {
            Toast.makeText(this, "Tên khách hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
            return; // Dừng lại nếu tên khách hàng là null hoặc rỗng
        }

        ApiCaller.callApi("dathang/khachhang/" + tenKh + "/with-images?trangthai=" + trangthai, "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        orders.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            long id = jsonObject.getLong("id_dathang");
                            String tenKH = jsonObject.getString("tenkh");
                            String diaChi = jsonObject.getString("diachi");
                            String sdt = jsonObject.getString("sdt");
                            double tongThanhToan = jsonObject.getDouble("tongthanhtoan");
                            int trangThai = jsonObject.getInt("trangthai");
                            String ngayDatHang = jsonObject.getString("ngaydathang");
                            
                            Order order = new Order(id, tenKH, diaChi, sdt, (float) tongThanhToan, ngayDatHang, trangThai);

                            // Xử lý ảnh sản phẩm đầu tiên
                            if (jsonObject.has("firstProductImage") && !jsonObject.isNull("firstProductImage")) {
                                JSONObject imageObject = jsonObject.getJSONObject("firstProductImage");
                                if (imageObject.has("data") && !imageObject.isNull("data")) {
                                    try {
                                        String imageBase64 = imageObject.getString("data");
                                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                                            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                            order.setFirstProductImage(imageBytes);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            
                            orders.add(order);
                        }
                        donHangAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DonHang_User_Activity.this, "Lỗi xử lý dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DonHang_User_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // Refresh orders when real-time updates are received
        loadOrdersByStatus(TRANG_THAI);
    }
    
    @Override
    public void onDatHangStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String idDatHang = data.getString("id_dathang");
                int newStatus = data.getInt("trangthai");
                String statusText = data.getString("statusText");
                
                boolean orderFound = false;
                
                // Find and update the order in current list
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getIdDatHang() == Integer.parseInt(idDatHang)) {
                        orderFound = true;
                        // If the order status still matches the current filter, update it
                        if (newStatus == TRANG_THAI) {
                            orders.get(i).setTrangThai(newStatus);
                            donHangAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đơn hàng của bạn: " + statusText, Toast.LENGTH_LONG).show();
                        } else {
                            // Remove from current list if status no longer matches filter
                            orders.remove(i);
                            donHangAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Đơn hàng chuyển sang: " + statusText, Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                }
                
                // If order wasn't found in current list and the new status matches current filter
                // then it might be a new order that should appear in this tab
                if (!orderFound && newStatus == TRANG_THAI) {
                    // Only reload if we have valid username to avoid the "invalid customer name" error
                    if (tenDN != null && !tenDN.isEmpty()) {
                        loadOrdersByStatus(TRANG_THAI);
                        Toast.makeText(this, "Có đơn hàng mới: " + statusText, Toast.LENGTH_LONG).show();
                    }
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
                // In case of JSON parsing error, try to reload data safely
                if (tenDN != null && !tenDN.isEmpty()) {
                    loadOrdersByStatus(TRANG_THAI);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // In case of number format error, try to reload data safely
                if (tenDN != null && !tenDN.isEmpty()) {
                    loadOrdersByStatus(TRANG_THAI);
                }
            }
        });
    }

    private void loadOrdersByStatus(int trangthai) {
        // Use the current tenDN instead of getting from SharedPreferences with wrong key
        if (tenDN != null && !tenDN.isEmpty()) {
            loadDonHang(tenDN, trangthai);
        } else {
            // Fallback: get from SharedPreferences with correct key "tendn"
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String tenKh = sharedPreferences.getString("tendn", "");
            if (tenKh != null && !tenKh.isEmpty()) {
                tenDN = tenKh;
                loadDonHang(tenKh, trangthai);
            }
        }
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btncart);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu đơn hàng (vì đây là DonHang_User_Activity)
            highlightMenuButton(R.id.btndonhang);
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

