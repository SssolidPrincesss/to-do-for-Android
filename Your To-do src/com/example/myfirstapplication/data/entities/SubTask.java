package com.example.myfirstapplication.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "subtasks",
        foreignKeys = @ForeignKey(entity = Note.class,
                parentColumns = "id",
                childColumns = "noteId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("noteId")})
public class SubTask {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long noteId;
    public String text;
    public boolean isCompleted = false;

    public SubTask() {}

    public SubTask(long noteId, String text) {
        this.noteId = noteId;
        this.text = text;
    }
}