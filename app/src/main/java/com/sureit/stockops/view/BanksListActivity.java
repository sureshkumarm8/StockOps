package com.sureit.stockops.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.Util.StockDataRetrieveService;
import com.sureit.stockops.adapter.BanksAdapter;

import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;

import com.sureit.stockops.db.BankNiftyDao;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.FAV_ROT;
import static com.sureit.stockops.Util.Constants.PARCEL_KEY;

public class BanksListActivity extends AppCompatActivity implements VolleyJsonRespondsListener {

    private static final String LOG_TAG = "BanksListActivity";
    NoInternetDialog noInternetDialog;

    private RecyclerView recyclerView;
    private BanksAdapter adapter;
    private List<BankNiftyList> bankNiftyLists;
    private List<BanksList> banksLists;

    BanksDao banksDao;
    BankNiftyDao bankNiftyDao;

    public static final String URL_BankNifty = "https://www.nseindia.com/api/option-chain-indices?symbol=BANKNIFTY";
    public static final String URL_BanksTradeInfo = "https://www.nseindia.com/api/quote-equity?symbol=";
    public static final String URL_NSE = "https://www.nseindia.com/";
    public static String URL_MacFTPServer_BanksLiveData = "http://192.168.43.251:1313/Desktop/Suresh/Stock/liveQuotesData/banksData";
    public static String URL_MacFTPServer_BankNiftyOIData = "http://192.168.43.251:1313/Desktop/Suresh/Stock/liveQuotesData/bankNifty.json";
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
    private BankNiftyList bankNiftyList;
    private BanksList banksList;
    List<String> banksRetryList = new ArrayList<>();

    Handler mHandler = new Handler();
    private boolean bnkT = false;
    private boolean mainT = false;
    private boolean retry = false;
    List<String> bankNiftyOIdata = new ArrayList<>();
    private boolean macFTPfile = false;
    private ProgressDialog progressDialogFTP;
    long allBanksBuyQuantity;
    long allBanksSellQuantity;
    long allBanksQuantityTraded;
    long allBanksDeliveryQuantity;
    double allBanksDeliveryPercent;
    RelativeLayout relativeLayout_allBanksTotalRV;
    TextView bankNameAllTV;
    TextView totalBuyQuantityCEAllTV;
    TextView totalAskQuantityPEAllTV;
    TextView totalTradedAllTV;
    TextView totalDeliveryAllTV;
    TextView totalDeliveryAllPCTV;
    private int retrievesDataCompleted=0;
    private BanksViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bankslist);

        bankNiftyLists = new ArrayList<>();
        banksLists = new ArrayList<>();
        noInternetDialog = new NoInternetDialog.Builder(this).build();

        banksDao = BanksDatabase.getInstance(getApplicationContext()).getBanks();
        bankNiftyDao = BanksDatabase.getInstance(getApplicationContext()).getBankNiftyCP();
        viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);

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

        // initialize the View objects for All banks total
        relativeLayout_allBanksTotalRV = findViewById(R.id.allBanksTotalRV);
        bankNameAllTV = findViewById(R.id.tvBankNameAll);
        totalBuyQuantityCEAllTV = findViewById(R.id.tvTotalBuyQuantityBankAll);
        totalAskQuantityPEAllTV = findViewById(R.id.tvpTotalSellQuantityBankAll);
        totalTradedAllTV = findViewById(R.id.tvQuantityTradedBankAll);
        totalDeliveryAllTV = findViewById(R.id.tvDeliveryQtyBankAll);
        totalDeliveryAllPCTV = findViewById(R.id.tvDeliveryPCTAll);

        recyclerView = findViewById(R.id.recyclerViewBanks);
        recyclerView.setHasFixedSize(true);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing latest data....", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
//                checkNseUrl(URL_NSE);
//                loadBankNiftyUrlData(URL_BankNifty);
//                loadBanksTradeInfo();
                bankNiftyLists.clear();
                banksLists.clear();
                liveDisplayUI();
//                downloadBankNiftyOIDataFromMAC_FTP();
//                downloadBanksLiveDataFromMAC_FTP();
            }
        });

        strikePriceTVBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(BanksListActivity.this, BankNiftyActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("bankNiftyData", (Serializable) bankNiftyLists);
//                    bundle.putSerializable("bankNiftyOIdata", (Serializable) bankNiftyOIdata);
                    myIntent.putExtras(bundle);
                    BanksListActivity.this.startActivity(myIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //open Option Chain Details List
        relativeLayout_allBanksTotalRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"All Banks");
                v.getContext().startActivity(skipIntent);
            }
        });

        //Open CE PE History
        ceOpenInterestBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"OI History");
                v.getContext().startActivity(skipIntent);
            }
        });
        peOpenInterestBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"OI History");
                v.getContext().startActivity(skipIntent);
            }
        });

        totalVolumeCEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"CE History");
                v.getContext().startActivity(skipIntent);
            }
        });
        totalBuyQuantityCEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"CE History");
                v.getContext().startActivity(skipIntent);
            }
        });
        totalAskQuantityCEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"CE History");
                v.getContext().startActivity(skipIntent);
            }
        });

        totalVolumePEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"PE History");
                v.getContext().startActivity(skipIntent);
            }
        });
        totalBuyQuantityPEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"PE History");
                v.getContext().startActivity(skipIntent);
            }
        });
        totalAskQuantityPEBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,"PE History");
                v.getContext().startActivity(skipIntent);
            }
        });



        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PARCEL_KEY)) {
                bankNiftyLists = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }

//        checkNseUrl(URL_NSE);
//        loadBankNiftyUrlData(URL_BankNifty);
//        loadBanksTradeInfo();
        checkFTPURL("http://192.168.43.251:1313/Desktop/Suresh/");
        Intent intent = new Intent(getApplicationContext(), StockDataRetrieveService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }
        progressDialogFTP = new ProgressDialog(this);
        progressDialogFTP.setMessage("Downloading data from FTP ...");
        progressDialogFTP.show();
//        downloadBankNiftyOIDataFromMAC_FTP();
//        downloadBanksLiveDataFromMAC_FTP();
//        liveDisplayUI();
        // Call this to start the task first time
        mHandler.postDelayed(mRunnableTask, (5 * 1000));
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

            case R.id.newIP:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                getNewIP();
                return true;

            case R.id.banknifty:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                progressDialogFTP.show();
                bankNiftyLists.clear();
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
                bankNiftyLists.clear();
                banksLists.clear();
                progressDialogFTP.show();
                downloadBankNiftyOIDataFromMAC_FTP();
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
        deleteCache(this);
        macFTPfile = true;
        progressDialogFTP.show();
        try {
            for(int i=1; i<= 3; i++){
                new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,"Banks", URL_MacFTPServer_BanksLiveData +i+".json", null);
            }
//            new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,"StrikePrice", URL_MacFTPServer_BanksLiveData +"StrikeP.json", null);
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
        deleteCache(this);
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

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),
                                    ceBody.getString("identifier").substring(25, 32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),
                                    peBody.getString("identifier").substring(25, 32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
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
                Parcelable>) bankNiftyLists);
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
        viewModel.getBanksHistory().observe(this, new Observer<List<BanksList>>() {
            @Override
            public void onChanged(@Nullable List<BanksList> taskEntries) {
                new BanksAdapter(taskEntries,getApplicationContext());
            }
        });
    }

    private int minsCount=0;
    Runnable mRunnableTask = new Runnable() {
        @Override
        public void run() {
//            checkNseUrl(URL_NSE);
//            loadBankNiftyUrlData(URL_BankNifty);
//            loadBanksTradeInfo();
            progressDialogFTP.dismiss();
            bankNiftyLists.clear();
            banksLists.clear();
            String start = "09:15";
            Date marketOpen=null;
            String limit = "15:17";
            Date marketClose=null;
            Date now = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
            String currentTime = dateFormat.format(new Date()).toString();
            try {
                marketOpen = dateFormat.parse(start);
                marketClose = dateFormat.parse(limit);
                now = dateFormat.parse(currentTime);
                if (now.after(marketClose)||now.before(marketOpen)){
                    mHandler.removeCallbacksAndMessages(null);
                    liveDisplayUI();
                }else{
//                    downloadBanksLiveDataFromMAC_FTP();
                    progressDialogFTP = new ProgressDialog(BanksListActivity.this);
                    progressDialogFTP.setMessage("Refreshing latest data from FTP ...");
                    progressDialogFTP.show();
                    liveDisplayUI();
                    minsCount++;
                    if(minsCount == 5){
                        downloadBankNiftyOIDataFromMAC_FTP();
                        minsCount=0;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // this will repeat this task again at specified time interval
            mHandler.postDelayed(this, (60 * 1000));
        }
    };


    @Override
    public void onSuccessJson(String response, String type) {
//        if(type.equals("StrikePrice")){
//            JSONObject jsonObject = null;
//            try {
//                jsonObject = new JSONObject(response);
//                JSONArray indices = jsonObject.getJSONArray("data");
//                JSONObject jo = indices.getJSONObject(3);
//                strikePriceTVBanks.setText(jo.getString("last"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
        if (type.equals("BankNifty")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject recordDetails = jsonObject.getJSONObject("records");
                JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
                timeStampValue = recordDetails.get("timestamp").toString();
                underlyingValue = recordDetails.getDouble("underlyingValue");
                int ulValue = underlyingValue.intValue();
                for (int i = 35; i < 86; i++) {
                    JSONObject jo = filteredDataArray.getJSONObject(i);

                    Double strikePrice = null;
                    int sPrice = 0;
                    if (strikePrice == null) {
                        JSONObject usBody = jo.getJSONObject("CE");
                        strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                        sPrice = strikePrice.intValue();
                    }
                    if (sPrice <= ulValue + 700 && sPrice >= ulValue - 700) {
                        JSONObject ceBody = jo.getJSONObject("CE");
                        JSONObject peBody = jo.getJSONObject("PE");

                        if(sPrice>ulValue-200) {

                            //Add total values for main top card
                            ceTotalTradedVolume += (int) ceBody.get("totalTradedVolume");
                            ceTotalBuyQuantity += (int) ceBody.get("totalBuyQuantity");
                            ceTotalSellQuantity += (int) ceBody.get("totalSellQuantity");
                            ceOpenInterest += (int) ceBody.get("openInterest");

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                    ceBody.getString("identifier").substring(25, 32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        }
                        if(sPrice<ulValue-200) {

                            //Add total values for main top card
                            peTotalTradedVolume += (int) peBody.get("totalTradedVolume");
                            peTotalBuyQuantity += (int) peBody.get("totalBuyQuantity");
                            peTotalSellQuantity += (int) peBody.get("totalSellQuantity");
                            peOpenInterest += (int) peBody.get("openInterest");

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                    peBody.getString("identifier").substring(25, 32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        }
                    }
                }

                //Add data for "OI History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"OI History", (long) (ceOpenInterest / 1000), (long) (peOpenInterest / 1000), (long) (ceTotalTradedVolume / 1000),(long) (peTotalTradedVolume / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);

                //Add data for "CE History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"CE History", (long) (ceTotalBuyQuantity / 1000), (long) (ceTotalSellQuantity / 1000), (long) (ceTotalTradedVolume / 1000),(long) (ceOpenInterest / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);

                //Add data for "PE History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"PE History", (long) (peTotalBuyQuantity / 1000), (long) (peTotalSellQuantity / 1000), (long) (peTotalTradedVolume / 1000),(long) (peOpenInterest / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);


                // Storing data into SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("NiftyOILiveDisplaySP", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("timeStampValue", timeStampValue);
                editor.putString("underlyingValue", String.valueOf(underlyingValue));
                editor.putString("ceTotalTradedVolume", String.valueOf(ceTotalTradedVolume / 1000));
                editor.putString("peTotalTradedVolume", String.valueOf(peTotalTradedVolume / 1000));
                editor.putString("ceTotalBuyQuantity", String.valueOf(ceTotalBuyQuantity / 1000));
                editor.putString("ceTotalSellQuantity", String.valueOf(ceTotalSellQuantity / 1000));
                editor.putString("peTotalBuyQuantity", String.valueOf(peTotalBuyQuantity / 1000));
                editor.putString("peTotalSellQuantity", String.valueOf(peTotalSellQuantity / 1000));
                editor.putString("ceOpenInterest", String.valueOf(ceOpenInterest / 1000));
                editor.putString("peOpenInterest", String.valueOf(peOpenInterest / 1000));
                Gson gson = new Gson();
                String json = gson.toJson(bankNiftyLists);
                editor.putString("bankNiftyData", json);
                editor.commit();

                ceTotalTradedVolume=0;
                peTotalTradedVolume=0;
                ceTotalBuyQuantity=0;
                ceTotalSellQuantity=0;
                peTotalBuyQuantity=0;
                peTotalSellQuantity=0;
                ceOpenInterest=0;
                peOpenInterest=0;
                bankNiftyLists.clear();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(BanksListActivity.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, e.getMessage(), e);
            }

        } else {
            long totalBuyQuantity = 0;
            long totalSellQuantity = 0;
            long tradedVolume = 0;
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
//                String quantityTradedStr = securityWiseDP.getString("tradedVolume");
//                tradedVolume = Long.parseLong(quantityTradedStr.trim()) / 1000;
//                String deliveryQuantityStr = securityWiseDP.getString("deliveryQuantity");
//                deliveryQuantity = Long.parseLong(deliveryQuantityStr.trim()) / 1000;
//
//                banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity);
//                banksLists.add(banksList);
//
//                //add new data to database
//                long tsLong = System.currentTimeMillis() / 1000;
//                String ts = Long.toString(tsLong);
//                BanksDao banksDao = BanksDatabase.getInstance(getApplicationContext()).notes();
//                banksList = new BanksList(ts, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity);
//                banksDao.insertBankNiftyData(banksList);
//
//            } catch (JSONException | InterruptedException e) {
//                e.printStackTrace();
//                Log.e(LOG_TAG, e.getMessage(), e);
//            }
//        }else{
            try {
                retrievesDataCompleted++;
                //add new data to database
//                SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
////                String currentTime = dateFormat.format(new Date()).toString();
                String currentTime;

//                Thread.sleep(2000);
                JSONObject jsonObject = new JSONObject(response);
                currentTime = jsonObject.getString("lastUpdateTime").substring(12,17);
                JSONArray banksArr = jsonObject.getJSONArray("data");
                for (int i = 0; i < banksArr.length(); i++) {
                    JSONObject jo = banksArr.getJSONObject(i);
                    bankName = jo.getString("symbol");
                    String totalBuyQuantityStr = jo.getString("totalBuyQuantity");
                    totalBuyQuantity = parseToLongfrom_(totalBuyQuantityStr.trim());
                    String totalSellQuantityStr = jo.getString("totalSellQuantity");
                    totalSellQuantity = parseToLongfrom_(totalSellQuantityStr.trim());

                    String tradedVolumeStr = jo.getString("totalTradedVolume");
                    tradedVolume = parseToLongfrom_(tradedVolumeStr.trim());
                    String deliveryQuantityStr = jo.getString("deliveryQuantity");
                    deliveryQuantity = parseToLongfrom_(deliveryQuantityStr.trim());

                    String deliveryToTradedQuantityStr = jo.getString("deliveryToTradedQuantity");
                    try {
                        deliveryPercent = Double.parseDouble(deliveryToTradedQuantityStr.trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        parseToLongfrom_(deliveryToTradedQuantityStr.trim());
                    }

                    //Live display Data
                    banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity, deliveryPercent);
                    banksLists.add(banksList);

                    allBanksBuyQuantity+=totalBuyQuantity;
                    allBanksSellQuantity+=totalSellQuantity;
                    allBanksQuantityTraded+=tradedVolume;
                    allBanksDeliveryQuantity+=deliveryQuantity;
                    allBanksDeliveryPercent+=deliveryPercent;

                    //To store in DB
                    banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity, deliveryPercent);
                    banksDao.insertBankData(banksList);
                }
                macFTPfile = false;
                if(retrievesDataCompleted==3){
                    banksList = new BanksList(currentTime, "All Banks", allBanksBuyQuantity, allBanksSellQuantity, allBanksQuantityTraded, allBanksDeliveryQuantity, allBanksDeliveryPercent);
                    banksDao.insertBankData(banksList);

                    // Storing data into SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("AllBanksLiveDisplaySP", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("All Banks", "All Banks");
                    editor.putString("allBanksBuyQuantity", String.valueOf(allBanksBuyQuantity));
                    editor.putString("allBanksSellQuantity", String.valueOf(allBanksSellQuantity));
                    editor.putString("allBanksQuantityTraded", String.valueOf(allBanksQuantityTraded));
                    editor.putString("allBanksDeliveryQuantity", String.valueOf(allBanksDeliveryQuantity));
                    editor.putFloat("allBanksDeliveryPercent", (float) allBanksDeliveryPercent);
                    Gson gson = new Gson();
                    String json = gson.toJson(banksLists);
                    editor.putString("BanksLiveDisplayData", json);
                    editor.commit();
//                    banksListActivityObj.liveDisplayUI();

                    allBanksBuyQuantity=0;
                    allBanksSellQuantity=0;
                    allBanksQuantityTraded=0;
                    allBanksDeliveryQuantity=0;
                    allBanksDeliveryPercent=0.0;
                    retrievesDataCompleted=0;
                    banksLists.clear();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage(), e);
            }
//        }
        }
    }

    private void setSentimentColorsBanksTotal() {
        try {
            BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
            List<BanksList> all_banks = viewModel.getBanksHistory("All Banks");
            int size = all_banks.size() - 1;
            if (all_banks.get(size).getTotalBuyQuantity() == all_banks.get(size - 1).getTotalBuyQuantity()) {
                totalBuyQuantityCEAllTV.setBackgroundColor(Color.WHITE);
            } else if (all_banks.get(size).getTotalBuyQuantity() > all_banks.get(size - 1).getTotalBuyQuantity()) {
                totalBuyQuantityCEAllTV.setBackgroundColor(Color.GREEN);
            } else {
                totalBuyQuantityCEAllTV.setBackgroundColor(Color.RED);
            }

            if (all_banks.get(size).getTotalSellQuantity() == all_banks.get(size - 1).getTotalSellQuantity()) {
                totalAskQuantityPEAllTV.setBackgroundColor(Color.WHITE);
            } else if (all_banks.get(size).getTotalSellQuantity() < all_banks.get(size - 1).getTotalSellQuantity()) {
                totalAskQuantityPEAllTV.setBackgroundColor(Color.GREEN);
            } else {
                totalAskQuantityPEAllTV.setBackgroundColor(Color.RED);
            }

            if (all_banks.get(size).getTotalBuyQuantity() > all_banks.get(size - 1).getTotalBuyQuantity()
                    && all_banks.get(size).getTotalSellQuantity() < all_banks.get(size - 1).getTotalSellQuantity()) {
                bankNameAllTV.setBackgroundColor(Color.GREEN);
            } else if (all_banks.get(size).getTotalBuyQuantity() < all_banks.get(size - 1).getTotalBuyQuantity()
                    && all_banks.get(size).getTotalSellQuantity() > all_banks.get(size - 1).getTotalSellQuantity()) {
                bankNameAllTV.setBackgroundColor(Color.RED);
            } else {
                bankNameAllTV.setBackgroundColor(Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSentimentColorsOI(String historyStr){
        try {
            BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
            List<BankNiftyList> oi_history = viewModel.getOIHistory(historyStr);
            int size = oi_history.size() - 1;

            switch (historyStr) {
                case "OI History":
                    if (oi_history.get(size).getCalloi() == oi_history.get(size - 1).getCalloi()) {
                        ceOpenInterestBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getCalloi() < oi_history.get(size - 1).getCalloi()) {
                        ceOpenInterestBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        ceOpenInterestBanks.setBackgroundColor(Color.RED);
                    }
                    if (oi_history.get(size).getPutoi() == oi_history.get(size - 1).getPutoi()) {
                        peOpenInterestBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getPutoi() > oi_history.get(size - 1).getPutoi()) {
                        peOpenInterestBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        peOpenInterestBanks.setBackgroundColor(Color.RED);
                    }
                    break;

                case "CE History":
                    if (oi_history.get(size).getCalloi() == oi_history.get(size - 1).getCalloi()) {
                        totalBuyQuantityCEBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getCalloi() > oi_history.get(size - 1).getCalloi()) {
                        totalBuyQuantityCEBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        totalBuyQuantityCEBanks.setBackgroundColor(Color.RED);
                    }
                    if (oi_history.get(size).getPutoi() == oi_history.get(size - 1).getPutoi()) {
                        totalAskQuantityCEBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getPutoi() < oi_history.get(size - 1).getPutoi()) {
                        totalAskQuantityCEBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        totalAskQuantityCEBanks.setBackgroundColor(Color.RED);
                    }
                    break;

                case "PE History":
                    if (oi_history.get(size).getCalloi() == oi_history.get(size - 1).getCalloi()) {
                        totalBuyQuantityPEBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getCalloi() > oi_history.get(size - 1).getCalloi()) {
                        totalBuyQuantityPEBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        totalBuyQuantityPEBanks.setBackgroundColor(Color.RED);
                    }
                    if (oi_history.get(size).getPutoi() == oi_history.get(size - 1).getPutoi()) {
                        totalAskQuantityPEBanks.setBackgroundColor(Color.WHITE);
                    } else if (oi_history.get(size).getPutoi() < oi_history.get(size - 1).getPutoi()) {
                        totalAskQuantityPEBanks.setBackgroundColor(Color.GREEN);
                    } else {
                        totalAskQuantityPEBanks.setBackgroundColor(Color.RED);
                    }
                    break;

                default:
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

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
    public void onFailureJson(int responseCode, String msg, String type) {
        progressDialogFTP.dismiss();
        Toast.makeText(BanksListActivity.this, type + "Error:" + msg, Toast.LENGTH_LONG).show();
        if (type.equals("BankNifty"))
            timeStampBanks.setText(msg);
        strikePriceTVBanks.setText(msg);
        ceOpenInterestBanks.setText(msg);
        peOpenInterestBanks.setText(msg);
        banksRetryList.add(type);
//        if(!retry){
//            retry=true;
//            Toast.makeText(BanksListActivity.this, "banksRetryList :" + banksRetryList.toString(), Toast.LENGTH_LONG).show();
////            retryFailedBanksTradeInfo(banksRetryList);
//        }
    }

    public static void deleteCache(Context context) {
        String start = "09:15";
        Date marketOpen=null;
        String limit = "15:17";
        Date marketClose=null;
        Date now = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
        String currentTime = dateFormat.format(new Date()).toString();
        try {
            marketOpen = dateFormat.parse(start);
            marketClose = dateFormat.parse(limit);
            now = dateFormat.parse(currentTime);
            if (now.after(marketClose)||now.before(marketOpen)){
                Log.v(LOG_TAG,"Don't delete cache during market close");
            }else{
                File dir = context.getCacheDir();
                deleteDir(dir);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void checkFTPURL(String ftpurl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking FTP URL...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, ftpurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                getNewIP();
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

    private void getNewIP() {
        final EditText taskEditText = new EditText(BanksListActivity.this);
        AlertDialog dialog = new AlertDialog.Builder(BanksListActivity.this)
                .setTitle("FTP IP")
                .setMessage("Enter new FTP IP")
                .setView(taskEditText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newIP = String.valueOf(taskEditText.getText());
                        URL_MacFTPServer_BanksLiveData = "http://"+ newIP +":1313/Desktop/Suresh/Stock/liveQuotesData/banksData";
                        URL_MacFTPServer_BankNiftyOIData = "http://"+ newIP +":1313/Desktop/Suresh/Stock/liveQuotesData/bankNifty.json";

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @SuppressLint("WrongConstant")
    public void liveDisplayUI(){
        banksLists.clear();

        movementTrackingBanks("All Banks");
        final String[] banks = {"AUBANK", "RBLBANK", "BANDHANBNK", "FEDERALBNK", "IDFCFIRSTB", "PNB", "INDUSINDBK", "AXISBANK", "SBIN", "KOTAKBANK", "ICICIBANK", "HDFCBANK"};
        try {
            for (int i = 0; i < banks.length; i++) {
                movementTrackingBanks(banks[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialogFTP.dismiss();
        SharedPreferences sh = getSharedPreferences("AllBanksLiveDisplaySP", MODE_APPEND);
        if(sh.getString("All Banks","").contains("All Banks")){
            Gson gson = new Gson();
            String json = sh.getString("BanksLiveDisplayData", "");
            Type type = new TypeToken<List<BanksList>>() {}.getType();
            banksLists = gson.fromJson(json, type);

            DecimalFormat df = new DecimalFormat("0.00");
            if (sh.getString("All Banks", "").contains("All Banks") && banksLists.size() > 0) {
                bankNameAllTV.setText("All Banks");
                totalBuyQuantityCEAllTV.setText(sh.getString("allBanksBuyQuantity", ""));
                totalAskQuantityPEAllTV.setText(sh.getString("allBanksSellQuantity", ""));
                totalTradedAllTV.setText(sh.getString("allBanksQuantityTraded", ""));
                totalDeliveryAllTV.setText(sh.getString("allBanksDeliveryQuantity", ""));
                allBanksDeliveryPercent = sh.getFloat("allBanksDeliveryPercent", 0);
                totalDeliveryAllPCTV.setText(df.format(allBanksDeliveryPercent / 12) + "%");
                setSentimentColorsOI("OI History");
                setSentimentColorsOI("CE History");
                setSentimentColorsOI("PE History");
                setSentimentColorsBanksTotal();

                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                int position = recyclerView.getAdapter().getItemCount() - 1;
                recyclerView.smoothScrollToPosition(position);
                adapter.notifyDataSetChanged();

            }
        }

            SharedPreferences shOI = getSharedPreferences("NiftyOILiveDisplaySP", MODE_APPEND);
            if(shOI.getString("timeStampValue","").length()>5) {
                timeStampBanks.setText(shOI.getString("timeStampValue", ""));
                strikePriceTVBanks.setText(shOI.getString("underlyingValue", ""));
                totalVolumeCEBanks.setText(shOI.getString("ceTotalTradedVolume", ""));
                totalVolumePEBanks.setText(shOI.getString("peTotalTradedVolume", ""));
                totalBuyQuantityCEBanks.setText(shOI.getString("ceTotalBuyQuantity", ""));
                totalAskQuantityCEBanks.setText(shOI.getString("ceTotalSellQuantity", ""));
                totalBuyQuantityPEBanks.setText(shOI.getString("peTotalBuyQuantity", ""));
                totalAskQuantityPEBanks.setText(shOI.getString("peTotalSellQuantity", ""));
                ceOpenInterestBanks.setText(shOI.getString("ceOpenInterest", ""));
                peOpenInterestBanks.setText(shOI.getString("peOpenInterest", ""));
            }

        movementTrackingNifty("OI History");
        movementTrackingNifty("CE History");
        movementTrackingNifty("PE History");
        try {
            String underlyingValue1 = shOI.getString("underlyingValue", "").substring(0, 5);
            int underlyingValue = Integer.parseInt(underlyingValue1)/100;
            underlyingValue = underlyingValue*100;
            for (int i = 1; i < 8; i++) {
                movementTrackingNifty("CE" + underlyingValue);
                movementTrackingNifty("PE" + underlyingValue);
                underlyingValue = underlyingValue+100;
            }
        } catch (Exception e) {
            Log.v("underlyingValue to int", e.getMessage());
        }

    }

    private void movementTrackingBanks(String bankName) {

        BanksList curBanksList = null;
        BanksList prevBanksList = null;

        try {
            List<BanksList> banksHistoryPrev = viewModel.getBanksHistory(bankName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 1) {
                curBanksList = banksHistoryPrev.get(bankHisLen);
                prevBanksList = banksHistoryPrev.get(bankHisLen - 1);
                addmValDataBanks(bankName, curBanksList, prevBanksList);
            } else {
                curBanksList = banksHistoryPrev.get(0);
                prevBanksList = banksHistoryPrev.get(0);
                addmValDataBanks(bankName, curBanksList, prevBanksList);
            }
        }catch (Exception e){
            Log.v("movementTrackingBanks",e.getMessage());
        }
    }

    private void addmValDataBanks(String bankName, BanksList curBanksList, BanksList prevBanksList) {
        double mValTBQ = getmVal(curBanksList.getTotalBuyQuantity(), prevBanksList.getTotalBuyQuantity());
        double mValTSQ = getmVal(curBanksList.getTotalSellQuantity(), prevBanksList.getTotalSellQuantity());
        double mVAlQTS = getmVal (curBanksList.getQuantityTradedsure() , prevBanksList.getQuantityTradedsure());
        double mValDQ = getmVal (curBanksList.getDeliveryQuantity() , prevBanksList.getDeliveryQuantity());
        double mValDP = getmVal(curBanksList.getDeliveryPercent() , prevBanksList.getDeliveryPercent());

        BanksList banksList = new BanksList(curBanksList.getTimeStamp(),
                bankName + "mval", curBanksList.getTotalBuyQuantity(), curBanksList.getTotalSellQuantity(),
                mValTBQ, mValTSQ, mVAlQTS, mValDQ, mValDP);
        banksDao.insertBankData(banksList);
    }

    private double getmVal(double curVal, double prevVal) {
        try{
            if(prevVal==0.0){
                return 0.0;
            }else {
                double mValD = curVal / prevVal;
                Log.v("mValDoubles", String.valueOf(mValD));
                return mValD;
            }
        }catch (Exception e){
            return 0.0;
        }
    }

    private void movementTrackingNifty(String oiName) {

        BankNiftyList curNiftyList = null;
        BankNiftyList prevNiftyList = null;

        try {
            List<BankNiftyList> banksHistoryPrev = viewModel.getOIHistory(oiName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 1) {
                curNiftyList = banksHistoryPrev.get(bankHisLen);
                prevNiftyList = banksHistoryPrev.get(bankHisLen - 1);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList);
            } else {
                curNiftyList = banksHistoryPrev.get(0);
                prevNiftyList = banksHistoryPrev.get(0);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList);
            }
        }catch (Exception e){
            Log.v("movementTrackingBanks",e.getMessage());
        }
    }

    private void addmValDataNifty(String oiName, BankNiftyList curNiftyList, BankNiftyList prevNiftyList) {
        double mValTBQ = getmVal(curNiftyList.getCalloi(), prevNiftyList.getCalloi());
        double mValTSQ = getmVal(curNiftyList.getPutoi(), prevNiftyList.getPutoi());
        double mVAlQTS = getmVal (curNiftyList.getBntotalbuyquantity() , prevNiftyList.getBntotalbuyquantity());
        double mValDQ = getmVal (curNiftyList.getBntotalsellquantity() , prevNiftyList.getBntotalsellquantity());
        double mValDP = getmVal(curNiftyList.getUnderlyvalue() , prevNiftyList.getUnderlyvalue());

        bankNiftyList = new BankNiftyList(curNiftyList.getTimestamp(),oiName+"mval", curNiftyList.getCalloi(),
                mValTBQ, mValTSQ,mVAlQTS,mValDQ,mValDP);
        bankNiftyDao.insertBankNiftyData(bankNiftyList);
    }

}


