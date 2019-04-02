package com.example.drawit;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;

public class NoteViewActivity extends AppCompatActivity {

    private ImageView noteImageView;
    private Toolbar toolbar;
    private SpinKitView loadingIndicator;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseReferance;
    private ImageButton deleteButton;
    private ImageButton shareButton;
    private boolean isImageLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        noteImageView = findViewById(R.id.note_view);
        deleteButton = findViewById(R.id.delete_button);
        shareButton = findViewById(R.id.share_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageLoaded) {
                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(NoteViewActivity.this);
                    builder.setTitle("Delete note?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Delete", (dialog, which) -> {
                        deleteNote();
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    builder.show();
                }
            }
        });

        shareButton.setOnClickListener(v -> {
            final String noteUrl = getIntent().getStringExtra("note_url");
            if (noteUrl != null) {
                shareImage();
            }
        });

        loadingIndicator = findViewById(R.id.spin_kit);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        mStorage = FirebaseStorage.getInstance();
        mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Notes");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);


        String note_url = getIntent().getStringExtra("note_url");
        if (note_url != null) {
            Picasso.get().load(note_url).fit().centerCrop().into(noteImageView, new Callback() {
                @Override
                public void onSuccess() {
                    loadingIndicator.setVisibility(View.GONE);
                    isImageLoaded = true;
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //handle the home button onClick event here.
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteNote() {
        final String noteKey = getIntent().getStringExtra("note_key");
        if (noteKey != null) {
            final String noteName = getIntent().getStringExtra("note_name");
            final String noteUrl = getIntent().getStringExtra("note_url");
            StorageReference storageReference = mStorage.getReferenceFromUrl(noteUrl);
            android.app.AlertDialog loadingDialog;
            loadingDialog = new SpotsDialog.Builder().setContext(this).setTheme(R.style.LoadingDialogTheme).build();
            loadingDialog.setMessage("Deleting note " + noteName);
            loadingDialog.show();
            storageReference.delete().addOnSuccessListener(aVoid -> {
                mDatabaseReferance.child(noteKey).removeValue();
                if (noteName != null) {
                    Toast.makeText(getApplicationContext(), noteName + " deleted successfully", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
                finish();
            }).addOnFailureListener(e -> {
                if (noteName != null) {
                    Toast.makeText(getApplicationContext(), "Unable to delete " + noteName, Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            });
        }
    }

    private void shareImage(){

    }


}

