package com.example.drawit;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "001";
    private DrawView drawView;
    private Toolbar mToolbar;
    private RecyclerView mColorPicker;
    private ColorAdapter mColorAdapter;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = findViewById(R.id.draw_view);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawView.init(metrics);
        drawView.setDrawingCacheEnabled(true);
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mColorPicker = findViewById(R.id.color_picker);
        mColorPicker.setHasFixedSize(true);
        mColorPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mColorPicker.setItemAnimator(new DefaultItemAnimator());


        mColorAdapter = new ColorAdapter(colorListGenerator(50), new ColorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Color color) {
                drawView.setCurrentColor(color.getColor());
            }
        });
        mColorPicker.setAdapter(mColorAdapter);

        mStorageReference = FirebaseStorage.getInstance().getReference("Notes");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Notes");

        /*String note_url = getIntent().getStringExtra("note_url");
        if (note_url != null) {
            Toast.makeText(this, "Cos tam jest", Toast.LENGTH_SHORT).show();
            mColorPicker.setVisibility(View.GONE);
            drawView.setDrawOn(true);
            Picasso.get().load(note_url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    //??

                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:
                drawView.undo();
                return true;

            case R.id.action_clear:
                drawView.clear();
                return true;

            case R.id.action_save:
                saveImage();
                return true;

            case R.id.action_share:
                shareImage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareImage() {
        /*rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        Bitmap bitmap = Utills.loadBitmapFromView(drawView);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "test.jpeg"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg"));
                        startActivity(Intent.createChooser(share, "Share Image"));
                    } else {

                    }
                });*/

    }

    private void saveImage() {
        showSaveDialog();
    }

    public List<Color> colorListGenerator(int size) {
        List<Color> colorList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            colorList.add(new Color(random.nextInt()));
        }

        return colorList;
    }

    private void showSaveDialog() {
        Bitmap bitmap = Utills.loadBitmapFromView(drawView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter note name");
        builder.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Upload", (dialog, which) -> {
            final int progressMax = 100;

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_delete_forever_24px)
                    .setContentTitle("Upload")
                    .setContentText("Upload in progress")
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setProgress(progressMax, 0, false);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(1, notification.build());

            String noteName = input.getText().toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + ".jpeg");
            mUploadTask = fileReference.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            Note note = new Note(noteName.trim(), millisToDate(taskSnapshot.getMetadata().getCreationTimeMillis()), url);
                            String uploadId = mDatabaseReference.push().getKey();
                            mDatabaseReference.child(uploadId).setValue(note);

                            notification.setContentText("Upload finishied")
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            notificationManagerCompat.notify(1, notification.build());
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        int progress = (int) (((double) taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100);
                        notification.setProgress(100, progress, false);
                        notificationManagerCompat.notify(1, notification.build());
                    });
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String millisToDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date resultDate = new Date(millis);
        return sdf.format(resultDate);
    }

    private void showNotification() {


    }
}