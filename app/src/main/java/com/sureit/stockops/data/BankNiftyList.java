package com.sureit.stockops.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "bankNiftyDB")
public class BankNiftyList implements Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private int id=0;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "calloi")
    private long calloi;

    @ColumnInfo(name = "putoi")
    private long putoi;

    @ColumnInfo(name = "bNtotalBuyQuantity")
    private long bntotalbuyquantity;

    @ColumnInfo(name = "bntotalsellquantity")
    private long bntotalsellquantity;

    @ColumnInfo(name = "underlyvalue")
    private double underlyvalue;

    private BankNiftyList(Parcel in) {
        timestamp =in.readString();
        calloi = in.readLong();
        putoi = in.readLong();
        bntotalbuyquantity = in.readLong();
        bntotalsellquantity = in.readLong();
        underlyvalue =in.readDouble();
    }

    public static final Creator<BankNiftyList> CREATOR = new Creator<BankNiftyList>() {
        @Override
        public BankNiftyList createFromParcel(Parcel in) {
            return new BankNiftyList(in);
        }

        @Override
        public BankNiftyList[] newArray(int size) {
            return new BankNiftyList[size];
        }
    };

    public String getTimestamp() {
        return timestamp;
    }

    public long getCalloi() {
        return calloi;
    }

    public long getPutoi() {
        return putoi;
    }

    public long getBntotalbuyquantity() {
        return bntotalbuyquantity;
    }

    public long getBntotalsellquantity(){
        return bntotalsellquantity;
    }

    public double getUnderlyvalue() {
        return underlyvalue;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setCalloi(long calloi) {
        this.calloi = calloi;
    }

    public void setPutoi(long putoi) {
        this.putoi = putoi;
    }

    public void setBntotalbuyquantity(long bntotalbuyquantity) {
        this.bntotalbuyquantity = bntotalbuyquantity;
    }

    public void setBntotalsellquantity(long bntotalsellquantity) {
        this.bntotalsellquantity = bntotalsellquantity;
    }

    public void setUnderlyvalue(double underlyvalue) {
        this.underlyvalue = underlyvalue;
    }

    public BankNiftyList(String timestamp, long calloi,  long putoi, long bntotalbuyquantity, long bntotalsellquantity, double underlyvalue) {
        this.timestamp = timestamp;
        this.calloi = calloi;
        this.putoi = putoi;
        this.bntotalbuyquantity = bntotalbuyquantity;
        this.bntotalsellquantity = bntotalsellquantity;
        this.underlyvalue = underlyvalue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeLong(calloi);
        dest.writeLong(putoi);
        dest.writeLong(bntotalbuyquantity);
        dest.writeLong(bntotalsellquantity);
        dest.writeDouble(underlyvalue);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
