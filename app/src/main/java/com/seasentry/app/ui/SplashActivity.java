package com.seasentry.app.ui;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.seasentry.app.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Wait 2 seconds then go to Settings
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, VesselSettingsActivity.class));
            finish();
        }, 2000);
    }
}