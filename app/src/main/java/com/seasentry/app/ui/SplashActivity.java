package com.seasentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.seasentry.app.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This MUST match the XML file name
        setContentView(R.layout.activity_splash);

        Button authBtn = findViewById(R.id.btnSplashAuth);

        if (authBtn != null) {
            authBtn.setOnClickListener(v -> {
                // GO TO DASHBOARD (THE MAIN HUB)
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}