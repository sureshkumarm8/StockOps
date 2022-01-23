package com.sureit.stockops.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;

import java.util.List;

public class BanksViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = BanksViewModel.class.getSimpleName();

    // COMPLETED (2) Add a tasks member variable for a list of TaskEntry objects wrapped in a LiveData
    private LiveData<List<BanksList>> banksLiveData;
    private LiveData<List<BankNiftyList>> oiLiveData;

    public BanksViewModel(Application application) {
        super(application);
        // COMPLETED (4) In the constructor use the loadAllTasks of the taskDao to initialize the tasks variable
        BanksDatabase database = BanksDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        banksLiveData = database.getBanks().getAllData();
        oiLiveData = database.getBankNiftyCP().getAllData();
    }

    // COMPLETED (3) Create a getter for the tasks variable
    public LiveData<List<BanksList>> getBanksHistory() {
        return banksLiveData;
    }

    public List<BanksList> getBanksHistory(String Str) {
        BanksDatabase database = BanksDatabase.getInstance(this.getApplication());
        return database.getBanks().getBankHistory(Str);
    }

    public LiveData<List<BankNiftyList>> getOIHistory() {
        return oiLiveData;
    }

    public List<BankNiftyList> getOIHistory(String Str) {
        BanksDatabase database = BanksDatabase.getInstance(this.getApplication());
        return database.getBankNiftyCP().getBankNiftyHistory(Str);
    }

}
