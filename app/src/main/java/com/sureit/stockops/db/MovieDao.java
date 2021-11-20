package com.sureit.stockops.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.sureit.stockops.data.MovieList;

import java.util.List;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Dao
public interface MovieDao {

    @Insert
    void insert(MovieList movie);

    @Update
    void update(MovieList... repos);

    @Delete
    void delete(MovieList movie);

    @Query("SELECT * FROM  moviesfav")
    LiveData <List<MovieList>> getMovies();

    @Query("SELECT * FROM moviesfav WHERE id = :number")
    boolean getMovieWithId(String number);

}
