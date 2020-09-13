package com.narga.landmarkhunter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int COLUMNS = 5;
    private FloatingActionButton button;
    private ArrayList<String> items;
    private GalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        button = findViewById(R.id.add_photo);

        RecyclerView scrollableImages = findViewById(R.id.scrollable_images);
        //Inizializzo il dataset
        items = new ArrayList<>();
        //Imposto un LayoutManager per la RecyclerView
        GridLayoutManager lm = new GridLayoutManager(this, COLUMNS);
        scrollableImages.setLayoutManager(lm);
        //Imposto un GalleryAdapter per la RecyclerView
        adapter = new GalleryAdapter(items);
        scrollableImages.setAdapter(adapter);
        //Imposto il click listener per il tasto di aggiunta foto
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ImagePickerDialogFragment dialog = new ImagePickerDialogFragment();
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ImagePickerDialogFragment.TAKE_PICTURE_CODE && resultCode == RESULT_OK) {
            if(data != null)
                items.add(data.getDataString());
            adapter.setItems(items);
        } else if(requestCode == ImagePickerDialogFragment.PICK_IMAGE_CODE && resultCode == RESULT_OK) {

        }
    }

    public static class ImagePickerDialogFragment extends DialogFragment {
        static int TAKE_PICTURE_CODE = 0;
        static int PICK_IMAGE_CODE = 1;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] options = {"Fotocamera", "Galleria", "Annulla"};
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Aggiungi foto");
            builder.setItems(options, (dialog, which) -> {
                switch(which) {
                    case 0:
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePicture.setType("image/*");
                        startActivityForResult(takePicture, TAKE_PICTURE_CODE);
                        break;
                    case 1:
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickPhoto.setType("image/*");
                        startActivityForResult(pickPhoto, PICK_IMAGE_CODE);
                        break;
                    case 2:
                        dialog.dismiss();
                }
            });
            return builder.create();
        }
    }
}