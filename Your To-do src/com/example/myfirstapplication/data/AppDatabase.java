package com.example.myfirstapplication.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.myfirstapplication.data.dao.NoteDao;
import com.example.myfirstapplication.data.dao.SubTaskDao;
import com.example.myfirstapplication.data.entities.Note;
import com.example.myfirstapplication.data.entities.SubTask;

@Database(entities = {Note.class, SubTask.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract NoteDao noteDao();
    public abstract SubTaskDao subTaskDao();

    public static synchronized AppDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "notes_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}