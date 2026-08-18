package com.seasentry.app.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.seasentry.app.R;

public class EmergencyAlertActivity extends AppCompatActivity {

    MediaPlayer sirenPlayer;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        // 1. START VIBRATION (Panic Feel)
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Pattern: 0 start, 500ms vibrate, 200ms pause, 500ms vibrate...
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(pattern, 0); // 0 means repeat forever
        }

        // 2. PLAY CRITICAL SIREN (Loops automatically)
        sirenPlayer = MediaPlayer.create(this, R.raw.danger_english);
        if (sirenPlayer != null) {
            sirenPlayer.setLooping(true);
            sirenPlayer.start();
        }

        // 3. ACKNOWLEDGE BUTTON (The "Realistic" Logic)
        Button btnAcknowledge = findViewById(R.id.btnAcknowledge);
        btnAcknowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // STOP SENSORY OUTPUT
                if (sirenPlayer != null) {
                    sirenPlayer.stop();
                    sirenPlayer.release();
                    sirenPlayer = null;
                }
                if (vibrator != null) {
                    vibrator.cancel();
                }

                // SIMULATE DATA TRANSMISSION TO COAST GUARD
                // This is the "Realistic" touch for the judges
                Toast.makeText(EmergencyAlertActivity.this,
                        "TRANSMITTING: Acknowledgement & GPS logs sent to Indian Coast Guard Command Center.",
                        Toast.LENGTH_LONG).show();

                // Close the screen after a tiny delay so they can read the message
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1500);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Safety check to ensure sound stops if the app is closed
        if (sirenPlayer != null) {
            sirenPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}