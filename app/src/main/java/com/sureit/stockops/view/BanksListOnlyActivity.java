package com.sureit.stockops.view;

import static com.sureit.stockops.Util.Constants.FAV_ROT;
import static com.sureit.stockops.Util.Constants.PARCEL_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sureit.stockops.R;
import com.sureit.stockops.Util.CSVWriter;
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
import java.io.FileWriter;
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

public class BanksListOnlyActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "BanksListActivity";

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private BanksAdapter adapter;
    private List<BankNiftyList> bankNiftyLists;
    private List<BanksList> banksLists;

    BanksDao banksDao;
    BankNiftyDao bankNiftyDao;

    RelativeLayout relativeLayoutBanks;
    TextView strikePriceTVBanks;

    private Double underlyingValue;

    private BanksList banksList;


    Handler mHandler = new Handler();

    private boolean mainT = false;

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
    private int retrievesDataCompleted = 0;
    private BanksViewModel viewModel;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bankslist_only);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initUI();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PARCEL_KEY)) {
                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }

    }

    private void initUI() {
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView nv = (NavigationView) findViewById(R.id.navView);
        nv.setNavigationItemSelectedListener(this);

        bankNiftyLists = new ArrayList<>();
        banksLists = new ArrayList<>();


        banksDao = BanksDatabase.getInstance(getApplicationContext()).getBanks();
        bankNiftyDao = BanksDatabase.getInstance(getApplicationContext()).getBankNiftyCP();
        viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);

        // initialize the View objects
        relativeLayoutBanks = findViewById(R.id.relativeLayoutRVbanks);
        strikePriceTVBanks = findViewById(R.id.tvStrikePriceBank);

        // initialize the View objects for All banks total
        relativeLayout_allBanksTotalRV = findViewById(R.id.allBanksTotalRV);
        bankNameAllTV = findViewById(R.id.tvBankNameAll);
        totalBuyQuantityCEAllTV = findViewById(R.id.tvTotalBuyQuantityBankAll);
        totalAskQuantityPEAllTV = findViewById(R.id.tvpTotalSellQuantityBankAll);
        totalTradedAllTV = findViewById(R.id.tvQuantityTradedBankAll);
//        totalDeliveryAllTV = findViewById(R.id.tvDeliveryQtyBankAll);
//        totalDeliveryAllPCTV = findViewById(R.id.tvDeliveryPCTAll);

        recyclerView = findViewById(R.id.recyclerViewBanks);
        recyclerView.setHasFixedSize(true);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }

        @SuppressLint("WrongConstant")
        SharedPreferences sh = getSharedPreferences("AllBanksLiveDisplaySP", MODE_APPEND);
        if (sh.getString("All Banks", "").contains("All Banks")) {
            Gson gson = new Gson();
            String json = sh.getString("BanksLiveDisplayData", "");
            Type type = new TypeToken<List<BanksList>>() {
            }.getType();
            banksLists = gson.fromJson(json, type);

            DecimalFormat df = new DecimalFormat("0.00");
            if (sh.getString("All Banks", "").contains("All Banks") && banksLists.size() > 0) {
                bankNameAllTV.setText("All Banks");
                totalBuyQuantityCEAllTV.setText(sh.getString("allBanksBuyQuantity", ""));
                totalAskQuantityPEAllTV.setText(sh.getString("allBanksSellQuantity", ""));
                totalTradedAllTV.setText(sh.getString("allBanksQuantityTraded", ""));
                setSentimentColorsBanksTotal();

                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                int position = recyclerView.getAdapter().getItemCount() - 5;
                recyclerView.smoothScrollToPosition(position);
                adapter.notifyDataSetChanged();

            }
        }

        //open Option Chain Details List
        relativeLayout_allBanksTotalRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY, "All Banks");
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    Runnable mRunnableTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            progressDialogFTP.dismiss();
            bankNiftyLists.clear();
            banksLists.clear();
            //Live data update on UI using sharedPreferences
            liveDisplayUI();
            // this will repeat this task again at specified time interval
            mHandler.postDelayed(this, (45 * 1000));
        }
    };

    public boolean isMarketClosed() {
        String start = "08:55";
        Date marketOpen = null;
        String limit = "15:30";
        Date marketClose = null;
        Date now = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
        String currentTime = dateFormat.format(new Date()).toString();
        try {
            marketOpen = dateFormat.parse(start);
            marketClose = dateFormat.parse(limit);
            now = dateFormat.parse(currentTime);
            if (now.after(marketClose) || now.before(marketOpen)) {
//                mHandler.removeCallbacksAndMessages(null);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    dateFormat = new SimpleDateFormat("DD_MM_YYYY_HH_mm_");
//                    currentTime = dateFormat.format(new Date()).toString();
//                }
//                saveDbToCSV(currentTime+"BankNifty");
//                saveDbToCSV(currentTime+"AllBanks");
//                liveDisplayUI();
                return true;
            } else {
////                    downloadBanksLiveDataFromMAC_FTP();
//                progressDialogFTP = new ProgressDialog(BanksListActivity.this);
//                progressDialogFTP.setMessage("Refreshing latest data from FTP ...");
//                progressDialogFTP.show();
//                liveDisplayUI();
//                minsCount++;
//                if(minsCount == 5){
//                    mvlForBankNifty();
////                        downloadBankNiftyOIDataFromMAC_FTP();
//                    minsCount=0;
//                }
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
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
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setupViewModel() {
        BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
        viewModel.getBanksHistory().observe(this, new Observer<List<BanksList>>() {
            @Override
            public void onChanged(@Nullable List<BanksList> taskEntries) {
                new BanksAdapter(taskEntries, getApplicationContext());
            }
        });
    }


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
                currentTime = jsonObject.getString("lastUpdateTime").substring(12, 17);
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

                    allBanksBuyQuantity += totalBuyQuantity;
                    allBanksSellQuantity += totalSellQuantity;
                    allBanksQuantityTraded += tradedVolume;
                    allBanksDeliveryQuantity += deliveryQuantity;
                    allBanksDeliveryPercent += deliveryPercent;

                    //To store in DB
                    banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity, deliveryPercent);
                    banksDao.insertBankData(banksList);
                }

                if (retrievesDataCompleted == 3) {
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

                    allBanksBuyQuantity = 0;
                    allBanksSellQuantity = 0;
                    allBanksQuantityTraded = 0;
                    allBanksDeliveryQuantity = 0;
                    allBanksDeliveryPercent = 0.0;
                    retrievesDataCompleted = 0;
                    banksLists.clear();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage(), e);
            }
//        }

        liveDisplayUI();
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

            if (all_banks.get(size).getPercentDiff() == all_banks.get(size - 1).getPercentDiff()) {
                totalDeliveryAllPCTV.setBackgroundColor(Color.WHITE);
            } else if (all_banks.get(size).getPercentDiff() > all_banks.get(size - 1).getPercentDiff()) {
                totalDeliveryAllPCTV.setBackgroundColor(Color.GREEN);
            } else {
                totalDeliveryAllPCTV.setBackgroundColor(Color.RED);
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

    private Long parseToLongfrom_(String trim) {
        if (trim.length() > 2) {
            trim = trim.replaceAll(",", "");
            return Long.parseLong(trim) / 1000;
        } else {
            return 0L;
        }
    }

    @SuppressLint("WrongConstant")
    public void liveDisplayUI() {
        banksLists.clear();
        progressDialogFTP.dismiss();

        SharedPreferences sh = getSharedPreferences("AllBanksLiveDisplaySP", MODE_APPEND);
        if (sh.getString("All Banks", "").contains("All Banks")) {
            Gson gson = new Gson();
            String json = sh.getString("BanksLiveDisplayData", "");
            Type type = new TypeToken<List<BanksList>>() {
            }.getType();
            banksLists = gson.fromJson(json, type);

            DecimalFormat df = new DecimalFormat("0.00");
            if (sh.getString("All Banks", "").contains("All Banks") && banksLists.size() > 0) {
                bankNameAllTV.setText("All Banks");
                totalBuyQuantityCEAllTV.setText(sh.getString("allBanksBuyQuantity", ""));
                totalAskQuantityPEAllTV.setText(sh.getString("allBanksSellQuantity", ""));
                totalTradedAllTV.setText(sh.getString("allBanksQuantityTraded", ""));
//                totalDeliveryAllTV.setText(sh.getString("underlyingValue", ""));
                allBanksDeliveryPercent = sh.getFloat("allBanksDeliveryPercent", 0);
//                totalDeliveryAllPCTV.setText(df.format(allBanksDeliveryPercent / 12) + "%");
                strikePriceTVBanks.setText(sh.getString("underlyingValue", ""));
                setSentimentColorsBanksTotal();

                adapter = new BanksAdapter(banksLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                int position = recyclerView.getAdapter().getItemCount() - 5;
                recyclerView.smoothScrollToPosition(position);
                adapter.notifyDataSetChanged();

            }
        }


    }

    private void addmValDataBanks(String bankName, BanksList curBanksList, BanksList prevBanksList, BanksList prevBanksList2) {
        double mValTBQ = getmVal(curBanksList.getTotalBuyQuantity(), prevBanksList.getTotalBuyQuantity(), prevBanksList2.getTotalBuyQuantity());
        double mValTSQ = getmVal(curBanksList.getTotalSellQuantity(), prevBanksList.getTotalSellQuantity(), prevBanksList2.getTotalSellQuantity());
        double mVAlQTS = getmVal(curBanksList.getQuantityTradedsure(), prevBanksList.getQuantityTradedsure(), prevBanksList2.getQuantityTradedsure());
        double mValDQ = getmVal(curBanksList.getUnderlyingValue(), prevBanksList.getUnderlyingValue(), prevBanksList2.getUnderlyingValue());
        double mValDP = getmVal(curBanksList.getPercentDiff(), prevBanksList.getPercentDiff(), prevBanksList2.getPercentDiff());

        BanksList banksList = new BanksList(curBanksList.getTimeStamp(),
                bankName + "mval", curBanksList.getTotalBuyQuantity(), curBanksList.getTotalSellQuantity(),
                mValTBQ, mValTSQ, mVAlQTS, mValDQ, mValDP);
        banksDao.insertBankData(banksList);
    }

    private double getmVal(double curVal, double prevVal, double prevVal2) {
        try {
            if (prevVal == 0.0) {
                return 0.0;
            } else {
                DecimalFormat df = new DecimalFormat("0.00");
                double val = (curVal / prevVal);
                val = val * 100;
                double mValD = Double.parseDouble(df.format(val));
                Log.v("mValDoubles", String.valueOf(mValD));
                return mValD;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getmValPercent(double curVal, double prevVal, double prevVal2) {
        try {
            if (prevVal == 0.0) {
                return 0.0;
            } else {
                DecimalFormat df = new DecimalFormat("0.0");
                double prevPV = prevVal - prevVal2;
                if (prevPV == 0.0) prevPV = 0.01;
                double curPV = curVal - prevVal;
                if (curPV == 0.0) curPV = 0.01;
                double val = (curPV / prevPV);
                val = val * 100;
                double mValD = Double.parseDouble(df.format(val));
                Log.v("mValDoubles", String.valueOf(mValD));
                return mValD;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Home) {
            // Handle the camera action
        } else if (id == R.id.nav_BankNifty) {
            Intent myIntent = new Intent(BanksListOnlyActivity.this, BankNiftyActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("bankNiftyData", (Serializable) bankNiftyLists);
            myIntent.putExtras(bundle);
            BanksListOnlyActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_Banks) {
            Intent myIntent = new Intent(BanksListOnlyActivity.this, BanksListOnlyActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("bankNiftyData", (Serializable) bankNiftyLists);
//            myIntent.putExtras(bundle);
            BanksListOnlyActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_manage) {
            Intent myIntent = new Intent(BanksListOnlyActivity.this, NiftyOPsListActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

/*    private void mvlForBankNifty() {
        movementTrackingNifty("OI History");
        movementTrackingNifty("CE History");
        movementTrackingNifty("PE History");
        try {
            @SuppressLint("WrongConstant") SharedPreferences shUl = getSharedPreferences("NiftyOILiveDisplaySP", MODE_APPEND);
            String underlyingValue1 = shUl.getString("underlyingValue", "").substring(0, 5);
            int underlyingValue = Integer.parseInt(underlyingValue1) / 100;
            int underlyingValueCE = underlyingValue * 100;
            int underlyingValuePE = underlyingValueCE - 700;
            for (int i = 1; i < 8; i++) {
                movementTrackingNifty("CE" + underlyingValueCE);
                movementTrackingNifty("PE" + underlyingValuePE);
                underlyingValueCE += 100;
                underlyingValuePE += 100;
            }
        } catch (Exception e) {
            Log.v("underlyingValue to int", e.getMessage());
        }
    }

    private void movementTrackingBanks(String bankName) {

        BanksList curBanksList = null;
        BanksList prevBanksList = null;
        BanksList prevBanksList2 = null;

        try {
            List<BanksList> banksHistoryPrev = viewModel.getBanksHistory(bankName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 2) {
                curBanksList = banksHistoryPrev.get(bankHisLen - 1);
                prevBanksList = banksHistoryPrev.get(bankHisLen - 2);
                prevBanksList2 = banksHistoryPrev.get(bankHisLen - 3);
                addmValDataBanks(bankName, curBanksList, prevBanksList, prevBanksList2);
            } else if (bankHisLen > 1) {
                curBanksList = banksHistoryPrev.get(bankHisLen - 1);
                prevBanksList = banksHistoryPrev.get(0);
                prevBanksList2 = banksHistoryPrev.get(0);
                addmValDataBanks(bankName, curBanksList, prevBanksList, prevBanksList2);
            } else {
                curBanksList = banksHistoryPrev.get(0);
                prevBanksList = banksHistoryPrev.get(0);
                prevBanksList2 = banksHistoryPrev.get(0);
                addmValDataBanks(bankName, curBanksList, prevBanksList, prevBanksList2);
            }
        } catch (Exception e) {
            Log.v("movementTrackingBanks", e.getMessage());
        }
    }

    private void movementTrackingNifty(String oiName) {

        BankNiftyList curNiftyList = null;
        BankNiftyList prevNiftyList = null;
        BankNiftyList prevNiftyList2 = null;

        try {
            List<BankNiftyList> banksHistoryPrev = viewModel.getOIHistory(oiName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 2) {
                curNiftyList = banksHistoryPrev.get(bankHisLen - 1);
                prevNiftyList = banksHistoryPrev.get(bankHisLen - 2);
                prevNiftyList2 = banksHistoryPrev.get(bankHisLen - 3);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList, prevNiftyList2);
            } else if (bankHisLen > 1) {
                curNiftyList = banksHistoryPrev.get(bankHisLen - 1);
                prevNiftyList = banksHistoryPrev.get(0);
                prevNiftyList2 = banksHistoryPrev.get(0);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList, prevNiftyList2);
            } else {
                curNiftyList = banksHistoryPrev.get(0);
                prevNiftyList = banksHistoryPrev.get(0);
                prevNiftyList2 = banksHistoryPrev.get(0);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList, prevNiftyList2);
            }
        } catch (Exception e) {
            Log.v("movementTrackingBanks", e.getMessage());
        }
    }

*/
}


