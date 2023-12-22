package com.sureit.stockops.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

@Entity(tableName = "bankNiftyDB", primaryKeys = {"timestamp","calloi","putoi","NBQmvVal"})
public class BankNiftyList implements Parcelable {

//    @PrimaryKey(autoGenerate = true)
//    @NonNull
    @ColumnInfo(name = "id")
    private int id=0;

    @ColumnInfo(name = "oiname")
    private String oiname;

    @NonNull
    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @NonNull
    @ColumnInfo(name = "calloi")
    private long calloi;

    @NonNull
    @ColumnInfo(name = "putoi")
    private long putoi;

    @ColumnInfo(name = "bNtotalBuyQuantity")
    private long bntotalbuyquantity;

    @ColumnInfo(name = "bntotalsellquantity")
    private long bntotalsellquantity;

    @ColumnInfo(name = "underlyvalue")
    private double underlyvalue;

    @NonNull
    @ColumnInfo(name = "NBQmvVal")
    private double NBQmvVal;

    @ColumnInfo(name = "NSQmvVal")
    private double NSQmvVal;

    @ColumnInfo(name = "NVOLmvVal")
    private double NVOLmvVal;

    @ColumnInfo(name = "NOImvVal")
    private double NOImvVal;

    private BankNiftyList(Parcel in) {
        oiname =in.readString();
        timestamp =in.readString();
        calloi = in.readLong();
        putoi = in.readLong();
        bntotalbuyquantity = in.readLong();
        bntotalsellquantity = in.readLong();
        underlyvalue =in.readDouble();
        NBQmvVal =in.readDouble();
        NSQmvVal =in.readDouble();
        NVOLmvVal =in.readDouble();
        NOImvVal =in.readDouble();
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

    public BankNiftyList(String timestamp,String oiname, long calloi,  long putoi, long bntotalbuyquantity, long bntotalsellquantity, double underlyvalue) {
        this.timestamp = timestamp;
        this.oiname = oiname;
        this.calloi = calloi;
        this.putoi = putoi;
        this.bntotalbuyquantity = bntotalbuyquantity;
        this.bntotalsellquantity = bntotalsellquantity;
        this.underlyvalue = underlyvalue;
    }

    @Ignore
    public BankNiftyList(String timestamp,String oiname, long calloi, double NBQmvVal, double NSQmvVal, double NVOLmvVal, double NOImvVal, double underlyvalue) {
        this.timestamp = timestamp;
        this.oiname = oiname;
        this.calloi = calloi;
        this.NBQmvVal = NBQmvVal;
        this.NSQmvVal = NSQmvVal;
        this.NVOLmvVal = NVOLmvVal;
        this.NOImvVal = NOImvVal;
        this.underlyvalue = underlyvalue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(oiname);
        dest.writeString(timestamp);
        dest.writeLong(calloi);
        dest.writeLong(putoi);
        dest.writeLong(bntotalbuyquantity);
        dest.writeLong(bntotalsellquantity);
        dest.writeDouble(underlyvalue);
        dest.writeDouble(NBQmvVal);
        dest.writeDouble(NSQmvVal);
        dest.writeDouble(NVOLmvVal);
        dest.writeDouble(NOImvVal);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOiname() {
        return oiname;
    }

    public void setOiname(String oiname) {
        this.oiname = oiname;
    }

    public double getNBQmvVal() {
        return NBQmvVal;
    }

    public void setNBQmvVal(double NBQmvVal) {
        this.NBQmvVal = NBQmvVal;
    }

    public double getNSQmvVal() {
        return NSQmvVal;
    }

    public void setNSQmvVal(double NSQmvVal) {
        this.NSQmvVal = NSQmvVal;
    }

    public double getNVOLmvVal() {
        return NVOLmvVal;
    }

    public void setNVOLmvVal(double NVOLmvVal) {
        this.NVOLmvVal = NVOLmvVal;
    }

    public double getNOImvVal() {
        return NOImvVal;
    }

    public void setNOImvVal(double NOImvVal) {
        this.NOImvVal = NOImvVal;
    }
}
