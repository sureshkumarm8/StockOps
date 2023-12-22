package com.sureit.stockops.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;

import java.util.List;

@Database(entities = {BanksList.class, BankNiftyList.class}, version = 2,exportSchema = false)
public abstract class BanksDatabase extends RoomDatabase {
    private static BanksDatabase appDatabase;
    private BanksDao notesDAO;
    private BankNiftyDao bankNiftyDao;
    
    public abstract BanksDao getBanks();
    public abstract BankNiftyDao getBankNiftyCP();

    private Context context;
    public static BanksDatabase getInstance(Context context){
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), BanksDatabase.class, "Banks-databaseNew")
                    .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }

    public List<BanksList> getBanksInfo(Context context, String storeStr) {
        if (notesDAO == null) {
            notesDAO = BanksDatabase.getInstance(context).getBanks();
        }
        return notesDAO.getBankHistory(storeStr+"%");
    }
}
