package com.narga.landmarkhunter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//Fragment contenente il punteggio e la lista dei luoghi esplorati
public class VisitedPlacesFragment extends Fragment {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visited_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        scoreText = view.findViewById(R.id.score);
        //Recupero il ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        //viewModel.clearPois();//TODO REMOVE
        //Inizializzo il dataset
        ArrayList<PointOfInterest> items = new ArrayList<>();
        //Ottengo la recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        //Imposto un LayoutManager verticale per la RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lm);
        //Associo un adapter alla RecyclerView
        adapter = new VisitedPlacesAdapter(items);
        recyclerView.setAdapter(adapter);

        //Inizio ad osservare i dati per essere notificato di cambiamenti
        observerSetup();
    }

    //Imposto l' observer per i dati sui luoghi esplorati
    private void observerSetup() {
        viewModel.getAllPois().observe(getViewLifecycleOwner(), new Observer<List<PointOfInterest>>() {
            @Override
            public void onChanged(List<PointOfInterest> items) {
                adapter.setItems(items);
                score = adapter.getItemCount() * 10;
                scoreText.setText(getString(R.string.score_str, score));
            }
        });
    }
}