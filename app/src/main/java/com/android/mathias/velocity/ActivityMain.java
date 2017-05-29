package com.android.mathias.velocity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ActivityMain extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
        showFragment(FragmentCurrent.class);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
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
            default: break;
        }
        if (fragment != null && !item.isChecked()) {
            showFragment(fragment);
            item.setChecked(true);
            setTitle(item.getTitle());
        }
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
