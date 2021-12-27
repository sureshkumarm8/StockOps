package com.sureit.stockops.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "banksDataDB")
public class BanksList implements Parcelable {


    @ColumnInfo(name = "timeStamp")
    private String timeStamp;

    @ColumnInfo(name = "bankName")
    private String bankName;

    @ColumnInfo(name = "totalBuyQuantity")
    private long totalBuyQuantity;

    @ColumnInfo(name = "totalSellQuantity")
    private long totalSellQuantity;

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "quantityTraded")
    private long quantityTraded=0L;

    @ColumnInfo(name = "quantityTradedsure")
    private long quantityTradedsure;

    @ColumnInfo(name = "deliveryQuantity")
    private long deliveryQuantity;

    @ColumnInfo(name = "deliveryPercent")
    private double deliveryPercent;


    private BanksList(Parcel in) {
        bankName =in.readString();
        totalBuyQuantity=in.readLong();
        totalSellQuantity = in.readLong();
        quantityTradedsure = in.readLong();
        deliveryQuantity = in.readLong();
        deliveryPercent = in.readDouble();
    }

    public static final Creator<BanksList> CREATOR = new Creator<BanksList>() {
        @Override
        public BanksList createFromParcel(Parcel in) {
            return new BanksList(in);
        }

        @Override
        public BanksList[] newArray(int size) {
            return new BanksList[size];
        }
    };

    public BanksList(String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTradedsure, long deliveryQuantity, double deliveryPercent) {
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTradedsure = quantityTradedsure;
        this.deliveryQuantity = deliveryQuantity;
        this.deliveryPercent = deliveryPercent;
    }

    public BanksList(String timeStampValue, String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTradedsure, long deliveryQuantity, double deliveryPercent) {
        this.timeStamp = timeStampValue;
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTradedsure = quantityTradedsure;
        this.deliveryQuantity = deliveryQuantity;
        this.deliveryPercent = deliveryPercent;
    }

    public String getBankName() {
        return bankName;
    }

    public long getQuantityTradedsure() {
        return quantityTradedsure;
    }

    public long getDeliveryQuantity() {
        return deliveryQuantity;
    }

    public long getTotalSellQuantity() {
        return totalSellQuantity;
    }


    public long getTotalBuyQuantity() {
        return totalBuyQuantity;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public double getDeliveryPercent() {
        return deliveryPercent;
    }

    public long getQuantityTraded() {
        return quantityTraded;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setQuantityTradedsure(long quantityTradedsure) {
        this.quantityTradedsure = quantityTradedsure;
    }

    public void setDeliveryQuantity(long deliveryQuantity) {
        this.deliveryQuantity = deliveryQuantity;
    }

    public void setTotalSellQuantity(long totalSellQuantity) {
        this.totalSellQuantity = totalSellQuantity;
    }

    public void setTotalBuyQuantity(long totalBuyQuantity) { this.totalBuyQuantity = totalBuyQuantity; }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDeliveryPercent(double deliveryPercent) {
        this.deliveryPercent = deliveryPercent;
    }

    public void setQuantityTraded(long quantityTraded) {
        this.quantityTraded = quantityTraded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bankName);
        dest.writeLong(totalBuyQuantity);
        dest.writeLong(totalSellQuantity);
        dest.writeLong(quantityTradedsure);
        dest.writeLong(deliveryQuantity);
        dest.writeDouble(deliveryPercent);
    }



}
