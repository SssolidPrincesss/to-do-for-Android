package com.example.myfirstapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.example.myfirstapplication.data.entities.Note;
import com.example.myfirstapplication.data.entities.NoteWithSubTasks;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE isCompleted = 0 ORDER BY deadline ASC")
    LiveData<List<Note>> getAllActiveNotes();

    @Query("SELECT * FROM notes WHERE isCompleted = 1 ORDER BY completionTime DESC")
    LiveData<List<Note>> getAllCompletedNotes();

    @Query("SELECT * FROM notes WHERE category = :category AND isCompleted = 0 ORDER BY deadline ASC")
    LiveData<List<Note>> getActiveNotesByCategory(String category);

    @Query("SELECT * FROM notes WHERE category = :category AND isCompleted = 1 ORDER BY completionTime DESC")
    LiveData<List<Note>> getCompletedNotesByCategory(String category);

    @Query("SELECT DISTINCT category FROM notes WHERE category != 'All'")
    LiveData<List<String>> getAllCategories();

    @Query("DELETE FROM notes WHERE isCompleted = 1 AND :currentTime - completionTime > 86400000")
    void deleteOldCompletedNotes(long currentTime);

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<NoteWithSubTasks> getNoteWithSubTasks(long noteId);

    @Query("DELETE FROM notes WHERE category = :category")
    void deleteNotesByCategory(String category);
}