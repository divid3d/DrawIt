package com.example.drawit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseNotesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseReferance;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Note> mNotes = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private boolean initNotesSort = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.browser_layout, container, false);
        mSwipeRefreshLayout = retView.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = retView.findViewById(R.id.notes_recycler_view);
        noteAdapter = new NoteAdapter(getContext(),mNotes, null);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
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

                if (initNotesSort) {
                    Collections.sort(mNotes, (n1, n2) -> n1.getName().trim().compareTo(n2.getName().trim()));
                    initNotesSort = false;
                }

                noteAdapter.notifyDataSetChanged();
                recyclerView.scheduleLayoutAnimation();
                mSwipeRefreshLayout.setRefreshing(false);

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return retView;
    }
    @Override
    public void onRefresh() {
        noteAdapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        mSwipeRefreshLayout.setRefreshing(false);
    }

}
