package com.sureit.stockops.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sureit.stockops.R;
import com.sureit.stockops.adapter.MovieAdapter;
import com.sureit.stockops.data.MovieList;
import com.sureit.stockops.db.MovieDao;
import com.sureit.stockops.db.MovieDatabase;
import com.sureit.stockops.db.MovieViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.DB_NAME;
import static com.sureit.stockops.Util.Constants.FAV_ROT;
import static com.sureit.stockops.Util.Constants.PARCEL_KEY;
import static com.sureit.stockops.Util.Constants.POPULAR_MOVIES_URL;
import static com.sureit.stockops.Util.Constants.TOP_RATED_MOVIES_URL;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    NoInternetDialog noInternetDialog;

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<MovieList> movieLists;

    private MovieDao mMovieDao;

    public static final String URL_BankNifty = "https://www.nseindia.com/api/option-chain-indices?symbol=BANKNIFTY";
    public static final String URL_NSE = "https://www.nseindia.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMovie);
        recyclerView.setHasFixedSize(true);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }
        movieLists = new ArrayList<>();
        noInternetDialog = new NoInternetDialog.Builder(this).build();

        mMovieDao = Room.databaseBuilder(this, MovieDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getMovieDao();

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(PARCEL_KEY)) {
                movieLists = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
                adapter = new MovieAdapter(movieLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
        if(FAV_ROT){
            loadFavMovies();
        }else {
//            loadUrlData(BASE_URL_MOVIE);
            checkNseUrl(URL_NSE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sort_settings,menu);
    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){

            case R.id.popular:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                movieLists.clear();
                FAV_ROT = false;
                loadUrlData(POPULAR_MOVIES_URL);
                return true;

            case R.id.rated:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                movieLists.clear();
                FAV_ROT = false;
                loadUrlData(TOP_RATED_MOVIES_URL);
                return true;

            case R.id.myfav:
                item.setChecked(true);
                movieLists.clear();
                FAV_ROT = true;
                loadFavMovies();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadFavMovies() {
        LiveData<List<MovieList>> movieListsL = mMovieDao.getMovies();
        movieListsL.observe(this, new Observer<List<MovieList>>() {
            @Override
            public void onChanged(@Nullable List<MovieList> movieLists) {
                adapter.setMoviesLive(movieLists);
                adapter = new MovieAdapter(adapter.getMoviesLive(), getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void loadUrlData(String bankNiftyUrl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, bankNiftyUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);
//                    JSONObject recordDetails = jsonObject.getJSONObject("records");
//                    JSONArray expiryDatesArray = recordDetails.getJSONArray("expiryDates");
//                    JSONArray dataArray = recordDetails.getJSONArray("data");
//                    JSONArray strikePricesArray = recordDetails.getJSONArray("strikePrices");
//                    JSONArray indexArray = recordDetails.getJSONArray("index");

                    JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                    JSONArray filteredDataArray = filteredDetails.getJSONArray("data");

                    for (int i = 0; i < filteredDataArray.length(); i++){

                        JSONObject jo = filteredDataArray.getJSONObject(i);
                        JSONObject peBody= jo.getJSONObject("PE");
                        JSONObject ceBody= jo.getJSONObject("CE");
//                        MovieList movieList = new MovieList(jo.getLong("strikePrice"),jo.getString("expiryDate"), jo.getString("openInterest"),
//                                jo.getString("totalTradedVolume"),jo.getString("bidQty"),jo.getString("askQty"));
                        MovieList movieList = new MovieList(jo.getLong("strikePrice"),peBody.get("totalTradedVolume").toString(), ceBody.get("totalTradedVolume").toString(),null,null,null);
                        movieLists.add(movieList);

                    }

                    adapter = new MovieAdapter(movieLists, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                progressDialog.dismiss();
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    //handler error 401 unauthorized from here
                    Toast.makeText(MainActivity.this, "401 Error, connecting again...." + error.toString(), Toast.LENGTH_LONG).show();
                    checkNseUrl(URL_NSE);
                }
                Toast.makeText(MainActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void checkNseUrl(String nseurl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, nseurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response!=null) {
                    try {
                        Thread.sleep(5000);
                        loadUrlData(URL_BankNifty);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(PARCEL_KEY, (ArrayList<? extends
                Parcelable>) movieLists);
    }

    private void setupViewModel() {
        MovieViewModel viewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<MovieList>>() {
            @Override
            public void onChanged(@Nullable List<MovieList> taskEntries) {
                adapter.setMoviesLive(taskEntries);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(FAV_ROT){
            loadFavMovies();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
