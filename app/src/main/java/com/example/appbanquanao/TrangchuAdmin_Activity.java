package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrangchuAdmin_Activity extends BaseRealtimeActivity {

    RecyclerView grv2;
    GridView grv1;
    ArrayList<SanPham> mangSPgrv1; // Danh sách cho GridView

    ArrayList<NhomSanPham> mangNSPgrv2; // Danh sách cho ListView

    NhomSanPhamHorizontalAdapter adapterGrv2;
    SanPham_TrangChuAdmin_Adapter adapterGrv1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu_admin);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String quyen = sharedPreferences.getString("quyen", "");

        if (!isLoggedIn || !"admin".equals(quyen)) {
            Intent intent = new Intent(TrangchuAdmin_Activity.this, Login_Activity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
            return;
        }
        
        // Setup menu highlighting - Trang chủ được highlight
        setupMenuHighlighting();
        
        ImageButton btncanhan=findViewById(R.id.btncanhan);
        btncanhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đã đăng nhập, chuyển đến trang 2
                Intent intent = new Intent(getApplicationContext(), TrangCaNhan_admin_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ImageButton btndonhang=findViewById(R.id.btndonhang);
        btndonhang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đã đăng nhập, chuyển đến trang 2
                Intent intent = new Intent(getApplicationContext(), DonHang_admin_Activity.class);
                startActivity(intent);
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
        grv2=findViewById(R.id.grv2);
        grv1=findViewById(R.id.grv1);
        mangNSPgrv2= new ArrayList<>(); // Khởi tạo danh sách
        mangSPgrv1= new ArrayList<>(); // Khởi tạo danh sách
        adapterGrv2 = new NhomSanPhamHorizontalAdapter(this, mangNSPgrv2);
        grv2.setAdapter(adapterGrv2);
        grv2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapterGrv1= new SanPham_TrangChuAdmin_Adapter(this, mangSPgrv1, false) ;

        grv1.setAdapter(adapterGrv1);


        Loaddulieubacsigridview2();
        Loaddulieubacsigridview1();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity to reflect any changes
        Loaddulieubacsigridview2();
        Loaddulieubacsigridview1();
    }

    private void Loaddulieubacsigridview2() {
        ApiCaller.callApi("nhomsanpham", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangNSPgrv2.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String ma = jsonObject.getString("maso");
                            String ten = jsonObject.getString("tennsp");
                            byte[] blob = null;
                            if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                String anhBase64 = jsonObject.getString("anh");
                                try {
                                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    blob = null; // Set blob to null if decoding fails
                                }
                            }
                            mangNSPgrv2.add(new NhomSanPham(ma, ten, blob));
                        }
                        adapterGrv2.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TrangchuAdmin_Activity.this, "Lỗi xử lý dữ liệu nhóm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TrangchuAdmin_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void Loaddulieubacsigridview1() {
        ApiCaller.callApi("sanpham?sort=sales", "GET", null, response -> {
            runOnUiThread(() -> {
                if (response != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        mangSPgrv1.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int masp = jsonObject.getInt("masp");
                            String tensp = jsonObject.getString("tensp");
                            float dongia = (float) jsonObject.getDouble("dongia");
                            String mota = jsonObject.getString("mota");
                            String ghichu = jsonObject.getString("ghichu");
                            int soluongkho = jsonObject.getInt("soluongkho");
                            
                            // Get category code from nested nhomSanPham object
                            String maso = "";
                            if (jsonObject.has("nhomSanPham") && !jsonObject.isNull("nhomSanPham")) {
                                JSONObject nhomSanPhamObject = jsonObject.getJSONObject("nhomSanPham");
                                if (nhomSanPhamObject.has("maso") && !nhomSanPhamObject.isNull("maso")) {
                                    maso = String.valueOf(nhomSanPhamObject.getInt("maso"));
                                }
                            }
                            byte[] blob = null;
                            if (jsonObject.has("anh") && !jsonObject.isNull("anh")) {
                                String anhBase64 = jsonObject.getString("anh");
                                try {
                                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                    blob = null; // Set blob to null if decoding fails
                                }
                            }
                            mangSPgrv1.add(new SanPham(String.valueOf(masp), tensp, dongia, mota, ghichu, soluongkho, maso, blob));
                        }
                        adapterGrv1.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TrangchuAdmin_Activity.this, "Lỗi xử lý dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TrangchuAdmin_Activity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // Refresh data when real-time updates are received
        loadSanPham();
        loadNhomSanPham();
    }
    
    // Override specific real-time event handlers for more detailed behavior
    @Override
    public void onSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                // Add the new product to the list
                int masp = data.getInt("masp");
                String tensp = data.getString("tensp");
                double dongia = data.getDouble("dongia");
                String mota = data.optString("mota", "");
                String ghichu = data.optString("ghichu", "");
                int soluongkho = data.getInt("soluongkho");
                int maso = data.getInt("maso");
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                SanPham newProduct = new SanPham(String.valueOf(masp), tensp, (float)dongia, mota, ghichu, soluongkho, String.valueOf(maso), blob);
                mangSPgrv1.add(0, newProduct); // Add to beginning of list
                adapterGrv1.notifyDataSetChanged();
                
                Toast.makeText(this, "Sản phẩm mới: " + tensp, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onSanPhamDeleted(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String deletedMasp = data.getString("masp");
                
                // Remove the product from the list
                for (int i = 0; i < mangSPgrv1.size(); i++) {
                    if (mangSPgrv1.get(i).getMasp().equals(deletedMasp)) {
                        String productName = mangSPgrv1.get(i).getTensp();
                        mangSPgrv1.remove(i);
                        adapterGrv1.notifyDataSetChanged();
                        Toast.makeText(this, "Đã xóa sản phẩm: " + productName, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onNhomSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                // Add the new category to the list
                int maso = data.getInt("maso");
                String tennsp = data.getString("tennsp");
                
                byte[] blob = null;
                String anhBase64 = data.optString("anh");
                if (!anhBase64.isEmpty()) {
                    blob = Base64.decode(anhBase64, Base64.DEFAULT);
                }
                
                NhomSanPham newCategory = new NhomSanPham(String.valueOf(maso), tennsp, blob);
                mangNSPgrv2.add(0, newCategory); // Add to beginning of list
                adapterGrv2.notifyDataSetChanged();
                
                Toast.makeText(this, "Danh mục mới: " + tennsp, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onNhomSanPhamDeleted(JSONObject data) {
        runOnUiThread(() -> {
            try {
                int deletedMaso = data.getInt("maso");
                
                // Remove the category from the list
                for (int i = 0; i < mangNSPgrv2.size(); i++) {
                    if (Integer.parseInt(mangNSPgrv2.get(i).getMa()) == deletedMaso) {
                        String categoryName = mangNSPgrv2.get(i).getTennhom();
                        mangNSPgrv2.remove(i);
                        adapterGrv2.notifyDataSetChanged();
                        Toast.makeText(this, "Đã xóa danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
                        break;
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
                
                Log.d("TrangchuAdmin_Activity", "🔔 Payment method updated - Order: " + idDatHang + ", Customer: " + tenkh + ", Method: " + paymentMethodText);
                
                // Show notification on admin dashboard
                Toast.makeText(this, "💳 " + message, Toast.LENGTH_LONG).show();
                
                Log.d("TrangchuAdmin_Activity", "📱 Payment method update handled (no auto-reload to prevent race condition)");
                // Note: onRealtimeDataChanged() removed to prevent conflicts with realtime updates
                // Admin can manually refresh if needed
                
            } catch (JSONException e) {
                Log.e("TrangchuAdmin_Activity", "❌ Error handling payment method update", e);
            }
        });
    }

    private void loadSanPham() {
        Loaddulieubacsigridview1();
    }

    private void loadNhomSanPham() {
        Loaddulieubacsigridview2();
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
            
            // Highlight menu trang chủ (vì đây là TrangchuAdmin_Activity)
            highlightMenuButton(R.id.btntrangchu);
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
                button.setBackgroundColor(android.graphics.Color.parseColor("#D3D3D3")); // Green highlight
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