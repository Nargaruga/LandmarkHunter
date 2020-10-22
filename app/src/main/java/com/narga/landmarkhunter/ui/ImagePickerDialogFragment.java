package com.narga.landmarkhunter.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//Dialog che da all' utente la possibilità di scegliere se aprire la galleria o la fotocamera
public class ImagePickerDialogFragment extends DialogFragment {
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); //Formato per le date
    private ActivityResultLauncher<Uri> cameraLauncher; //Launcher per aprire la fotocamera
    private ActivityResultLauncher<Intent> galleryLauncher; //Launcher per aprire la galleria

    public ImagePickerDialogFragment(ActivityResultLauncher<Uri> cameraLauncher, ActivityResultLauncher<Intent> galleryLauncher){
        this.cameraLauncher = cameraLauncher;
        this.galleryLauncher = galleryLauncher;
    }

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
                        //Creo il filepath in cui verrà salvata la foto e lo passo alla LargeImageActivity
                        String filename = generateFileName();
                        File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".fileprovider", file);
                        LargeImageActivity activity = (LargeImageActivity) requireActivity();
                        activity.setFilepath(file.getAbsolutePath());

                        cameraLauncher.launch(uri);
                    }
                    break;
                case 1:
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(intent);
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