package com.example.drawit;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewMain extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private FloatingActionButton fab;
    private SearchView mSearchView;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseReferance;
    private FirebaseStorage mStorage;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Note> mNotes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private boolean sortedByName = false;
    private boolean sortedByDate = false;
    private boolean sortedByFavourites = false;
    private boolean initRecyclerViewAnimation = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);


        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(mToolbar);
        fab = findViewById(R.id.fab);
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
        noteAdapter = new NoteAdapter(getApplicationContext(), mNotes, new NoteAdapter.OnSelectModeEnabled() {
            @Override
            public void onSelectMode(boolean isEnabled) {
                invalidateOptionsMenu();
            }
        });
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);

        /*recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(noteAdapter.isSelectMode()){
                    noteAdapter.startSelectMode();
                }else{
                    noteAdapter.stopSelectMode();
                }
                return  true;
            }
        });*/


        mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Notes");
        mStorage = FirebaseStorage.getInstance();
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
                if (initRecyclerViewAnimation) {
                    initRecyclerViewAnimation = false;
                    recyclerView.scheduleLayoutAnimation();
                }
                mSwipeRefreshLayout.setRefreshing(false);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        /*fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, new BrowseNotesFragment()).commit();*/


        fab.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            if (noteAdapter.isSelectMode()) {
                noteAdapter.stopSelectMode();
                invalidateOptionsMenu();
            }

        });


    }

    @Override
    public void onRefresh() {
        if (noteAdapter.isSelectMode()) {
            noteAdapter.stopSelectMode();
            invalidateOptionsMenu();
        }
        noteAdapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;

            case R.id.action_sort_by_name:
                sortedByDate = false;
                if (!sortedByName) {
                    Collections.sort(mNotes, (n1, n2) -> n1.getName().trim().toLowerCase().compareTo(n2.getName().trim().toLowerCase()));
                    sortedByName = true;
                } else {
                    Collections.sort(mNotes, (n1, n2) -> n2.getName().trim().toLowerCase().compareTo(n1.getName().trim().toLowerCase()));
                    sortedByName = false;
                }
                noteAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_sort_by_date:
                sortedByName = false;
                if (!sortedByDate) {
                    Collections.sort(mNotes, (n1, n2) -> (int) (Utills.dateToMillis(n1.getDate()) - Utills.dateToMillis(n2.getDate())));
                    sortedByDate = true;
                } else {
                    Collections.sort(mNotes, (n1, n2) -> (int) (Utills.dateToMillis(n2.getDate()) - Utills.dateToMillis(n1.getDate())));
                    sortedByDate = false;
                }
                noteAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_sort_by_favourites:
                sortedByName = false;
                sortedByDate = false;

                Collections.sort(mNotes, (n1, n2) -> {
                    if (n1.isFavourite() == n2.isFavourite()) {
                        return n1.getName().compareTo(n2.getName());
                    } else {
                        if (n1.isFavourite() && !n2.isFavourite()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                noteAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_delete:
                if (!noteAdapter.isSelectMode()) {
                    noteAdapter.startSelectMode();
                } else {
                    noteAdapter.stopSelectMode();
                }
                return true;

            case R.id.action_delete_selected:
                if (noteAdapter.isSelectMode()) {
                    deleteSelectedNotes();
                    noteAdapter.stopSelectMode();
                    invalidateOptionsMenu();
                }
                return true;

            case R.id.action_stop_select_mode:

                if (noteAdapter.isSelectMode()) {
                    noteAdapter.stopSelectMode();
                    invalidateOptionsMenu();
                }

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
        } else if (noteAdapter.isSelectMode()) {
            noteAdapter.stopSelectMode();
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!noteAdapter.isSelectMode()) {
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
        } else {
            getMenuInflater().inflate(R.menu.select_mode_menu, menu);

        }
        return true;
    }

    private void deleteSelectedNotes() {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            NoteAdapter.MyViewHolder holder = (NoteAdapter.MyViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder.isSelected) {
                Note note = mNotes.get(holder.getAdapterPosition());
                StorageReference storageReference = mStorage.getReferenceFromUrl(note.getNoteUrl());
                storageReference.delete().addOnSuccessListener(aVoid -> {
                    mDatabaseReferance.child(note.getKey()).removeValue();
                }).addOnFailureListener(e -> {

                });
            }
        }

        if (noteAdapter.isSelectMode()) {
            noteAdapter.stopSelectMode();
        }
    }

}
