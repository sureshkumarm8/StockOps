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
import com.sureit.stockops.data.BankNiftyList;

import java.util.List;

public class OIHistoryAdapter extends RecyclerView.Adapter<OIHistoryAdapter.ViewHolder> {

    private List<BankNiftyList> bankNiftyLists;
    private Context context;

    public OIHistoryAdapter(List<BankNiftyList> bankNiftyLists, Context context){
        this.bankNiftyLists = bankNiftyLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bankhistory_item_card, parent, false);
        return new OIHistoryAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final BankNiftyList banksList = bankNiftyLists.get(position);
        if(banksList.getOiname().contains("mval") || banksList.getOiname().contains("MStr")){
            holder.timeStampTV.setText(banksList.getTimestamp());
            holder.bidsHistory.setText(String.valueOf(banksList.getNBQmvVal()));
            holder.offersHistory.setText(String.valueOf(banksList.getNSQmvVal()));
            holder.delvPercent.setText(String.valueOf(banksList.getNVOLmvVal()));
            holder.volHistory.setText(String.valueOf(banksList.getNOImvVal()));
            holder.delvHistory.setText(String.valueOf(banksList.getUnderlyvalue()));
        }else if(banksList.getOiname().contains("OI History") || banksList.getOiname().contains("CE History") || banksList.getOiname().contains("PE History")){
            holder.timeStampTV.setText(banksList.getTimestamp());
            holder.bidsHistory.setText(String.valueOf(banksList.getCalloi()));
            holder.offersHistory.setText(String.valueOf(banksList.getPutoi()));
            holder.delvPercent.setText(String.valueOf(banksList.getBntotalbuyquantity()));
            holder.volHistory.setText(String.valueOf(banksList.getBntotalsellquantity()));
            holder.delvHistory.setText(String.valueOf(banksList.getUnderlyvalue()));
        }else {
            holder.timeStampTV.setText(banksList.getTimestamp());
            holder.bidsHistory.setText(String.valueOf(banksList.getPutoi()));
            holder.offersHistory.setText(String.valueOf(banksList.getBntotalbuyquantity()));
            holder.delvPercent.setText(String.valueOf(banksList.getCalloi()));
            holder.volHistory.setText(String.valueOf(banksList.getBntotalsellquantity()));
            holder.delvHistory.setText(String.valueOf(banksList.getUnderlyvalue()));
        }
        //set color for bullish or bearish
        if(position>=1){
            BankNiftyList banksList_1 = bankNiftyLists.get(position - 1);

            if(banksList.getCalloi()==banksList_1.getCalloi()){
                holder.bidsHistory.setBackgroundColor(Color.WHITE);
            }
            else if(banksList.getCalloi()<banksList_1.getCalloi()){
                holder.bidsHistory.setBackgroundColor(Color.GREEN);
            }else {holder.bidsHistory.setBackgroundColor(Color.RED);}

            if(banksList.getPutoi()==banksList_1.getPutoi()){
                holder.offersHistory.setBackgroundColor(Color.WHITE);
            }
            else if(banksList.getPutoi()>banksList_1.getPutoi()){
                holder.offersHistory.setBackgroundColor(Color.GREEN);
            }else {holder.offersHistory.setBackgroundColor(Color.RED);}

            if(banksList.getCalloi()<banksList_1.getCalloi() && banksList.getPutoi()>banksList_1.getPutoi()){
                holder.delvHistory.setBackgroundColor(Color.GREEN);
            }else if(banksList.getCalloi()>banksList_1.getCalloi() && banksList.getPutoi()<banksList_1.getPutoi()){
                holder.delvHistory.setBackgroundColor(Color.RED);
            }else{
                holder.delvHistory.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return bankNiftyLists.size();
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
