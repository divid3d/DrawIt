package com.example.drawit;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jgabrielfreitas.core.BlurImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;

public class NoteViewActivity extends AppCompatActivity {

    private BlurImageView noteImageView;
    private Toolbar toolbar;
    private SpinKitView loadingIndicator;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseReferance;
    private LinearLayout butonLayout;
    private ImageButton deleteButton;
    private ImageButton shareButton;
    private boolean isImageLoaded = false;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        getWindow().setEnterTransition(null);

        supportPostponeEnterTransition();


        noteImageView = findViewById(R.id.note_view);
        deleteButton = findViewById(R.id.delete_button);
        shareButton = findViewById(R.id.share_button);
        butonLayout = findViewById(R.id.button_layout);

        deleteButton.setOnClickListener(v -> {
            if (isImageLoaded) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteViewActivity.this);
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


        String noteUrl = getIntent().getStringExtra("note_url");
        String noteName = getIntent().getStringExtra("note_name");

        if(noteName !=null){
            getSupportActionBar().setTitle(noteName);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = getIntent().getStringExtra("note_transition_name");
            if(imageTransitionName != null) {
                noteImageView.setTransitionName(imageTransitionName);
            }
        }

        if (noteUrl != null ) {
            Picasso.get().load(noteUrl).noFade().fit().centerInside().into(noteImageView, new Callback() {
                @Override
                public void onSuccess() {
                    supportStartPostponedEnterTransition();

                    loadingIndicator.setVisibility(View.GONE);
                    isImageLoaded = true;
                }

                @Override
                public void onError(Exception e) {
                    supportStartPostponedEnterTransition();
                }
            });
        }

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // when a scale gesture is detected, use it to resize the image
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            noteImageView.setScaleX(mScaleFactor);
            noteImageView.setScaleY(mScaleFactor);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //handle the home button onClick event here.
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_menu, menu);
        if (getIntent().getBooleanExtra("note_favourite", false)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav_checked_24dp));
        }

        return true;
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

    private void shareImage() {

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

