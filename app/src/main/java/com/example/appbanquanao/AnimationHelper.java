package com.example.appbanquanao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class AnimationHelper {
    
    /**
     * Bắt đầu activity với animation slide từ trái sang phải
     */
    public static void startActivityWithSlideAnimation(Context context, Intent intent) {
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(
                com.example.appbanquanao.R.anim.slide_in_right, 
                com.example.appbanquanao.R.anim.slide_out_left
            );
        }
    }
    
    /**
     * Kết thúc activity với animation slide ngược lại
     */
    public static void finishActivityWithSlideAnimation(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(
            com.example.appbanquanao.R.anim.slide_in_left, 
            com.example.appbanquanao.R.anim.slide_out_right
        );
    }
    
    /**
     * Override cho back button để có animation
     */
    public static void setupBackAnimation(Activity activity) {
        // Gọi method này trong onBackPressed()
        activity.overridePendingTransition(
            com.example.appbanquanao.R.anim.slide_in_left, 
            com.example.appbanquanao.R.anim.slide_out_right
        );
    }
}
