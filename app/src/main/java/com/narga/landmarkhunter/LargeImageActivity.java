package com.narga.landmarkhunter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

//Activity che mostra una versione ingrandita dell' immagine della thumbnail
public class LargeImageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = LargeImageActivity.class.getSimpleName();
    private static final int TAKE_PICTURE_CODE = 1;
    private static final int PICK_IMAGE_CODE = 2;
    private static final int STORAGE_PERMISSIONS = 0;
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance();
    private ImageView imageView;
    private SharedViewModel viewModel;
    private String id;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageView = findViewById(R.id.selected_image);
        Button editButton = findViewById(R.id.edit_picture);

        //Recupero il ViewModel condiviso
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        //Creo il BitmapHandler per caricare immagini downscalate
        //Se nel database era già presente un filepath visualizzo l' immagine
        String path = getIntent().getStringExtra("path");
        id = getIntent().getStringExtra("id");

        //Attendo che la ImageView sia stata caricata
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if(path != null)
                    new BitmapHandlingTask(imageView.getWidth(), imageView.getHeight(), imageView).execute(path);
            }
        });
        editButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //Controllo se ho i permessi per accedere allo storage e nel caso li richiedo
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS);
        } else {
            //Mostro un dialog all' utente per permettergli di scegliere se aprire galleria o fotocamera
            ImagePickerDialogFragment dialog = new ImagePickerDialogFragment();
            dialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == STORAGE_PERMISSIONS) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePickerDialogFragment dialog = new ImagePickerDialogFragment();
                dialog.show(getSupportFragmentManager(), null);
            } else {
                Toast.makeText(this, "I permessi sono necessari per poter aggiungere foto.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK) {
            //L' utente ha scattato una fotografia
            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyPhoto.jpg");
            new BitmapHandlingTask(imageView.getWidth(), imageView.getHeight(), imageView).execute(file.getAbsolutePath());
            viewModel.updatePoi(file.getAbsolutePath(), id);
        } else if(requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
            //L' utente ha selezionato un' immagine dalla galleria
            if(data != null) {
                new BitmapHandlingTask(imageView.getWidth(), imageView.getHeight(), imageView).execute(getPathFromUri(data.getData()));
                viewModel.updatePoi((getPathFromUri(data.getData())), id);
            }
        }
    }

    //Recupera il percorso del file identificato dall' URI
    private String getPathFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        int columnIndex;
        String picturePath;

        cursor.moveToFirst();
        columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }


    //Dialog che da all' utente la possibilità di scegliere se aprire la galleria o la fotocamera
    public static class ImagePickerDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] options = {"Fotocamera", "Galleria", "Annulla"};
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Aggiungi foto");
            builder.setItems(options, (dialog, which) -> {
                switch(which) {
                    case 0:
                        if(!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                            Toast.makeText(requireActivity(), "Il dispositivo non è dotato di fotocamera.", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyPhoto.jpg");
                            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".fileprovider", file);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            requireActivity().startActivityForResult(intent, TAKE_PICTURE_CODE);
                        }
                        break;
                    case 1:
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        requireActivity().startActivityForResult(intent, PICK_IMAGE_CODE);
                        break;
                    case 2:
                        dialog.dismiss();
                }
            });
            return builder.create();
        }

        private String generateFileName() {
            return "JPEG_" + dateFormat.format(new Date());
        }
    }
}