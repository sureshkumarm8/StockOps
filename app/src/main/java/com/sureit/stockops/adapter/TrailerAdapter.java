package com.sureit.stockops.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.sureit.stockops.R;
import com.sureit.stockops.data.TrailerList;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter <TrailerAdapter.ViewHolder> {

    String trailer_name="trailerName";
    String favourite_data="FavouriteData";

    private List<TrailerList> trailerLists;
    private Context context;

    private final String YOUTUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg";

    public TrailerAdapter(){}

    public TrailerAdapter(List<TrailerList> trailerLists, Context context){
        this.trailerLists = trailerLists;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_trailer,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views

        final TrailerList trailersList = trailerLists.get(position);

        Picasso.with(context)
                .load(String.format(YOUTUBE_THUMBNAIL_URL, trailerLists.get(position).getTrailerKey()))
                .placeholder(R.drawable.youtube)
                .into(holder.videoViewTrailer);

        holder.relativeLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrailerList trailerList1 = trailerLists.get(position);
               v.getContext().startActivity(
                       new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v="+trailerList1.getTrailerKey())));

                Toast.makeText(v.getContext(), trailerList1.getTrailerKey(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return trailerLists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView videoViewTrailer;
        RelativeLayout relativeLayout2;

        ViewHolder(View itemView) {
            super(itemView);
            videoViewTrailer = itemView.findViewById(R.id.videoViewTrailer);
            relativeLayout2 = itemView.findViewById(R.id.relativeLayoutRV2);
        }
    }
}
