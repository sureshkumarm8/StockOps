package com.sureit.stockops.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.adapter.BankHistoryAdapter;
import com.sureit.stockops.adapter.OIHistoryAdapter;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.DB_NAME;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banknifty_details);

        banksDatabase = Room.databaseBuilder(this, BanksDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();

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

        Bundle data=getIntent().getExtras();
        assert data != null;
        String getBankName = data.getString(Constants.PARCEL_KEY);
        getSupportActionBar().setTitle(getBankName);
        BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
        if(getBankName.equals("OI History")){
            ceoi.setText("CE OI");
            peoi.setText("PE OI");
            buyqty.setText("CE Vol");
            sellqty.setText("PE Vol");
            underlyv.setText("UnderlyV");
            oiHistoryAdapter = new OIHistoryAdapter(viewModel.getOIHistory(getBankName),getApplicationContext());
            recycleViewBankHistory.setAdapter(oiHistoryAdapter);
            oiHistoryAdapter.notifyDataSetChanged();
        }else {
            adapterHistory = new BankHistoryAdapter(viewModel.getBanksHistory(getBankName), getApplicationContext());
            recycleViewBankHistory.setAdapter(adapterHistory);
            adapterHistory.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
