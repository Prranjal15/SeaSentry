package com.seasentry.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.seasentry.app.R;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 3. Hamburger Icon logic
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Floating SOS Button logic
        findViewById(R.id.fabSos).setOnClickListener(v -> {
            // Instant jump to the Red Alert Screen
            startActivity(new Intent(this, EmergencyAlertActivity.class));
            Toast.makeText(this, "EMERGENCY PROTOCOL STARTED", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // PERFORM ACTIONS BASED ON CLICK
        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, VesselSettingsActivity.class));
        }
        else if (id == R.id.nav_history) {
            startActivity(new Intent(this, SafetyHistoryActivity.class));
        }
        else if (id == R.id.nav_sos) {
            // Launches the Critical Red Screen for Demo
            startActivity(new Intent(this, EmergencyAlertActivity.class));
        }
        else if (id == R.id.nav_dashboard) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Close sidebar after clicking
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}