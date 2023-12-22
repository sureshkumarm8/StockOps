
package com.sureit.stockops.view;

import static com.sureit.stockops.Util.Constants.FAV_ROT;
import static com.sureit.stockops.Util.Constants.PARCEL_KEY;
import static com.sureit.stockops.Util.Constants.POPULAR_MOVIES_URL;
import static com.sureit.stockops.Util.Constants.TOP_RATED_MOVIES_URL;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sureit.stockops.R;
import com.sureit.stockops.adapter.BankNiftyAdapter;
import com.sureit.stockops.data.BankNiftyList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankNiftyActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private RecyclerView recyclerView;
    private BankNiftyAdapter adapter;
    private List<BankNiftyList> bankNiftyLists;
    List<String> bankNiftyOIdata;

    public static final String URL_BankNifty = "https://www.nseindia.com/api/option-chain-indices?symbol=BANKNIFTY";
    public static final String URL_BanksTradeInfo = "https://www.nseindia.com/api/quote-equity?symbol=";
    public static final String URL_NSE = "https://www.nseindia.com/";
    public String cookiedataMain;
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
    private BankNiftyList bankNiftyList;

    Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banknifty);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize the View objects
        relativeLayoutMain = findViewById(R.id.relativeLayoutRVmain);
        strikePriceTVMain = findViewById(R.id.tvStrikePricemain);
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
//                mMovieDao.delete(movieList);
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
        bankNiftyLists = new ArrayList<>();


        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(PARCEL_KEY)) {
                bankNiftyLists = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
                adapter = new BankNiftyAdapter(bankNiftyLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }

//        Bundle bundle = getIntent().getExtras();
//        bankNiftyLists = (ArrayList<BankNiftyList>) bundle.getSerializable("bankNiftyData");
//        bankNiftyOIdata = (List<String>) bundle.getSerializable("bankNiftyOIdata");

//        timeStampMain.setText(bankNiftyOIdata.get(0));
//        strikePriceTVMain.setText(bankNiftyOIdata.get(1));
//        totalVolumeCEMain.setText(bankNiftyOIdata.get(2));
//        totalVolumePEMain.setText(bankNiftyOIdata.get(3));
//        totalBuyQuantityCEMain.setText(bankNiftyOIdata.get(4));
//        totalAskQuantityCEMain.setText(bankNiftyOIdata.get(5));
//        totalBuyQuantityPEMain.setText(bankNiftyOIdata.get(6));
//        totalAskQuantityPEMain.setText(bankNiftyOIdata.get(7));
//        ceOpenInterestMain.setText(bankNiftyOIdata.get(8));
//        peOpenInterestMain.setText(bankNiftyOIdata.get(9));

        @SuppressLint("WrongConstant")
        SharedPreferences shOI = getSharedPreferences("NiftyOILiveDisplaySP", MODE_APPEND);
        bankNiftyLists.clear();
        Gson gson = new Gson();
        String json = shOI.getString("bankNiftyData", "");
        Type type = new TypeToken<List<BankNiftyList>>() {}.getType();
        bankNiftyLists = gson.fromJson(json, type);
//        if(shOI.getString("timeStampValue","").length()>5) {
            timeStampMain.setText(shOI.getString("timeStampValue", ""));
            strikePriceTVMain.setText(shOI.getString("underlyingValue", ""));
            totalVolumeCEMain.setText(shOI.getString("ceTotalTradedVolume", ""));
            totalVolumePEMain.setText(shOI.getString("peTotalTradedVolume", ""));
            totalBuyQuantityCEMain.setText(shOI.getString("ceTotalBuyQuantity", ""));
            totalAskQuantityCEMain.setText(shOI.getString("ceTotalSellQuantity", ""));
            totalBuyQuantityPEMain.setText(shOI.getString("peTotalBuyQuantity", ""));
            totalAskQuantityPEMain.setText(shOI.getString("peTotalSellQuantity", ""));
            ceOpenInterestMain.setText(shOI.getString("ceOpenInterest", ""));
            peOpenInterestMain.setText(shOI.getString("peOpenInterest", ""));
//        }
        //sort the cards
        Collections.sort(bankNiftyLists, new Comparator<BankNiftyList>() {
            @Override
            public int compare(BankNiftyList val1, BankNiftyList val2) {
                if(val1.getCalloi() > val2.getCalloi()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });


        adapter = new BankNiftyAdapter(bankNiftyLists, getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

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

            case R.id.banknifty:
                item.setChecked(true);
                bankNiftyLists.clear();
                FAV_ROT = false;
                loadUrlData(POPULAR_MOVIES_URL);
                return true;

            case R.id.banks:
                item.setChecked(true);
                bankNiftyLists.clear();
                FAV_ROT = false;
                loadUrlData(TOP_RATED_MOVIES_URL);
                return true;

            case R.id.alldata:
                item.setChecked(true);
                bankNiftyLists.clear();
                FAV_ROT = true;
//                loadFavMovies();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                    for (int i = 35; i < 70; i++){
                        JSONObject jo = filteredDataArray.getJSONObject(i);

                        Double strikePrice = null;
                        int sPrice = 0;
                        if(strikePrice==null) {
                            JSONObject usBody = jo.getJSONObject("CE");
                            strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                            sPrice=strikePrice.intValue();
                        }
//                        String expiryDate = jo.getString("expiryDate");
                        if (sPrice<=ulValue+600 && sPrice>=ulValue-600) {
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

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),
                                    ceBody.getString("identifier").substring(25,32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),
                                    peBody.getString("identifier").substring(25,32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
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
                    Collections.sort(bankNiftyLists, new Comparator<BankNiftyList>() {
                        @Override
                        public int compare(BankNiftyList val1, BankNiftyList val2) {
                            if(val1.getCalloi() > val2.getCalloi()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });

                    adapter = new BankNiftyAdapter(bankNiftyLists, getApplicationContext());
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
                    Toast.makeText(BankNiftyActivity.this, "401 Error, connecting again...." + error.toString(), Toast.LENGTH_LONG).show();
                    checkNseUrl(URL_NSE);
                }
                Toast.makeText(BankNiftyActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authority", "www.nseindia.com");
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                params.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
                params.put("Accept-Encoding", "none");
                params.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
                params.put("Connection", "keep-alive");
                params.put("cookie", cookiedataMain);
                return params;
            }};

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

                Toast.makeText(BankNiftyActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    Runnable mRunnableTask = new Runnable()
    {
        @Override
        public void run() {
            bankNiftyLists.clear();
            checkNseUrl(URL_NSE);
            // this will repeat this task again at specified time interval
            mHandler.postDelayed(this, 5 * (60*1000));
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(PARCEL_KEY, (ArrayList<? extends
                Parcelable>) bankNiftyLists);
    }

//    private void setupViewModel() {
//        MovieViewModel viewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
//        viewModel.getBanksHistory().observe(this, new Observer<List<MovieList>>() {
//            @Override
//            public void onChanged(@Nullable List<MovieList> taskEntries) {
//                adapter.setMoviesLive(taskEntries);
//            }
//        });
//    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(FAV_ROT){
//            loadFavMovies();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
