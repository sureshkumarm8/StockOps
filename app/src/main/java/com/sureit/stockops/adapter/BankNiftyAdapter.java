package com.sureit.stockops.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sureit.stockops.R;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.view.BankNiftyDetailsActivity;

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
        holder.strikePriceTV.setText(String.valueOf(developersList.getOiname()));
        holder.totalVolumeCE.setText(String.valueOf(developersList.getCalloi()));
        holder.totalBuyQuantityCE.setText(String.valueOf(developersList.getPutoi()));
        holder.totalAskQuantityPE.setText(String.valueOf(developersList.getBntotalbuyquantity()));
        holder.oiChange.setText(String.valueOf(developersList.getBntotalsellquantity()));
        DecimalFormat df = new DecimalFormat("0.00");
        holder.pOIchange.setText(df.format(developersList.getUnderlyvalue()));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BankNiftyList bankNiftyList1 = bankNiftyLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), BankNiftyDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,developersList.getOiname());
                v.getContext().startActivity(skipIntent);
            }
        });

        //set color for bullish or bearish
        if(position>=1){
            BankNiftyList banksList_1 = bankNiftyLists.get(position - 1);
            if(developersList.getPutoi()==banksList_1.getPutoi()){
                holder.totalBuyQuantityCE.setBackgroundColor(Color.WHITE);
            }
            else if(developersList.getPutoi()>banksList_1.getPutoi()){
                holder.totalBuyQuantityCE.setBackgroundColor(Color.GREEN);
            }else {holder.totalBuyQuantityCE.setBackgroundColor(Color.RED);}

            if(developersList.getBntotalbuyquantity()==banksList_1.getBntotalbuyquantity()){
                holder.totalAskQuantityPE.setBackgroundColor(Color.WHITE);
            }
            else if(developersList.getBntotalbuyquantity()<banksList_1.getBntotalbuyquantity()){
                holder.totalAskQuantityPE.setBackgroundColor(Color.GREEN);
            }else {holder.totalAskQuantityPE.setBackgroundColor(Color.RED);}

            if(developersList.getBntotalsellquantity()==banksList_1.getBntotalsellquantity()){
                holder.oiChange.setBackgroundColor(Color.WHITE);
            }
            else if(developersList.getBntotalsellquantity()>banksList_1.getBntotalsellquantity()){
                holder.oiChange.setBackgroundColor(Color.GREEN);
            }else {holder.oiChange.setBackgroundColor(Color.RED);}

            if(developersList.getPutoi()>banksList_1.getPutoi()
                    && developersList.getBntotalbuyquantity()<banksList_1.getBntotalbuyquantity()){
                holder.strikePriceTV.setBackgroundColor(Color.GREEN);
            }else if(developersList.getPutoi()<banksList_1.getPutoi()
                    && developersList.getBntotalbuyquantity()>banksList_1.getBntotalbuyquantity()){
                holder.strikePriceTV.setBackgroundColor(Color.RED);
            }else{
                holder.strikePriceTV.setBackgroundColor(Color.WHITE);
            }
        }
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
