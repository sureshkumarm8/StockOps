package com.sureit.stockops.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.view.BankNiftyDetailsActivity;
import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.R;

import java.text.DecimalFormat;
import java.util.List;

public class BankNiftyAdapter extends RecyclerView.Adapter<BankNiftyAdapter.ViewHolder> {


    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_VOTE_AVERAGE= "vote_average";
    public static final String KEY_RELEASE_DATE= "release_date";
    // we define a list from the DevelopersList java class

    private List<BankNiftyList> bankNiftyLists;
    private Context context;

    public BankNiftyAdapter(List<BankNiftyList> bankNiftyLists, Context context) {

        // generate constructors to initialise the List and Context objects

        this.bankNiftyLists = bankNiftyLists;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.strike_price_item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views
        final BankNiftyList developersList = bankNiftyLists.get(position);
        holder.strikePriceTV.setText(String.valueOf(developersList.getTimestamp()));
        holder.totalVolumeCE.setText(String.valueOf(developersList.getCalloi()/1000));
        holder.totalBuyQuantityCE.setText(String.valueOf(developersList.getBntotalbuyquantity()/1000));
        holder.totalAskQuantityPE.setText(String.valueOf(developersList.getPutoi()/1000));
        holder.oiChange.setText(String.valueOf(developersList.getBntotalsellquantity()/1000));
        DecimalFormat df = new DecimalFormat("0.00");
        holder.pOIchange.setText(df.format(developersList.getUnderlyvalue()));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BankNiftyList bankNiftyList1 = bankNiftyLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,
                        new BankNiftyList(bankNiftyList1.getTimestamp() , bankNiftyList1.getCalloi(), bankNiftyList1.getBntotalbuyquantity(), bankNiftyList1.getPutoi(),
                                bankNiftyList1.getBntotalsellquantity(), bankNiftyList1.getUnderlyvalue()));
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    @Override

    //return the size of the listItems (developersList)

    public int getItemCount() {
        return bankNiftyLists.size();
    }

    public void setMoviesLive(List<BankNiftyList> moviesLive) {
        this.bankNiftyLists = moviesLive;
    }

    public List<BankNiftyList> getMoviesLive() {
        return bankNiftyLists;
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        RelativeLayout relativeLayout;
        TextView strikePriceTV;
        TextView totalVolumeCE;
        TextView totalBuyQuantityCE;
        TextView totalAskQuantityPE;
        TextView pOIchange;
        TextView oiChange;

        private ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects
            relativeLayout = itemView.findViewById(R.id.relativeLayoutRV);
            strikePriceTV = itemView.findViewById(R.id.tvStrikePrice2);
            totalVolumeCE = itemView.findViewById(R.id.tvVolumeCE);
            totalBuyQuantityCE = itemView.findViewById(R.id.tvQuantityTradedMain);
            totalAskQuantityPE = itemView.findViewById(R.id.tvDeliveryQtyMain);
            pOIchange = itemView.findViewById(R.id.tvpTotalSellQuantityMain);
            oiChange = itemView.findViewById(R.id.tvTotalBuyQuantityMain);
        }

    }
}
