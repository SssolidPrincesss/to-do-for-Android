package com.example.myfirstapplication.data.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class NoteWithSubTasks {
    @Embedded
    public Note note;

    @Relation(parentColumn = "id", entityColumn = "noteId")
    public List<SubTask> subTasks;
}