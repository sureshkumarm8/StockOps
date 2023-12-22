package com.sureit.stockops.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

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

    @ColumnInfo(name = "underlyingValue")
    private long underlyingValue;

    @ColumnInfo(name = "percentDiff")
    private double percentDiff;

    @NonNull
    @ColumnInfo(name = "TBQmvVal")
    private double TBQmvVal;

    @ColumnInfo(name = "TSQmvVal")
    private double TSQmvVal;

    @ColumnInfo(name = "QTSmvVal")
    private double QTSmvVal;

    @ColumnInfo(name = "DPmvVal")
    private double PDmvVal;

    private BanksList(Parcel in) {
        bankName =in.readString();
        totalBuyQuantity=in.readLong();
        totalSellQuantity = in.readLong();
        quantityTradedsure = in.readLong();
        underlyingValue = in.readLong();
        percentDiff = in.readDouble();
        TBQmvVal = in.readDouble();
        TSQmvVal = in.readDouble();
        QTSmvVal = in.readDouble();
        PDmvVal = in.readDouble();
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

    public BanksList(String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTradedsure, long underlyingValue, double percentDiff) {
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTradedsure = quantityTradedsure;
        this.underlyingValue = underlyingValue;
        this.percentDiff = percentDiff;
    }

    public BanksList(String timeStampValue, String bankName, long totalBuyQuantity, long totalSellQuantity, long quantityTradedsure, long underlyingValue, double percentDiff) {
        this.timeStamp = timeStampValue;
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.quantityTradedsure = quantityTradedsure;
        this.underlyingValue = underlyingValue;
        this.percentDiff = percentDiff;
    }

    public BanksList(String currentTime, String bankName, long totalBuyQuantity, long totalSellQuantity, double TBQmvVal, double tSQmvVal, double QTSmvVal, double PDmvVal, double dPmvVal) {
        this.timeStamp = currentTime;
        this.bankName = bankName;
        this.totalBuyQuantity =totalBuyQuantity;
        this.totalSellQuantity = totalSellQuantity;
        this.TBQmvVal = TBQmvVal;
        this.TSQmvVal = tSQmvVal;
        this.QTSmvVal = QTSmvVal;
        this.PDmvVal = PDmvVal;
        this.percentDiff = dPmvVal;
    }

    public String getBankName() {
        return bankName;
    }

    public long getQuantityTradedsure() {
        return quantityTradedsure;
    }

    public long getUnderlyingValue() {
        return underlyingValue;
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

    public double getPercentDiff() {
        return percentDiff;
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

    public double getPDmvVal() {
        return PDmvVal;
    }


    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setQuantityTradedsure(long quantityTradedsure) {
        this.quantityTradedsure = quantityTradedsure;
    }

    public void setUnderlyingValue(long underlyingValue) {
        this.underlyingValue = underlyingValue;
    }

    public void setTotalSellQuantity(long totalSellQuantity) {
        this.totalSellQuantity = totalSellQuantity;
    }

    public void setTotalBuyQuantity(long totalBuyQuantity) { this.totalBuyQuantity = totalBuyQuantity; }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setPercentDiff(double percentDiff) {
        this.percentDiff = percentDiff;
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

    public void setPDmvVal(double PDmvVal) {
        this.PDmvVal = PDmvVal;
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
        dest.writeLong(underlyingValue);
        dest.writeDouble(percentDiff);
        dest.writeDouble(TBQmvVal);
        dest.writeDouble(TSQmvVal);
        dest.writeDouble(QTSmvVal);
        dest.writeDouble(PDmvVal);

    }

}
