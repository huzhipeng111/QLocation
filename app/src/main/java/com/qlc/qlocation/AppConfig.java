package com.qlc.qlocation;

import androidx.multidex.MultiDexApplication;

public class AppConfig extends MultiDexApplication {
    public static AppConfig instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
