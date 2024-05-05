package com.sharmaji.spideystream.room;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.sharmaji.spideystream.models.HistoryModel;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Repository {

    private final HistoryDao dao;
    private final LiveData<List<HistoryModel>> historyList;
    private final Executor executor;

    public Repository(Application application) {
        MoviesDb database = MoviesDb.getInstance(application);
        dao = database.Dao();
        historyList = dao.getAllHistory();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(HistoryModel model) {
        executor.execute(() -> dao.insert(model));
    }

    public void updateAvailability(String url, boolean isAvailable) {
        executor.execute(() -> dao.updateAvailability(url, isAvailable));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public LiveData<List<HistoryModel>> getHistoryList() {
        return historyList;
    }
}
