package com.example.atlanticgrains;

import android.app.Activity;
import android.content.SharedPreferences;

public class utility_class {

    public String getIPAddress(Activity activity){
        SharedPreferences sharedPreferences2 = activity.getSharedPreferences("CONFIG", activity.MODE_PRIVATE);
        return sharedPreferences2.getString("IPAddress", "");
    }
}
