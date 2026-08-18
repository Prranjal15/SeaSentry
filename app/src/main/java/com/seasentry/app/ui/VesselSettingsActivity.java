package com.seasentry.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.seasentry.app.R;

public class VesselSettingsActivity extends AppCompatActivity {
    boolean isNight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vessel_settings);

        // 1. LINK ELEMENTS
        LinearLayout root = findViewById(R.id.rootLayout);
        TextView title = findViewById(R.id.tvTitle);
        CardView toggle = findViewById(R.id.cardToggle);
        TextView toggleText = findViewById(R.id.tvToggleMode);
        Spinner spinner = findViewById(R.id.spinnerLanguage);
        Button btnSave = findViewById(R.id.btnSave);

        // 2. SETUP DROPDOWN (This fixes the empty list)
        String[] langs = {"English", "Hindi", "Tamil", "Marathi", "Gujarati"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langs);
        if(spinner != null) spinner.setAdapter(adapter);

        // 3. DAY/NIGHT TOGGLE
        if(toggle != null) {
            toggle.setOnClickListener(v -> {
                if (!isNight) {
                    root.setBackgroundColor(Color.parseColor("#001F3F")); // Night
                    title.setTextColor(Color.WHITE);
                    toggleText.setText("🌙 NIGHT");
                    isNight = true;
                } else {
                    root.setBackgroundColor(Color.parseColor("#B3E5FC")); // Day
                    title.setTextColor(Color.parseColor("#0D2137"));
                    toggleText.setText("🔆 DAY");
                    isNight = false;
                }
            });
        }

        // 4. NAVIGATION TO HISTORY (Click the Title to go to next page)
        title.setOnClickListener(v -> {
            startActivity(new Intent(this, SafetyHistoryActivity.class));
        });

        // 5. SAVE BUTTON
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show();
        });
    }
}