package com.sureit.stockops.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import android.database.Cursor;

import com.sureit.stockops.data.BanksList;

import java.util.List;
@Dao
public interface BanksDao {

    @Query("Select * from banksDataDB")
    public LiveData<List<BanksList>> getAllData();

    @Delete
    public void deleteMessage(BanksList banksList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertBankData(BanksList banksList);

    @Query("Select * from banksDataDB where bankName LIKE :s")
    public List<BanksList> getBankHistory(String s);

    @Query("Select * from banksDataDB ORDER BY `bankName` DESC")
    public List<BanksList> getAllNotesTitles();

    @Query("DELETE FROM banksDataDB WHERE timeStamp NOT IN (SELECT MIN(timeStamp) FROM banksDataDB GROUP BY timeStamp, totalBuyQuantity,totalSellQuantity)")
    void deleteDuplicates();

    @Query("Select * from banksDataDB")
    public Cursor getAllDataCSV();

}