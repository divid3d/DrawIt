package com.example.drawit;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewMain extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private FloatingActionButton fab;
    private SearchView mSearchView;
    private Toolbar mToolbar;
    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseReferance;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Note> mNotes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private boolean sortedInOrder = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);


        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        fab = findViewById(R.id.fab);
        //mToolabar = findViewById(R.id.toolbar);
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

        mSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = findViewById(R.id.notes_recycler_view);
        noteAdapter = new NoteAdapter(getApplicationContext(), mNotes, null);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);

        mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Notes");
        mDatabaseReferance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mNotes.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Note note = postSnapshot.getValue(Note.class);
                    note.setKey(postSnapshot.getKey());
                    mNotes.add(note);
                }



                noteAdapter.notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();
                mSwipeRefreshLayout.setRefreshing(false);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        /*fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, new BrowseNotesFragment()).commit();*/


        fab.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));


    }

    @Override
    public void onRefresh() {
        noteAdapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }else if(id == R.id.action_sort){
            if(!sortedInOrder) {
                Collections.sort(mNotes, (n1, n2) -> n1.getName().trim().compareTo(n2.getName().trim()));
                sortedInOrder = true;
            }else{
                Collections.sort(mNotes, (n1, n2) -> n2.getName().trim().compareTo(n1.getName().trim()));
                sortedInOrder = false;
            }
            noteAdapter.notifyDataSetChanged();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            mSearchView.clearFocus();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setQueryHint("Search for note...");

        // listening to search query text change
        mSearchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                noteAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                noteAdapter.getFilter().filter(query);
                return true;
            }
        });
        return true;
    }
}
