package com.sureit.stockops.adapter;

import static com.sureit.stockops.Util.Constants.DB_NAME;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.data.MovieList;
import com.sureit.stockops.db.MovieDao;
import com.sureit.stockops.db.MovieDatabase;
import com.sureit.stockops.view.MovieDetailsActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BanksAdapter extends RecyclerView.Adapter<BanksAdapter.ViewHolder> {

    private List<BanksList> banksLists;
    private Context context;
    private MovieDao mMovieDao;

    public BanksAdapter(List<BanksList> banksLists, Context context) {

        // generate constructors to initialise the List and Context objects
        this.banksLists = banksLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bankslist_item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views
        final BanksList banksData = banksLists.get(position);
        holder.bankNameTV.setText(String.valueOf(banksData.getBankName()));
        holder.totalBuyQuantityCE.setText(String.valueOf(banksData.getTotalBuyQuantity()));
        holder.totalAskQuantityPE.setText(String.valueOf(banksData.getTotalSellQuantity()));
        holder.totalDeliveryTV.setText(String.valueOf(banksData.getDeliveryQuantity()));
        holder.totalTradedTV.setText(String.valueOf(banksData.getQuantityTraded()));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BanksList banksData = banksLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), MovieDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,banksData.getBankName());
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    @Override

    //return the size of the listItems (developersList)

    public int getItemCount() {
        return banksLists.size();
    }

    public void setMoviesLive(List<BanksList> moviesLive) {
        this.banksLists = moviesLive;
    }

    public List<BanksList> getMoviesLive() {
        return banksLists;
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        RelativeLayout relativeLayout;
        TextView bankNameTV;
        TextView totalBuyQuantityCE;
        TextView totalAskQuantityPE;
        TextView totalTradedTV;
        TextView totalDeliveryTV;

        private ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects
            relativeLayout = itemView.findViewById(R.id.bankRelativeLayoutRV);
            bankNameTV = itemView.findViewById(R.id.tvBankName);
            totalBuyQuantityCE = itemView.findViewById(R.id.tvTotalBuyQuantityBank);
            totalAskQuantityPE = itemView.findViewById(R.id.tvpTotalSellQuantityBank);
            totalTradedTV = itemView.findViewById(R.id.tvQuantityTradedBank);
            totalDeliveryTV = itemView.findViewById(R.id.tvDeliveryQtyBank);
        }

    }
}