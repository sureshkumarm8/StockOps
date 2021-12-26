package com.sureit.stockops.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.sureit.stockops.data.BanksList;

import java.util.List;

public class BanksViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = BanksViewModel.class.getSimpleName();

    // COMPLETED (2) Add a tasks member variable for a list of TaskEntry objects wrapped in a LiveData
    private LiveData<List<BanksList>> tasks;

    public BanksViewModel(Application application) {
        super(application);
        // COMPLETED (4) In the constructor use the loadAllTasks of the taskDao to initialize the tasks variable
        BanksDatabase database = BanksDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        tasks = database.notes().getAllData();
    }

    // COMPLETED (3) Create a getter for the tasks variable
    public LiveData<List<BanksList>> getTasks() {
        return tasks;
    }

    public List<BanksList> getTasks(String Str) {
        BanksDatabase database = BanksDatabase.getInstance(this.getApplication());
        return database.notes().getBankHistory(Str);
    }
}
