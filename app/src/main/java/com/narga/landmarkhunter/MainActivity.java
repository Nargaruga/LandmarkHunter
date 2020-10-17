package com.narga.landmarkhunter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int MAP_TAB = 0;
    public static final int VISITED_TAB = 1;
    private ViewPager2 pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new FragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle()));
        pager.setUserInputEnabled(false);
        pager.setOffscreenPageLimit(1);

        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            if(position == 0)
                tab.setText("Mappa");
            else
                tab.setText("Luoghi visitati");
        }).attach();
    }

    //Passo al tab specificato
    public void switchTab(int tabIndex){
        pager.setCurrentItem(tabIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}