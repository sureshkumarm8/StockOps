package com.sureit.stockops.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.sureit.stockops.R;
import com.sureit.stockops.Util.AppExecutors;
import com.sureit.stockops.Util.Constants;
import com.sureit.stockops.adapter.MovieAdapter;
import com.sureit.stockops.adapter.ReviewsAdapter;
import com.sureit.stockops.adapter.TrailerAdapter;
import com.sureit.stockops.data.MovieList;
import com.sureit.stockops.data.ReviewsList;
import com.sureit.stockops.data.TrailerList;
import com.sureit.stockops.db.MovieDao;
import com.sureit.stockops.db.MovieDatabase;
import com.sureit.stockops.db.MovieViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.stockops.Util.Constants.API_KEY;
import static com.sureit.stockops.Util.Constants.DB_NAME;
import static com.sureit.stockops.Util.Constants.POSTER_BASE_URL;
import static com.sureit.stockops.Util.Constants.POSTER_BASE_URL2;
import static com.sureit.stockops.Util.Constants.REVIEW_SEG;
import static com.sureit.stockops.Util.Constants.TRAILERS_MOVIES_URL;
import static com.sureit.stockops.Util.Constants.TRAILER_SEG;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";
    NoInternetDialog noInternetDialog;
    private List<TrailerList> trailerLists;
    private TrailerAdapter adapter;
    private RecyclerView recyclerViewTr;
    private long idVal;

    private List<ReviewsList> reviewsLists;
    private ReviewsAdapter adapter2;
    private RecyclerView recyclerViewRV;

    private TextView favTV;
    private ImageView favView;
    private boolean isFavorite = false;

    private MovieDatabase movieDatabase;
    private MovieDao mMovieDao;
    private MovieList movie;
    private boolean update;
    private MovieList movieList;
    public static String stringT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mMovieDao = Room.databaseBuilder(this, MovieDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getMovieDao();

        setupUI();

    }

    public void setupUI(){
        noInternetDialog = new NoInternetDialog.Builder(this).build();
        ImageView posterBannerIV= findViewById(R.id.posterBanner);
        ImageView posterImageView = findViewById(R.id.posterImageView);
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView description = findViewById(R.id.tVdescription);
        TextView releaseTV= findViewById(R.id.tVreleaseDate);
        TextView ratingTV= findViewById(R.id.tVRatVal);
        favTV = findViewById(R.id.tVfav);

        ImageView trailerVV = findViewById(R.id.videoViewTrailer);
        recyclerViewTr = findViewById(R.id.trailerRV);
        recyclerViewTr.setHasFixedSize(false);
        recyclerViewTr.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerViewTr.setItemAnimator(new DefaultItemAnimator());
        trailerLists = new ArrayList<>();

        recyclerViewRV = findViewById(R.id.reviewsRV);
        recyclerViewRV.setHasFixedSize(false);
        recyclerViewRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRV.setItemAnimator(new DefaultItemAnimator());
        reviewsLists = new ArrayList<>();

//        Intent intent = getIntent();
//        final String titleText = intent.getStringExtra(MovieAdapter.KEY_NAME);
//        String image = intent.getStringExtra(MovieAdapter.KEY_IMAGE);
//        final String descriptionText = intent.getStringExtra(MovieAdapter.KEY_DESCRIPTION);
//        final String ratings =intent.getStringExtra(MovieAdapter.KEY_VOTE_AVERAGE)+" / 10";
//        final String releaseDate=intent.getStringExtra(MovieAdapter.KEY_RELEASE_DATE);

        Bundle data=getIntent().getExtras();
        assert data != null;
        movieList= data.getParcelable(Constants.PARCEL_KEY);
        assert movieList != null;

        idVal = movieList.getId();
        final String titleText = String.valueOf(movieList.getTitle());
        String image = POSTER_BASE_URL+ movieList.getPosterUrl();
        String imageB = POSTER_BASE_URL2+ movieList.getPosterUrl();
        final String descriptionText = String.valueOf(movieList.getDescription());
        final String ratings =movieList.getVote_average()+" / 10";
        final String releaseDate= String.valueOf(movieList.getReleaseDate());

        loadTrailers();
        loadReviews();

        Picasso.with(this)
                .load(imageB)
                .into(posterBannerIV);

        Picasso.with(this)
                .load(image)
                .into(posterImageView);

        titleTextView.setText(titleText);
        ratingTV.setText(ratings);
        releaseTV.setText(releaseDate);
        description.setText(descriptionText);

        favView = findViewById(R.id.favIcon);
        favView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(v);
            }
        });
        if(mMovieDao.getMovieWithId(idVal)){
            favView.setImageResource(R.drawable.fav_fill);
            favTV.setVisibility(View.GONE);
            isFavorite = true;
        }

    }

    private void loadReviews() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                TRAILERS_MOVIES_URL+idVal+REVIEW_SEG+API_KEY , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray array = jsonObject.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject jo = array.getJSONObject(i);

                        ReviewsList reviewsListData = new ReviewsList(jo.getString("author"),jo.getString("content"));
                        reviewsLists.add(reviewsListData);

                    }

                    adapter2 = new ReviewsAdapter(reviewsLists, getApplicationContext());
                    recyclerViewRV.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MovieDetailsActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadTrailers() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                TRAILERS_MOVIES_URL+idVal+TRAILER_SEG+API_KEY , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray array = jsonObject.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject jo = array.getJSONObject(i);

                        TrailerList trailerListData = new TrailerList(jo.getString("id"),jo.getString("key"));
                        trailerLists.add(trailerListData);

                    }

                    adapter = new TrailerAdapter(trailerLists, getApplicationContext());
                    recyclerViewTr.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MovieDetailsActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void toggleFavorite(final View v) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFavorite) {
                            // favorite new task
                            try {
                                mMovieDao.insert(movieList);
//                            setResult(RESULT_OK);
                            } catch (SQLiteConstraintException e) {
                                Snackbar.make(v.getRootView(), "A movie with same details already exists.", Snackbar.LENGTH_SHORT).show();
                            }
                            favView.setImageResource(R.drawable.fav_fill);
                            favTV.setVisibility(View.GONE);
                            Snackbar.make(v.getRootView(),"Added to favorites", Snackbar.LENGTH_SHORT).show();
                            isFavorite = true;
                        } else {
                            //unfavorite task
                            mMovieDao.delete(movieList);
                            favTV.setVisibility(View.VISIBLE);
                            favView.setImageResource(R.drawable.fav_empty);
                            setResult(RESULT_OK);
                            Snackbar.make(v.getRootView(),"Removed from favorites", Snackbar.LENGTH_SHORT).show();
                            isFavorite = false;
                            setupViewModel();
                        }
                    }
                });
            }
        });

    }

    private void setupViewModel() {
        MovieViewModel viewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<MovieList>>() {
            @Override
            public void onChanged(@Nullable List<MovieList> taskEntries) {
                new MovieAdapter(taskEntries,getApplicationContext());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}
