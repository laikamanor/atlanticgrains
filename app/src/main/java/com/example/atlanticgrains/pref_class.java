package com.example.atlanticgrains;

import android.app.Activity;
import android.content.SharedPreferences;
import static android.content.Context.MODE_PRIVATE;
class prefs_class {
    public void loggedOut(Activity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("LOGIN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username").apply();
        editor.remove("userid").apply();
        editor.remove("password").apply();
    }

    public void removeToken(Activity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("TOKEN",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token").apply();
    }
}
