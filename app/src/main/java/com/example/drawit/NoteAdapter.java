package com.example.drawit;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jgabrielfreitas.core.BlurImageView;
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
    private boolean selectMode = false;


    public NoteAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.filteredNotes = notes;
        this.listener = listener;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Notes");
    }

    public boolean isSelectMode() {
        return this.selectMode;
    }

    public void startSelectMode() {
        selectMode = true;
        notifyDataSetChanged();
    }

    public void stopSelectMode() {
        selectMode = false;
        notifyDataSetChanged();
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
        holder.isSelected = false;
        if (filteredNotes.get(position).isFavourite()) {
            holder.favButton.setImageResource(R.drawable.ic_fav_checked_24dp);
        } else {
            holder.favButton.setImageResource(R.drawable.ic_fav_uncheck_24dp);
        }
        Picasso.get().load(filteredNotes.get(position).getNoteUrl()).fit().centerInside().into(holder.image, new Callback() {
            @Override
            public void onSuccess() {
                Animation fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        holder.loadingSpinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                holder.loadingSpinner.startAnimation(fadeOutAnim);
                holder.isLoaded = true;
            }

            @Override
            public void onError(Exception e) {
                holder.image.setImageResource(R.drawable.ic_error_black_24dp);
            }
        });

        holder.image.setOnClickListener(v -> {
            if (holder.isLoaded && !selectMode) {
                Intent intent = new Intent(context, NoteViewActivity.class);
                intent.putExtra("note_url", filteredNotes.get(position).getNoteUrl());
                intent.putExtra("note_key", filteredNotes.get(position).getKey());
                intent.putExtra("note_name", filteredNotes.get(position).getName());
                context.startActivity(intent);
                //Toast.makeText(context,String.valueOf(notes.get(position).getName()), Toast.LENGTH_SHORT).show();
            }
        });

        holder.favButton.setOnClickListener(v -> {
            if (!filteredNotes.get(position).isFavourite()) {
                mDatabaseReference.child(filteredNotes.get(position).getKey()).child("favourite").setValue(true);
                filteredNotes.get(position).setFavourite(true);
                holder.favButton.setImageResource(R.drawable.ic_fav_checked_24dp);
            } else {
                mDatabaseReference.child(filteredNotes.get(position).getKey()).child("favourite").setValue(false);
                filteredNotes.get(position).setFavourite(false);
                holder.favButton.setImageResource(R.drawable.ic_fav_uncheck_24dp);
            }
        });

        if (selectMode) {

            holder.selectCheckbox.setVisibility(View.VISIBLE);

            Animation fadeInAnim = AnimationUtils.loadAnimation(context,R.anim.chechbox_fadein_anim);
            holder.selectCheckbox.startAnimation(fadeInAnim);

        } else {
           Animation fadeOutAnim = AnimationUtils.loadAnimation(context,R.anim.chechbox_fadeout_anim);
           fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
               @Override
               public void onAnimationStart(Animation animation) {

               }

               @Override
               public void onAnimationEnd(Animation animation) {
                   holder.selectCheckbox.setVisibility(View.GONE);

               }

               @Override
               public void onAnimationRepeat(Animation animation) {

               }
           });
           holder.selectCheckbox.startAnimation(fadeOutAnim);
        }

        holder.selectCheckbox.setOnClickListener(v -> {
            if(selectMode) {
                if (holder.selectCheckbox.isChecked()) {
                    holder.isSelected = true;
                    holder.image.setBlur(2);
                } else {
                    holder.isSelected = false;
                    holder.image.setBlur(0);
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
        public BlurImageView image;
        public ImageButton favButton;
        public SpinKitView loadingSpinner;
        public CheckBox selectCheckbox;
        public boolean isLoaded = false;
        public boolean isSelected = false;

        public MyViewHolder(View v) {
            super(v);
            textViewTitle = v.findViewById(R.id.note_title);
            textViewDate = v.findViewById(R.id.note_date);
            image = v.findViewById(R.id.note_image);
            favButton = v.findViewById(R.id.favourite_button);
            loadingSpinner = v.findViewById(R.id.spin_kit);
            selectCheckbox = v.findViewById(R.id.select_checkbox);

        }

        public void bind(final Note note, final OnNoteClickListener listener) {

            itemView.setOnClickListener(v -> {
                //listener.onItemClick();

            });
        }
    }
}