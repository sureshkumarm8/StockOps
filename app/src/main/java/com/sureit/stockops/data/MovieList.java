package com.sureit.stockops.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "moviesfav")
public class MovieList implements Parcelable {


    @PrimaryKey
    @ColumnInfo
    private long id;
    private String title;
    private String posterUrl;
    private String description;
    private String vote_average;
    private String releaseDate;
    private String totalBuyQuantity;
    private String openInterest;
    private String interest;

    private MovieList(Parcel in) {
        id=in.readLong();
        title = in.readString();
        posterUrl = in.readString();
        description = in.readString();
        vote_average = in.readString();
        releaseDate = in.readString();
        totalBuyQuantity=in.readString();
        openInterest=in.readString();
        interest=in.readString();
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
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getVote_average(){
        return vote_average;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getTotalBuyQuantity() {
        return totalBuyQuantity;
    }

    public String getOpenInterest() {
        return openInterest;
    }

    public String getInterest() {
        return interest;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVote_average(String vote_average) {
        this.vote_average = vote_average;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setTotalBuyQuantity(String totalBuyQuantity) { this.totalBuyQuantity = totalBuyQuantity; }

    public void setOpenInterest(String openInterest) { this.openInterest = openInterest; }

    public void setInterest(String interest) { this.interest = interest; }

    public MovieList(long id, String title, String description, String posterUrl, String vote_average, String releaseDate, String totalBuyQuantity, String openInterest, String interest) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.description = description;
        this.vote_average = vote_average;
        this.releaseDate = releaseDate;
        this.totalBuyQuantity =totalBuyQuantity;
        this.openInterest=openInterest;
        this.interest=interest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(posterUrl);
        dest.writeString(description);
        dest.writeString(vote_average);
        dest.writeString(releaseDate);
        dest.writeString(totalBuyQuantity);
        dest.writeString(openInterest);
        dest.writeString(interest);
    }
}
