package com.sureit.stockops.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import android.database.Cursor;

import com.sureit.stockops.data.BankNiftyList;

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