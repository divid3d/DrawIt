package com.example.drawit;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dmax.dialog.SpotsDialog;


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
        mToolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(mToolbar);
        setAppBarTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        mColorPicker = findViewById(R.id.color_picker);
        mColorPicker.setHasFixedSize(true);
        mColorPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mColorPicker.setItemAnimator(new DefaultItemAnimator());


        List<Color> colors = colorListGenerator(50);
        mColorAdapter = new ColorAdapter(colors, new ColorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Color color) {
                drawView.setCurrentColor(color.getColor());
            }
        });
        mColorPicker.setAdapter(mColorAdapter);
        drawView.setCurrentColor(colors.get(0).getColor());

        mStorageReference = FirebaseStorage.getInstance().getReference("Notes");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Notes");

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

            case android.R.id.home:
                finish();
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
        Random r = new Random();
        for (int i = 0; i < size; i++) {
            colorList.add(new Color(android.graphics.Color.argb(255, r.nextInt(255), r.nextInt(255), r.nextInt(255))));
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

        android.app.AlertDialog loadingDialog;
        loadingDialog = new SpotsDialog.Builder().setContext(this).setTheme(R.style.LoadingDialogTheme).build();


        builder.setPositiveButton("Upload", (dialog, which) -> {

            String noteName = input.getText().toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + ".jpeg");
            loadingDialog.setMessage("Uploading note " + noteName);
            loadingDialog.show();
            mUploadTask = fileReference.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            Note note = new Note(noteName.trim(), Utills.millisToDate(taskSnapshot.getMetadata().getCreationTimeMillis()), url);
                            String uploadId = mDatabaseReference.push().getKey();
                            mDatabaseReference.child(uploadId).setValue(note);
                            loadingDialog.dismiss();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Upload error", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                    });

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void setAppBarTitle() {
        TextView tv = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText("DrawIt!");
        tv.setTextSize(20);
        tv.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/pacifico.ttf");
        tv.setTypeface(tf);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(tv);
    }
}