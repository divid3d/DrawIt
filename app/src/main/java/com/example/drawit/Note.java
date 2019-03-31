package com.example.drawit;

import android.graphics.drawable.Drawable;

public class Note {
    private String name;
    private String noteUrl;
    private String date;
    private String key;
    private  boolean isFavourite = false;

    public Note() {
        //empty constructor
    }

    public Note(String name, String date, String noteUrl) {
        if (name.trim().equals("")) {
            name = "No name";
        }
        this.date = date;
        this.name = name;
        this.noteUrl = noteUrl;
    }

    public String getName() {
        return name;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public String getDate() {
        return date;
    }

    public String getKey() {
        return key;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
