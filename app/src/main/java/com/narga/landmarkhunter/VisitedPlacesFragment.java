package com.narga.landmarkhunter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//Fragment contenente il punteggio e la lista dei luoghi esplorati
public class VisitedPlacesFragment extends Fragment {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
    private static final int SCORE_INCREMENT = 1;
    private VisitedPlacesAdapter adapter;
    private SharedViewModel viewModel;
    private TextView scoreText;
    private int score;

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
        //Recupero il ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        //Inizializzo il dataset
        ArrayList<PointOfInterest> items = new ArrayList<>();
        //Ottengo la recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        //Associo un adapter alla RecyclerView
        adapter = new VisitedPlacesAdapter(items);
        recyclerView.setAdapter(adapter);
        //Imposto un LayoutManager verticale per la RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lm);

        //Inizio ad osservare i dati per essere notificato di cambiamenti
        observerSetup();
    }

    //Imposto l' observer per i dati sui luoghi esplorati
    private void observerSetup() {
        viewModel.getAllPois().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
            score = adapter.getItemCount() * SCORE_INCREMENT;
            scoreText.setText(getString(R.string.score_str, score));
        });
    }
}