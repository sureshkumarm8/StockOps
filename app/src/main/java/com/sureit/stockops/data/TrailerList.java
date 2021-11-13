package com.sureit.stockops.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TrailerList implements Parcelable{
    private String trailerItem;
    private String trailerKey;

    public TrailerList(String trailerItem, String trailerKey){
        this.trailerItem = trailerItem;
        this.trailerKey = trailerKey;
    }

    private TrailerList(Parcel in) {
        trailerItem = in.readString();
        trailerKey = in.readString();
    }

    public static final Creator<TrailerList> CREATOR = new Creator<TrailerList>() {
        @Override
        public TrailerList createFromParcel(Parcel in) {
            return new TrailerList(in);
        }

        @Override
        public TrailerList[] newArray(int size) {
            return new TrailerList[size];
        }
    };

    public String getTrailerKey() {
        return trailerKey;
    }

    public String getTrailerItem() {
        return trailerItem;
    }

    public void setTrailerKey(String trailerKey) {
        this.trailerKey = trailerKey;
    }

    public void setTrailerItem(String trailerItem) {
        this.trailerItem = trailerItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trailerItem);
        dest.writeString(trailerKey);
    }
}
