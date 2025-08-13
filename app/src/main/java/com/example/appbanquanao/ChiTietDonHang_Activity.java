package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.util.Base64;
import java.util.List;

public class ChiTietDonHang_Activity extends BaseRealtimeActivity {

    private static final String TAG = "ChiTietDonHang_Activity";
    ListView listViewChiTiet; // Danh sách hiển thị chi tiết đơn hàng
    ChiTietDonHangAdapter chiTietAdapter; // Adapter để hiển thị chi tiết
    private List<ChiTietDonHang> chiTietList;
    private TextView tvTotalAmount; // TextView hiển thị tổng tiền
    private String currentOrderId; // Store current order ID for realtime updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_hang);
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btncard = findViewById(R.id.btncart);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        ImageView ql = (ImageView) findViewById(R.id.back);
        ql.setOnClickListener(v-> {
            // Lấy trạng thái hiện tại từ đơn hàng (ưu tiên trạng thái realtime)
            int currentOrderStatus = -1;
            
            // Lấy trạng thái từ đơn hàng hiện tại (luôn có dữ liệu mới nhất)
            if (!chiTietList.isEmpty()) {
                currentOrderStatus = chiTietList.get(0).getTrangthai();
            }
            
            // Nếu vẫn không có, thử lấy từ SharedPreferences
            if (currentOrderStatus == -1) {
                SharedPreferences prefs = getSharedPreferences("UserOrderStatus", MODE_PRIVATE);
                currentOrderStatus = prefs.getInt("lastOrderStatus", 0); // Default về 0 (Đã đặt hàng)
            }
            
            // Quay về danh sách đơn hàng user với tab trạng thái phù hợp
            SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String tendn = sharedPrefs.getString("tendn", "");
            
            Intent intent = new Intent(this, DonHang_User_Activity.class);
            intent.putExtra("tendn", tendn);
            intent.putExtra("selectedStatus", currentOrderStatus);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Khởi tạo ListView để hiển thị chi tiết đơn hàng
        listViewChiTiet = findViewById(R.id.listtk); // Đảm bảo rằng bạn đã định nghĩa ListView trong layout
        tvTotalAmount = findViewById(R.id.tv_total_amount); // Khởi tạo TextView tổng tiền
        chiTietList = new ArrayList<>();
        chiTietAdapter = new ChiTietDonHangAdapter(this, chiTietList);
        listViewChiTiet.setAdapter(chiTietAdapter);

        // Lấy ID đơn hàng từ Intent
        String donHangIdStr = getIntent().getStringExtra("donHangId");

        if (donHangIdStr != null) {
            try {
                // Chuyển đổi chuỗi donHangId thành kiểu long để tránh overflow
                long donHangId = Long.parseLong(donHangIdStr);
                loadChiTietDonHang(donHangId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ID đơn hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Không có ID đơn hàng!", Toast.LENGTH_SHORT).show();
        }


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
                } else {
                    // Đã đăng nhập, chuyển đến trang 2
                    Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
                    startActivity(intent);
                }
            }
        });
        btntrangchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đã đăng nhập, chuyển đến trang đơn hàng
                Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);

                startActivity(intent);
            }
        });
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kiểm tra trạng thái đăng nhập của ng dùng
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                if (isLoggedIn) {
                    // Đã đăng nhập, chuyển đến trang đơn hàng
                    Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);
                    // Lấy tên đăng nhập từ SharedPreferences và truyền qua Intent
                    String tendn = sharedPreferences.getString("tendn", "");
                    intent.putExtra("tendn", tendn);
                    startActivity(intent);
                } else {
                    // Chưa đăng nhập, chuyển đến trang login
                    Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                    startActivity(intent);
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
                } else {
                    // Đã đăng nhập, chuyển đến trang 2
                    Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);
                    startActivity(intent);
                }
            }
        });

        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(getApplicationContext(),TimKiemSanPham_Activity.class);
                startActivity(a);
            }
        });
    }

    private void loadChiTietDonHang(long donHangId) {
        // Store current order ID for realtime updates
        currentOrderId = String.valueOf(donHangId);
        Log.d(TAG, "Loading chi tiet don hang for order ID: " + currentOrderId);
        
        // Lấy token từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        
        if (token == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem chi tiết đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Thêm thông báo debug
        Toast.makeText(this, "Đang tải chi tiết đơn hàng ID: " + donHangId, Toast.LENGTH_SHORT).show();
        
        ApiCaller.callApi("chitietdonhang/" + donHangId, "GET", null, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    // Debug: In ra response để kiểm tra
                    System.out.println("API Response: " + response);
                    
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        
                        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                            // Thành công, lấy dữ liệu
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            chiTietList.clear();
                            
                            if (jsonArray.length() == 0) {
                                Toast.makeText(ChiTietDonHang_Activity.this, "Đơn hàng này không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            // Lấy thông tin đơn hàng từ orderInfo nếu có
                            JSONObject orderInfo = null;
                            if (jsonResponse.has("orderInfo")) {
                                orderInfo = jsonResponse.getJSONObject("orderInfo");
                            }
                            
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id_chitiet = jsonObject.getInt("id_chitiet");
                                long id_dathang = jsonObject.getLong("id_dathang");
                                int masp = jsonObject.getInt("masp");
                                String tensp = jsonObject.getString("tensp");
                                int soLuong = jsonObject.getInt("soluong");
                                float donGia = (float) jsonObject.getDouble("dongia");
                                
                                // Xử lý ảnh an toàn hơn
                                byte[] blob = null;
                                try {
                                    if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                        JSONObject anhObject = jsonObject.getJSONObject("anh");
                                        if (anhObject.has("data") && !anhObject.isNull("data")) {
                                            String anhBase64 = anhObject.getString("data");
                                            if (anhBase64 != null && !anhBase64.isEmpty()) {
                                                blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // Sử dụng ảnh mặc định nếu có lỗi decode
                                    blob = null;
                                }
                                
                                int trangthai = jsonObject.getInt("trangthai");
                                
                                ChiTietDonHang chiTiet = new ChiTietDonHang(id_chitiet, id_dathang, masp, tensp, soLuong, donGia, blob, trangthai);
                                
                                // Thêm thông tin thanh toán từ orderInfo nếu có
                                if (orderInfo != null) {
                                    if (orderInfo.has("trangthaithanhtoan")) {
                                        chiTiet.setTrangthaithanhtoan(orderInfo.getInt("trangthaithanhtoan"));
                                    }
                                    if (orderInfo.has("phuongthucthanhtoan")) {
                                        chiTiet.setPhuongthucthanhtoan(orderInfo.getInt("phuongthucthanhtoan"));
                                    }
                                }
                                
                                chiTietList.add(chiTiet);
                            }
                            chiTietAdapter.notifyDataSetChanged();
                            updateTotalAmount(); // Cập nhật tổng tiền
                            
                            // Lưu trạng thái ban đầu của đơn hàng để nút back sử dụng
                            if (!chiTietList.isEmpty()) {
                                SharedPreferences prefs = getSharedPreferences("UserOrderStatus", MODE_PRIVATE);
                                prefs.edit().putInt("lastOrderStatus", chiTietList.get(0).getTrangthai()).apply();
                            }
                            
                            Toast.makeText(ChiTietDonHang_Activity.this, "Tải chi tiết đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Có lỗi từ server
                            String errorMsg = jsonResponse.optString("msg", "Không thể lấy chi tiết đơn hàng");
                            Toast.makeText(ChiTietDonHang_Activity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Fallback: thử parse như format cũ (array)
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            chiTietList.clear();
                            
                            if (jsonArray.length() == 0) {
                                Toast.makeText(ChiTietDonHang_Activity.this, "Đơn hàng này không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id_chitiet = jsonObject.getInt("id_chitiet");
                                long id_dathang = jsonObject.getLong("id_dathang");
                                int masp = jsonObject.getInt("masp");
                                String tensp = jsonObject.getString("tensp");
                                int soLuong = jsonObject.getInt("soluong");
                                float donGia = (float) jsonObject.getDouble("dongia");
                                
                                // Xử lý ảnh an toàn hơn
                                byte[] blob = null;
                                try {
                                    if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                        JSONObject anhObject = jsonObject.getJSONObject("anh");
                                        if (anhObject.has("data") && !anhObject.isNull("data")) {
                                            String anhBase64 = anhObject.getString("data");
                                            if (anhBase64 != null && !anhBase64.isEmpty()) {
                                                blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                            }
                                        }
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    blob = null;
                                }
                                
                                int trangthai = jsonObject.getInt("trangthai");
                                chiTietList.add(new ChiTietDonHang(id_chitiet, id_dathang, masp, tensp, soLuong, donGia, blob, trangthai));
                            }
                            chiTietAdapter.notifyDataSetChanged();
                            Toast.makeText(ChiTietDonHang_Activity.this, "Tải chi tiết đơn hàng thành công (format cũ)!", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                            Toast.makeText(ChiTietDonHang_Activity.this, "Lỗi xử lý dữ liệu chi tiết đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(ChiTietDonHang_Activity.this, "Lỗi kết nối hoặc không có quyền truy cập. Vui lòng kiểm tra đăng nhập và thử lại.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Method để tính và cập nhật tổng tiền
    private void updateTotalAmount() {
        if (chiTietList == null || chiTietList.isEmpty()) {
            tvTotalAmount.setText("Tổng: 0đ");
            return;
        }
        
        float total = 0;
        for (ChiTietDonHang chiTiet : chiTietList) {
            total += chiTiet.getSoLuong() * chiTiet.getDonGia();
        }
        
        // Format tiền tệ VND
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        String formattedTotal = currencyFormat.format(total) + "đ";
        tvTotalAmount.setText("Tổng: " + formattedTotal);
    }

    // Override BaseRealtimeActivity methods for chi tiết đơn hàng realtime updates
    @Override
    protected void onRealtimeDataChanged() {
        // COMPLETELY DISABLED to prevent data override issues
        Log.d(TAG, "🚫 onRealtimeDataChanged() called but DISABLED to prevent payment method override");
        /* Disabled to prevent payment method changes from being overridden
        Log.d(TAG, "Realtime data changed, updating chi tiet don hang smartly");
        if (currentOrderId != null) {
            try {
                long orderId = Long.parseLong(currentOrderId);
                // Chỉ reload data, không hiển thị toast thông báo loading
                loadChiTietDonHangSilently(orderId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid order ID for refresh: " + currentOrderId, e);
            }
        }
        */
    }

    // Method mới để load dữ liệu mà không hiển thị toast loading
    private void loadChiTietDonHangSilently(long donHangId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        
        if (token == null) {
            return;
        }
        
        ApiCaller.callApi("chitietdonhang/" + donHangId, "GET", null, token, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        
                        if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            chiTietList.clear();
                            
                            // Lấy thông tin đơn hàng từ orderInfo nếu có
                            JSONObject orderInfo = null;
                            if (jsonResponse.has("orderInfo")) {
                                orderInfo = jsonResponse.getJSONObject("orderInfo");
                            }
                            
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id_chitiet = jsonObject.getInt("id_chitiet");
                                long id_dathang = jsonObject.getLong("id_dathang");
                                int masp = jsonObject.getInt("masp");
                                String tensp = jsonObject.getString("tensp");
                                int soLuong = jsonObject.getInt("soluong");
                                float donGia = (float) jsonObject.getDouble("dongia");
                                
                                // Xử lý ảnh an toàn
                                byte[] hinhAnh = null;
                                if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                    JSONObject anhObject = jsonObject.getJSONObject("anh");
                                    if (anhObject.has("data") && !anhObject.isNull("data")) {
                                        try {
                                            String imageBase64 = anhObject.getString("data");
                                            if (imageBase64 != null && !imageBase64.isEmpty()) {
                                                hinhAnh = Base64.decode(imageBase64, Base64.DEFAULT);
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error decoding image", e);
                                        }
                                    }
                                }
                                
                                // Lấy trạng thái từ orderInfo
                                int trangThai = 0;
                                if (orderInfo != null && orderInfo.has("trangthai")) {
                                    trangThai = orderInfo.getInt("trangthai");
                                } else if (jsonObject.has("trangthai")) {
                                    trangThai = jsonObject.getInt("trangthai");
                                }
                                
                                ChiTietDonHang chiTiet = new ChiTietDonHang(id_chitiet, id_dathang, masp, tensp, soLuong, donGia, hinhAnh, trangThai);
                                chiTietList.add(chiTiet);
                            }
                            
                            chiTietAdapter.notifyDataSetChanged();
                            updateTotalAmount();
                            // Không hiển thị toast để tránh spam thông báo
                            Log.d(TAG, "Chi tiet data updated silently");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing realtime update", e);
                    }
                }
            });
        });
    }

    @Override
    public void onChiTietDonHangFetched(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("orderId", "");
                if (orderId.equals(currentOrderId)) {
                    Log.d(TAG, "Chi tiết đơn hàng fetched for current order: " + orderId);
                    // Data already refreshed through normal loading mechanism
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang fetched", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    String productName = data.optString("tensp", "Sản phẩm");
                    int quantity = data.optInt("soluong", 0);
                    Log.d(TAG, "New chi tiết item added to current order");
                    Toast.makeText(this, "Đã thêm " + quantity + " " + productName + " vào đơn hàng", Toast.LENGTH_SHORT).show();
                    // TEMPORARILY DISABLED: onRealtimeDataChanged();
                    // User can refresh manually to see new items
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang created", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    String productName = data.optString("tensp", "Sản phẩm");
                    int quantity = data.optInt("soluong", 0);
                    Log.d(TAG, "Chi tiết item updated in current order");
                    Toast.makeText(this, "Đã cập nhật " + productName + " (SL: " + quantity + ")", Toast.LENGTH_SHORT).show();
                    // TEMPORARILY DISABLED: onRealtimeDataChanged();
                    // User can refresh manually to see updates
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang updated", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    int status = data.optInt("trangthai", 0);
                    String statusText = getStatusText(status);
                    Log.d(TAG, "📋 Order status updated for current order: " + statusText);
                    
                    // Update the status in data model first
                    for (ChiTietDonHang chiTiet : chiTietList) {
                        if (chiTiet.getId_dathang() == Long.parseLong(orderId)) {
                            Log.d(TAG, "📝 Updating order status in model from " + chiTiet.getTrangthai() + " to " + status);
                            chiTiet.setTrangthai(status);
                        }
                    }
                    
                    // Cập nhật trạng thái trong SharedPreferences để nút back sử dụng
                    SharedPreferences prefs = getSharedPreferences("UserOrderStatus", MODE_PRIVATE);
                    prefs.edit().putInt("lastOrderStatus", status).apply();
                    
                    // Refresh adapter to update UI with new status
                    if (chiTietAdapter != null) {
                        chiTietAdapter.updateOrderStatusForRealtimeUpdate(status);
                    }
                    
                    Toast.makeText(this, "✅ Trạng thái đơn hàng cập nhật: " + statusText, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang status updated", e);
            }
        });
    }

    @Override
    public void onDatHangPaymentStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    int paymentStatus = data.optInt("trangthaithanhtoan", 0);
                    String paymentStatusText = data.optString("paymentStatusText", "");
                    Log.d(TAG, "💰 Payment status updated for current order: " + paymentStatusText);
                    Log.d(TAG, "🔄 New payment status value: " + paymentStatus + " for order " + orderId);
                    
                    // Cập nhật trạng thái trong data model
                    for (ChiTietDonHang chiTiet : chiTietList) {
                        if (chiTiet.getId_dathang() == Long.parseLong(orderId)) {
                            Log.d(TAG, "📝 Updating payment status in model from " + chiTiet.getTrangthaithanhtoan() + " to " + paymentStatus);
                            chiTiet.setTrangthaithanhtoan(paymentStatus);
                            
                            // Removed auto-set payment method logic to preserve user's choice
                        }
                    }
                    
                    // Refresh adapter để hiển thị thay đổi và cập nhật UI controls
                    chiTietAdapter.notifyDataSetChanged();
                    
                    // Hiển thị thông báo phù hợp
                    if (paymentStatus == 1) {
                        Toast.makeText(this, "✅ " + paymentStatusText + " - Phương thức thanh toán đã được cố định", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "🔄 " + paymentStatusText, Toast.LENGTH_SHORT).show();
                    }
                    
                    // COMPLETELY REMOVED delayed sync to prevent any data override
                    // User can manually refresh if needed using pull-to-refresh or exit/enter
                    Log.d(TAG, "✅ Payment status updated successfully via real-time event (no auto-sync)");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling payment status update", e);
            }
        });
    }

    @Override
    public void onDatHangPaymentMethodUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    int paymentMethod = data.optInt("phuongthucthanhtoan", 0);
                    String paymentMethodText = data.optString("paymentMethodText", "");
                    Log.d(TAG, "💳 Payment method updated for current order: " + paymentMethodText);
                    Log.d(TAG, "🔄 New payment method value: " + paymentMethod + " for order " + orderId);
                    
                    // Cập nhật phương thức thanh toán trong data model
                    for (ChiTietDonHang chiTiet : chiTietList) {
                        if (chiTiet.getId_dathang() == Long.parseLong(orderId)) {
                            Log.d(TAG, "📝 Updating payment method in model from " + chiTiet.getPhuongthucthanhtoan() + " to " + paymentMethod);
                            chiTiet.setPhuongthucthanhtoan(paymentMethod);
                        }
                    }
                    
                    // Refresh adapter để hiển thị thay đổi ngay lập tức
                    chiTietAdapter.notifyDataSetChanged();
                    
                    // Hiển thị thông báo
                    Toast.makeText(this, "🔄 " + paymentMethodText, Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "✅ Payment method updated successfully via real-time event");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling payment method update", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangDeleted(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                if (orderId.equals(currentOrderId)) {
                    Log.d(TAG, "Chi tiết item deleted from current order");
                    Toast.makeText(this, "Đã xóa một mục khỏi đơn hàng", Toast.LENGTH_SHORT).show();
                    // TEMPORARILY DISABLED: onRealtimeDataChanged();
                    // User can refresh manually to see deletions
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang deleted", e);
            }
        });
    }

    // Override to prevent auto-reload during payment status changes 
    @Override
    public void onSanPhamInventoryUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String tensp = data.optString("tensp", "");
                int soluongkho = data.optInt("soluongkho", 0);
                String changeType = data.optString("changeType", "");
                String orderId = data.optString("id_dathang", "");
                
                Log.d(TAG, "🔄 Product inventory updated: " + tensp + " - " + soluongkho + " (type: " + changeType + ")");
                
                // If inventory change is due to payment/order status and relates to current order,
                // DON'T reload to prevent overriding the payment status UI update
                if (("payment_status_change".equals(changeType) || "order_status_change".equals(changeType)) && orderId.equals(currentOrderId)) {
                    Log.d(TAG, "🚫 Skipping reload for " + changeType + " inventory change to prevent UI override");
                    Toast.makeText(this, "Kho " + tensp + " đã cập nhật: " + soluongkho, Toast.LENGTH_SHORT).show();
                } else {
                    // For other inventory changes, show toast but NO reload to prevent override
                    Toast.makeText(this, "Số lượng " + tensp + " đã cập nhật: " + soluongkho, Toast.LENGTH_SHORT).show();
                    // DISABLED: onRealtimeDataChanged(); // Prevent payment method override
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling inventory update", e);
            }
        });
    }

    // Helper method to convert status code to text
    private String getStatusText(int status) {
        switch (status) {
            case 0: return "Chờ xác nhận";
            case 1: return "Đã xác nhận";
            case 2: return "Đang giao";
            case 3: return "Đã giao";
            case 4: return "Đã hủy";
            default: return "Không xác định";
        }
    }
}
