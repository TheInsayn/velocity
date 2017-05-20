package com.android.mathias.velocity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        showFragment(FragmentCurrent.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.moveTaskToBack(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_current:
                fragment = FragmentCurrent.class;
                break;
            case R.id.nav_history:
                fragment = FragmentHistory.class;
                break;
            case R.id.nav_routes:
                fragment = FragmentRoutes.class;
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, ActivitySettings.class));
                break;
            default: break;
        }
        if (fragment != null && !item.isChecked()) {
            showFragment(fragment);
            item.setChecked(true);
            setTitle(item.getTitle());
        }
        DrawerLayout drawer = findViewById(R.id.layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(Class fragmentClass) {
        FragmentManager manager = getSupportFragmentManager();
        String backStateName = fragmentClass.getName();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped) {
            FragmentTransaction ft = manager.beginTransaction();
            try {
                ft.replace(R.id.frame_content, (Fragment) fragmentClass.newInstance());
                ft.addToBackStack(backStateName);
                ft.commit();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
