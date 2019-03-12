package com.example.drawit;


import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyViewHolder> {

    private List<Color> colorList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Color color);
    }

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

        holder.relativeLayout.getBackground().setColorFilter(colorList.get(position).getColor(), PorterDuff.Mode.MULTIPLY);
        holder.bind(colorList.get(position), listener);
    }


    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout relativeLayout;

        public void bind(final Color color, final OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(color);

                }
            });
        }



        public MyViewHolder(View v) {
            super(v);
            relativeLayout = v.findViewById(R.id.color_item);
        }
    }
}
