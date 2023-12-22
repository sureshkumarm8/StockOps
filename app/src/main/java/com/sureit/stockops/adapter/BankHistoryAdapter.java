package com.sureit.stockops.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sureit.stockops.R;
import com.sureit.stockops.data.BanksList;

import java.text.DecimalFormat;
import java.util.List;

public class BankHistoryAdapter extends RecyclerView.Adapter<BankHistoryAdapter.ViewHolder> {

    private List<BanksList> banksLists;
    private Context context;
    private DecimalFormat df = new DecimalFormat("0");
    private DecimalFormat dff = new DecimalFormat("0.0");
    private boolean normalBankColor;

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

        if(banksList.getBankName().contains("mval") || banksList.getBankName().contains("MStr")){
            holder.timeStampTV.setText(banksList.getTimeStamp());
            holder.bidsHistory.setText(dff.format(banksList.getTBQmvVal()));
            holder.offersHistory.setText(dff.format(banksList.getTSQmvVal()));
            holder.percentDiff.setText(dff.format(banksList.getPercentDiff()));
            holder.totalVolHistory.setText(dff.format(banksList.getQTSmvVal()));
            holder.underlyingValue.setText(df.format(banksList.getPDmvVal()));
        }else {
            normalBankColor = true;
            holder.timeStampTV.setText(banksList.getTimeStamp());
            holder.bidsHistory.setText(String.valueOf(banksList.getTotalBuyQuantity()));
            holder.offersHistory.setText(String.valueOf(banksList.getTotalSellQuantity()));
            holder.percentDiff.setText(dff.format(banksList.getPercentDiff()));
            if(position>=1){ // Displaying points moved, commented out total Volume
                BanksList banksList_1 = banksLists.get(position - 1);
                holder.totalVolHistory.setText(String.valueOf(banksList.getUnderlyingValue()-banksList_1.getUnderlyingValue()));
            }else
                holder.totalVolHistory.setText(String.valueOf("0"));
//            holder.totalVolHistory.setText(String.valueOf(banksList.getQuantityTradedsure()));
            holder.underlyingValue.setText(String.valueOf(banksList.getUnderlyingValue()));
        }

        //set color for bullish or bearish
        if(position>=1){
            BanksList banksList_1 = banksLists.get(position - 1);
            if(!normalBankColor)
                colorMstr(holder, banksList, banksList_1);
            else
                colorNormalBanks(holder, banksList, banksList_1);
        }
    }

    private void colorMstr(@NonNull ViewHolder holder, BanksList banksList, BanksList banksList_1) {
        if(banksList.getTotalBuyQuantity()== banksList_1.getTotalBuyQuantity()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getTotalBuyQuantity()> banksList_1.getTotalBuyQuantity()){
            holder.bidsHistory.setBackgroundColor(Color.GREEN);
        }else {
            holder.bidsHistory.setBackgroundColor(Color.RED);}

        if(banksList.getTotalSellQuantity()== banksList_1.getTotalSellQuantity()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getTotalSellQuantity()< banksList_1.getTotalSellQuantity()){
            holder.offersHistory.setBackgroundColor(Color.GREEN);
        }else {
            holder.offersHistory.setBackgroundColor(Color.RED);}

        if(banksList.getPercentDiff()== banksList_1.getPercentDiff()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getPercentDiff()> banksList_1.getPercentDiff()) {
            holder.percentDiff.setBackgroundColor(Color.GREEN);
        }else {
            holder.percentDiff.setBackgroundColor(Color.RED);}

        if(banksList.getQTSmvVal()== banksList_1.getQTSmvVal()){
            holder.totalVolHistory.setBackgroundColor(Color.WHITE);
        }else if(banksList.getQTSmvVal()> banksList_1.getQTSmvVal()) {
            holder.totalVolHistory.setBackgroundColor(Color.GREEN);
        }else {
            holder.totalVolHistory.setBackgroundColor(Color.RED);}

        if(banksList.getTotalBuyQuantity()> banksList_1.getTotalBuyQuantity()
                && banksList.getTotalSellQuantity()< banksList_1.getTotalSellQuantity()
                && banksList.getPercentDiff()> banksList_1.getPercentDiff() && banksList.getQTSmvVal()> banksList_1.getQTSmvVal()){
            holder.timeStampTV.setBackgroundColor(Color.GREEN);
            holder.underlyingValue.setBackgroundColor(Color.GREEN);
        }else if(banksList.getTotalBuyQuantity()< banksList_1.getTotalBuyQuantity()
                && banksList.getTotalSellQuantity()> banksList_1.getTotalSellQuantity()
                && banksList.getPercentDiff()< banksList_1.getPercentDiff() && banksList.getQTSmvVal()< banksList_1.getQTSmvVal()){
            holder.timeStampTV.setBackgroundColor(Color.RED);
            holder.underlyingValue.setBackgroundColor(Color.RED);
        }else{
            holder.timeStampTV.setBackgroundColor(Color.WHITE);
            holder.underlyingValue.setBackgroundColor(Color.WHITE);
        }
    }

    private void colorNormalBanks(@NonNull ViewHolder holder, BanksList banksList, BanksList banksList_1) {
        if(banksList.getTotalBuyQuantity()== banksList_1.getTotalBuyQuantity()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getTotalBuyQuantity()> banksList_1.getTotalBuyQuantity()){
            holder.bidsHistory.setBackgroundColor(Color.GREEN);
        }else {
            holder.bidsHistory.setBackgroundColor(Color.RED);}

        if(banksList.getTotalSellQuantity()== banksList_1.getTotalSellQuantity()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getTotalSellQuantity()< banksList_1.getTotalSellQuantity()){
            holder.offersHistory.setBackgroundColor(Color.GREEN);
        }else {
            holder.offersHistory.setBackgroundColor(Color.RED);}

        if(banksList.getPercentDiff()== banksList_1.getPercentDiff()){
            holder.percentDiff.setBackgroundColor(Color.WHITE);
        }else if(banksList.getPercentDiff()> banksList_1.getPercentDiff()) {
            holder.percentDiff.setBackgroundColor(Color.GREEN);
        }else {
            holder.percentDiff.setBackgroundColor(Color.RED);}

        if(banksList.getTotalBuyQuantity()> banksList_1.getTotalBuyQuantity()
                && banksList.getTotalSellQuantity()< banksList_1.getTotalSellQuantity()
                && banksList.getPercentDiff()> banksList_1.getPercentDiff()){
            holder.timeStampTV.setBackgroundColor(Color.GREEN);
            holder.underlyingValue.setBackgroundColor(Color.GREEN);
            holder.totalVolHistory.setBackgroundColor(Color.GREEN);
        }else if(banksList.getTotalBuyQuantity()< banksList_1.getTotalBuyQuantity()
                && banksList.getTotalSellQuantity()> banksList_1.getTotalSellQuantity()
                && banksList.getPercentDiff()< banksList_1.getPercentDiff()){
            holder.timeStampTV.setBackgroundColor(Color.RED);
            holder.underlyingValue.setBackgroundColor(Color.RED);
            holder.totalVolHistory.setBackgroundColor(Color.RED);
        }else{
            holder.timeStampTV.setBackgroundColor(Color.WHITE);
            holder.underlyingValue.setBackgroundColor(Color.WHITE);
            holder.totalVolHistory.setBackgroundColor(Color.WHITE);
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
        TextView totalVolHistory;
        TextView underlyingValue;
        TextView percentDiff;


        ViewHolder(View itemView) {
            super(itemView);
            timeStampTV = itemView.findViewById(R.id.tvTimeStamp);
            bidsHistory = itemView.findViewById(R.id.tvBidsHistory);
            offersHistory = itemView.findViewById(R.id.tvOffersHistory);
            totalVolHistory = itemView.findViewById(R.id.tvVolHistory);
            underlyingValue = itemView.findViewById(R.id.tvDelvHistory);
            percentDiff = itemView.findViewById(R.id.tvDelvPercent);
        }
    }
}
