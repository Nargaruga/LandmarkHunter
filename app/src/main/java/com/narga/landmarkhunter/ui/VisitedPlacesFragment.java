package com.narga.landmarkhunter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.narga.landmarkhunter.data.PointOfInterest;
import com.narga.landmarkhunter.R;
import com.narga.landmarkhunter.ui.adapters.VisitedPlacesAdapter;
import com.narga.landmarkhunter.database.SharedViewModel;

import java.util.ArrayList;

//Fragment contenente il punteggio e la lista dei luoghi esplorati
public class VisitedPlacesFragment extends Fragment {
    private static final String LOG_TAG = VisitedPlacesAdapter.class.getSimpleName();
    private VisitedPlacesAdapter adapter; //Adapter per associare gli elemento alla RecyclerList
    private SharedViewModel viewModel; //ViewModel per l' interazione con il DB
    private TextView scoreText; //TextView che visualizza il numero di luoghi visitati
    private int score; //Numero di luoghi visitati

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
            score = adapter.getItemCount();
            scoreText.setText(getString(R.string.score_str, score));
        });
    }
}