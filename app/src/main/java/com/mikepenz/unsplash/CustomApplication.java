package com.mikepenz.unsplash;

import android.app.Application;
import android.content.Context;

public class CustomApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
