package com.example.myfirstapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.data.entities.SubTask;

import java.util.List;

@Dao
public interface SubTaskDao {
    @Insert
    void insert(SubTask subTask);

    @Insert
    void insertAll(List<SubTask> subTasks);

    @Delete
    void delete(SubTask subTask);

    @Query("SELECT * FROM subtasks WHERE noteId = :noteId ORDER BY id ASC")
    List<SubTask> getSubTasksForNote(long noteId);

    @Query("DELETE FROM subtasks WHERE noteId = :noteId")
    void deleteAllSubTasksForNote(long noteId);

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    void updateSubTaskStatus(long subTaskId, boolean isCompleted);
}