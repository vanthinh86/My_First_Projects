package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

public abstract class BaseRealtimeActivity extends AppCompatActivity implements SocketManager.SocketEventListener {
    private static final String TAG = "BaseRealtimeActivity";
    private SocketManager socketManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize socket manager
        socketManager = SocketManager.getInstance();
        socketManager.initialize(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        socketManager.addListener(this);
        socketManager.connect();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        socketManager.removeListener(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketManager.removeListener(this);
    }
    
    // Default implementations - subclasses can override these methods
    @Override
    public void onSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Product created (default): " + data.toString());
            showToast("Sản phẩm mới đã được thêm!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onSanPhamUpdated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Product updated (default): " + data.toString());
            showToast("Sản phẩm đã được cập nhật!");
            onRealtimeDataChanged();
        });
    }

    @Override
    public void onSanPhamInventoryUpdated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Product inventory updated (default): " + data.toString());
            try {
                String tensp = data.optString("tensp", "");
                int soluongkho = data.optInt("soluongkho", 0);
                showToast("Số lượng " + tensp + " đã cập nhật: " + soluongkho);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling inventory update", e);
            }
        });
    }
    
    @Override
    public void onSanPhamDeleted(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Product deleted (default): " + data.toString());
            showToast("Sản phẩm đã được xóa!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onNhomSanPhamCreated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Category created (default): " + data.toString());
            showToast("Danh mục mới đã được thêm!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onNhomSanPhamUpdated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Category updated (default): " + data.toString());
            showToast("Danh mục đã được cập nhật!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onNhomSanPhamDeleted(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Category deleted (default): " + data.toString());
            showToast("Danh mục đã được xóa!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onDatHangCreated(JSONObject data) {
        runOnUiThread(() -> {
            Log.d(TAG, "Order created (default): " + data.toString());
            showToast("Đơn hàng mới đã được tạo!");
            onRealtimeDataChanged();
        });
    }
    
    @Override
    public void onDatHangStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String statusText = data.optString("statusText", "");
                Log.d(TAG, "Order status updated (default): " + data.toString());
                showToast("Trạng thái đơn hàng: " + statusText);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling order status update", e);
            }
        });
    }

    @Override
    public void onDatHangPaymentMethodUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String paymentMethodText = data.optString("paymentMethodText", "");
                String tenkh = data.optString("tenkh", "Khách hàng");
                String orderId = data.optString("id_dathang", "");
                Log.d(TAG, "🔔 Payment method updated (default): " + data.toString());
                Log.d(TAG, "🔄 Order " + orderId + " - Customer: " + tenkh + " - New payment method: " + paymentMethodText);
                showToast("Khách hàng " + tenkh + " đã cập nhật phương thức thanh toán đơn hàng " + orderId + " thành: " + paymentMethodText);
                
                // Note: onRealtimeDataChanged() removed to prevent race condition with data model updates
                // Child classes should handle specific updates in their overridden methods
                Log.d(TAG, "📱 Payment method update handled (no auto-reload to prevent race condition)");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error handling payment method update", e);
            }
        });
    }

    @Override
    public void onDatHangPaymentStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("id_dathang", "");
                String paymentStatusText = data.optString("paymentStatusText", "");
                int paymentStatus = data.optInt("trangthaithanhtoan", 0);
                Log.d(TAG, "💰 Payment status updated (default): " + data.toString());
                Log.d(TAG, "💳 Order " + orderId + " - New payment status: " + paymentStatusText + " (" + paymentStatus + ")");
                showToast("Trạng thái thanh toán đơn hàng " + orderId + ": " + paymentStatusText);
                
                // Note: onRealtimeDataChanged() removed to prevent race condition
                // Child classes can override this method if they need custom reload behavior
                Log.d(TAG, "📱 Payment status update handled (no auto-reload to prevent race condition)");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error handling payment status update", e);
            }
        });
    }

    // Chi Tiết Đơn Hàng events - default implementations
    @Override
    public void onChiTietDonHangFetched(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String orderId = data.optString("orderId", "");
                String customerName = data.optJSONObject("orderInfo").optString("tenkh", "");
                Log.d(TAG, "Chi tiết đơn hàng fetched (default): " + data.toString());
                showToast("Đã tải chi tiết đơn hàng " + orderId + " của " + customerName);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang fetched", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangCreated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String productName = data.optString("tensp", "Sản phẩm");
                String customerName = data.optString("tenkh", "");
                int quantity = data.optInt("soluong", 0);
                Log.d(TAG, "Chi tiết đơn hàng created (default): " + data.toString());
                showToast("Đã thêm " + quantity + " " + productName + " vào đơn hàng của " + customerName);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang created", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String productName = data.optString("tensp", "Sản phẩm");
                String customerName = data.optString("tenkh", "");
                int quantity = data.optInt("soluong", 0);
                Log.d(TAG, "Chi tiết đơn hàng updated (default): " + data.toString());
                showToast("Đã cập nhật " + productName + " (SL: " + quantity + ") cho " + customerName);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang updated", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangStatusUpdated(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String customerName = data.optString("tenkh", "");
                int status = data.optInt("trangthai", 0);
                String statusText = getStatusText(status);
                Log.d(TAG, "Chi tiết đơn hàng status updated (default): " + data.toString());
                showToast("Trạng thái đơn hàng của " + customerName + ": " + statusText);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang status updated", e);
            }
        });
    }

    @Override
    public void onChiTietDonHangDeleted(JSONObject data) {
        runOnUiThread(() -> {
            try {
                String customerName = data.optString("tenkh", "");
                Log.d(TAG, "Chi tiết đơn hàng deleted (default): " + data.toString());
                showToast("Đã xóa chi tiết đơn hàng của " + customerName);
                onRealtimeDataChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error handling chi tiet don hang deleted", e);
            }
        });
    }
    
    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "Socket connected");
            // Optionally show connection status
        });
    }
    
    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "Socket disconnected");
            // Optionally show disconnection status
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "Socket error: " + error);
            // Optionally handle socket errors
        });
    }
    
    // Abstract method that subclasses must implement to refresh their data
    protected abstract void onRealtimeDataChanged();
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
