package com.narga.landmarkhunter.ui;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.narga.landmarkhunter.utility.BitmapHandlingTask;
import com.narga.landmarkhunter.R;
import com.narga.landmarkhunter.database.SharedViewModel;
import com.narga.landmarkhunter.utility.QueryHandler;

//Activity che mostra una versione ingrandita dell' immagine della thumbnail
public class LargeImageActivity extends AppCompatActivity implements View.OnClickListener, QueryHandler.AsyncQueryListener {
    private static final int STORAGE_PERMISSIONS = 0;
    private PhotoView photoView;
    private SharedViewModel viewModel;
    private String id;
    private String path;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private AsyncQueryHandler asyncQueryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        photoView = findViewById(R.id.selected_image);
        Button editImage = findViewById(R.id.edit_image);

        asyncQueryHandler = new QueryHandler(getContentResolver(), this);

        //Recupero il ViewModel condiviso
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        //Se nel database era già presente un filepath visualizzo l' immagine
        path = getIntent().getStringExtra("path");
        id = getIntent().getStringExtra("id");

        //Ripristino lo stato salvato, se applicabile
        if(savedInstanceState != null) {
            path = savedInstanceState.getString("path");
            id = savedInstanceState.getString("id");
        }

        //Attendo che la ImageView sia stata caricata
        photoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                photoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                new BitmapHandlingTask(photoView.getWidth(), photoView.getHeight(), photoView).execute(path);
            }
        });
        editImage.setOnClickListener(this);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if(result) {
                        //Mostro la foto scattata e salvo il path nel DB
                        new BitmapHandlingTask(photoView.getWidth(), photoView.getHeight(), photoView).execute(path);
                        viewModel.updatePoiImage(path, id);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getData() != null && result.getData().getData() != null) {
                        //Effetto una query al ContentResolver passandovi l' URI ottenuto
                        asyncQueryHandler.startQuery(0,
                                null,
                                result.getData().getData(),
                                new String[]{MediaStore.Images.Media.DATA},
                                null,
                                null,
                                null);
                    } else {
                        Toast.makeText(this, "Errore durante il caricamento dell' immagine.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void setFilepath(String s) {
        path = s;
    }

    @Override
    public void onClick(View v) {
        //Controllo se ho i permessi per accedere allo storage e nel caso li richiedo
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS);
        } else {
            //Mostro un dialog all' utente per permettergli di scegliere se aprire galleria o fotocamera
            ImagePickerDialogFragment dialog = new ImagePickerDialogFragment(cameraLauncher, galleryLauncher);
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == STORAGE_PERMISSIONS) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePickerDialogFragment dialog = new ImagePickerDialogFragment(cameraLauncher, galleryLauncher);
                dialog.show(getSupportFragmentManager(), null);
            } else {
                Toast.makeText(this, "I permessi sono necessari per poter aggiungere foto.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(cursor != null) {
            //Recupero il primo risultato della query
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(new String[]{MediaStore.Images.Media.DATA}[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            new BitmapHandlingTask(photoView.getWidth(), photoView.getHeight(), photoView).execute(picturePath);
            viewModel.updatePoiImage(picturePath, id);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("id", id);
        savedInstanceState.putString("path", path);
    }
}