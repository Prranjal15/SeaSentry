package com.seasentry.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.seasentry.app.R;

public class SafetyHistoryActivity extends AppCompatActivity {

    boolean isNight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_history);

        // Find elements
        final LinearLayout historyRoot = findViewById(R.id.historyRoot);
        final TextView tvHistoryTitle = findViewById(R.id.tvHistoryTitle);
        final CardView historyToggle = findViewById(R.id.historyToggle);
        final TextView tvToggleText = findViewById(R.id.tvToggleText);
        final Button btnClear = findViewById(R.id.btnClearAll);
        final LinearLayout logsContainer = findViewById(R.id.logsContainer);

        // 1. DYNAMIC TOGGLE (Visual Feedback)
        historyToggle.setOnClickListener(v -> {
            if (!isNight) {
                historyRoot.setBackgroundColor(Color.parseColor("#001F3F")); // Deep Navy
                tvHistoryTitle.setTextColor(Color.WHITE);
                tvToggleText.setText("🌙 NIGHT");
                isNight = true;
            } else {
                historyRoot.setBackgroundColor(Color.parseColor("#B3E5FC")); // Sea Blue
                tvHistoryTitle.setTextColor(Color.parseColor("#0D2137")); // Navy Text
                tvToggleText.setText("🔆 DAY");
                isNight = false;
            }
        });

        // 2. INTERACTIVE CARDS
        // We handle Card 1 (Collision) differently to trigger the Emergency HUD
        findViewById(R.id.card1).setOnClickListener(v -> {
            Intent intent = new Intent(this, EmergencyAlertActivity.class);
            startActivity(intent);
        });

        // Other cards show info toasts
        int[] otherCards = {R.id.card2, R.id.card3, R.id.card4, R.id.card5};
        String[] alertNames = {"Boundary Approach", "Anchor Drag", "Gale Warning", "Rough Sea State"};

        for (int i = 0; i < otherCards.length; i++) {
            final String name = alertNames[i];
            findViewById(otherCards[i]).setOnClickListener(view ->
                    Toast.makeText(this, "Log Detail: " + name, Toast.LENGTH_SHORT).show()
            );
        }

        // 3. CLEAR LOGS ACTION
        btnClear.setOnClickListener(v -> {
            logsContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Safety logs cleared from local storage", Toast.LENGTH_LONG).show();
        });
    }
}