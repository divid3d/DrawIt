package com.example.drawit;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NoteViewActivity extends AppCompatActivity {

    private ImageView noteImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        noteImageView = findViewById(R.id.note_view);

        String note_url = getIntent().getStringExtra("note_url");
        if (note_url != null) {
            Toast.makeText(this, "Cos tam jest", Toast.LENGTH_SHORT).show();
            Picasso.get().load(note_url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

        }
    }
}
