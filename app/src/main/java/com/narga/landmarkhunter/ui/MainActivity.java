package com.narga.landmarkhunter.ui;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.narga.landmarkhunter.ui.adapters.FragmentAdapter;
import com.narga.landmarkhunter.R;

//Activity contenente un tablayout
public class MainActivity extends AppCompatActivity {
    public static final int MAP_TAB = 0;
    public static final int VISITED_TAB = 1;
    private ViewPager2 pager;
    private boolean firstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new FragmentAdapter(this.getSupportFragmentManager(), this.getLifecycle()));
        pager.setUserInputEnabled(false);
        //Entrambi i fragment rimarranno caricati in memoria finchè l' applicazione è aperta
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
        if(tabIndex != MAP_TAB && tabIndex != VISITED_TAB)
            return;

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