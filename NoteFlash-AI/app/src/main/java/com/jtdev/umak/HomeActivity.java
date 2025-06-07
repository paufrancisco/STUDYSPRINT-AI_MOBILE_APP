package com.jtdev.umak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.jtdev.umak.Fragments.About;
import com.jtdev.umak.Fragments.CardBrowser;
import com.jtdev.umak.Fragments.Decks;
import com.jtdev.umak.Fragments.PomodoroNotification;
import com.jtdev.umak.Fragments.Statistics;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Decks()).commit();
            navigationView.setCheckedItem(R.id.nav_decks);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;


        if (item.getItemId() == R.id.nav_decks) {
            selectedFragment = new Decks();
        } else if (item.getItemId() == R.id.nav_cardbrowser) {
            selectedFragment = new CardBrowser();
        } else if (item.getItemId() == R.id.nav_statistics) {
            selectedFragment = new Statistics();
        } else if (item.getItemId() == R.id.nav_about) {
            selectedFragment = new About();
        } else if (item.getItemId() == R.id.nav_settings) {
            selectedFragment = new PomodoroNotification();
        } else if (item.getItemId() == R.id.nav_logout) {
            logout();
            return true;
        }

         if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

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


    private void logout() {
         FirebaseAuth.getInstance().signOut();

         Toast.makeText(this, "Logout Successful!", Toast.LENGTH_SHORT).show();

         Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
         startActivity(intent);
         finish();
    }

}
