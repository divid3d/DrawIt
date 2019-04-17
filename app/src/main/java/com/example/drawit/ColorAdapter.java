package com.example.drawit;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyViewHolder> {

    int selected_position = 0; // You have to set this globally in the Adapter class
    private List<Color> colorList;
    private OnItemClickListener listener;


    public ColorAdapter(List<Color> colors, OnItemClickListener listener) {
        colorList = colors;
        this.listener = listener;
    }

    @Override
    public ColorAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.color.setBackgroundColor(colorList.get(position).getColor());
        holder.border.setBackgroundColor(selected_position == position ? android.graphics.Color.WHITE : android.graphics.Color.TRANSPARENT);
        holder.bind(colorList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }


    public interface OnItemClickListener {
        void onItemClick(Color color);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout colorItem;
        public CardView border;
        public RelativeLayout color;

        public MyViewHolder(View v) {
            super(v);
            colorItem = v.findViewById(R.id.color_item);
            border = v.findViewById(R.id.border);
            color = v.findViewById(R.id.color);
        }

        public void bind(final Color color, final OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(color);
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
                selected_position = getAdapterPosition();
                notifyDataSetChanged();
            });
        }
    }
}
