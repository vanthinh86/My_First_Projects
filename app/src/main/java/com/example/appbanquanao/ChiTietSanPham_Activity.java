package com.example.appbanquanao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class ChiTietSanPham_Activity extends BaseRealtimeActivity {

    private static final String TAG = "ChiTietSanPham";
    String masp, tendn;
    Button btndathang, btnaddcart;
    private ChiTietSanPham chiTietSanPham;
    private GioHangManager gioHangManager;

    // UI elements that need to be updated
    private TextView tenspView;
    private TextView dongiaView;
    private TextView motaView;
    private TextView ghichuView;
    private TextView soluongkhoView;
    private ImageView imgspView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_san_pham);

        // Khởi tạo các thành phần giao diện
        ImageButton backButton = findViewById(R.id.back);
        btndathang = findViewById(R.id.btndathang);
        btnaddcart = findViewById(R.id.btnaddcart);

        backButton.setOnClickListener(v -> finish());

        TextView tensp = findViewById(R.id.tensp);
        ImageView imgsp = findViewById(R.id.imgsp);
        TextView dongia = findViewById(R.id.dongia);
        TextView mota = findViewById(R.id.mota);

        TextView ghichu=findViewById(R.id.ghichu);
        TextView soluongkho = findViewById(R.id.soluongkho);
        
        // Store references for later updates
        tenspView = tensp;
        imgspView = imgsp;
        dongiaView = dongia;
        motaView = mota;
        ghichuView = ghichu;
        soluongkhoView = soluongkho;
        gioHangManager = GioHangManager.getInstance(); // Sử dụng singleton
        TextView textTendn = findViewById(R.id.tendn); // TextView hiển thị tên đăng nhập

        // Lấy tendn từ SharedPreferences
        SharedPreferences sharedPre = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        tendn = sharedPre.getString("tendn", null);

        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            Intent intent = new Intent(ChiTietSanPham_Activity.this, Login_Activity.class);
            startActivity(intent);
            finish(); // Kết thúc activity nếu chưa đăng nhập
            return;
        }

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();


        // Nhận chi tiết sản phẩm nếu có
        chiTietSanPham = intent.getParcelableExtra("chitietsanpham");

        if (chiTietSanPham == null) {
            // Fallback to reading individual extras
            String ma_sp = intent.getStringExtra("masp");
            String ten_sp = intent.getStringExtra("tensp");
            float don_gia = intent.getFloatExtra("dongia", 0.0f);
            String mo_ta = intent.getStringExtra("mota");
            String ghi_chu = intent.getStringExtra("ghichu");
            int so_luong_kho = intent.getIntExtra("soluongkho", 0);
            String ma_so = intent.getStringExtra("maso");
            byte[] anh_byte = intent.getByteArrayExtra("anh");

            // Create a new ChiTietSanPham object
            chiTietSanPham = new ChiTietSanPham(ma_sp, ten_sp, don_gia, mo_ta, ghi_chu, so_luong_kho, ma_so, anh_byte);
        }

        // Now, populate the views using the chiTietSanPham object
        if (chiTietSanPham != null && chiTietSanPham.getMasp() != null) {
            masp = chiTietSanPham.getMasp(); // Lấy mã sản phẩm từ chi tiết
            tensp.setText(chiTietSanPham.getTensp());
            dongia.setText(chiTietSanPham.getDongia() != null ? String.valueOf(chiTietSanPham.getDongia()) : "Không có dữ liệu");
            mota.setText(chiTietSanPham.getMota() != null ? chiTietSanPham.getMota() : "Không có dữ liệu");
            soluongkho.setText(String.valueOf(chiTietSanPham.getSoluongkho()));
            ghichu.setText(chiTietSanPham.getGhichu() != null ? chiTietSanPham.getGhichu() : "Không có dữ liệu");
            byte[] anhByteArray = chiTietSanPham.getAnh();
            if (anhByteArray != null && anhByteArray.length > 0) {
                Bitmap imganhbs = BitmapFactory.decodeByteArray(anhByteArray, 0, anhByteArray.length);
                imgsp.setImageBitmap(imganhbs);
            } else {
                imgsp.setImageResource(R.drawable.vest); // Ảnh mặc định
            }
        } else {
            tensp.setText("Không có dữ liệu");
        }


        // Kiểm tra trạng thái đăng nhập và thêm sản phẩm vào giỏ hàng
        btnaddcart.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            if (!isLoggedIn) {
                Intent loginIntent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(loginIntent);
            } else {
                gioHangManager.addItem(chiTietSanPham); // Gọi phương thức addItem
                Toast.makeText(ChiTietSanPham_Activity.this, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
            }
        });
        // Kiểm tra trạng thái đăng nhập và thêm sản phẩm vào giỏ hàng
        btndathang.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            if (!isLoggedIn) {
                Intent loginIntent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(loginIntent);
            } else {
                gioHangManager.addItem(chiTietSanPham); // Gọi phương thức addItem
                Intent intent1=new Intent(ChiTietSanPham_Activity.this,GioHang_Activity.class);
                startActivity(intent1);
            }
        });
        // Các nút điều hướng
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        ImageButton btntrangchu = findViewById(R.id.btntrangchu);
        ImageButton btntimkiem = findViewById(R.id.btntimkiem);
        ImageButton btndonhang = findViewById(R.id.btndonhang);
        ImageButton btngiohang = findViewById(R.id.btncart);
        ImageButton btncanhan = findViewById(R.id.btncanhan);

        btntrangchu.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), TrangchuNgdung_Activity.class);
            startActivity(intent);
        });
        btntimkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a=new Intent(ChiTietSanPham_Activity.this,TimKiemSanPham_Activity.class);
                startActivity(a);
            }
        });
        btngiohang.setOnClickListener(view -> navigateToCart());
        btndonhang.setOnClickListener(view -> navigateToOrder());
        btncanhan.setOnClickListener(view -> navigateToProfile());
    }

    private void navigateToCart() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), GioHang_Activity.class);
            startActivity(intent);
        }
    }

    private void navigateToOrder() {
        // Kiểm tra trạng thái đăng nhập của người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Đã đăng nhập, chuyển đến trang đơn hàng
            Intent intent = new Intent(getApplicationContext(), DonHang_User_Activity.class);

            // Truyền tendn qua Intent
            intent.putExtra("tendn", tendn);  // Thêm dòng này để truyền tendn

            startActivity(intent);
        } else {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        }

    }

    private void navigateToProfile() {
        // Kiểm tra trạng thái đăng nhập của người dùng
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Đã đăng nhập, chuyển đến trang đơn hàng
            Intent intent = new Intent(getApplicationContext(), TrangCaNhan_nguoidung_Activity.class);

            // Truyền tendn qua Intent
            intent.putExtra("tendn", tendn);  // Thêm dòng này để truyền tendn

            startActivity(intent);
        } else {
            // Chưa đăng nhập, chuyển đến trang login
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
        }
    }
    
    @Override
    protected void onRealtimeDataChanged() {
        // This method is called when any realtime data changes
        // We don't need to refresh everything here since we have specific handlers
        Log.d(TAG, "Realtime data changed");
    }
    
    @Override
    public void onSanPhamUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String updatedMasp = data.optString("masp", "");
                
                // Check if this is the product we're viewing
                if (masp != null && masp.equals(updatedMasp)) {
                    Log.d(TAG, "Current product updated: " + data.toString());
                    
                    // Update product details
                    updateProductDetails(data);
                    Toast.makeText(this, "Thông tin sản phẩm đã được cập nhật!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling product update", e);
            }
        });
    }
    
    @Override
    public void onSanPhamInventoryUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String updatedMasp = data.optString("masp", "");
                
                // Check if this is the product we're viewing
                if (masp != null && masp.equals(updatedMasp)) {
                    Log.d(TAG, "Current product inventory updated: " + data.toString());
                    
                    int newQuantity = data.optInt("soluongkho", 0);
                    String tensp = data.optString("tensp", "");
                    
                    // Update the quantity display
                    if (soluongkhoView != null) {
                        soluongkhoView.setText(String.valueOf(newQuantity));
                    }
                    
                    // Update the chiTietSanPham object
                    if (chiTietSanPham != null) {
                        chiTietSanPham.setSoluongkho(newQuantity);
                    }
                    
                    Toast.makeText(this, "Số lượng " + tensp + " đã cập nhật: " + newQuantity, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling inventory update", e);
            }
        });
    }
    
    private void updateProductDetails(JSONObject data) {
        try {
            // Update product name
            String tensp = data.optString("tensp", "");
            if (!tensp.isEmpty() && tenspView != null) {
                tenspView.setText(tensp);
                if (chiTietSanPham != null) chiTietSanPham.setTensp(tensp);
            }
            
            // Update price
            double dongia = data.optDouble("dongia", 0.0);
            if (dongia > 0 && dongiaView != null) {
                dongiaView.setText(String.valueOf(dongia));
                if (chiTietSanPham != null) chiTietSanPham.setDongia((float) dongia);
            }
            
            // Update description
            String mota = data.optString("mota", "");
            if (motaView != null) {
                motaView.setText(mota.isEmpty() ? "Không có dữ liệu" : mota);
                if (chiTietSanPham != null) chiTietSanPham.setMota(mota);
            }
            
            // Update notes
            String ghichu = data.optString("ghichu", "");
            if (ghichuView != null) {
                ghichuView.setText(ghichu.isEmpty() ? "Không có dữ liệu" : ghichu);
                if (chiTietSanPham != null) chiTietSanPham.setGhichu(ghichu);
            }
            
            // Update quantity
            int soluongkho = data.optInt("soluongkho", 0);
            if (soluongkhoView != null) {
                soluongkhoView.setText(String.valueOf(soluongkho));
                if (chiTietSanPham != null) chiTietSanPham.setSoluongkho(soluongkho);
            }
            
            // Update image if provided
            String anhBase64 = data.optString("anh", "");
            if (!anhBase64.isEmpty() && imgspView != null) {
                try {
                    byte[] decodedString = Base64.decode(anhBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgspView.setImageBitmap(decodedByte);
                    if (chiTietSanPham != null) chiTietSanPham.setAnh(decodedString);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating image", e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating product details", e);
        }
    }
}