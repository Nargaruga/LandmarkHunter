package com.narga.landmarkhunter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.narga.landmarkhunter.data.PointOfInterest;
import com.narga.landmarkhunter.R;
import com.narga.landmarkhunter.database.PlacesRepository;
import com.narga.landmarkhunter.ui.adapters.VisitedPlacesAdapter;
import com.narga.landmarkhunter.database.SharedViewModel;

import java.util.ArrayList;

//Fragment contenente il punteggio e la lista dei luoghi esplorati
public class VisitedPlacesFragment extends Fragment implements PlacesRepository.DBQueryListener<Integer> {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
    private TextView scoreText; //TextView che visualizza il numero di luoghi visitati
    private int score; //Numero di luoghi visitati
    private VisitedPlacesAdapter adapter;
    private int visibleCount;

    public VisitedPlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visited_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        scoreText = view.findViewById(R.id.score);

        adapter = new VisitedPlacesAdapter();
        //ViewModel per l' interazione con il DB
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getPoisByDate().observe(getViewLifecycleOwner(), pagedList -> adapter.submitList(pagedList));
        viewModel.getCount(this);

        RecyclerView recyclerView = view.findViewById(R.id.places_list);
        LinearLayoutManager lm = new LinearLayoutManager(requireActivity());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onQueryResult(Integer result) {
        scoreText.setText(getResources().getString(R.string.score_str, result));
    }
}