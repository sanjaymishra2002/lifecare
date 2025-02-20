package com.example.lifecare;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

        // Full-screen mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Find views
        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_text);

        // Apply gradient to text
        applyGradientToText(title);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Apply animations
        logo.startAnimation(fadeIn);
        title.startAnimation(slideUp);

        // Delay for 3 seconds then start HomePage
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePage.class));
            finish(); // Close splash screen
        }, 3000); // 3 seconds delay
    }

    // Method to apply gradient color to TextView
    private void applyGradientToText(TextView textView) {
        Shader textShader = new LinearGradient(
                0, 0, textView.getPaint().measureText(textView.getText().toString()), textView.getTextSize(),
                new int[]{Color.parseColor("#025c29"), Color.parseColor("#2ab222")}, // Orange to Yellow
                null,
                Shader.TileMode.CLAMP);

        textView.getPaint().setShader(textShader);
    }
}
