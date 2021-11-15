package com.sureit.stockops.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.view.MovieDetailsActivity;
import com.sureit.stockops.data.MovieList;
import com.sureit.stockops.R;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {


    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_VOTE_AVERAGE= "vote_average";
    public static final String KEY_RELEASE_DATE= "release_date";
    // we define a list from the DevelopersList java class

    private List<MovieList> movieLists;
    private Context context;

    public MovieAdapter(List<MovieList> movieLists, Context context) {

        // generate constructors to initialise the List and Context objects

        this.movieLists = movieLists;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.strike_price_item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views
        final MovieList developersList = movieLists.get(position);
        holder.strikePriceTV.setText(String.valueOf(developersList.getId()));
        holder.totalVolumeCE.setText(developersList.getTitle());
        holder.totalVolumePE.setText(developersList.getDescription());
        holder.totalBuyQuantityCE.setText(developersList.getPosterUrl());
        holder.totalAskQuantityCE.setText(developersList.getVote_average());
        holder.totalBuyQuantityPE.setText(developersList.getReleaseDate());
        holder.totalAskQuantityPE.setText(developersList.getTotalBuyQuantity());
        holder.strikePriceTV2.setText(developersList.getOpenInterest());
        holder.strikePriceTV3.setText(developersList.getInterest());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieList movieList1 = movieLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), MovieDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,
                        new MovieList(movieList1.getId() ,movieList1.getTitle(),movieList1.getDescription(),movieList1.getPosterUrl(),
                                movieList1.getVote_average(),movieList1.getReleaseDate(), movieList1.getTotalBuyQuantity(), movieList1.getOpenInterest(), movieList1.getInterest()));
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    @Override

    //return the size of the listItems (developersList)

    public int getItemCount() {
        return movieLists.size();
    }

    public void setMoviesLive(List<MovieList> moviesLive) {
        this.movieLists = moviesLive;
    }

    public List<MovieList> getMoviesLive() {
        return movieLists;
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        RelativeLayout relativeLayout;
        TextView strikePriceTV;
        TextView totalVolumeCE;
        TextView totalVolumePE;
        TextView totalBuyQuantityCE;
        TextView totalAskQuantityCE;
        TextView totalBuyQuantityPE;
        TextView totalAskQuantityPE;
        TextView strikePriceTV2;
        TextView strikePriceTV3;

        private ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects
            relativeLayout = itemView.findViewById(R.id.relativeLayoutRV);
            strikePriceTV = itemView.findViewById(R.id.tvStrikePrice);
            totalVolumeCE = itemView.findViewById(R.id.tvVolumeCE);
            totalVolumePE = itemView.findViewById(R.id.tvVolumePE);
            totalBuyQuantityCE = itemView.findViewById(R.id.tvBuyQuantityCE);
            totalAskQuantityCE = itemView.findViewById(R.id.tvASKQuantityCE);
            totalBuyQuantityPE = itemView.findViewById(R.id.tvBuyQuantityPE);
            totalAskQuantityPE = itemView.findViewById(R.id.tvASKQuantityPE);
            strikePriceTV2 = itemView.findViewById(R.id.tvStrikePrice2);
            strikePriceTV3 = itemView.findViewById(R.id.tvStrikePrice3);
        }

    }
}
