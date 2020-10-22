package com.narga.landmarkhunter.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.narga.landmarkhunter.ui.MapFragment;
import com.narga.landmarkhunter.ui.VisitedPlacesFragment;

//Adapter per associare Fragment a posizioni del ViewPager2
public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0)
            return new MapFragment();
        else
            return new VisitedPlacesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
