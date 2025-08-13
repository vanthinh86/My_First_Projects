package com.example.appbanquanao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Base Activity với animation slide được tích hợp sẵn
 * Tất cả Activity khác nên extend từ class này để có animation tự động
 */
public class BaseAnimationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply slide in animation khi activity được tạo
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Wrapper cho startActivity với animation tự động
     */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Override onBackPressed để có animation khi quay lại
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Override finish để có animation khi kết thúc activity
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Utility method để start activity với animation tùy chỉnh
     */
    public void startActivityWithCustomAnimation(Intent intent, int enterAnim, int exitAnim) {
        super.startActivity(intent);
        overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * Utility method để finish activity với animation tùy chỉnh
     */
    public void finishWithCustomAnimation(int enterAnim, int exitAnim) {
        super.finish();
        overridePendingTransition(enterAnim, exitAnim);
    }
}
