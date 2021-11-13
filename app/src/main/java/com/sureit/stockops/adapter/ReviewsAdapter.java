package com.sureit.stockops.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sureit.stockops.R;
import com.sureit.stockops.data.ReviewsList;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private List<ReviewsList> reviewsLists;
    private Context context;

    private final String YOUTUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg";

    public ReviewsAdapter(List<ReviewsList> reviewsLists, Context context){
        this.reviewsLists = reviewsLists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_review,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final ReviewsList reviewsList = reviewsLists.get(position);

        holder.authorTV.setText(reviewsList.getAuthor());
        holder.reviewTV.setText(reviewsList.getReviewContent());

    }

    @Override
    public int getItemCount() {
        return reviewsLists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorTV;
        TextView reviewTV;

        ViewHolder(View itemView) {
            super(itemView);
            authorTV = itemView.findViewById(R.id.authorNameTv);
            reviewTV = itemView.findViewById(R.id.contentTv);
        }
    }
}
