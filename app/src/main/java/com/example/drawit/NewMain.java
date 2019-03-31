package com.example.drawit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewMain extends AppCompatActivity {

    private FloatingActionButton fab;
    private Toolbar mToolabar;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        fab = findViewById(R.id.fab);
        mToolabar = findViewById(R.id.toolbar);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "Successfully log in", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, new BrowseNotesFragment()).commit();


        fab.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));


    }
}
