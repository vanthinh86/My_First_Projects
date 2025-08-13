package com.example.appbanquanao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Tạo Handler để chuyển Activity sau 10 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Chuyá»ƒn sang Activity2
                Intent intent = new Intent(MainActivity.this,Login_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish(); // Kết thúc Activity1 nếu không muốn quay lại
            }
        }, 0); // 1000 milliseconds = 1 seconds

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


}