package com.example.drawit;

import android.graphics.Bitmap;

import android.view.View;


import java.util.UUID;



public class Utills {


    public static Bitmap loadBitmapFromView(View view) {
        return view.getDrawingCache();
    }

    public static long getUniqueLongFromString (String value){
        return  UUID.nameUUIDFromBytes(value.getBytes()).getMostSignificantBits();
    }

}
