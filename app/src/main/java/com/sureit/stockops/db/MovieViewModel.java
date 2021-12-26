package com.sureit.stockops.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.sureit.stockops.data.MovieList;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MovieViewModel.class.getSimpleName();

    // COMPLETED (2) Add a tasks member variable for a list of TaskEntry objects wrapped in a LiveData
    private LiveData<List<MovieList>> tasks;

    public MovieViewModel(Application application) {
        super(application);
        // COMPLETED (4) In the constructor use the loadAllTasks of the taskDao to initialize the tasks variable
        MovieDatabase database = MovieDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        tasks = database.getMovieDao().getMovies();
    }

    // COMPLETED (3) Create a getter for the tasks variable
    public LiveData<List<MovieList>> getTasks() {
        return tasks;
    }
}
