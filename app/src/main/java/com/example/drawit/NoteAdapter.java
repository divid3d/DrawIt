package com.example.drawit;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> implements Filterable {

    private List<Note> notes;
    private List<Note> filteredNotes;
    private OnNoteClickListener listener;
    private DatabaseReference mDatabaseReference;
    private Context context;

    public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.filteredNotes = notes;
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();

                List<Note> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = notes;
                } else {
                    for (Note note : notes) {
                        if (note.getName().toLowerCase().contains(query.toLowerCase())) {
                            filtered.add(note);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.count = filtered.size();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                filteredNotes = (ArrayList<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public NoteAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);

        return new MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        /*holder.relativeLayout.getBackground().setColorFilter(colorList.get(position).getColor(), PorterDuff.Mode.MULTIPLY);
        holder.bind(colorList.get(position), listener);*/
        holder.textViewTitle.setText(filteredNotes.get(position).getName());
        holder.textViewDate.setText(filteredNotes.get(position).getDate());
        if(filteredNotes.get(position).isFavourite()){
            holder.favButton.setImageResource(R.drawable.ic_fav_checked_24dp);
        }else{
            holder.favButton.setImageResource(R.drawable.ic_fav_uncheck_24dp);
        }
        Picasso.get().load(filteredNotes.get(position).getNoteUrl()).fit().centerInside().into(holder.image, new Callback() {
            @Override
            public void onSuccess() {
                holder.loadingSpinner.setVisibility(View.GONE);
                holder.isLoaded = true;
            }

            @Override
            public void onError(Exception e) {
                holder.image.setImageResource(R.drawable.ic_error_black_24dp);
            }
        });

        holder.image.setOnClickListener(v -> {
            if(holder.isLoaded){
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("note_url", filteredNotes.get(position).getNoteUrl());
                context.startActivity(intent);
                //Toast.makeText(context,String.valueOf(notes.get(position).getName()), Toast.LENGTH_SHORT).show();
            }
        });

        holder.favButton.setOnClickListener(v -> {
            if (!filteredNotes.get(position).isFavourite()) {
                filteredNotes.get(position).setFavourite(true);
               // holder.favButton.startAnimation(AnimationUtils.loadAnimation(this,))
                holder.favButton.setImageResource(R.drawable.ic_fav_checked_24dp);
                try {
                    mDatabaseReference.child(filteredNotes.get(position).getKey()).setValue(filteredNotes.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                filteredNotes.get(position).setFavourite(false);
                holder.favButton.setImageResource(R.drawable.ic_fav_uncheck_24dp);
                try {
                    mDatabaseReference.child(filteredNotes.get(position).getKey()).setValue(filteredNotes.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredNotes.size();
    }


    public interface OnNoteClickListener {
        void onItemClick(Color color);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewDate;
        public ImageView image;
        public ImageButton favButton;
        public SpinKitView loadingSpinner;
        public boolean isLoaded = false;

        public MyViewHolder(View v) {
            super(v);
            textViewTitle = v.findViewById(R.id.note_title);
            textViewDate = v.findViewById(R.id.note_date);
            image = v.findViewById(R.id.note_image);
            favButton = v.findViewById(R.id.favourite_button);
            loadingSpinner = v.findViewById(R.id.spin_kit);
        }

        public void bind(final Note note, final OnNoteClickListener listener) {

            itemView.setOnClickListener(v -> {
                //listener.onItemClick();

            });
        }
    }
}