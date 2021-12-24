package com.sureit.stockops.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.data.MovieList;

/**
 * Created by Suresh on 12/31/17.
 */

@Database(entities = { MovieList.class }, version = 1)
//@TypeConverters({DateTypeConverter.class})
public abstract class MovieDatabase extends RoomDatabase {
    public abstract MovieDao getMovieDao();

    private static MovieDatabase INSTANCE;

    public static MovieDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MovieDatabase.class, Constants.DB_NAME)
                    .build();
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


}