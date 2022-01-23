package com.sureit.stockops.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "banksDataDB",primaryKeys = {"timeStamp","totalBuyQuantity","totalSellQuantity","TBQmvVal"})
public class BanksList implements Parcelable {


    //    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "timeStamp")
    private String timeStamp;

    @ColumnInfo(name = "bankName")
    private String bankName;

    @NonNull
    @ColumnInfo(name = "totalBuyQuantity")
    private long totalBuyQuantity;

    @NonNull
    @ColumnInfo(name = "totalSellQuantity")
    private long totalSellQuantity;

    //This will auto generate. quantityTradedsure is the real data
//    @PrimaryKey(autoGenerate = true)
//    @NonNull
    @ColumnInfo(name = "quantityTraded")
    private long quantityTraded=0L;

    @ColumnInfo(name = "quantityTradedsure")
    private long quantityTradedsure;

    @ColumnInfo(name = "deliveryQuantity")
    private long deliveryQuantity;

    @ColumnInfo(name = "deliveryPercent")
    private double deliveryPercent;

    @NonNull
    @ColumnInfo(name = "TBQmvVal")
    private double TBQmvVal;

    @ColumnInfo(name = "TSQmvVal")
    private double TSQmvVal;

    @ColumnInfo(name = "QTSmvVal")
    private double QTSmvVal;

    @ColumnInfo(name = "DQmvVal")
    private double DQmvVal;

    private BanksList(Parcel in) {
        bankName =in.readString();
        totalBuyQuantity=in.readLong();
        totalSellQuantity = in.readLong();
        quantityTradedsure = in.readLong();
        deliveryQuantity = in.readLong();
        deliveryPercent = in.readDouble();
        TBQmvVal = in.readDouble();
        TSQmvVal = in.readDouble();
        QTSmvVal = in.readDouble();
        DQmvVal = in.readDouble();
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

    public BanksList(String currentTime, String bankName,long totalBuyQuantity, long totalSellQuantity, double TBQmvVal, double tSQmvVal, double QTSmvVal, double DQmvVal, double dPmvVal) {
        this.timeStamp = currentTime;
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.TBQmvVal = TBQmvVal;
        this.TSQmvVal = tSQmvVal;
        this.QTSmvVal = QTSmvVal;
        this.DQmvVal = DQmvVal;
        this.deliveryPercent = dPmvVal;
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

    public double getTBQmvVal() {
        return TBQmvVal;
    }

    public double getTSQmvVal() {
        return TSQmvVal;
    }

    public double getQTSmvVal() {
        return QTSmvVal;
    }

    public double getDQmvVal() {
        return DQmvVal;
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

    public void setTBQmvVal(double TBQmvVal) {
        this.TBQmvVal = TBQmvVal;
    }

    public void setTSQmvVal(double TSQmvVal) {
        this.TSQmvVal = TSQmvVal;
    }


    public void setQTSmvVal(double QTSmvVal) {
        this.QTSmvVal = QTSmvVal;
    }

    public void setDQmvVal(double DQmvVal) {
        this.DQmvVal = DQmvVal;
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
        dest.writeDouble(TBQmvVal);
        dest.writeDouble(TSQmvVal);
        dest.writeDouble(QTSmvVal);
        dest.writeDouble(DQmvVal);

    }

}
