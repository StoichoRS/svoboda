package com.example.svoboda;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MenuActivity";
    private DrawerLayout drawerLayout;
    private Fragment currentActiveFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = this.getSupportFragmentManager();
        displayFragmnet(new MapFragment());
    }

    private void displayFragmnet(Fragment fragment)
    {
        if (currentActiveFragment == null ||
                !currentActiveFragment.getClass().toString().equals(fragment.getClass().toString()))
        {
            fragmentManager.beginTransaction().replace(R.id.mainLayout, fragment).commit();
            currentActiveFragment = fragment;
        }
    }

    // Handle navigation view item clicks here.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        /*
            Calls on the fragment manager to display a fragment base on
            the selected menu item
         */
        if (id == R.id.nav_camera)
        {

            displayFragmnet(new CameraFragment());
        }
        else if (id == R.id.nav_gallery)
        {
            displayFragmnet(new GalleryFragment());
        }
        else if (id == R.id.nav_map)
        {
            displayFragmnet(new MapFragment());
        }

        // Close the menu
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
