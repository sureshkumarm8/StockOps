package com.sureit.stockops.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.adapter.BanksAdapter;
import com.sureit.stockops.adapter.ReviewsAdapter;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.DB_NAME;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";
    NoInternetDialog noInternetDialog;

    private BanksDatabase banksDatabase;
    private BanksDao banksDao;
    RecyclerView recycleViewBankHistory;
    private List<BanksList> banksHostoryList = new ArrayList<>();
    private ReviewsAdapter adapterHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        banksDatabase = Room.databaseBuilder(this, BanksDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();

        recycleViewBankHistory = findViewById(R.id.rVBankHistory);
        recycleViewBankHistory.setHasFixedSize(true);
        recycleViewBankHistory.setLayoutManager(new LinearLayoutManager(this));

        noInternetDialog = new NoInternetDialog.Builder(this).build();
        Bundle data=getIntent().getExtras();
        assert data != null;
        String getBankName = data.getString(Constants.PARCEL_KEY);
//        setupViewModel(getBankName);

        BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
        adapterHistory = new ReviewsAdapter(viewModel.getTasks(getBankName),getApplicationContext());
        recycleViewBankHistory.setAdapter(adapterHistory);
        adapterHistory.notifyDataSetChanged();
    }

    private void setupViewModel() {
        BanksViewModel viewModel = ViewModelProviders.of(this).get(BanksViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<BanksList>>() {
            @Override
            public void onChanged(@Nullable List<BanksList> taskEntries) {
                adapterHistory = new ReviewsAdapter(taskEntries,getApplicationContext());
                recycleViewBankHistory.setAdapter(adapterHistory);
                adapterHistory.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
