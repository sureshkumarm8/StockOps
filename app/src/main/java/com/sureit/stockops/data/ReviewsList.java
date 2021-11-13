package com.sureit.stockops.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ReviewsList implements Parcelable{
    private String author;
    private String reviewContent;

    public ReviewsList(String author, String reviewContent){
        this.author = author;
        this.reviewContent = reviewContent;
    }

    private ReviewsList(Parcel in) {
        author = in.readString();
        reviewContent = in.readString();
    }

    public static final Creator<ReviewsList> CREATOR = new Creator<ReviewsList>() {
        @Override
        public ReviewsList createFromParcel(Parcel in) {
            return new ReviewsList(in);
        }

        @Override
        public ReviewsList[] newArray(int size) {
            return new ReviewsList[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(reviewContent);
    }
}
