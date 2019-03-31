package com.example.drawit;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.UnicodeSetSpanner;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    private List<Note> notes;
    private OnNoteClickListener listener;
    private Context context;

    public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
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
        holder.textViewTitle.setText(notes.get(position).getName());
        holder.textViewDate.setText(notes.get(position).getDate());
        Picasso.get().load(notes.get(position).getNoteUrl()).fit().centerInside().into(holder.image, new Callback() {
            @Override
            public void onSuccess() {
                holder.loadingProgressBar.setVisibility(View.GONE);
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
                intent.putExtra("note_url", notes.get(position).getNoteUrl());
                context.startActivity(intent);
                //Toast.makeText(context,String.valueOf(notes.get(position).getName()), Toast.LENGTH_SHORT).show();
            }
        });

        holder.favButton.setOnClickListener(v -> {
            if (!notes.get(position).isFavourite()) {
                notes.get(position).setFavourite(true);
               // holder.favButton.startAnimation(AnimationUtils.loadAnimation(this,))
                holder.favButton.setImageResource(R.drawable.ic_fav_checked_24dp);

            } else {
                notes.get(position).setFavourite(false);
                holder.favButton.setImageResource(R.drawable.ic_fav_uncheck_24dp);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


    public interface OnNoteClickListener {
        void onItemClick(Color color);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTitle;
        public TextView textViewDate;
        public ImageView image;
        public ImageButton favButton;
        public ProgressBar loadingProgressBar;
        public boolean isLoaded = false;

        public MyViewHolder(View v) {
            super(v);
            textViewTitle = v.findViewById(R.id.note_title);
            textViewDate = v.findViewById(R.id.note_date);
            image = v.findViewById(R.id.note_image);
            favButton = v.findViewById(R.id.favourite_button);
            loadingProgressBar = v.findViewById(R.id.loading_progress_bar);
        }

        public void bind(final Note note, final OnNoteClickListener listener) {

            itemView.setOnClickListener(v -> {
                //listener.onItemClick();

            });
        }
    }
}