package com.hiennd325.btl_android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hiennd325.btl_android.R;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private View fabAddTask;

    private TodayFragment todayFragment;
    private CalendarFragment calendarFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabAddTask = findViewById(R.id.fab_add_task);

        todayFragment = new TodayFragment();
        calendarFragment = new CalendarFragment();
        activeFragment = todayFragment;

        // Add both fragments and hide calendar initially
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, calendarFragment, "CALENDAR")
                .hide(calendarFragment)
                .add(R.id.fragment_container, todayFragment, "TODAY")
                .commit();

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_today) {
                if (activeFragment != todayFragment) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(activeFragment)
                            .show(todayFragment)
                            .commit();
                    activeFragment = todayFragment;
                    todayFragment.loadData();
                }
                return true;
            } else if (itemId == R.id.nav_calendar) {
                if (activeFragment != calendarFragment) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(activeFragment)
                            .show(calendarFragment)
                            .commit();
                    activeFragment = calendarFragment;
                    calendarFragment.loadTimeline();
                }
                return true;
            }
            return false;
        });

        // FAB (+) opens Add Task
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeFragment == todayFragment) {
            todayFragment.loadData();
        } else if (activeFragment == calendarFragment) {
            calendarFragment.loadTimeline();
        }
    }
}
