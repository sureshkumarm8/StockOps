package com.sureit.stockops.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
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

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.DB_NAME;
import static com.sureit.stockops.Util.Constants.mValConst;

public class BankNiftyDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";
    NoInternetDialog noInternetDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banknifty_details);

        banksDatabase = Room.databaseBuilder(this, BanksDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        banksDao = BanksDatabase.getInstance(getApplicationContext()).getBanks();

        recycleViewBankHistory = findViewById(R.id.rVBankHistory);
        recycleViewBankHistory.setHasFixedSize(true);
        recycleViewBankHistory.setLayoutManager(new LinearLayoutManager(this));

        noInternetDialog = new NoInternetDialog.Builder(this).build();

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

        final String[] banks = {"AUBANK", "RBLBANK", "BANDHANBNK", "FEDERALBNK", "IDFCFIRSTB", "PNB", "INDUSINDBK", "AXISBANK", "SBIN", "KOTAKBANK", "ICICIBANK", "HDFCBANK", "All Banks"};

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
                recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
                break;

            default:
                if (Arrays.asList(banks).contains(getBankName)) {
                    adapterHistory = new BankHistoryAdapter(viewModel.getBanksHistory(getBankName), getApplicationContext());
                    recycleViewBankHistory.setAdapter(adapterHistory);
                    position = recycleViewBankHistory.getAdapter().getItemCount() - 1;
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
                    recycleViewBankHistory.smoothScrollToPosition(position);
                    oiHistoryAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mins_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mvVal:
                if (!getBankName.contains("mval"))
                    getBankName = getBankName + "mval";
                filterByMval("mval");
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

    public void filterByMval(String mval) {
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
                recycleViewBankHistory.smoothScrollToPosition(position);
                oiHistoryAdapter.notifyDataSetChanged();
            }
        } else {

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
                recycleViewBankHistory.smoothScrollToPosition(position);
                adapterHistory.notifyDataSetChanged();
            }
        }
    }

    public void filterByMins(int min_filter) {
        if (getBankName.equals("OI Historymval")) {
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
            recycleViewBankHistory.smoothScrollToPosition(position);
            oiHistoryAdapter.notifyDataSetChanged();
        } else if (getBankName.equals("CE Historymval") || getBankName.equals("PE Historymval")) {
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
            recycleViewBankHistory.smoothScrollToPosition(position);
            adapterHistory.notifyDataSetChanged();
        }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
