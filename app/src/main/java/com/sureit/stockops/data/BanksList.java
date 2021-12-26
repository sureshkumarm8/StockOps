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

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "quantityTraded")
    private long quantityTraded;

    @ColumnInfo(name = "deliveryQuantity")
    private long deliveryQuantity;

    @ColumnInfo(name = "deliveryPercent")
    private double deliveryPercent;


    private BanksList(Parcel in) {
        bankName =in.readString();
        totalBuyQuantity=in.readLong();
        totalSellQuantity = in.readLong();
        quantityTraded = in.readLong();
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

    public BanksList(String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTraded, long deliveryQuantity, double deliveryPercent) {
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTraded = quantityTraded;
        this.deliveryQuantity = deliveryQuantity;
        this.deliveryPercent = deliveryPercent;
    }

    public BanksList(String timeStampValue, String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTraded, long deliveryQuantity, double deliveryPercent) {
        this.timeStamp = timeStampValue;
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTraded = quantityTraded;
        this.deliveryQuantity = deliveryQuantity;
        this.deliveryPercent = deliveryPercent;
    }

    public String getBankName() {
        return bankName;
    }

    public long getQuantityTraded() {
        return quantityTraded;
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

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setQuantityTraded(long quantityTraded) {
        this.quantityTraded = quantityTraded;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bankName);
        dest.writeLong(totalBuyQuantity);
        dest.writeLong(totalSellQuantity);
        dest.writeLong(quantityTraded);
        dest.writeLong(deliveryQuantity);
        dest.writeDouble(deliveryPercent);
    }




}
