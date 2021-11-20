package com.sureit.stockops.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "moviesfav")
public class MovieList implements Parcelable {


    @PrimaryKey
    @ColumnInfo
    @NonNull
    private String id;
    private long title;
    private long posterUrl;
    private long description;
    private long vote_average;
    private long releaseDate;
    private long totalBuyQuantity;
    private long openInterest;
    private long interest;
    private double pchangeoice;
    private double pchangeoipe;

    private MovieList(Parcel in) {
        id=in.readString();
        title = in.readLong();
        posterUrl = in.readLong();
        description = in.readLong();
        vote_average = in.readLong();
        releaseDate = in.readLong();
        totalBuyQuantity=in.readLong();
        openInterest=in.readLong();
        interest=in.readLong();
        pchangeoice=in.readDouble();
        pchangeoipe=in.readDouble();
    }

    public static final Creator<MovieList> CREATOR = new Creator<MovieList>() {
        @Override
        public MovieList createFromParcel(Parcel in) {
            return new MovieList(in);
        }

        @Override
        public MovieList[] newArray(int size) {
            return new MovieList[size];
        }
    };

    public MovieList() {

    }

    public MovieList(MovieList movieList) {
        this.id = movieList.getId();
        this.title = movieList.getTitle();
        this.posterUrl = movieList.getPosterUrl();
        this.description = movieList.getDescription();
        this.vote_average = movieList.getVote_average();
        this.releaseDate = movieList.getReleaseDate();
        this.totalBuyQuantity =movieList.getTotalBuyQuantity();
        this.openInterest= movieList.getOpenInterest();
        this.interest= movieList.getInterest();
        this.pchangeoice=movieList.getPchangeoice();
        this.pchangeoipe=movieList.getPchangeoipe();
    }

    public String getId() {
        return id;
    }

    public long getTitle() {
        return title;
    }

    public long getPosterUrl() {
        return posterUrl;
    }

    public long getDescription() {
        return description;
    }

    public long getVote_average(){
        return vote_average;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public long getTotalBuyQuantity() {
        return totalBuyQuantity;
    }

    public long getOpenInterest() {
        return openInterest;
    }

    public long getInterest() {
        return interest;
    }

    public double getPchangeoice() {
        return pchangeoice;
    }

    public double getPchangeoipe() {
        return pchangeoipe;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(long title) {
        this.title = title;
    }

    public void setPosterUrl(long posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setDescription(long description) {
        this.description = description;
    }

    public void setVote_average(long vote_average) {
        this.vote_average = vote_average;
    }

    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setTotalBuyQuantity(long totalBuyQuantity) { this.totalBuyQuantity = totalBuyQuantity; }

    public void setOpenInterest(long openInterest) { this.openInterest = openInterest; }

    public void setInterest(long interest) { this.interest = interest; }

    public void setPchangeoice(double pchangeoice) {
        this.pchangeoice = pchangeoice;
    }

    public void setPchangeoipe(double pchangeoipe) {
        this.pchangeoipe = pchangeoipe;
    }

    public MovieList(String id, long title, long description, long posterUrl, long vote_average, double pchangeoice) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.description = description;
        this.vote_average = vote_average;
        this.releaseDate = releaseDate;
        this.totalBuyQuantity =totalBuyQuantity;
        this.openInterest=openInterest;
        this.interest=interest;
        this.pchangeoice= Math.round(pchangeoice *100)/100;
        this.pchangeoipe=pchangeoipe;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(title);
        dest.writeLong(posterUrl);
        dest.writeLong(description);
        dest.writeLong(vote_average);
        dest.writeLong(releaseDate);
        dest.writeLong(totalBuyQuantity);
        dest.writeLong(openInterest);
        dest.writeLong(interest);
        dest.writeDouble(pchangeoice);
        dest.writeDouble(pchangeoipe);
    }
}
