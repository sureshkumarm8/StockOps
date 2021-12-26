package com.sureit.stockops.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sureit.stockops.R;
import com.sureit.stockops.adapter.BanksAdapter;

import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.data.MovieList;

import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;
import com.sureit.stockops.db.MovieDao;
import com.sureit.stockops.db.MovieDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.DB_NAME;
import static com.sureit.stockops.Util.Constants.FAV_ROT;
import static com.sureit.stockops.Util.Constants.PARCEL_KEY;

public class BanksListActivity extends AppCompatActivity implements VolleyJsonRespondsListener {

    private static final String LOG_TAG = "BanksListActivity";
    NoInternetDialog noInternetDialog;

    private RecyclerView recyclerView;
    private BanksAdapter adapter;
    private List<MovieList> movieLists;
    private List<BanksList> banksLists;

    private MovieDao mMovieDao;

    public static final String URL_BankNifty = "https://www.nseindia.com/api/option-chain-indices?symbol=BANKNIFTY";
    public static final String URL_BanksTradeInfo = "https://www.nseindia.com/api/quote-equity?symbol=";
    public static final String URL_NSE = "https://www.nseindia.com/";
    public static final String URL_MacFTPServer_BanksLiveData = "http://192.168.1.101:1313/Desktop/Suresh/Stock/liveQuotesData/banksData";
    public static final String URL_MacFTPServer_BankNiftyOIData = "http://192.168.1.101:1313/Desktop/Suresh/Stock/liveQuotesData/bankNifty.json";
    final Map<String, String> headers = new HashMap<String, String>();
    public String cookiedata;
    private int ceTotalTradedVolume;
    private int peTotalTradedVolume;
    private int ceTotalBuyQuantity;
    private int ceTotalSellQuantity;
    private int peTotalBuyQuantity;
    private int peTotalSellQuantity;
    private int ceOpenInterest;
    private int peOpenInterest;
    RelativeLayout relativeLayoutBanks;
    TextView strikePriceTVBanks;
    TextView totalVolumeCEBanks;
    TextView totalVolumePEBanks;
    TextView totalBuyQuantityCEBanks;
    TextView totalAskQuantityCEBanks;
    TextView totalBuyQuantityPEBanks;
    TextView totalAskQuantityPEBanks;
    TextView ceOpenInterestBanks;
    TextView peOpenInterestBanks;
    TextView timeStampBanks;
    private Double underlyingValue;
    private String timeStampValue;
    private MovieList movieList;
    private BanksList banksList;
    List<String> banksRetryList = new ArrayList<>();

    Handler mHandler = new Handler();
    private boolean bnkT = false;
    private boolean mainT = false;
    private boolean retry = false;
    List<String> bankNiftyOIdata = new ArrayList<>();
    private boolean macFTPfile = false;
    private ProgressDialog progressDialogFTP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bankslist);

        // initialize the View objects
        relativeLayoutBanks = findViewById(R.id.relativeLayoutRVbanks);
        strikePriceTVBanks = findViewById(R.id.tvStrikePriceBank);
        totalVolumeCEBanks = findViewById(R.id.tvVolumeCEbanks);
        totalVolumePEBanks = findViewById(R.id.tvVolumePEbanks);
        totalBuyQuantityCEBanks = findViewById(R.id.tvBuyQuantityCEbanks);
        totalAskQuantityCEBanks = findViewById(R.id.tvASKQuantityCEbanks);
        totalBuyQuantityPEBanks = findViewById(R.id.tvBuyQuantityPEbanks);
        totalAskQuantityPEBanks = findViewById(R.id.tvASKQuantityPEbanks);
        ceOpenInterestBanks = findViewById(R.id.tvStrikePrice2banks);
        peOpenInterestBanks = findViewById(R.id.tvStrikePrice3banks);
        timeStampBanks = findViewById(R.id.textViewTimeStamp);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing latest data....", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
//                checkNseUrl(URL_NSE);
//                loadBankNiftyUrlData(URL_BankNifty);
//                loadBanksTradeInfo();
                downloadBankNiftyOIDataFromMAC_FTP();
                downloadBanksLiveDataFromMAC_FTP();
            }
        });

        relativeLayoutBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(BanksListActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bankNiftyData", (Serializable) movieLists);
                    bundle.putSerializable("bankNiftyOIdata", (Serializable) bankNiftyOIdata);
                    myIntent.putExtras(bundle);
                    BanksListActivity.this.startActivity(myIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView = findViewById(R.id.recyclerViewBanks);
        recyclerView.setHasFixedSize(true);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }
        movieLists = new ArrayList<>();
        banksLists = new ArrayList<>();
        noInternetDialog = new NoInternetDialog.Builder(this).build();

        mMovieDao = Room.databaseBuilder(this, MovieDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getMovieDao();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PARCEL_KEY)) {
                movieLists = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }

//        checkNseUrl(URL_NSE);
//        loadBankNiftyUrlData(URL_BankNifty);
//        loadBanksTradeInfo();
        progressDialogFTP = new ProgressDialog(this);
        progressDialogFTP.setMessage("Downloading data from FTP ...");
        progressDialogFTP.show();
        downloadBankNiftyOIDataFromMAC_FTP();
        downloadBanksLiveDataFromMAC_FTP();
        // Call this to start the task first time
        mHandler.postDelayed(mRunnableTask, 5 * (60 * 1000));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.banknifty:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                progressDialogFTP.show();
                movieLists.clear();
                downloadBankNiftyOIDataFromMAC_FTP();
                return true;

            case R.id.banks:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                progressDialogFTP.show();
                banksLists.clear();
                downloadBanksLiveDataFromMAC_FTP();
                return true;

            case R.id.alldata:
                item.setChecked(true);
                movieLists.clear();
                banksLists.clear();
                progressDialogFTP.show();
                downloadBankNiftyOIDataFromMAC_FTP();
                downloadBanksLiveDataFromMAC_FTP();
                return true;

            case R.id.loadFTP:
                item.setChecked(true);
                banksLists.clear();
                progressDialogFTP.show();
                downloadBanksLiveDataFromMAC_FTP();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadBanksLiveDataFromMAC_FTP() {

        /*
        1. Start FTP server :  http-server ./ -p 1313
        2. Strart Node js: suresh@Suresh:~/Desktop/Suresh/Stock/stock-market-india$node app.js 3000
        3. Run Scripts:
            suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$python3 bankNiftydata.py
            suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$sh banksLiveQuotes.sh
        https://github.com/jugaad-py/jugaad-data
        https://github.com/maanavshah/stock-market-india
        */

        macFTPfile = true;
        progressDialogFTP.show();
        try {
            for(int i=1; i<= 3; i++){
                new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,"Banks", URL_MacFTPServer_BanksLiveData +i+".json", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadBankNiftyOIDataFromMAC_FTP() {

        /*
        1. Start FTP server :  http-server ./ -p 1313
        2. Strart Node js: suresh@Suresh:~/Desktop/Suresh/Stock/stock-market-india$node app.js 3000
        3. Run Screipt: suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$sh banksLiveQuotes.sh
        https://github.com/maanavshah/stock-market-india
        */
        progressDialogFTP.show();
        try {
                new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,"BankNifty", URL_MacFTPServer_BankNiftyOIData, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadBanksTradeInfo() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("All Banks Live Data Loading...");
        progressDialog.show();

        final String[] banks = {"AUBANK", "RBLBANK", "BANDHANBNK", "FEDERALBNK", "IDFCFIRSTB", "PNB", "INDUSINDBK", "AXISBANK", "SBIN", "KOTAKBANK", "ICICIBANK", "HDFCBANK"};
        try {
            for(int i=0; i< banks.length; i++){
                new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,banks[i],URL_BanksTradeInfo + banks[i] + "&section=trade_info", cookiedata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new BanksAdapter(banksLists, getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    private void loadBankNiftyUrlData(String bankNiftyUrl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("BankNifty Main Data Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, bankNiftyUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject recordDetails = jsonObject.getJSONObject("records");
                    JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                    JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
                    timeStampValue = recordDetails.get("timestamp").toString();
                    underlyingValue = recordDetails.getDouble("underlyingValue");
                    int ulValue = underlyingValue.intValue();
                    for (int i = 35; i < 70; i++) {
                        JSONObject jo = filteredDataArray.getJSONObject(i);

                        Double strikePrice = null;
                        int sPrice = 0;
                        if (strikePrice == null) {
                            JSONObject usBody = jo.getJSONObject("CE");
                            strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                            sPrice = strikePrice.intValue();
                        }
                        if (sPrice <= ulValue + 600 && sPrice >= ulValue - 600) {
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

                            movieList = new MovieList(ceBody.getString("identifier").substring(25, 32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            movieLists.add(movieList);
                            movieList = new MovieList(peBody.getString("identifier").substring(25, 32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            movieLists.add(movieList);
                        }
                    }
                    bankNiftyOIdata.add(timeStampValue);
                    bankNiftyOIdata.add(String.valueOf(underlyingValue));
                    bankNiftyOIdata.add(String.valueOf(ceTotalTradedVolume / 1000));
                    bankNiftyOIdata.add(String.valueOf(peTotalTradedVolume / 1000));
                    bankNiftyOIdata.add(String.valueOf(ceTotalBuyQuantity /1000));
                    bankNiftyOIdata.add(String.valueOf(ceTotalSellQuantity /1000));
                    bankNiftyOIdata.add(String.valueOf(peTotalBuyQuantity /1000));
                    bankNiftyOIdata.add(String.valueOf(peTotalSellQuantity /1000));
                    bankNiftyOIdata.add(String.valueOf(ceOpenInterest /1000));
                    bankNiftyOIdata.add(String.valueOf(peOpenInterest /1000));

                    timeStampBanks.setText(timeStampValue);
                    strikePriceTVBanks.setText(String.valueOf(underlyingValue));
                    totalVolumeCEBanks.setText(String.valueOf(ceTotalTradedVolume / 1000));
                    totalVolumePEBanks.setText(String.valueOf(peTotalTradedVolume / 1000));
                    totalBuyQuantityCEBanks.setText(String.valueOf(ceTotalBuyQuantity / 1000));
                    totalAskQuantityCEBanks.setText(String.valueOf(ceTotalSellQuantity / 1000));
                    totalBuyQuantityPEBanks.setText(String.valueOf(peTotalBuyQuantity / 1000));
                    totalAskQuantityPEBanks.setText(String.valueOf(peTotalSellQuantity / 1000));
                    ceOpenInterestBanks.setText(String.valueOf(ceOpenInterest / 1000));
                    peOpenInterestBanks.setText(String.valueOf(peOpenInterest / 1000));
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(BanksListActivity.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    //handler error 401 unauthorized from here
                    progressDialog.dismiss();
//                    Toast.makeText(BanksListActivity.this, "BankNifty Main Data: 401 Error, connecting again...." + error.toString(), Toast.LENGTH_LONG).show();
                }
                if (!mainT) {
                    Toast mainToast = Toast.makeText(BanksListActivity.this, "BankNifty Error" + error.toString(), Toast.LENGTH_SHORT);
                    mainToast.getView().setBackgroundColor(Color.TRANSPARENT);
                    mainToast.show();
                    mainT = true;
                }

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
                params.put("cookie", cookiedata);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void checkNseUrl(String nseurl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking NSE...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, nseurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    progressDialog.dismiss();
//                        loadUrlData(URL_BankNifty);
//                        loadBanksTradeInfo();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(BanksListActivity.this, "NSE Error" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // since we don't know which of the two underlying network vehicles
                // will Volley use, we have to handle and store session cookies manually
                Map<String, String> responseHeaders = response.headers;
                cookiedata = responseHeaders.get("Set-Cookie");
                return super.parseNetworkResponse(response);
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        progressDialog.dismiss();
    }

    private void retryFailedBanksTradeInfo(List<String> banksRetryList) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("All Banks Live Data Loading...");
        progressDialog.show();
        try {
            for(int i=0; i< banksRetryList.size(); i++){
                Thread.sleep(10000);
                new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this, banksRetryList.get(i),URL_BanksTradeInfo + banksRetryList.get(i) + "&section=trade_info", cookiedata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new BanksAdapter(banksLists, getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(PARCEL_KEY, (ArrayList<? extends
                Parcelable>) movieLists);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (FAV_ROT) {
//            loadFavMovies();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setupViewModel() {
        BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<BanksList>>() {
            @Override
            public void onChanged(@Nullable List<BanksList> taskEntries) {
                new BanksAdapter(taskEntries,getApplicationContext());
            }
        });
    }

    Runnable mRunnableTask = new Runnable() {
        @Override
        public void run() {
//            checkNseUrl(URL_NSE);
//            loadBankNiftyUrlData(URL_BankNifty);
//            loadBanksTradeInfo();
            movieLists.clear();
            banksLists.clear();
            downloadBankNiftyOIDataFromMAC_FTP();
            downloadBanksLiveDataFromMAC_FTP();
            // this will repeat this task again at specified time interval
            mHandler.postDelayed(this, 5 * (60 * 1000));
        }
    };


    @Override
    public void onSuccessJson(String response, String type) {
        if (type.equals("BankNifty")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject recordDetails = jsonObject.getJSONObject("records");
                JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
                timeStampValue = recordDetails.get("timestamp").toString();
                underlyingValue = recordDetails.getDouble("underlyingValue");
                int ulValue = underlyingValue.intValue();
                for (int i = 35; i < 70; i++) {
                    JSONObject jo = filteredDataArray.getJSONObject(i);

                    Double strikePrice = null;
                    int sPrice = 0;
                    if (strikePrice == null) {
                        JSONObject usBody = jo.getJSONObject("CE");
                        strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                        sPrice = strikePrice.intValue();
                    }
                    if (sPrice <= ulValue + 600 && sPrice >= ulValue - 600) {
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

                        movieList = new MovieList(ceBody.getString("identifier").substring(25, 32),
                                ceBody.getLong("totalTradedVolume"),
                                ceBody.getLong("totalBuyQuantity"),
                                ceBody.getLong("totalSellQuantity"),
                                ceBody.getLong("openInterest"),
                                ceBody.getDouble("pchangeinOpenInterest")
                        );
                        movieLists.add(movieList);
                        movieList = new MovieList(peBody.getString("identifier").substring(25, 32),
                                peBody.getLong("totalTradedVolume"),
                                peBody.getLong("totalBuyQuantity"),
                                peBody.getLong("totalSellQuantity"),
                                peBody.getLong("openInterest"),
                                peBody.getDouble("pchangeinOpenInterest")
                        );
                        movieLists.add(movieList);
                    }
                }
                bankNiftyOIdata.add(timeStampValue);
                bankNiftyOIdata.add(String.valueOf(underlyingValue));
                bankNiftyOIdata.add(String.valueOf(ceTotalTradedVolume / 1000));
                bankNiftyOIdata.add(String.valueOf(peTotalTradedVolume / 1000));
                bankNiftyOIdata.add(String.valueOf(ceTotalBuyQuantity /1000));
                bankNiftyOIdata.add(String.valueOf(ceTotalSellQuantity /1000));
                bankNiftyOIdata.add(String.valueOf(peTotalBuyQuantity /1000));
                bankNiftyOIdata.add(String.valueOf(peTotalSellQuantity /1000));
                bankNiftyOIdata.add(String.valueOf(ceOpenInterest /1000));
                bankNiftyOIdata.add(String.valueOf(peOpenInterest /1000));

                timeStampBanks.setText(timeStampValue);
                strikePriceTVBanks.setText(String.valueOf(underlyingValue));
                totalVolumeCEBanks.setText(String.valueOf(ceTotalTradedVolume / 1000));
                totalVolumePEBanks.setText(String.valueOf(peTotalTradedVolume / 1000));
                totalBuyQuantityCEBanks.setText(String.valueOf(ceTotalBuyQuantity / 1000));
                totalAskQuantityCEBanks.setText(String.valueOf(ceTotalSellQuantity / 1000));
                totalBuyQuantityPEBanks.setText(String.valueOf(peTotalBuyQuantity / 1000));
                totalAskQuantityPEBanks.setText(String.valueOf(peTotalSellQuantity / 1000));
                ceOpenInterestBanks.setText(String.valueOf(ceOpenInterest / 1000));
                peOpenInterestBanks.setText(String.valueOf(peOpenInterest / 1000));

                ceTotalTradedVolume=0;
                peTotalTradedVolume=0;
                ceTotalBuyQuantity=0;
                ceTotalSellQuantity=0;
                peTotalBuyQuantity=0;
                peTotalSellQuantity=0;
                ceOpenInterest=0;
                peOpenInterest=0;
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(BanksListActivity.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, e.getMessage(), e);
            }

        } else {
            long totalBuyQuantity = 0;
            long totalSellQuantity = 0;
            long quantityTraded = 0;
            long deliveryQuantity = 0;
            double deliveryPercent = 0.0;
            String bankName = "";
//        if(!macFTPfile) {
//            try {
//                Thread.sleep(2000);
//                JSONObject jsonObject = new JSONObject(response);
//
//                JSONObject marketDeptOrderBook = jsonObject.getJSONObject("marketDeptOrderBook");
//                String totalBuyQuantityStr = marketDeptOrderBook.getString("totalBuyQuantity");
//                totalBuyQuantity = Long.parseLong(totalBuyQuantityStr.trim()) / 1000;
//                String totalSellQuantityStr = marketDeptOrderBook.getString("totalSellQuantity");
//                totalSellQuantity = Long.parseLong(totalSellQuantityStr.trim()) / 1000;
//
//                JSONObject securityWiseDP = jsonObject.getJSONObject("securityWiseDP");
//                String quantityTradedStr = securityWiseDP.getString("quantityTraded");
//                quantityTraded = Long.parseLong(quantityTradedStr.trim()) / 1000;
//                String deliveryQuantityStr = securityWiseDP.getString("deliveryQuantity");
//                deliveryQuantity = Long.parseLong(deliveryQuantityStr.trim()) / 1000;
//
//                banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, quantityTraded, deliveryQuantity);
//                banksLists.add(banksList);
//
//                //add new data to database
//                long tsLong = System.currentTimeMillis() / 1000;
//                String ts = Long.toString(tsLong);
//                BanksDao banksDao = BanksDatabase.getInstance(getApplicationContext()).notes();
//                banksList = new BanksList(ts, bankName, totalBuyQuantity, totalSellQuantity, quantityTraded, deliveryQuantity);
//                banksDao.insertBankData(banksList);
//
//            } catch (JSONException | InterruptedException e) {
//                e.printStackTrace();
//                Log.e(LOG_TAG, e.getMessage(), e);
//            }
//        }else{
            try {
//                Thread.sleep(2000);
                JSONObject jsonObject = new JSONObject(response);
                JSONArray banksArr = jsonObject.getJSONArray("data");
                for (int i = 0; i < banksArr.length(); i++) {
                    JSONObject jo = banksArr.getJSONObject(i);
                    bankName = jo.getString("symbol");
                    String totalBuyQuantityStr = jo.getString("totalBuyQuantity");
                    totalBuyQuantity = parseToLongfrom_(totalBuyQuantityStr.trim());
                    String totalSellQuantityStr = jo.getString("totalSellQuantity");
                    totalSellQuantity = parseToLongfrom_(totalSellQuantityStr.trim());

                    String quantityTradedStr = jo.getString("quantityTraded");
                    quantityTraded = parseToLongfrom_(quantityTradedStr.trim());
                    String deliveryQuantityStr = jo.getString("deliveryQuantity");
                    deliveryQuantity = parseToLongfrom_(deliveryQuantityStr.trim());

                    String deliveryToTradedQuantityStr = jo.getString("deliveryToTradedQuantity");
                    deliveryPercent = Double.parseDouble(deliveryToTradedQuantityStr.trim());

                    banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, quantityTraded, deliveryQuantity, deliveryPercent);
                    banksLists.add(banksList);

                    //add new data to database
                    SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
                    String currentTime = dateFormat.format(new Date()).toString();

//                    long tsLong = System.currentTimeMillis() / 1000;
//                    String ts = Long.toString(tsLong);

                    BanksDao banksDao = BanksDatabase.getInstance(getApplicationContext()).notes();
                    banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, quantityTraded, deliveryQuantity, deliveryPercent);
                    banksDao.insertBankData(banksList);
                }
                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                macFTPfile = false;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage(), e);
            }
//        }
        }
        progressDialogFTP.dismiss();
    }
    private Long parseToLongfrom_(String trim) {
        if(trim.length()>2){
            trim = trim.replaceAll(",","");
           return Long.parseLong(trim)/1000;
        }else{
            return 0L;
        }
    }

    @Override
    public void onFailureJson(int responseCode, String msg, String retryBank) {
        progressDialogFTP.dismiss();
        Toast.makeText(BanksListActivity.this, retryBank + "Error:" + msg, Toast.LENGTH_LONG).show();
        banksRetryList.add(retryBank);
//        if(!retry){
//            retry=true;
//            Toast.makeText(BanksListActivity.this, "banksRetryList :" + banksRetryList.toString(), Toast.LENGTH_LONG).show();
////            retryFailedBanksTradeInfo(banksRetryList);
//        }
    }
}


