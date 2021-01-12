package com.byagowi.persiancalendar;

import android.app.Application;

import com.byagowi.persiancalendar.util.Utils;
import com.google.firebase.FirebaseApp;


public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.initUtils(getApplicationContext());
        FirebaseApp.initializeApp(this);

    }
}
