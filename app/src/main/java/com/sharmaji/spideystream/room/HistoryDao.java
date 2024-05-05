package com.sharmaji.spideystream.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sharmaji.spideystream.models.HistoryModel;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(HistoryModel historyModel);

    @Query("SELECT * FROM watch_history ORDER BY source_url_id DESC")
    LiveData<List<HistoryModel>> getAllHistory();

    @Query("DELETE FROM watch_history")
    int deleteAll();

    @Query("UPDATE watch_history SET isAvailable = :isAvailable WHERE source_url_id = :url")
    void updateAvailability(String url, boolean isAvailable);
    @Update
    void updateAvailability(HistoryModel historyModel);
}

