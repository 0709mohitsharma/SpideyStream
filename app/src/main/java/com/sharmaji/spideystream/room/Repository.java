package com.sharmaji.spideystream.room;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import com.sharmaji.spideystream.models.HistoryModel;
import java.util.List;

public class Repository {

    private final HistoryDao dao;
    private final LiveData<List<HistoryModel>> historyList;

    public Repository(Application application) {
        MoviesDb database = MoviesDb.getInstance(application);
        dao = database.Dao();
        historyList = dao.getAllHistory();
    }

    public void insert(HistoryModel model) {
        new InsertHistoryAsyncTask(dao).execute(model);
    }

    public void deleteAll(){ new DeleteHistoryAsyncTask(dao).execute();}


    public LiveData<List<HistoryModel>> getHistoryList() {
        return historyList;
    }

    private static class InsertHistoryAsyncTask extends AsyncTask<HistoryModel, Void, Void> {
        private final HistoryDao dao;

        private InsertHistoryAsyncTask(HistoryDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(HistoryModel... model) {
            // below line is used to insert our model in dao.
            dao.insert(model[0]);
            return null;
        }
    }
    private static class DeleteHistoryAsyncTask extends AsyncTask<HistoryModel, Void, Void> {
        private final HistoryDao dao;

        private DeleteHistoryAsyncTask(HistoryDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(HistoryModel... model) {
            // below line is used to insert our model in dao.
            dao.deleteAll();
            return null;
        }
    }
}
