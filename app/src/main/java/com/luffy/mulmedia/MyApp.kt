package com.luffy.mulmedia;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context application;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        application = base;
    }

    public static Context getApplication(){
        return application;
    }
}
