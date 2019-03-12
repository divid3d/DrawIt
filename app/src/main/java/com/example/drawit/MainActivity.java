package com.example.drawit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    Intent intent;
    private DrawView drawView;
    private Toolbar mToolbar;
    private RecyclerView mColorPicker;
    private ColorAdapter mColorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawView = findViewById(R.id.draw_view);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawView.init(metrics);
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        Bitmap bitmap = Utills.loadBitmapFromView(drawView);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void saveImage() {
        Bitmap bitmap = Utills.loadBitmapFromView(drawView);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String hash = String.valueOf(Utills.getUniqueLongFromString(Calendar.getInstance().getTime().toString()));
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + hash);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            Toast.makeText(this, "Saved image as:" + hash, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public List<Color> colorListGenerator(int size) {
        List<Color> colorList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            colorList.add(new Color(random.nextInt()));
        }

        return colorList;
    }
}