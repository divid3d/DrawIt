package com.example.drawit;

import android.support.annotation.ColorInt;

public class Color {
    private @ColorInt int color;

    public Color(@ColorInt int color){
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
