package com.sureit.stockops.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;

import java.util.List;

@Dao
public interface BankNiftyDao {

    @Query("Select * from bankNiftyDB")
    public LiveData<List<BankNiftyList>> getAllData();

    @Delete
    public void deleteMessage(BankNiftyList bankNiftyList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertBankNiftyData(BankNiftyList bankNiftyList);

    @Query("Select * from bankNiftyDB where oiname LIKE :s")
    public List<BankNiftyList> getBankNiftyHistory(String s);

    @Query("Select * from bankNiftyDB ORDER BY timestamp DESC")
    public List<BankNiftyList> getAllNotesTitles();

    @Query("Select * from bankNiftyDB")
    public Cursor getAllDataCSV();
}