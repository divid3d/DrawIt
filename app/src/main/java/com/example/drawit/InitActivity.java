package com.example.drawit;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class InitActivity extends AppCompatActivity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        text = findViewById(R.id.text);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/pacifico.ttf");
        text.setTypeface(tf);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), NewMain.class));
                    finish();
                }, 3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        text.startAnimation(fadeInAnimation);

    }
}
