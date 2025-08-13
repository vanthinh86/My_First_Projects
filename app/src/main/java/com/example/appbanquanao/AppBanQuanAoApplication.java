package com.example.appbanquanao;

import android.app.Application;

public class AppBanQuanAoApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize SocketManager when the app starts
        SocketManager.getInstance().initialize(this);
    }
}
