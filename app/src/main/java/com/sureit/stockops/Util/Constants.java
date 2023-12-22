package com.sureit.stockops.Util;

import com.sureit.stockops.BuildConfig;

import java.util.Map;

public class Constants {
    public static final String BASE_URL_MOVIE = "https://api.themoviedb.org/3/discover/movie?api_key=";
    public static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?api_key=";
    public static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=";
    public static final String TRAILERS_MOVIES_URL = "https://api.themoviedb.org/3/movie/";

    public static final String API_KEY = BuildConfig.MovieSecAPIKEY;

    public static final String PARCEL_KEY="MovieParcel";
    public static final String DB_NAME ="stockops.db";
    public static  boolean FAV_ROT = false;
    public static double mValConst = 0.0;

    public static String access_token_pm;
    public static String BankNiftyScripIDsPM;
    public static Map<String, String> kvpBankNiftyScripIDsPM;

}
