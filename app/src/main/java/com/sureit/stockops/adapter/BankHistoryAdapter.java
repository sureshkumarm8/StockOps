package com.sureit.stockops.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sureit.stockops.R;
import com.sureit.stockops.data.BanksList;

import java.util.List;

public class BankHistoryAdapter extends RecyclerView.Adapter<BankHistoryAdapter.ViewHolder> {

    private List<BanksList> banksLists;
    private Context context;

    public BankHistoryAdapter(List<BanksList> banksLists, Context context){
        this.banksLists = banksLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bankhistory_item_card, parent, false);
        return new BankHistoryAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BanksList banksList = banksLists.get(position);

        holder.timeStampTV.setText(banksList.getTimeStamp());
        holder.bidsHistory.setText(String.valueOf(banksList.getTotalBuyQuantity()));
        holder.offersHistory.setText(String.valueOf(banksList.getTotalSellQuantity()));
        holder.delvPercent.setText(String.valueOf(banksList.getDeliveryPercent()));
        holder.volHistory.setText(String.valueOf(banksList.getQuantityTradedsure()));
        holder.delvHistory.setText(String.valueOf(banksList.getDeliveryQuantity()));

        //set color for bullish or bearish
        if(position>=1){
            BanksList banksList_1 = banksLists.get(position - 1);
            if(banksList.getTotalBuyQuantity()<banksList_1.getTotalBuyQuantity()){
                holder.bidsHistory.setBackgroundColor(Color.GREEN);
            }else {holder.bidsHistory.setBackgroundColor(Color.RED);}
            if(banksList.getTotalSellQuantity()>banksList_1.getTotalSellQuantity()){
                holder.offersHistory.setBackgroundColor(Color.GREEN);
            }else {holder.offersHistory.setBackgroundColor(Color.RED);}
            if(banksList.getDeliveryPercent()==banksList_1.getDeliveryPercent()){
                holder.delvPercent.setBackgroundColor(Color.WHITE);
            }
            else if(banksList.getDeliveryPercent()<banksList_1.getDeliveryPercent()) {
                holder.delvPercent.setBackgroundColor(Color.GREEN);
            }else {holder.delvPercent.setBackgroundColor(Color.RED);}
        }
    }

    @Override
    public int getItemCount() {
        return banksLists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeStampTV;
        TextView bidsHistory;
        TextView offersHistory;
        TextView volHistory;
        TextView delvHistory;
        TextView delvPercent;


        ViewHolder(View itemView) {
            super(itemView);
            timeStampTV = itemView.findViewById(R.id.tvTimeStamp);
            bidsHistory = itemView.findViewById(R.id.tvBidsHistory);
            offersHistory = itemView.findViewById(R.id.tvOffersHistory);
            volHistory = itemView.findViewById(R.id.tvVolHistory);
            delvHistory = itemView.findViewById(R.id.tvDelvHistory);
            delvPercent = itemView.findViewById(R.id.tvDelvPercent);
        }
    }
}
