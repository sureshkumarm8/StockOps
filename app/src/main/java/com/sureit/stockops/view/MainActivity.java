package com.sureit.stockops.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.Comparator;
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
    private int ceTotalTradedVolume;
    private int peTotalTradedVolume;
    private int ceTotalBuyQuantity;
    private int ceTotalSellQuantity;
    private int peTotalBuyQuantity;
    private int peTotalSellQuantity;
    private int ceOpenInterest;
    private int peOpenInterest;
    RelativeLayout relativeLayoutMain;
    TextView strikePriceTVMain;
    TextView totalVolumeCEMain;
    TextView totalVolumePEMain;
    TextView totalBuyQuantityCEMain;
    TextView totalAskQuantityCEMain;
    TextView totalBuyQuantityPEMain;
    TextView totalAskQuantityPEMain;
    TextView ceOpenInterestMain;
    TextView peOpenInterestMain;
    TextView timeStampMain;
    private Double underlyingValue;
    private String timeStampValue;
    private MovieList movieList;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the View objects
        relativeLayoutMain = findViewById(R.id.relativeLayoutRVmain);
        strikePriceTVMain = findViewById(R.id.tvStrikePriceMain);
        totalVolumeCEMain = findViewById(R.id.tvVolumeCEmain);
        totalVolumePEMain = findViewById(R.id.tvVolumePEmain);
        totalBuyQuantityCEMain = findViewById(R.id.tvBuyQuantityCEmain);
        totalAskQuantityCEMain = findViewById(R.id.tvASKQuantityCEmain);
        totalBuyQuantityPEMain = findViewById(R.id.tvBuyQuantityPEmain);
        totalAskQuantityPEMain = findViewById(R.id.tvASKQuantityPEmain);
        ceOpenInterestMain = findViewById(R.id.tvStrikePrice2main);
        peOpenInterestMain = findViewById(R.id.tvStrikePrice3main);
        timeStampMain = findViewById(R.id.textViewTimeStamp);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing latest data....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mMovieDao.delete(movieList);
                checkNseUrl(URL_NSE);
            }
        });

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
            // Call this to start the task first time
            mHandler.postDelayed(mRunnableTask, 5 * (60*1000));
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
//                loadFavMovies();
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
                Collections.sort(movieLists, new Comparator<MovieList>() {
                    @Override
                    public int compare(MovieList val1, MovieList val2) {
                        if(val1.getTitle() < val2.getTitle() && val1.getDescription() < val2.getDescription()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                adapter = new MovieAdapter(movieLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void loadUrlData(String bankNiftyUrl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("BankNifty Data Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, bankNiftyUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONObject recordDetails = jsonObject.getJSONObject("records");
//                    JSONArray expiryDatesArray = recordDetails.getJSONArray("expiryDates");
//                    String curExpiry = expiryDatesArray.get(0).toString();
                    String underlyingValueMain = recordDetails.getString("underlyingValue");
//                    long roundOffValue = Math.round(Double.parseDouble(underlyingValue) / 100) * 100;
//                    JSONArray dataArray = recordDetails.getJSONArray("data");
//                    for (int i = 0; i < dataArray.length(); i++) {
//                        JSONObject jo = dataArray.getJSONObject(i);
//                        JSONObject expiryDate = jo.getJSONObject("expiryDate");
//                        if (expiryDate.has(curExpiry)){
//                            JSONObject peBody= jo.getJSONObject("PE");
//                            JSONObject ceBody= jo.getJSONObject("CE");
//                            MovieList movieList = new MovieList(jo.getLong("strikePrice"), peBody.get("totalTradedVolume").toString(), ceBody.get("totalTradedVolume").toString(), null, null, null);
//                            movieLists.add(movieList);
//                        }
//
//                    }

                    JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                    JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
                    timeStampValue = recordDetails.get("timestamp").toString();
                    underlyingValue = recordDetails.getDouble("underlyingValue");
                    int ulValue = underlyingValue.intValue();
                    for (int i = 45; i < 72; i++){
                        JSONObject jo = filteredDataArray.getJSONObject(i);

                        Double strikePrice = null;
                        int sPrice = 0;
                        if(strikePrice==null) {
                            JSONObject usBody = jo.getJSONObject("CE");
                            strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                            sPrice=strikePrice.intValue();
                        }
//                        String expiryDate = jo.getString("expiryDate");
                        if (sPrice<=ulValue+500 && sPrice>=ulValue-500) {
                            JSONObject ceBody = jo.getJSONObject("CE");
                            JSONObject peBody = jo.getJSONObject("PE");

                            //Add total values for main top card
                              ceTotalTradedVolume += (int) ceBody.get("totalTradedVolume");
                              peTotalTradedVolume += (int) peBody.get("totalTradedVolume");
                              ceTotalBuyQuantity += (int) ceBody.get("totalBuyQuantity");
                              ceTotalSellQuantity += (int) ceBody.get("totalSellQuantity");
                              peTotalBuyQuantity += (int) peBody.get("totalBuyQuantity");
                              peTotalSellQuantity += (int) peBody.get("totalSellQuantity");
                              ceOpenInterest += (int) ceBody.get("openInterest");
                              peOpenInterest += (int) peBody.get("openInterest");

                             movieList = new MovieList(ceBody.getString("identifier").substring(25,32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            movieLists.add(movieList);
                            movieList = new MovieList(peBody.getString("identifier").substring(25,32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            movieLists.add(movieList);
//                            mMovieDao.insert(movieList);
                        }
                    }

                    timeStampMain.setText(timeStampValue);
                    strikePriceTVMain.setText(String.valueOf(underlyingValue));
                    totalVolumeCEMain.setText(String.valueOf(ceTotalTradedVolume/1000));
                    totalVolumePEMain.setText(String.valueOf(peTotalTradedVolume/1000));
                    totalBuyQuantityCEMain.setText(String.valueOf(ceTotalBuyQuantity/1000));
                    totalAskQuantityCEMain.setText(String.valueOf(ceTotalSellQuantity/1000));
                    totalBuyQuantityPEMain.setText(String.valueOf(peTotalBuyQuantity/1000));
                    totalAskQuantityPEMain.setText(String.valueOf(peTotalSellQuantity/1000));
                    ceOpenInterestMain.setText(String.valueOf(ceOpenInterest/1000));
                    peOpenInterestMain.setText(String.valueOf(peOpenInterest/1000));

                    //sort the cards
                    Collections.sort(movieLists, new Comparator<MovieList>() {
                        @Override
                        public int compare(MovieList val1, MovieList val2) {
                            if(val1.getTitle() > val2.getTitle()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });

                    adapter = new MovieAdapter(movieLists, getApplicationContext());
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                progressDialog.dismiss();
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    //handler error 401 unauthorized from here
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "401 Error, connecting again...." + error.toString(), Toast.LENGTH_LONG).show();
                    checkNseUrl(URL_NSE);
                }
                Toast.makeText(MainActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        ceTotalTradedVolume=0;
        peTotalTradedVolume=0;
        ceTotalBuyQuantity=0;
        ceTotalSellQuantity=0;
        peTotalBuyQuantity=0;
        peTotalSellQuantity=0;
        ceOpenInterest=0;
        peOpenInterest=0;
        underlyingValue=0.0;
        timeStampValue=null;
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

    Runnable mRunnableTask = new Runnable()
    {
        @Override
        public void run() {
            movieLists.clear();
            checkNseUrl(URL_NSE);
            // this will repeat this task again at specified time interval
            mHandler.postDelayed(this, 5 * (60*1000));
        }
    };


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
        mHandler.removeCallbacksAndMessages(null);
    }
}
