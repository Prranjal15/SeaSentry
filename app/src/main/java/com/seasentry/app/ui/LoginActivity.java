package com.seasentry.app.ui;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.seasentry.app.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We will create activity_login.xml next
        setContentView(R.layout.activity_vessel_settings); // Temporary link to avoid crash
    }
}