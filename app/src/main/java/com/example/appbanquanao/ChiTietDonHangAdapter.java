package com.example.appbanquanao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChiTietDonHangAdapter extends ArrayAdapter<ChiTietDonHang> {

    public static final int DA_DAT_HANG = 0;
    public static final int DANG_VAN_CHUYEN = 1;
    public static final int VAN_CHUYEN_THANH_CONG = 2;
    public static final int HOAN_HANG = 3;

    public static final int CHUA_THANH_TOAN = 0;
    public static final int DA_THANH_TOAN = 1;

    public static final int TIEN_MAT = 0;
    public static final int CHUYEN_KHOAN = 1;

    private boolean isAdmin = false;
    private Map<Integer, byte[]> imageCache = new ConcurrentHashMap<>();

    public ChiTietDonHangAdapter(Context context, List<ChiTietDonHang> details) {
        super(context, 0, details);
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChiTietDonHang detail = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ds_chitietdonhang, parent, false);
        }

        TextView tvID_dathang = convertView.findViewById(R.id.txt_Iddathang);
        TextView tvMaSp = convertView.findViewById(R.id.txtMasp);
        TextView tvTenSp = convertView.findViewById(R.id.txtTensp);
        TextView tvSoLuong = convertView.findViewById(R.id.txtSoLuong);
        TextView tvDonGia = convertView.findViewById(R.id.txtGia);
        TextView tvTrangThaiThanhToan = convertView.findViewById(R.id.txtTrangThaiThanhToan);
        ImageView ivAnh = convertView.findViewById(R.id.imgsp);
        
        // Admin-only update date info
        LinearLayout layoutNgayCapNhat = convertView.findViewById(R.id.layout_ngay_capnhat);
        TextView tvNgayCapNhat = convertView.findViewById(R.id.txt_ngay_capnhat);
        
        // Order status radio buttons
        RadioButton rdDaDatHang = convertView.findViewById(R.id.rd_da_dat_hang);
        RadioButton rdDangVanChuyen = convertView.findViewById(R.id.rd_dang_van_chuyen);
        RadioButton rdVanChuyenThanhCong = convertView.findViewById(R.id.rd_van_chuyen_thanh_cong);
        RadioButton rdHoanHang = convertView.findViewById(R.id.rd_hoan_hang);
        
        // Payment status radio buttons
        RadioButton rdChuaThanhToan = convertView.findViewById(R.id.rd_chua_thanh_toan);
        RadioButton rdDaThanhToan = convertView.findViewById(R.id.rd_da_thanh_toan);
        
        // Payment method radio buttons
        RadioButton rdTienMat = convertView.findViewById(R.id.rd_tien_mat);
        RadioButton rdChuyenKhoan = convertView.findViewById(R.id.rd_chuyen_khoan);
        
        // Single save button
        Button btnLuuTrangThai = convertView.findViewById(R.id.btnLuuTrangThai);

        tvID_dathang.setText(String.valueOf(detail.getId_dathang()));
        tvMaSp.setText(String.valueOf(detail.getMasp()));
        tvTenSp.setText(detail.getTenSanPham());
        tvSoLuong.setText(String.valueOf(detail.getSoLuong()));
        tvDonGia.setText(String.valueOf(detail.getDonGia()));

        // Set payment status text display
        int paymentStatus = detail.getTrangthaithanhtoan();
        if (paymentStatus == DA_THANH_TOAN) {
            tvTrangThaiThanhToan.setText("Đã thanh toán");
            tvTrangThaiThanhToan.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTrangThaiThanhToan.setText("Chưa thanh toán");
            tvTrangThaiThanhToan.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        // Show update date only for admin
        if (isAdmin && detail.getNgaycapnhat() != null && !detail.getNgaycapnhat().isEmpty()) {
            layoutNgayCapNhat.setVisibility(View.VISIBLE);
            tvNgayCapNhat.setText(detail.getNgaycapnhat());
        } else {
            layoutNgayCapNhat.setVisibility(View.GONE);
        }

        // Debug: Log current values
        android.util.Log.d("ChiTietDonHang", "🔍 Setting values - ID: " + detail.getId_dathang() + 
            ", TrangThai: " + detail.getTrangthai() + 
            ", PaymentStatus: " + paymentStatus + ", PaymentMethod: " + detail.getPhuongthucthanhtoan());
        
        // Reset all radio buttons before setting new values
        resetRadioButtons(convertView);
        
        // Force RadioGroup to clear any existing selections first
        RadioGroup rgPaymentMethod = convertView.findViewById(R.id.rg_payment_method);
        RadioGroup rgPaymentStatus = convertView.findViewById(R.id.rg_payment_status);
        RadioGroup rgOrderStatus = convertView.findViewById(R.id.rg_order_status);
        
        if (rgPaymentMethod != null) rgPaymentMethod.clearCheck();
        if (rgPaymentStatus != null) rgPaymentStatus.clearCheck();
        if (rgOrderStatus != null) rgOrderStatus.clearCheck();
        
        // Small delay to ensure UI is ready
        final View finalConvertView = convertView; // Create final reference for lambda
        final int finalTrangThai = detail.getTrangthai(); // Create final reference for lambda
        
        convertView.post(() -> {
            // Set order status (ensure we handle all cases including 0)
            int trangThai = finalTrangThai;
            switch (trangThai) {
                case 0: // DA_DAT_HANG
                    rdDaDatHang.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "✅ Set rdDaDatHang checked for order " + detail.getId_dathang());
                    break;
                case 1: // DANG_VAN_CHUYEN
                    rdDangVanChuyen.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "✅ Set rdDangVanChuyen checked for order " + detail.getId_dathang());
                    break;
                case 2: // VAN_CHUYEN_THANH_CONG
                    rdVanChuyenThanhCong.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "✅ Set rdVanChuyenThanhCong checked for order " + detail.getId_dathang());
                    break;
                case 3: // HOAN_HANG
                    rdHoanHang.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "✅ Set rdHoanHang checked for order " + detail.getId_dathang());
                    break;
                default:
                    rdDaDatHang.setChecked(true); // Default to first option
                    android.util.Log.d("ChiTietDonHang", "✅ Set rdDaDatHang checked (default) for order " + detail.getId_dathang());
                    break;
            }
            
            // Apply status progression logic if admin
            if (isAdmin) {
                // Apply initial disable logic based on current status
                updateOrderStatusButtons(finalConvertView, trangThai);
                addDisabledButtonTooltips(finalConvertView, trangThai);
            } else {
                // For users, all order status buttons should be disabled
                rdDaDatHang.setEnabled(false);
                rdDangVanChuyen.setEnabled(false);
                rdVanChuyenThanhCong.setEnabled(false);
                rdHoanHang.setEnabled(false);
            }
            
            // Set payment status radio buttons (always from object data to ensure sync)
            int currentPaymentStatus = detail.getTrangthaithanhtoan();
            
            // Reset all first to avoid conflicts
            rdChuaThanhToan.setChecked(false);
            rdDaThanhToan.setChecked(false);
            
            switch (currentPaymentStatus) {
                case 0: // CHUA_THANH_TOAN
                    rdChuaThanhToan.setChecked(true);
                    break;
                case 1: // DA_THANH_TOAN
                    rdDaThanhToan.setChecked(true);
                    break;
                default:
                    rdChuaThanhToan.setChecked(true); // Default to first option
                    break;
            }
            
            // Set payment method (ensure we handle all cases including 0)
            int paymentMethod = detail.getPhuongthucthanhtoan();
            switch (paymentMethod) {
                case 0: // TIEN_MAT
                    rdTienMat.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "Set rdTienMat checked");
                    break;
                case 1: // CHUYEN_KHOAN
                    rdChuyenKhoan.setChecked(true);
                    android.util.Log.d("ChiTietDonHang", "Set rdChuyenKhoan checked");
                    break;
                default:
                    rdTienMat.setChecked(true); // Default to first option
                    android.util.Log.d("ChiTietDonHang", "Set rdTienMat checked (default)");
                    break;
            }
        });

        // Now set up listeners AFTER setting the checked states
        // This ensures listeners work properly regardless of checked state
        
        // Set permissions based on user type
        if (!isAdmin) {
            // User permissions: can only edit payment method
            rdDaDatHang.setEnabled(false);
            rdDangVanChuyen.setEnabled(false);
            rdVanChuyenThanhCong.setEnabled(false);
            rdHoanHang.setEnabled(false);
            
            rdChuaThanhToan.setEnabled(false);
            rdDaThanhToan.setEnabled(false);
            
            // Kiểm tra trạng thái thanh toán để quyết định enable/disable
            boolean isAlreadyPaid = (detail.getTrangthaithanhtoan() == DA_THANH_TOAN);
            
            // Kiểm tra xem user đã xác nhận thanh toán chưa (từ SharedPreferences)
            SharedPreferences userConfirmPrefs = getContext().getSharedPreferences("UserPaymentConfirm", Context.MODE_PRIVATE);
            boolean userHasConfirmed = userConfirmPrefs.getBoolean("confirmed_" + detail.getId_dathang(), false);
            
            if (isAlreadyPaid) {
                // Nếu admin đã xác nhận thanh toán: disable tất cả và hiển thị "Đã thanh toán"
                rdTienMat.setEnabled(false);
                rdChuyenKhoan.setEnabled(false);
                btnLuuTrangThai.setEnabled(false);
                btnLuuTrangThai.setText("Đã thanh toán");
            } else if (userHasConfirmed) {
                // Nếu user đã xác nhận nhưng admin chưa duyệt: disable UI và hiển thị "Chờ xác nhận"
                rdTienMat.setEnabled(false);
                rdChuyenKhoan.setEnabled(false);
                btnLuuTrangThai.setEnabled(false);
                btnLuuTrangThai.setText("Chờ xác nhận");
            } else {
                // Nếu user chưa xác nhận: cho phép thay đổi phương thức thanh toán
                rdTienMat.setEnabled(true);
                rdChuyenKhoan.setEnabled(true);
                btnLuuTrangThai.setEnabled(true);
                btnLuuTrangThai.setText("Xác nhận thanh toán");
            }
            
            // Set up payment method change listeners for users
            rdTienMat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !isAlreadyPaid && !userHasConfirmed) detail.setPhuongthucthanhtoan(TIEN_MAT);
            });
            rdChuyenKhoan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !isAlreadyPaid && !userHasConfirmed) detail.setPhuongthucthanhtoan(CHUYEN_KHOAN);
            });
            
            // Set up save button for payment method
            btnLuuTrangThai.setOnClickListener(view -> {
                if (isAlreadyPaid) {
                    Toast.makeText(getContext(), "Đơn hàng này đã được thanh toán!", Toast.LENGTH_SHORT).show();
                } else if (userHasConfirmed) {
                    Toast.makeText(getContext(), "Đang chờ admin xác nhận thanh toán!", Toast.LENGTH_SHORT).show();
                } else {
                    // User chưa xác nhận - cho phép xác nhận
                    // Disable ngay lập tức để tránh user bấm nhiều lần
                    btnLuuTrangThai.setEnabled(false);
                    rdTienMat.setEnabled(false);
                    rdChuyenKhoan.setEnabled(false);
                    btnLuuTrangThai.setText("Đang xử lý...");
                    
                    // Xác nhận thanh toán: chỉ cập nhật phương thức thanh toán
                    savePaymentMethodForUser(detail, btnLuuTrangThai, rdTienMat, rdChuyenKhoan, tvTrangThaiThanhToan);
                }
            });
        } else {
            // Admin permissions: can edit order status and payment status, but not payment method
            // All buttons enabled initially - disable logic only applied after saving
            rdDaDatHang.setEnabled(true);
            rdDangVanChuyen.setEnabled(true);
            rdVanChuyenThanhCong.setEnabled(true);
            rdHoanHang.setEnabled(true);
            
            // Kiểm tra trạng thái thanh toán để quyết định enable/disable payment status buttons
            boolean isAlreadyPaid = (detail.getTrangthaithanhtoan() == DA_THANH_TOAN);
            
            if (isAlreadyPaid) {
                // Nếu đã thanh toán: disable nút "Chưa thanh toán" 
                rdChuaThanhToan.setEnabled(false);
                rdDaThanhToan.setEnabled(true); // Vẫn có thể checked nhưng không thay đổi được
                
                // DON'T auto-change payment method - just display current value from database
                // Removing auto-change logic to prevent conflicts with realtime updates
                // Let the radio buttons show the actual value from detail.getPhuongthucthanhtoan()
                
                btnLuuTrangThai.setText("Cập nhật trạng thái");
            } else {
                // Nếu chưa thanh toán: cho phép thay đổi cả hai trạng thái
                rdChuaThanhToan.setEnabled(true);
                rdDaThanhToan.setEnabled(true);
                btnLuuTrangThai.setText("Lưu trạng thái");
            }
            
            rdTienMat.setEnabled(false);
            rdChuyenKhoan.setEnabled(false);

            // Set up order status change listeners for admin
            final View finalConvertViewForListeners = convertView; // Final reference for listeners
            
            rdDaDatHang.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    detail.setTrangthai(DA_DAT_HANG);
                    // Don't disable buttons here - only after saving
                }
            });
            rdDangVanChuyen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    detail.setTrangthai(DANG_VAN_CHUYEN);
                    // Don't disable buttons here - only after saving
                }
            });
            rdVanChuyenThanhCong.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    detail.setTrangthai(VAN_CHUYEN_THANH_CONG);
                    // Don't disable buttons here - only after saving
                }
            });
            rdHoanHang.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    detail.setTrangthai(HOAN_HANG);
                    // Don't disable buttons here - only after saving
                }
            });
            
            // Set up payment status change listeners for admin
            rdChuaThanhToan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !isAlreadyPaid) {
                    detail.setTrangthaithanhtoan(CHUA_THANH_TOAN);
                    updatePaymentStatusText(tvTrangThaiThanhToan, detail);
                } else if (isChecked && isAlreadyPaid) {
                    // Không cho phép chuyển từ "Đã thanh toán" về "Chưa thanh toán"
                    rdDaThanhToan.setChecked(true);
                    rdChuaThanhToan.setChecked(false);
                    Toast.makeText(getContext(), "Không thể hủy thanh toán đơn hàng đã thanh toán!", Toast.LENGTH_SHORT).show();
                }
            });
            rdDaThanhToan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    detail.setTrangthaithanhtoan(DA_THANH_TOAN);
                    updatePaymentStatusText(tvTrangThaiThanhToan, detail);
                    // Khi chuyển sang "Đã thanh toán", disable nút "Chưa thanh toán"
                    rdChuaThanhToan.setEnabled(false);
                }
            });

            // Set up save button for admin (saves both order status and payment status)
            btnLuuTrangThai.setOnClickListener(view -> {
                saveOrderStatus(detail, finalConvertViewForListeners);
                savePaymentStatus(detail);
            });
        }

        if (detail.getAnh() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(detail.getAnh(), 0, detail.getAnh().length);
            ivAnh.setImageBitmap(bitmap);
        } else {
            loadProductImage(detail.getMasp(), ivAnh);
        }

        return convertView;
    }

    private void resetRadioButtons(View convertView) {
        // Order status radio buttons
        RadioButton rdDaDatHang = convertView.findViewById(R.id.rd_da_dat_hang);
        RadioButton rdDangVanChuyen = convertView.findViewById(R.id.rd_dang_van_chuyen);
        RadioButton rdVanChuyenThanhCong = convertView.findViewById(R.id.rd_van_chuyen_thanh_cong);
        RadioButton rdHoanHang = convertView.findViewById(R.id.rd_hoan_hang);
        
        // Payment status radio buttons
        RadioButton rdChuaThanhToan = convertView.findViewById(R.id.rd_chua_thanh_toan);
        RadioButton rdDaThanhToan = convertView.findViewById(R.id.rd_da_thanh_toan);
        
        // Payment method radio buttons
        RadioButton rdTienMat = convertView.findViewById(R.id.rd_tien_mat);
        RadioButton rdChuyenKhoan = convertView.findViewById(R.id.rd_chuyen_khoan);
        
        // Clear listeners temporarily to avoid triggering events during reset
        rdDaDatHang.setOnCheckedChangeListener(null);
        rdDangVanChuyen.setOnCheckedChangeListener(null);
        rdVanChuyenThanhCong.setOnCheckedChangeListener(null);
        rdHoanHang.setOnCheckedChangeListener(null);
        rdChuaThanhToan.setOnCheckedChangeListener(null);
        rdDaThanhToan.setOnCheckedChangeListener(null);
        rdTienMat.setOnCheckedChangeListener(null);
        rdChuyenKhoan.setOnCheckedChangeListener(null);
        
        // Reset order status
        rdDaDatHang.setChecked(false);
        rdDangVanChuyen.setChecked(false);
        rdVanChuyenThanhCong.setChecked(false);
        rdHoanHang.setChecked(false);
        
        // Reset payment status
        rdChuaThanhToan.setChecked(false);
        rdDaThanhToan.setChecked(false);
        
        // Reset payment method
        rdTienMat.setChecked(false);
        rdChuyenKhoan.setChecked(false);
        
        android.util.Log.d("ChiTietDonHang", "Reset all RadioButtons to unchecked");
    }

    private void saveOrderStatus(ChiTietDonHang detail, View convertView) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("trangthai", detail.getTrangthai());

            ApiCaller.callApi("dathang/" + detail.getId_dathang(), "PUT", updateData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ " + jsonResponse.optString("msg", "Cập nhật trạng thái đơn hàng thành công"), Toast.LENGTH_SHORT).show();
                                
                                // Apply disable logic ONLY after successful save
                                updateOrderStatusButtons(convertView, detail.getTrangthai());
                                addDisabledButtonTooltips(convertView, detail.getTrangthai());
                                
                                // Lưu trạng thái mới để nút back có thể sử dụng
                                if (getContext() instanceof Activity) {
                                    Activity activity = (Activity) getContext();
                                    SharedPreferences prefs = activity.getSharedPreferences("AdminOrderStatus", Context.MODE_PRIVATE);
                                    prefs.edit().putInt("lastOrderStatus", detail.getTrangthai()).apply();
                                }
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật trạng thái đơn hàng thất bại"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePaymentStatus(ChiTietDonHang detail) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("trangthaithanhtoan", detail.getTrangthaithanhtoan());

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment", "PUT", updateData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ " + jsonResponse.optString("msg", "Cập nhật trạng thái thanh toán thành công"), Toast.LENGTH_SHORT).show();
                                
                                // Nếu admin xác nhận thanh toán, xóa trạng thái "user đã xác nhận" để hiển thị "Đã thanh toán"
                                if (detail.getTrangthaithanhtoan() == DA_THANH_TOAN) {
                                    SharedPreferences userConfirmPrefs = getContext().getSharedPreferences("UserPaymentConfirm", Context.MODE_PRIVATE);
                                    userConfirmPrefs.edit().remove("confirmed_" + detail.getId_dathang()).apply();
                                }
                                
                                // Quay lại danh sách đơn hàng với trạng thái phù hợp (cho user)
                                if (getContext() instanceof Activity && !isAdmin) {
                                    navigateBackToUserOrderList(detail);
                                } else {
                                    // Admin - không redirect nữa, để admin ở lại xem real-time update
                                    notifyDataSetChanged();
                                }
                                
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật trạng thái thanh toán thất bại"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePaymentMethod(ChiTietDonHang detail) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("phuongthucthanhtoan", detail.getPhuongthucthanhtoan());

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment-method", "PUT", updateData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ " + jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thành công"), Toast.LENGTH_SHORT).show();
                                
                                // Thoát khỏi menu và quay lại danh sách đơn hàng
                                if (getContext() instanceof Activity) {
                                    ((Activity) getContext()).finish();
                                }
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thất bại"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductImage(int masp, ImageView imageView) {
        if (imageCache.containsKey(masp)) {
            byte[] imageData = imageCache.get(masp);
            if (imageData != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.vest); // Fallback image
            }
            return;
        }

        ApiCaller.callApi("sanpham/" + masp, "GET", null, null, response -> {
            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("data")) {
                        JSONArray dataArray = jsonResponse.getJSONArray("data");
                        if (dataArray.length() > 0) {
                            JSONObject productObject = dataArray.getJSONObject(0);
                            if (productObject.has("anh") && !productObject.isNull("anh")) {
                                JSONObject anhObject = productObject.getJSONObject("anh");
                                if (anhObject.has("data") && !anhObject.isNull("data")) {
                                    String anhBase64 = anhObject.getString("data");
                                    byte[] decodedString = Base64.decode(anhBase64, Base64.DEFAULT);
                                    
                                    imageCache.put(masp, decodedString);

                                    ((Activity) getContext()).runOnUiThread(() -> {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        imageView.setImageBitmap(bitmap);
                                    });
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Activity) getContext()).runOnUiThread(() -> {
                        imageView.setImageResource(R.drawable.vest);
                    });
                }
            } else {
                ((Activity) getContext()).runOnUiThread(() -> {
                    imageView.setImageResource(R.drawable.vest);
                });
            }
        });
    }
    
    private void updatePaymentStatusText(TextView tvTrangThaiThanhToan, ChiTietDonHang detail) {
        int paymentStatus = detail.getTrangthaithanhtoan();
        if (paymentStatus == DA_THANH_TOAN) {
            tvTrangThaiThanhToan.setText("Đã thanh toán");
            tvTrangThaiThanhToan.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvTrangThaiThanhToan.setText("Chưa thanh toán");
            tvTrangThaiThanhToan.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    /**
     * Method to update order status radio buttons for realtime updates
     * This ensures the correct radio button is selected when status changes via realtime
     */
    public void updateOrderStatusForRealtimeUpdate(int newStatus) {
        // This method can be called from the activity when receiving realtime updates
        // to ensure radio buttons reflect the current status correctly
        android.util.Log.d("ChiTietDonHang", "📋 Updating order status for realtime: " + newStatus);
        
        // Force refresh all views to update radio button states and enable/disable logic
        notifyDataSetChanged(); 
    }

    /**
     * Cập nhật trạng thái của các radio button theo logic tiến trình đơn hàng
     * Logic mới:
     * - Đã đặt hàng (0): chỉ cho phép chọn Đã đặt hàng hoặc Đang vận chuyển
     * - Đang vận chuyển (1): chỉ cho phép chọn Đang vận chuyển hoặc Vận chuyển thành công  
     * - Vận chuyển thành công (2): chỉ cho phép chọn Vận chuyển thành công hoặc Hoàn hàng
     * - Hoàn hàng (3): tất cả các tùy chọn khác đều disable
     */
    private void updateOrderStatusButtons(View convertView, int currentStatus) {
        RadioButton rdDaDatHang = convertView.findViewById(R.id.rd_da_dat_hang);
        RadioButton rdDangVanChuyen = convertView.findViewById(R.id.rd_dang_van_chuyen);
        RadioButton rdVanChuyenThanhCong = convertView.findViewById(R.id.rd_van_chuyen_thanh_cong);
        RadioButton rdHoanHang = convertView.findViewById(R.id.rd_hoan_hang);
        
        // Logic mới theo yêu cầu
        switch (currentStatus) {
            case DA_DAT_HANG: // Trạng thái 0 - Đã đặt hàng
                rdDaDatHang.setEnabled(true);           // Cho phép giữ nguyên
                rdDangVanChuyen.setEnabled(true);       // Cho phép chuyển tiếp
                rdVanChuyenThanhCong.setEnabled(false); // Disable
                rdHoanHang.setEnabled(false);           // Disable
                break;
                
            case DANG_VAN_CHUYEN: // Trạng thái 1 - Đang vận chuyển
                rdDaDatHang.setEnabled(false);          // Disable
                rdDangVanChuyen.setEnabled(true);       // Cho phép giữ nguyên
                rdVanChuyenThanhCong.setEnabled(true);  // Cho phép chuyển tiếp
                rdHoanHang.setEnabled(false);           // Disable
                break;
                
            case VAN_CHUYEN_THANH_CONG: // Trạng thái 2 - Vận chuyển thành công
                rdDaDatHang.setEnabled(false);          // Disable
                rdDangVanChuyen.setEnabled(false);      // Disable
                rdVanChuyenThanhCong.setEnabled(true);  // Cho phép giữ nguyên
                rdHoanHang.setEnabled(true);            // Cho phép chuyển sang hoàn hàng
                break;
                
            case HOAN_HANG: // Trạng thái 3 - Hoàn hàng
                rdDaDatHang.setEnabled(false);          // Disable
                rdDangVanChuyen.setEnabled(false);      // Disable  
                rdVanChuyenThanhCong.setEnabled(false); // Disable
                rdHoanHang.setEnabled(true);            // Chỉ cho phép hiển thị trạng thái hiện tại
                break;
                
            default:
                // Fallback cho trường hợp không xác định
                rdDaDatHang.setEnabled(true);
                rdDangVanChuyen.setEnabled(true);
                rdVanChuyenThanhCong.setEnabled(true);
                rdHoanHang.setEnabled(true);
                break;
        }
        
        // Log để debug
        android.util.Log.d("ChiTietDonHang", "Updated button states for status " + currentStatus + 
            " - DaDatHang: " + rdDaDatHang.isEnabled() + 
            ", DangVanChuyen: " + rdDangVanChuyen.isEnabled() + 
            ", VanChuyenThanhCong: " + rdVanChuyenThanhCong.isEnabled() + 
            ", HoanHang: " + rdHoanHang.isEnabled());
    }

    /**
     * Thêm tooltip cho các nút bị disable để giải thích cho admin
     * Cập nhật theo logic mới của trạng thái đơn hàng
     */
    private void addDisabledButtonTooltips(View convertView, int currentStatus) {
        RadioButton rdDaDatHang = convertView.findViewById(R.id.rd_da_dat_hang);
        RadioButton rdDangVanChuyen = convertView.findViewById(R.id.rd_dang_van_chuyen);
        RadioButton rdVanChuyenThanhCong = convertView.findViewById(R.id.rd_van_chuyen_thanh_cong);
        RadioButton rdHoanHang = convertView.findViewById(R.id.rd_hoan_hang);
        
        // Xử lý tooltip cho từng trạng thái
        switch (currentStatus) {
            case DA_DAT_HANG: // Trạng thái 0 - Đã đặt hàng
                // Vận chuyển thành công và Hoàn hàng bị disable
                rdVanChuyenThanhCong.setOnClickListener(v -> {
                    if (!rdVanChuyenThanhCong.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Phải chuyển qua 'Đang vận chuyển' trước khi 'Vận chuyển thành công'", Toast.LENGTH_SHORT).show();
                    }
                });
                rdHoanHang.setOnClickListener(v -> {
                    if (!rdHoanHang.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Từ trạng thái 'Đã đặt hàng' không thể chuyển trực tiếp sang 'Hoàn hàng'", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
                
            case DANG_VAN_CHUYEN: // Trạng thái 1 - Đang vận chuyển
                // Đã đặt hàng và Hoàn hàng bị disable
                rdDaDatHang.setOnClickListener(v -> {
                    if (!rdDaDatHang.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Không thể quay lại 'Đã đặt hàng' khi đã 'Đang vận chuyển'", Toast.LENGTH_SHORT).show();
                    }
                });
                rdHoanHang.setOnClickListener(v -> {
                    if (!rdHoanHang.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Từ trạng thái 'Đang vận chuyển' không thể chuyển trực tiếp sang 'Hoàn hàng'", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
                
            case VAN_CHUYEN_THANH_CONG: // Trạng thái 2 - Vận chuyển thành công
                // Đã đặt hàng và Đang vận chuyển bị disable
                rdDaDatHang.setOnClickListener(v -> {
                    if (!rdDaDatHang.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Không thể quay lại 'Đã đặt hàng' khi đã 'Vận chuyển thành công'", Toast.LENGTH_SHORT).show();
                    }
                });
                rdDangVanChuyen.setOnClickListener(v -> {
                    if (!rdDangVanChuyen.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Không thể quay lại 'Đang vận chuyển' khi đã 'Vận chuyển thành công'", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
                
            case HOAN_HANG: // Trạng thái 3 - Hoàn hàng
                // Tất cả các trạng thái khác bị disable
                rdDaDatHang.setOnClickListener(v -> {
                    if (!rdDaDatHang.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Đơn hàng đã 'Hoàn hàng', không thể thay đổi trạng thái", Toast.LENGTH_SHORT).show();
                    }
                });
                rdDangVanChuyen.setOnClickListener(v -> {
                    if (!rdDangVanChuyen.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Đơn hàng đã 'Hoàn hàng', không thể thay đổi trạng thái", Toast.LENGTH_SHORT).show();
                    }
                });
                rdVanChuyenThanhCong.setOnClickListener(v -> {
                    if (!rdVanChuyenThanhCong.isEnabled()) {
                        Toast.makeText(getContext(), "⚠️ Đơn hàng đã 'Hoàn hàng', không thể thay đổi trạng thái", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void savePaymentMethodForUser(ChiTietDonHang detail, Button btnLuuTrangThai, RadioButton rdTienMat, RadioButton rdChuyenKhoan, TextView tvTrangThaiThanhToan) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("phuongthucthanhtoan", detail.getPhuongthucthanhtoan());

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment-method", "PUT", updateData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ Đã xác nhận thanh toán. Chờ admin duyệt.", Toast.LENGTH_SHORT).show();
                                
                                // Lưu trạng thái user đã xác nhận thanh toán
                                SharedPreferences userConfirmPrefs = getContext().getSharedPreferences("UserPaymentConfirm", Context.MODE_PRIVATE);
                                userConfirmPrefs.edit().putBoolean("confirmed_" + detail.getId_dathang(), true).apply();
                                
                                // Lưu trạng thái đơn hàng để nút back có thể sử dụng
                                if (getContext() instanceof Activity) {
                                    Activity activity = (Activity) getContext();
                                    SharedPreferences prefs = activity.getSharedPreferences("UserOrderStatus", Context.MODE_PRIVATE);
                                    prefs.edit().putInt("lastOrderStatus", detail.getTrangthai()).apply();
                                }
                                
                                // Disable phương thức thanh toán và nút ngay sau khi user xác nhận
                                rdTienMat.setEnabled(false);
                                rdChuyenKhoan.setEnabled(false);
                                btnLuuTrangThai.setEnabled(false);
                                btnLuuTrangThai.setText("Chờ xác nhận");
                                
                                // KHÔNG thay đổi trạng thái thanh toán - chỉ admin mới được làm điều này
                                // Giữ nguyên text hiển thị "Chưa thanh toán" cho đến khi admin xác nhận
                                
                                // Delay một chút rồi thoát ra danh sách đơn hàng ở tab đúng trạng thái
                                btnLuuTrangThai.postDelayed(() -> {
                                    if (getContext() instanceof Activity) {
                                        navigateBackToUserOrderList(detail);
                                    }
                                }, 1500); // Delay 1.5 giây để user có thể đọc thông báo
                                
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thất bại"), Toast.LENGTH_SHORT).show();
                                // Nếu thất bại, enable lại các controls
                                rdTienMat.setEnabled(true);
                                rdChuyenKhoan.setEnabled(true);
                                btnLuuTrangThai.setEnabled(true);
                                btnLuuTrangThai.setText("Xác nhận thanh toán");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                            // Nếu lỗi, enable lại các controls
                            rdTienMat.setEnabled(true);
                            rdChuyenKhoan.setEnabled(true);
                            btnLuuTrangThai.setEnabled(true);
                            btnLuuTrangThai.setText("Xác nhận thanh toán");
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                        // Nếu lỗi, enable lại các controls
                        rdTienMat.setEnabled(true);
                        rdChuyenKhoan.setEnabled(true);
                        btnLuuTrangThai.setEnabled(true);
                        btnLuuTrangThai.setText("Xác nhận thanh toán");
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
            // Nếu lỗi, enable lại các controls
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setEnabled(true);
            btnLuuTrangThai.setText("Xác nhận thanh toán");
        }
    }

    // Method mới CHỈ cập nhật phương thức thanh toán (không thay đổi trạng thái thanh toán)
    private void updatePaymentMethodOnly(ChiTietDonHang detail, Button btnLuuTrangThai, RadioButton rdTienMat, RadioButton rdChuyenKhoan, TextView tvTrangThaiThanhToan) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            // Reset UI nếu lỗi
            btnLuuTrangThai.setEnabled(true);
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setText("Lưu phương thức");
            return;
        }

        try {
            // CHỈ cập nhật phương thức thanh toán
            JSONObject updatePaymentMethodData = new JSONObject();
            updatePaymentMethodData.put("phuongthucthanhtoan", detail.getPhuongthucthanhtoan());

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment-method", "PUT", updatePaymentMethodData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    android.util.Log.d("ChiTietDonHang", "💳 Payment method ONLY API response: " + response);
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✅ Cập nhật phương thức thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                android.util.Log.d("ChiTietDonHang", "✅ Payment method updated successfully - no status change");
                                
                                // Reset UI về trạng thái bình thường để user có thể tiếp tục chỉnh sửa
                                btnLuuTrangThai.setEnabled(true);
                                rdTienMat.setEnabled(true);
                                rdChuyenKhoan.setEnabled(true);
                                btnLuuTrangThai.setText("Lưu phương thức");
                                
                                // Delay một chút rồi quay lại danh sách đơn hàng user
                                btnLuuTrangThai.postDelayed(() -> {
                                    if (getContext() instanceof Activity) {
                                        navigateBackToUserOrderList(detail);
                                    }
                                }, 1500); // Delay 1.5 giây để user có thể đọc thông báo
                                
                            } else {
                                String errorMsg = jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thất bại");
                                android.util.Log.e("ChiTietDonHang", "❌ Payment method update failed: " + errorMsg);
                                
                                // Xử lý lỗi cụ thể cho user - payment method
                                if (errorMsg.toLowerCase().contains("not authorized") || 
                                    errorMsg.toLowerCase().contains("admin") ||
                                    errorMsg.toLowerCase().contains("unauthorized")) {
                                    Toast.makeText(getContext(), "✗ Lỗi phân quyền khi cập nhật phương thức thanh toán. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "✗ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }
                                
                                // Reset UI nếu lỗi
                                btnLuuTrangThai.setEnabled(true);
                                rdTienMat.setEnabled(true);
                                rdChuyenKhoan.setEnabled(true);
                                btnLuuTrangThai.setText("Lưu phương thức");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                            // Reset UI nếu lỗi
                            btnLuuTrangThai.setEnabled(true);
                            rdTienMat.setEnabled(true);
                            rdChuyenKhoan.setEnabled(true);
                            btnLuuTrangThai.setText("Lưu phương thức");
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                        // Reset UI nếu lỗi
                        btnLuuTrangThai.setEnabled(true);
                        rdTienMat.setEnabled(true);
                        rdChuyenKhoan.setEnabled(true);
                        btnLuuTrangThai.setText("Lưu phương thức");
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
            // Reset UI nếu lỗi
            btnLuuTrangThai.setEnabled(true);
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setText("Lưu phương thức");
        }
    }

    // Method mới để xác nhận thanh toán cho user
    private void confirmPaymentForUser(ChiTietDonHang detail, Button btnLuuTrangThai, RadioButton rdTienMat, RadioButton rdChuyenKhoan, TextView tvTrangThaiThanhToan) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            // Reset UI nếu lỗi
            btnLuuTrangThai.setEnabled(true);
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setText("Xác nhận thanh toán");
            return;
        }

        try {
            // Bước 1: Cập nhật phương thức thanh toán
            JSONObject updatePaymentMethodData = new JSONObject();
            updatePaymentMethodData.put("phuongthucthanhtoan", detail.getPhuongthucthanhtoan());

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment-method", "PUT", updatePaymentMethodData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    android.util.Log.d("ChiTietDonHang", "💳 Payment method API response: " + response);
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                // Bước 2: Cập nhật trạng thái thanh toán thành "Đã thanh toán"
                                confirmPaymentStatus(detail, token, btnLuuTrangThai, rdTienMat, rdChuyenKhoan, tvTrangThaiThanhToan);
                            } else {
                                String errorMsg = jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thất bại");
                                android.util.Log.e("ChiTietDonHang", "❌ Payment method update failed: " + errorMsg);
                                
                                // Xử lý lỗi cụ thể cho user - payment method
                                if (errorMsg.toLowerCase().contains("not authorized") || 
                                    errorMsg.toLowerCase().contains("admin") ||
                                    errorMsg.toLowerCase().contains("unauthorized")) {
                                    Toast.makeText(getContext(), "✗ Lỗi phân quyền khi cập nhật phương thức thanh toán. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "✗ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }
                                
                                // Reset UI nếu lỗi
                                btnLuuTrangThai.setEnabled(true);
                                rdTienMat.setEnabled(true);
                                rdChuyenKhoan.setEnabled(true);
                                btnLuuTrangThai.setText("Xác nhận thanh toán");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                            // Reset UI nếu lỗi
                            btnLuuTrangThai.setEnabled(true);
                            rdTienMat.setEnabled(true);
                            rdChuyenKhoan.setEnabled(true);
                            btnLuuTrangThai.setText("Xác nhận thanh toán");
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                        // Reset UI nếu lỗi
                        btnLuuTrangThai.setEnabled(true);
                        rdTienMat.setEnabled(true);
                        rdChuyenKhoan.setEnabled(true);
                        btnLuuTrangThai.setText("Xác nhận thanh toán");
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
            // Reset UI nếu lỗi
            btnLuuTrangThai.setEnabled(true);
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setText("Xác nhận thanh toán");
        }
    }

    // Method để cập nhật trạng thái thanh toán thành "Đã thanh toán"
    private void confirmPaymentStatus(ChiTietDonHang detail, String token, Button btnLuuTrangThai, RadioButton rdTienMat, RadioButton rdChuyenKhoan, TextView tvTrangThaiThanhToan) {
        try {
            JSONObject paymentStatusData = new JSONObject();
            paymentStatusData.put("trangthaithanhtoan", DA_THANH_TOAN);

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment", "PUT", paymentStatusData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    android.util.Log.d("ChiTietDonHang", "💰 Payment status API response: " + response);
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✅ Thanh toán thành công! Phương thức thanh toán và nút xác nhận đã được vô hiệu hóa.", Toast.LENGTH_LONG).show();
                                android.util.Log.d("ChiTietDonHang", "✅ Payment confirmed successfully");
                                
                                // Cập nhật local data
                                detail.setTrangthaithanhtoan(DA_THANH_TOAN);
                                
                                // Cập nhật UI ngay lập tức
                                btnLuuTrangThai.setEnabled(false);
                                rdTienMat.setEnabled(false);
                                rdChuyenKhoan.setEnabled(false);
                                btnLuuTrangThai.setText("Đã thanh toán");
                                
                                // Cập nhật text hiển thị trạng thái thanh toán
                                updatePaymentStatusText(tvTrangThaiThanhToan, detail);
                                
                                // Refresh adapter để đảm bảo tất cả views được cập nhật
                                notifyDataSetChanged();
                                
                                // Chỉ quay lại danh sách đơn hàng nếu là user, admin thì ở lại
                                if (!isAdmin) {
                                    // Delay một chút rồi quay lại danh sách đơn hàng user
                                    btnLuuTrangThai.postDelayed(() -> {
                                        if (getContext() instanceof Activity) {
                                            navigateBackToUserOrderList(detail);
                                        }
                                    }, 2000); // Delay 2 giây để user có thể đọc thông báo
                                }
                                
                            } else {
                                String errorMsg = jsonResponse.optString("msg", "Xác nhận thanh toán thất bại");
                                android.util.Log.e("ChiTietDonHang", "❌ Payment status update failed: " + errorMsg);
                                
                                // Xử lý lỗi cụ thể cho user - payment status  
                                if (errorMsg.toLowerCase().contains("not authorized") || 
                                    errorMsg.toLowerCase().contains("admin") ||
                                    errorMsg.toLowerCase().contains("unauthorized")) {
                                    Toast.makeText(getContext(), "✗ Lỗi phân quyền khi xác nhận thanh toán. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "✗ " + errorMsg, Toast.LENGTH_SHORT).show();
                                }
                                
                                // Reset UI nếu thất bại
                                btnLuuTrangThai.setEnabled(true);
                                rdTienMat.setEnabled(true);
                                rdChuyenKhoan.setEnabled(true);
                                btnLuuTrangThai.setText("Xác nhận thanh toán");
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                            // Reset UI nếu lỗi
                            btnLuuTrangThai.setEnabled(true);
                            rdTienMat.setEnabled(true);
                            rdChuyenKhoan.setEnabled(true);
                            btnLuuTrangThai.setText("Xác nhận thanh toán");
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                        // Reset UI nếu lỗi
                        btnLuuTrangThai.setEnabled(true);
                        rdTienMat.setEnabled(true);
                        rdChuyenKhoan.setEnabled(true);
                        btnLuuTrangThai.setText("Xác nhận thanh toán");
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu thanh toán.", Toast.LENGTH_SHORT).show();
            // Reset UI nếu lỗi
            btnLuuTrangThai.setEnabled(true);
            rdTienMat.setEnabled(true);
            rdChuyenKhoan.setEnabled(true);
            btnLuuTrangThai.setText("Xác nhận thanh toán");
        }
    }

    // Method cũ được giữ lại để tương thích với code khác (chỉ cập nhật phương thức thanh toán)
    private void savePaymentMethodForUserOld(ChiTietDonHang detail) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // CHỈ cập nhật phương thức thanh toán, KHÔNG thay đổi trạng thái thanh toán
            JSONObject updateData = new JSONObject();
            updateData.put("phuongthucthanhtoan", detail.getPhuongthucthanhtoan());

            // KHÔNG tự động set trạng thái thanh toán nữa, để user quyết định
            // detail.setTrangthaithanhtoan(DA_THANH_TOAN); // <- Removed this line

            // Gọi API cập nhật phương thức thanh toán
            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment-method", "PUT", updateData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ " + jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thành công"), Toast.LENGTH_SHORT).show();
                                
                                // Quay lại danh sách đơn hàng user với trạng thái phù hợp
                                if (getContext() instanceof Activity) {
                                    navigateBackToUserOrderList(detail);
                                }
                                
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật phương thức thanh toán thất bại"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu cập nhật.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePaymentStatus(ChiTietDonHang detail, String token) {
        try {
            JSONObject paymentData = new JSONObject();
            paymentData.put("trangthaithanhtoan", DA_THANH_TOAN);

            ApiCaller.callApi("dathang/" + detail.getId_dathang() + "/payment", "PUT", paymentData, token, response -> {
                ((Activity) getContext()).runOnUiThread(() -> {
                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.optBoolean("success", false)) {
                                Toast.makeText(getContext(), "✓ Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                
                                // Refresh adapter để hiển thị thay đổi ngay lập tức
                                notifyDataSetChanged();
                                
                                // Quay lại trang đơn hàng với trạng thái phù hợp cho user
                                if (getContext() instanceof Activity) {
                                    Activity activity = (Activity) getContext();
                                    Intent intent = new Intent(activity, DonHang_User_Activity.class);
                                    intent.putExtra("selectedStatus", detail.getTrangthai());
                                    
                                    // Lấy tenDN từ SharedPreferences để truyền cho User Activity
                                    SharedPreferences sharedPrefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                    String tenDN = sharedPrefs.getString("tendn", "");
                                    intent.putExtra("tendn", tenDN);
                                    
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                            } else {
                                Toast.makeText(getContext(), "✗ " + jsonResponse.optString("msg", "Cập nhật trạng thái thanh toán thất bại"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "✗ Lỗi xử lý phản hồi từ server.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "✗ Lỗi kết nối hoặc không nhận được phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "✗ Lỗi tạo dữ liệu thanh toán.", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức điều hướng quay lại danh sách đơn hàng user với tab đúng
    private void navigateBackToUserOrderList(ChiTietDonHang detail) {
        Activity activity = (Activity) getContext();
        Intent intent = new Intent(activity, DonHang_User_Activity.class);
        
        // Lấy thông tin user từ SharedPreferences
        SharedPreferences sharedPrefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String tendn = sharedPrefs.getString("tendn", "");
        intent.putExtra("tendn", tendn);
        
        // Truyền trạng thái đơn hàng để chuyển đến đúng tab
        // detail.getTrangthai() chứa trạng thái của đơn hàng (0: Đã đặt, 1: Đang vận chuyển, 2: Thành công, 3: Hoàn hàng)
        intent.putExtra("selectedStatus", detail.getTrangthai());
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}