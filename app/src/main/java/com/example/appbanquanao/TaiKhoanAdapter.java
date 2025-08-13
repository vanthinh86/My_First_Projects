package com.example.appbanquanao;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TaiKhoanAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<TaiKhoan> taiKhoanList;

    public TaiKhoanAdapter(Context context, int layout, List<TaiKhoan> taiKhoanList) {
        this.context = context;
        this.layout = layout;
        this.taiKhoanList = taiKhoanList;
    }

    @Override
    public int getCount() {
        return taiKhoanList.size();
    }

    @Override
    public Object getItem(int position) {
        return taiKhoanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View viewtemp;
        if (view == null) {
            viewtemp = View.inflate(viewGroup.getContext(), R.layout.ds_taikhoan, null);
        } else {
            viewtemp = view;
        }

        TaiKhoan tt = taiKhoanList.get(i);
        TextView tendn = viewtemp.findViewById(R.id.tdn1);
        TextView matkhau = viewtemp.findViewById(R.id.mk1);
        TextView quyenhang = viewtemp.findViewById(R.id.quyen1);
        ImageButton sua = viewtemp.findViewById(R.id.imgsua);
        ImageButton xoa = viewtemp.findViewById(R.id.imgxoa);

        tendn.setText(tt.getTdn());
        matkhau.setText(tt.getMk());
        quyenhang.setText(tt.getQuyen());

        sua.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(viewGroup.getContext());
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_sua_tai_khoan, null);
            EditText editTdn = dialogView.findViewById(R.id.tdn);
            EditText editMk = dialogView.findViewById(R.id.mk);
            RadioButton user = dialogView.findViewById(R.id.user);
            RadioButton admin = dialogView.findViewById(R.id.admin);
            TextView togglePasswordVisibility = dialogView.findViewById(R.id.togglePasswordVisibility);

            editTdn.setText(tt.getTdn());
            editMk.setText(tt.getMk());

            // Password visibility toggle for edit dialog
            final boolean[] isPasswordVisible = {false};
            togglePasswordVisibility.setOnClickListener(toggleView -> {
                if (isPasswordVisible[0]) {
                    editMk.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setText("👁");
                    isPasswordVisible[0] = false;
                } else {
                    editMk.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setText("🙈");
                    isPasswordVisible[0] = true;
                }
                editMk.setSelection(editMk.getText().length());
            });

            if ("admin".equals(tt.getQuyen())) {
                admin.setChecked(true);
            } else {
                user.setChecked(true);
            }

            builder.setView(dialogView)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String newTdn = editTdn.getText().toString().trim();
                        String newMk = editMk.getText().toString().trim();
                        String quyen = user.isChecked() ? "user" : "admin";

                        JSONObject putData = new JSONObject();
                        try {
                            putData.put("tendn", newTdn);
                            putData.put("matkhau", newMk);
                            putData.put("quyen", quyen);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(viewGroup.getContext(), "Lỗi tạo dữ liệu JSON", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", "");

                        ApiCaller.callApi("auth/users/" + tt.getTdn(), "PUT", putData, token, response -> {
                            ((Taikhoan_admin_Activity) context).runOnUiThread(() -> {
                                if (response != null) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.has("_id")) {
                                            Toast.makeText(viewGroup.getContext(), "Cập nhật tài khoản thành công!", Toast.LENGTH_LONG).show();
                                            tt.setTdn(newTdn);
                                            tt.setMk(newMk);
                                            tt.setQuyen(quyen);
                                            notifyDataSetChanged();
                                        } else {
                                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Cập nhật thất bại";
                                            Toast.makeText(viewGroup.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(viewGroup.getContext(), "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(viewGroup.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        xoa.setOnClickListener(v -> {
            new AlertDialog.Builder(viewGroup.getContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xóa tài khoản này?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", "");

                        ApiCaller.callApi("auth/users/" + tt.getTdn(), "DELETE", null, token, response -> {
                            ((Taikhoan_admin_Activity) context).runOnUiThread(() -> {
                                if (response != null) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.has("msg") && jsonResponse.getString("msg").equals("TaiKhoan removed")) {
                                            Toast.makeText(viewGroup.getContext(), "Xóa tài khoản thành công", Toast.LENGTH_LONG).show();
                                            taiKhoanList.remove(i);
                                            notifyDataSetChanged();
                                        } else {
                                            String errorMessage = jsonResponse.has("msg") ? jsonResponse.getString("msg") : "Xóa thất bại";
                                            Toast.makeText(viewGroup.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(viewGroup.getContext(), "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(viewGroup.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return viewtemp;
    }
}
