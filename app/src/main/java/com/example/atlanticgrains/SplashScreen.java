package com.example.atlanticgrains;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;
import android.os.Handler;


public class SplashScreen extends AppCompatActivity {
    Animation topAnim,bottomAnim;
    ImageView imageView;
    TextView sloganTitle,txtVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        imageView = findViewById(R.id.imageView);
        sloganTitle = findViewById(R.id.slogan_title);
        txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText("v." + BuildConfig.VERSION_NAME);

        imageView.setAnimation(topAnim);
        sloganTitle.setAnimation(bottomAnim);
        txtVersion.setAnimation(bottomAnim);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, IPAddress.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(r, 3000);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}