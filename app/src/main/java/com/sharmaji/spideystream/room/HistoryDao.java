package com.sharmaji.spideystream.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sharmaji.spideystream.models.HistoryModel;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryModel historyModel);

    @Query("SELECT * FROM watch_history ORDER BY id DESC")
    LiveData<List<HistoryModel>> getAllHistory();

    @Query("DELETE FROM watch_history")
    int deleteAll();
}

