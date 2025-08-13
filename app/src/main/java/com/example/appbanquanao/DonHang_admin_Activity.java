package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DonHang_admin_Activity extends BaseRealtimeActivity {

    private ListView listView;
    private DonHang_Adapter donHangAdapter;

    private Button btnDaDatHang, btnDangVanChuyen, btnVanChuyenThanhCong, btnHoanHang;

    private List<Order> orders;

    private static final int DA_DAT_HANG = 0;
    private static final int DANG_VAN_CHUYEN = 1;
    private static final int VAN_CHUYEN_THANH_CONG = 2;
    private static final int HOAN_HANG = 3;
    private static int TRANG_THAI = DA_DAT_HANG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang_admin);

        // Setup menu highlighting - Đơn hàng được highlight
        setupMenuHighlighting();

        // Khởi tạo các thành phần
        listView = findViewById(R.id.listViewChiTiet);
        orders = new ArrayList<>();
        donHangAdapter = new DonHang_Adapter(this, orders);
        listView.setAdapter(donHangAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order = donHangAdapter.getItem(position);

                if (order != null) {
                    Intent intent = new Intent(DonHang_admin_Activity.this, ChiTietDonHang_Admin_Activity.class);
                    intent.putExtra("donHangId", String.valueOf(order.getId()));
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

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
            loadDonHang(TRANG_THAI);
        } );
        btnDangVanChuyen.setOnClickListener(v -> {
            resetBackgroundColor();
            btnDangVanChuyen.setBackgroundColor(Color.GRAY);
            TRANG_THAI = DANG_VAN_CHUYEN;
            loadDonHang(TRANG_THAI);
        });

        btnVanChuyenThanhCong.setOnClickListener(v -> {
            resetBackgroundColor();
            btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
            TRANG_THAI = VAN_CHUYEN_THANH_CONG;
            loadDonHang(TRANG_THAI);
        });

        btnHoanHang.setOnClickListener(v -> {
            resetBackgroundColor();
            btnHoanHang.setBackgroundColor(Color.GRAY);
            TRANG_THAI = HOAN_HANG;
            loadDonHang(TRANG_THAI);
        });

        ImageButton btntrangchu=findViewById(R.id.btntrangchu);
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),TrangchuAdmin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btncanhan=findViewById(R.id.btncanhan);
        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (!isLoggedIn) {
                    Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_admin_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        ImageButton btndonhang=findViewById(R.id.btndonhang);
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),DonHang_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnsanpham    =findViewById(R.id.btnsanpham);
        btnsanpham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Sanpham_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btnnhomsp   =findViewById(R.id.btnnhomsp);
        btnnhomsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Nhomsanpham_admin_Actvity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btntaikhoan    =findViewById(R.id.btntaikhoan);
        btntaikhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),Taikhoan_admin_Activity.class);
                startActivity(a);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Load dữ liệu ban đầu - kiểm tra selectedStatus từ Intent
        int selectedStatus = getIntent().getIntExtra("selectedStatus", DA_DAT_HANG);
        TRANG_THAI = selectedStatus;
        resetBackgroundColor();
        
        // Highlight tab tương ứng
        if (selectedStatus == 1) { // DANG_VAN_CHUYEN
            btnDangVanChuyen.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 2) { // VAN_CHUYEN_THANH_CONG
            btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 3) { // HOAN_HANG
            btnHoanHang.setBackgroundColor(Color.GRAY);
        } else { // DA_DAT_HANG
            btnDaDatHang.setBackgroundColor(Color.GRAY);
        }
        
        loadDonHang(TRANG_THAI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi activity này quay trở lại foreground
        loadDonHang(TRANG_THAI);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent hiện tại

        // Xử lý selectedStatus từ Intent mới
        int selectedStatus = intent.getIntExtra("selectedStatus", DA_DAT_HANG);
        TRANG_THAI = selectedStatus;
        resetBackgroundColor();

        // Highlight tab tương ứng
        if (selectedStatus == 1) { // DANG_VAN_CHUYEN
            btnDangVanChuyen.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 2) { // VAN_CHUYEN_THANH_CONG
            btnVanChuyenThanhCong.setBackgroundColor(Color.GRAY);
        } else if (selectedStatus == 3) { // HOAN_HANG
            btnHoanHang.setBackgroundColor(Color.GRAY);
        } else { // DA_DAT_HANG
            btnDaDatHang.setBackgroundColor(Color.GRAY);
        }
        
        loadDonHang(TRANG_THAI);
    }

    private void resetBackgroundColor() {
        btnDaDatHang.setBackgroundColor(Color.TRANSPARENT);
        btnDangVanChuyen.setBackgroundColor(Color.TRANSPARENT);
        btnVanChuyenThanhCong.setBackgroundColor(Color.TRANSPARENT);
        btnHoanHang.setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadDonHang(int trangthai) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        ApiCaller.callApi("dathang/with-images?trangthai=" + trangthai, "GET", null, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        
                        if (jsonResponse.has("msg")) {
                            Toast.makeText(DonHang_admin_Activity.this, "Lỗi server: " + jsonResponse.getString("msg"), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        JSONArray jsonArray = jsonResponse.getJSONArray("donhang");
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
                        
                        if (orders.isEmpty()) {
                            Toast.makeText(DonHang_admin_Activity.this, "Không có đơn hàng nào ở trạng thái này", Toast.LENGTH_SHORT).show();
                        }
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DonHang_admin_Activity.this, "Lỗi xử lý dữ liệu đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(DonHang_admin_Activity.this, "Lỗi kết nối hoặc không có quyền truy cập", Toast.LENGTH_SHORT).show();
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
    public void onDatHangCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                // If we're viewing "Da dat hang" status (0), add the new order
                if (TRANG_THAI == DA_DAT_HANG) {
                    String idDatHang = data.getString("id_dathang");
                    String tenkh = data.getString("tenkh");
                    String diachi = data.getString("diachi");
                    String sdt = data.getString("sdt");
                    double tongThanhToan = data.getDouble("tongthanhtoan");
                    int trangthai = data.getInt("trangthai");
                    String ngayDatHang = data.getString("ngaydathang");
                    
                    Order newOrder = new Order(Long.parseLong(idDatHang), tenkh, diachi, sdt, (float)tongThanhToan, ngayDatHang, trangthai);

                    // Xử lý ảnh sản phẩm đầu tiên nếu có
                    if (data.has("firstProductImage") && !data.isNull("firstProductImage")) {
                        JSONObject imageObject = data.getJSONObject("firstProductImage");
                        if (imageObject.has("data") && !imageObject.isNull("data")) {
                            try {
                                String imageBase64 = imageObject.getString("data");
                                if (imageBase64 != null && !imageBase64.isEmpty()) {
                                    byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                    newOrder.setFirstProductImage(imageBytes);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    
                    orders.add(0, newOrder); // Add to beginning of list
                    donHangAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Đơn hàng mới từ: " + tenkh, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onDatHangStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String idDatHang = data.getString("id_dathang");
                int newStatus = data.getInt("trangthai");
                String statusText = data.getString("statusText");
                
                // Find and update the order in current list
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getIdDatHang() == Integer.parseInt(idDatHang)) {
                        // If the order status still matches the current filter, update it
                        if (newStatus == TRANG_THAI) {
                            orders.get(i).setTrangThai(newStatus);
                            donHangAdapter.notifyDataSetChanged();
                        } else {
                            // Remove from current list if status no longer matches filter
                            orders.remove(i);
                            donHangAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(this, "Đơn hàng " + idDatHang + ": " + statusText, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                
                // If current filter matches the new status and order wasn't in current list, reload
                if (newStatus == TRANG_THAI) {
                    boolean orderExists = false;
                    for (Order order : orders) {
                        if (order.getIdDatHang() == Integer.parseInt(idDatHang)) {
                            orderExists = true;
                            break;
                        }
                    }
                    if (!orderExists) {
                        loadOrdersByStatus(TRANG_THAI);
                    }
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onDatHangPaymentMethodUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String idDatHang = data.getString("id_dathang");
                String paymentMethodText = data.getString("paymentMethodText");
                String tenkh = data.getString("tenkh");
                String message = data.getString("msg");
                
                // Show detailed notification for admin
                Toast.makeText(this, "🔔" + message, Toast.LENGTH_LONG).show();
                
                // Log for debugging
                Log.d("DonHang_admin_Activity", "Payment method updated - Order: " + idDatHang + 
                      ", Customer: " + tenkh + ", Method: " + paymentMethodText);
                
                // Delay reload to ensure realtime update has been processed first
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(() -> {
                    onRealtimeDataChanged();
                }, 500); // 500ms delay
                
            } catch (JSONException e) {
                Log.e("DonHang_admin_Activity", "Error handling payment method update", e);
            }
        });
    }

    private void loadOrdersByStatus(int trangthai) {
        loadDonHang(trangthai);
    }

    private void setupMenuHighlighting() {
        try {
            // Reset tất cả menu về trạng thái bình thường
            resetMenuButton(R.id.btntrangchu);
            resetMenuButton(R.id.btnnhomsp);
            resetMenuButton(R.id.btnsanpham);
            resetMenuButton(R.id.btndonhang);
            resetMenuButton(R.id.btntaikhoan);
            resetMenuButton(R.id.btncanhan);

            // Highlight menu đơn hàng (vì đây là DonHang_admin_Activity)
            highlightMenuButton(R.id.btndonhang);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void resetMenuButton(int buttonId) {
        try {
            android.widget.ImageButton button = findViewById(buttonId);
            if (button != null) {
                button.setBackgroundColor(android.graphics.Color.WHITE);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void highlightMenuButton(int buttonId) {
        try {
            android.widget.ImageButton button = findViewById(buttonId);
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

