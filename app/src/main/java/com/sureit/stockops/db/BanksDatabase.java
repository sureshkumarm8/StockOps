package com.sureit.stockops.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.sureit.stockops.data.BanksList;

import java.util.List;

@Database(entities = {BanksList.class}, version = 1)
public abstract class BanksDatabase extends RoomDatabase {
    private static BanksDatabase appDatabase;
    private BanksDao notesDAO;

    public abstract BanksDao notes();
    private Context context;
    public static BanksDatabase getInstance(Context context){
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), BanksDatabase.class, "Banks-database")
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
            notesDAO = BanksDatabase.getInstance(context).notes();
        }
        return notesDAO.getBankHistory(storeStr+"%");
    }
}
