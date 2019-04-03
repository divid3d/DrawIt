package com.example.drawit;

import android.graphics.Bitmap;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;



public class Utills {


    public static Bitmap loadBitmapFromView(View view) {
        return view.getDrawingCache();
    }

    public static long getUniqueLongFromString (String value){
        return  UUID.nameUUIDFromBytes(value.getBytes()).getMostSignificantBits();
    }

    public static String millisToDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date resultDate = new Date(millis);
        return sdf.format(resultDate);
    }

    public static long dateToMillis(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        long millis = 0;
        try {
            Date mDate = sdf.parse(date);
            millis = mDate.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  millis;
    }

}
