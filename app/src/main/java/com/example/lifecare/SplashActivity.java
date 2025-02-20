package com.example.lifecare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Use splash layout

        // Find views
        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_text);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Apply animations
        logo.startAnimation(fadeIn);
        title.startAnimation(slideUp);

        // Delay for 3 seconds then start MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, HomePage.class));
                finish(); // Close splash screen
            }
        }, 3000); // 3 seconds delay
    }
}
