package com.sureit.stockops.view;

import static com.sureit.stockops.Util.Constants.DB_NAME;
import static com.sureit.stockops.Util.Constants.mValConst;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.Util.StockDataRetrieveService;
import com.sureit.stockops.adapter.BankHistoryAdapter;
import com.sureit.stockops.adapter.OIHistoryAdapter;
import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankNiftyDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";


    private BanksDatabase banksDatabase;
    private BanksDao banksDao;
    RecyclerView recycleViewBankHistory;
    private List<BanksList> banksHostoryList = new ArrayList<>();
    private BankHistoryAdapter adapterHistory;
    private OIHistoryAdapter oiHistoryAdapter;

    TextView timeStampOI;
    TextView ceoi;
    TextView peoi;
    TextView buyqty;
    TextView sellqty;
    TextView underlyv;
    private String getBankName;
    private BanksViewModel viewModel;
    private String allBanksName;
    private String[] banks;
    private Handler historyHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banknifty_details);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        banksDatabase = Room.databaseBuilder(this, BanksDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        banksDao = BanksDatabase.getInstance(getApplicationContext()).getBanks();

        recycleViewBankHistory = findViewById(R.id.rVBankHistory);
        recycleViewBankHistory.setHasFixedSize(true);
        recycleViewBankHistory.setLayoutManager(new LinearLayoutManager(this));

        timeStampOI = findViewById(R.id.tvTimeStampTtl);
        ceoi = findViewById(R.id.tvBidsHistoryTtl);
        peoi = findViewById(R.id.tvOffersHistoryTtl);
        buyqty = findViewById(R.id.tvDelvPercentTtl);
        sellqty = findViewById(R.id.tvVolHistoryTtl);
        underlyv = findViewById(R.id.tvDelvHistoryTtl);

        Bundle data = getIntent().getExtras();
        assert data != null;
        getBankName = data.getString(Constants.PARCEL_KEY);
        getSupportActionBar().setTitle(getBankName);
        viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);

        banks = new String[]{"AUBANK", "RBLBANK", "BANDHANBNK", "FEDERALBNK", "IDFCFIRSTB", "PNB", "INDUSINDBK", "AXISBANK", "SBIN", "KOTAKBANK", "ICICIBANK", "HDFCBANK", "All Banks"};

//        filterByNormalMarket(banks);
        getBankName = getBankName+"MStr";
        filterByMSTR();
        BanksListActivity objBLAct = new BanksListActivity();
        if (!objBLAct.isMarketClosed()) {
            historyHandler.postDelayed(mRunnableTask, (5 * 1000));
        }
    }

    Runnable mRunnableTask = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            filterByMSTR();
            // this will repeat this task again at specified time interval
            historyHandler.postDelayed(this, (45 * 1000));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mins_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.syncLive:
                liveRefreshData();
                filterByMSTR();
                return (true);

            case R.id.mvVal:
                if (getBankName.contains("MStr"))
                    getBankName = getBankName.replace("MStr","mval");
                if (!getBankName.contains("mval"))
                    getBankName = getBankName + "mval";
                filterByMval();
                return (true);

            case R.id.marketStrength:
                if (getBankName.contains("mval"))
                    getBankName = getBankName.replace("mval","MStr");
                if (!getBankName.contains("MStr"))
                    getBankName = getBankName + "MStr";
                filterByMSTR();
                return (true);
            case R.id.marketNormal:
                if (getBankName.contains("mval"))
                    getBankName = getBankName.replace("mval","");
                if (getBankName.contains("MStr"))
                getBankName = getBankName.replace("MStr","");
                filterByNormalMarket(banks);
                return (true);

            case R.id.min1:
                filterByMins(1);
                return (true);
            case R.id.min5:
                filterByMins(5);
                return (true);
            case R.id.min10:
                filterByMins(10);
                return (true);
            case R.id.min15:
                filterByMins(15);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void liveRefreshData() {
        if (stockDataRetrieveService != null) {
            stockDataRetrieveService.paytmBanksLiveData();
            stockDataRetrieveService.paytmNiftyBankLiveData();
        }
    }

    private void filterByNormalMarket(String[] banks) {
        int position;
        switch (getBankName) {
            case "OI History":
                ceoi.setText("CE OI");
                peoi.setText("PE OI");
                buyqty.setText("CE Vol");
                sellqty.setText("PE Vol");
                underlyv.setText("UnderlyV");
                oiHistoryAdapter = new OIHistoryAdapter(viewModel.getOIHistory(getBankName), getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
                break;

            case "CE History":
            case "PE History":
                ceoi.setText("Bids");
                peoi.setText("Offers");
                buyqty.setText("Tot Vol");
                sellqty.setText("OI");
                underlyv.setText("UnderlyV");
                oiHistoryAdapter = new OIHistoryAdapter(viewModel.getOIHistory(getBankName), getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
                break;

            default:
                if (Arrays.asList(banks).contains(getBankName)) {
                    sellqty.setText("Pts diff");
                    adapterHistory = new BankHistoryAdapter(viewModel.getBanksHistory(getBankName), getApplicationContext());
                    recycleViewBankHistory.setAdapter(adapterHistory);
                    position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                    if(position>5)
                        recycleViewBankHistory.smoothScrollToPosition(position);
                    adapterHistory.notifyDataSetChanged();
                } else if (getBankName.contains("CE") || getBankName.contains("PE")) {
                    ceoi.setText("Bids");
                    peoi.setText("Offers");
                    buyqty.setText("Tot Vol");
                    sellqty.setText("OI");
                    underlyv.setText("oiC%");
                    oiHistoryAdapter = new OIHistoryAdapter(viewModel.getOIHistory(getBankName), getApplicationContext());
                    recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                    position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                    if(position>5)
                        recycleViewBankHistory.smoothScrollToPosition(position);
                    oiHistoryAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    public void filterByMval() {
        if (getBankName.equals("OI Historymval")) {
            ceoi.setText("CE OI");
            peoi.setText("PE OI");
            buyqty.setText("CE Vol");
            sellqty.setText("PE Vol");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getCalloi() > mValConst || oiHistoryFiltered.get(0).getPutoi() > mValConst
                        || oiHistoryFiltered.get(0).getCalloi() < -mValConst || oiHistoryFiltered.get(0).getPutoi() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            if (oiHistoryFilteredNew.size() > 0) {
            oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
            recycleViewBankHistory.setAdapter(oiHistoryAdapter);
            int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
            if(position>5)
                recycleViewBankHistory.smoothScrollToPosition(position);
            oiHistoryAdapter.notifyDataSetChanged();
            }
        } else if (getBankName.equals("CE Historymval") || getBankName.equals("PE Historymval")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getNBQmvVal() > mValConst || oiHistoryFiltered.get(0).getNSQmvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNBQmvVal() < -mValConst || oiHistoryFiltered.get(0).getNSQmvVal() < -mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else if (getBankName.contains("CE") || getBankName.contains("PE") && !getBankName.contains("History")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("oiC%");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getNBQmvVal() > mValConst || oiHistoryFiltered.get(0).getNSQmvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNBQmvVal() < -mValConst || oiHistoryFiltered.get(0).getNSQmvVal() < -mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }
            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else {

            sellqty.setText("Tot. Vol");
            List<BanksList> banksHistoryFiltered = viewModel.getBanksHistory(getBankName);
            List<BanksList> banksHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < banksHistoryFiltered.size(); i++) {
                if (banksHistoryFiltered.get(0).getTBQmvVal() > mValConst || banksHistoryFiltered.get(0).getTSQmvVal() > mValConst
                        || banksHistoryFiltered.get(0).getTBQmvVal() < -mValConst || banksHistoryFiltered.get(0).getTSQmvVal() < -mValConst)
                    banksHistoryFilteredNew.add(banksHistoryFiltered.get(i));
            }

            if (banksHistoryFilteredNew.size() > 0) {
                adapterHistory = new BankHistoryAdapter(banksHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(adapterHistory);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                adapterHistory.notifyDataSetChanged();
            }
        }
    }

    public void filterByMins(int min_filter) {
        if (getBankName.equals("OI History")) {
            ceoi.setText("CE OI");
            peoi.setText("PE OI");
            buyqty.setText("CE Vol");
            sellqty.setText("PE Vol");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (i % min_filter == 0)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
            recycleViewBankHistory.setAdapter(oiHistoryAdapter);
            int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
            if(position>5)
                recycleViewBankHistory.smoothScrollToPosition(position);
            oiHistoryAdapter.notifyDataSetChanged();
        } else if (getBankName.equals("CE History") || getBankName.equals("PE History")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (i % min_filter == 0)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
            recycleViewBankHistory.setAdapter(oiHistoryAdapter);
            int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
            if(position>5)
                recycleViewBankHistory.smoothScrollToPosition(position);
            oiHistoryAdapter.notifyDataSetChanged();
        }
        else if (getBankName.contains("CE") || getBankName.contains("PE") && !getBankName.contains("History")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("oiC%");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (i % min_filter == 0)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }
            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else {

        List<BanksList> banksHistoryFiltered = viewModel.getBanksHistory(getBankName);
        List<BanksList> banksHistoryFilteredNew = new ArrayList<>();
        for (int i = 0; i < banksHistoryFiltered.size(); i++) {
            if (i % min_filter == 0)
                banksHistoryFilteredNew.add(banksHistoryFiltered.get(i));
        }

        if (banksHistoryFilteredNew.size() > 0) {
            adapterHistory = new BankHistoryAdapter(banksHistoryFilteredNew, getApplicationContext());
            recycleViewBankHistory.setAdapter(adapterHistory);
            int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
            if(position>5)
                recycleViewBankHistory.smoothScrollToPosition(position);
            adapterHistory.notifyDataSetChanged();
        }
        }
    }

    public void filterByMSTR() {
        if (getBankName.equals("OI HistoryMStr")) {
            ceoi.setText("CE OI");
            peoi.setText("PE OI");
            buyqty.setText("CE Vol");
            sellqty.setText("PE Vol");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getCalloi() > mValConst || oiHistoryFiltered.get(0).getPutoi() > mValConst
                        || oiHistoryFiltered.get(0).getCalloi() < -mValConst || oiHistoryFiltered.get(0).getPutoi() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else if (getBankName.equals("CE HistoryMStr") || getBankName.equals("PE HistoryMStr")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("uVal");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getNBQmvVal() > mValConst || oiHistoryFiltered.get(0).getNSQmvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNBQmvVal() < -mValConst || oiHistoryFiltered.get(0).getNSQmvVal() < -mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }

            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else if (getBankName.contains("CE") || getBankName.contains("PE") && !getBankName.contains("History")) {
            ceoi.setText("Bids");
            peoi.setText("Offers");
            buyqty.setText("Vol");
            sellqty.setText("OI");
            underlyv.setText("oiC%");

            List<BankNiftyList> oiHistoryFiltered = viewModel.getOIHistory(getBankName);
            List<BankNiftyList> oiHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < oiHistoryFiltered.size(); i++) {
                if (oiHistoryFiltered.get(0).getNBQmvVal() > mValConst || oiHistoryFiltered.get(0).getNSQmvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() > mValConst
                        || oiHistoryFiltered.get(0).getNBQmvVal() < -mValConst || oiHistoryFiltered.get(0).getNSQmvVal() < -mValConst
                        || oiHistoryFiltered.get(0).getNOImvVal() < -mValConst)
                    oiHistoryFilteredNew.add(oiHistoryFiltered.get(i));
            }
            if (oiHistoryFilteredNew.size() > 0) {
                oiHistoryAdapter = new OIHistoryAdapter(oiHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(oiHistoryAdapter);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else {

            sellqty.setText("MVal");
            List<BanksList> banksHistoryFiltered = viewModel.getBanksHistory(getBankName);
            List<BanksList> banksHistoryFilteredNew = new ArrayList<>();
            for (int i = 0; i < banksHistoryFiltered.size(); i++) {
                if (banksHistoryFiltered.get(0).getTBQmvVal() > mValConst || banksHistoryFiltered.get(0).getTSQmvVal() > mValConst
                        || banksHistoryFiltered.get(0).getTBQmvVal() < -mValConst || banksHistoryFiltered.get(0).getTSQmvVal() < -mValConst)
                    banksHistoryFilteredNew.add(banksHistoryFiltered.get(i));
            }

            if (banksHistoryFilteredNew.size() > 0) {
                adapterHistory = new BankHistoryAdapter(banksHistoryFilteredNew, getApplicationContext());
                recycleViewBankHistory.setAdapter(adapterHistory);
                int position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
                if(position>5)
                    recycleViewBankHistory.smoothScrollToPosition(position);
                adapterHistory.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        historyHandler.removeCallbacksAndMessages(null);
    }

    private StockDataRetrieveService stockDataRetrieveService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            stockDataRetrieveService = ((StockDataRetrieveService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stockDataRetrieveService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, StockDataRetrieveService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }
}
