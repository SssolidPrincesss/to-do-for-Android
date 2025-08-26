package com.example.myfirstapplication.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String text;
    public String category = "All";
    public long deadline;
    public boolean isCompleted = false;
    public long completionTime = 0;

    public Note() {}

    public Note(String text, String category, long deadline) {
        this.text = text;
        this.category = category;
        this.deadline = deadline;
    }
}