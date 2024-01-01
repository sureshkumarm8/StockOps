package com.sureit.stockops.Util;

import static com.sureit.stockops.Util.Constants.BankNiftyScripIDsPM;
import static com.sureit.stockops.Util.Constants.access_token_pm;
import static com.sureit.stockops.Util.Constants.kvpBankNiftyScripIDsPM;
import static com.sureit.stockops.view.BanksListActivity.URL_MacFTPServer_BankNiftyOIData;
import static com.sureit.stockops.view.BanksListActivity.URL_MacFTPServer_BanksLiveData;
import static com.sureit.stockops.view.BanksListActivity.deleteCache;
import static com.sureit.stockops.view.BanksListActivity.paytmURL;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.sureit.stockops.R;
import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.db.BankNiftyDao;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.view.BanksListActivity;
import com.sureit.stockops.view.PostVolleyJsonRequest;
import com.sureit.stockops.view.VolleyJsonRespondsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StockDataRetrieveService extends Service implements VolleyJsonRespondsListener {

    public static final int interval_min = 60 * 1000;  //interval between two services(Here Service run every 1/2 Minute)

    private String OpsID = null;
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    LocalBinder localBinder = new LocalBinder();

    private static final String LOG_TAG = "StockRetrieveService";
    private List<BankNiftyList> bankNiftyLists;
    private List<BanksList> banksLists;
    BanksListActivity banksListActivityObj;

    BanksDao banksDao;
    BankNiftyDao bankNiftyDao;

    final Map<String, String> headers = new HashMap<String, String>();
    public String cookiedata;
    private int ceTotalTradedVolume;
    private int peTotalTradedVolume;
    private int ceTotalBuyQuantity;
    private int ceTotalSellQuantity;
    private int peTotalBuyQuantity;
    private int peTotalSellQuantity;
    private int ceOpenInterest;
    private int peOpenInterest;
    private Double underlyingValue;
    private String timeStampValue;
    private BankNiftyList bankNiftyList;
    private BanksList banksList;

    private boolean bnkT = false;
    private boolean mainT = false;
    private boolean retry = false;
    private boolean macFTPfile = false;

    long allBanksBuyQuantity;
    long allBanksSellQuantity;
    long allBanksQuantityTraded;
    long allBanksDeliveryQuantity;
    double allBanksPercentDifference;
    private int retrievesDataCompleted = 0;
    private int minsCount = 0;
    private Context mContextSTRS;
    private double mValTBQAllBanks;
    private double mValTSQAllBanks;
    private double mVAlQTSAllBanks;
    private double mValDPAllBanks;
    private double mStrTBQAllBanks;
    private double mStrTSQAllBanks;
    private double mStrQTSAllBanks;
    private double mStrDPAllBanks;
    private DecimalFormat df = new DecimalFormat("0.00");
    private double mValDPtoMStr;



    public StockDataRetrieveService() {
    }

    public StockDataRetrieveService(StringBuilder opsID, Map<String, String> kvp) {
        BankNiftyScripIDsPM = String.valueOf(opsID);
        kvpBankNiftyScripIDsPM = kvp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContextSTRS = getApplicationContext();

        bankNiftyLists = new ArrayList<>();
        banksLists = new ArrayList<>();
        banksListActivityObj = new BanksListActivity();
        banksDao = BanksDatabase.getInstance(getApplicationContext()).getBanks();
        bankNiftyDao = BanksDatabase.getInstance(getApplicationContext()).getBankNiftyCP();
        importKeysFromFTP();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_id";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "my_channel_title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            @SuppressLint("WrongConstant") Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("@string/app_name")
                    .setContentText("Stock Data Retrieve Service Running in Background")
                    .setColor(ContextCompat.getColor(this, R.color.cardview_dark_background))
                    .setBadgeIconType(R.mipmap.ic_launcher_bull_bear)
                    .build();

            if (mTimer != null) // Cancel if already existed
                mTimer.cancel();
            else
                mTimer = new Timer();   //recreate new
            //TimerTask will run every Min. downloads Banks data files from FTP
            mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, interval_min);   //Schedule task

            startForeground(1, notification);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }

    public void paytmBanksLiveData() {

        try {
            String banksID = "NSE:25:INDEX,NSE:21238:EQUITY,NSE:5900:EQUITY,NSE:2263:EQUITY,NSE:1023:EQUITY,NSE:1333:EQUITY,NSE:11184:EQUITY,NSE:4963:EQUITY,NSE:5258:EQUITY,NSE:1922:EQUITY,NSE:10666:EQUITY,NSE:18391:EQUITY,NSE:3045:EQUITY";
            new PostVolleyJsonRequest(localBinder.getService(), StockDataRetrieveService.this, "payTMBanks", paytmURL+banksID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paytmNiftyBankLiveData() {
        String opsID = BankNiftyScripIDsPM;

//        String opsID = "NSE:25:INDEX,NSE:50118:OPTION";
        try {
            new PostVolleyJsonRequest(localBinder.getService(), StockDataRetrieveService.this, "payTMBankNifty", paytmURL+opsID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StockDataRetrieveService importKeysFromFTP() {
        File importDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            importDir = new File(getExternalFilesDir(null), "/StockOps/DBFiles/");
        }else{
            importDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/StockOps/DBFiles/");}

        File file = new File(importDir, "bankNiftyScrips.txt");
        Map<String, String> kvp = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line based on a delimiter, such as a comma or whitespace
                String[] parts = line.split("\\s*:\\s*"); // Example: split by comma

                // Add the key-value pair to the map
                if (parts.length == 2) {
                    kvp.put(parts[0].replaceAll("[\",]", ""),
                            parts[1].trim().replaceAll("[\",]", ""));
                }else{
                    access_token_pm = line;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder opsID = new StringBuilder("NSE:25:INDEX,");
        // Print the contents of the map
        for (String key : kvp.keySet()) {
            opsID.append("NSE:"+key+":OPTION,");
        }
        return new StockDataRetrieveService(opsID.deleteCharAt(opsID.length() - 1),kvp);
    }

    private void downloadBanksLiveDataFromMAC_FTP() {

        /*
        1. Start FTP server :  http-server ./ -p 1313
        2. Strart Node js: suresh@Suresh:~/Desktop/Suresh/Stock/stock-market-india$node app.js 3000
        3. Run Scripts:
            suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$python3 bankNiftydata.py
            suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$sh banksLiveQuotes.sh
        https://github.com/jugaad-py/jugaad-data
        https://github.com/maanavshah/stock-market-india
        */
        deleteCache(this);
        try {
//            for(int i=1; i<= 3; i++){
            new PostVolleyJsonRequest(localBinder.getService(), StockDataRetrieveService.this, "Banks", URL_MacFTPServer_BanksLiveData, null);
//            }
//            new PostVolleyJsonRequest(BanksListActivity.this, BanksListActivity.this,"StrikePrice", URL_MacFTPServer_BanksLiveData +"StrikeP.json", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadBankNiftyOIDataFromMAC_FTP() {

        /*
        1. Start FTP server :  http-server ./ -p 1313
        2. Strart Node js: suresh@Suresh:~/Desktop/Suresh/Stock/stock-market-india$node app.js 3000
        3. Run Screipt: suresh@Suresh:~/Desktop/Suresh/Stock/liveQuotesData$sh banksLiveQuotes.sh
        https://github.com/maanavshah/stock-market-india
        */
        deleteCache(this);
        try {
            new PostVolleyJsonRequest(StockDataRetrieveService.this, StockDataRetrieveService.this, "BankNifty", URL_MacFTPServer_BankNiftyOIData, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSuccessJson(String response, String type) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm:ss");
        String currentTime = dateFormat.format(new Date()).toString();
        Log.v("TimeStarts",currentTime);
        //Process the BankNifty JsonObject
        if (type.equals("BankNifty") && response.contains("data")) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StockDataRetrieveService.this.bankNiftySuccessJson(jsonObject);
//                        StockDataRetrieveService.this.bankNiftyPMSuccessJson(jsonObject);
                    }
                });
                t.start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Process the AllBanks JsonObject
        if(type.equals("Banks") && response.contains("data")) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StockDataRetrieveService.this.banksSuccessJson(jsonObject);
                    }
                });
                t.start();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(type.equals("payTMBanks")&& response.contains("data")) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StockDataRetrieveService.this.banksSuccessJsonPM(jsonObject);
                    }
                });
                t.start();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(type.equals("payTMBankNifty")&& response.contains("data")) {
            try {
                final JSONObject jsonObject = new JSONObject(response);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StockDataRetrieveService.this.bankNiftySuccessJsonPM(jsonObject);
                    }
                });
                t.start();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void banksSuccessJson(JSONObject jsonObject) {
        long totalBuyQuantity = 0;
        long totalSellQuantity = 0;
        long tradedVolume = 0;
        long deliveryQuantity = 0;
        String bankName = "";

        try {
            retrievesDataCompleted++;
            String currentTime;

            currentTime = jsonObject.getString("lastUpdateTime").substring(12, 17);
            JSONArray banksArr = jsonObject.getJSONArray("data");

//            JSONObject ul = banksArr.getJSONObject(banksArr.length() - 1);
//            //String ulValue = ul.getString("last").substring(0, 6).replaceAll(",", ""); //old value with last variable
//            String ulValue = ul.getString("lastPrice").substring(0,5);
//            long underlyingValue = Long.parseLong(ulValue);
            String ulValue = jsonObject.getString("lastprice").substring(0,5);
            long underlyingValue = Long.parseLong(ulValue);

            for (int i = 0; i < banksArr.length() - 1; i++) {
                JSONObject jo = banksArr.getJSONObject(i);
                bankName = jo.getString("symbol");
                String totalBuyQuantityStr = jo.getString("totalBuyQuantity");
                totalBuyQuantity = parseToLongfrom_(totalBuyQuantityStr.trim());
                String totalSellQuantityStr = jo.getString("totalSellQuantity");
                totalSellQuantity = parseToLongfrom_(totalSellQuantityStr.trim());

                String tradedVolumeStr = jo.getString("totalTradedVolume");
                tradedVolume = parseToLongfrom_(tradedVolumeStr.trim());
                String deliveryQuantityStr = jo.getString("deliveryQuantity");
                deliveryQuantity = parseToLongfrom_(deliveryQuantityStr.trim());

                double percentDiff = getPercentDiff(bankName + "MStr");

                //Live display Data
                banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, underlyingValue, percentDiff);
                banksLists.add(banksList);

                allBanksBuyQuantity += totalBuyQuantity;
                allBanksSellQuantity += totalSellQuantity;
                allBanksQuantityTraded += tradedVolume;
                allBanksDeliveryQuantity += deliveryQuantity;
                allBanksPercentDifference += percentDiff;

                //To store in DB
                banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, underlyingValue, percentDiff);
                banksDao.insertBankData(banksList);

                movementTrackingBanks(bankName);
            }
//                macFTPfile = false;
//                if(retrievesDataCompleted==3){
            banksList = new BanksList(currentTime, "All Banks", allBanksBuyQuantity, allBanksSellQuantity, allBanksQuantityTraded, underlyingValue, allBanksPercentDifference);
            banksDao.insertBankData(banksList);
//                    movementTrackingBanks("All Banks");

            mValTBQAllBanks = Double.parseDouble(df.format(mValTBQAllBanks / 12));
            mValTSQAllBanks = Double.parseDouble(df.format(mValTSQAllBanks / 12));
            mValDPAllBanks = Double.parseDouble(df.format(mValDPAllBanks / 12));
            banksList = new BanksList(currentTime, "All Banksmval", allBanksBuyQuantity, allBanksSellQuantity,
                    mValTBQAllBanks , mValTSQAllBanks , mVAlQTSAllBanks, underlyingValue, mValDPAllBanks );
            banksDao.insertBankData(banksList);

            mStrTBQAllBanks = Double.parseDouble(df.format(mStrTBQAllBanks / 12));
            mStrTSQAllBanks = Double.parseDouble(df.format(mStrTSQAllBanks / 12));
            mStrDPAllBanks = Double.parseDouble(df.format(mStrDPAllBanks / 12));
            //Added mValDPAllBanks instead of VOl column
            banksList = new BanksList(currentTime, "All BanksMStr", allBanksBuyQuantity, allBanksSellQuantity,
                    mStrTBQAllBanks , mStrTSQAllBanks , mValDPAllBanks, underlyingValue, mStrDPAllBanks);
            banksDao.insertBankData(banksList);

            mValTBQAllBanks = 0;
            mValTSQAllBanks = 0;
            mVAlQTSAllBanks = 0;
            mValDPAllBanks = 0;
            mStrTBQAllBanks = 0;
            mStrTSQAllBanks = 0;
            mStrQTSAllBanks = 0;
            mStrDPAllBanks = 0;

            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("AllBanksLiveDisplaySP", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("All Banks", "All Banks");
            editor.putString("allBanksBuyQuantity", String.valueOf(allBanksBuyQuantity));
            editor.putString("allBanksSellQuantity", String.valueOf(allBanksSellQuantity));
            editor.putString("allBanksQuantityTraded", String.valueOf(allBanksQuantityTraded));
            editor.putString("underlyingValue", String.valueOf(underlyingValue));
            editor.putFloat("allBanksDeliveryPercent", Float.parseFloat(df.format(allBanksPercentDifference/12)));
            Gson gson = new Gson();
            String json = gson.toJson(banksLists);
            editor.putString("BanksLiveDisplayData", json);
            editor.commit();

            allBanksBuyQuantity = 0;
            allBanksSellQuantity = 0;
            allBanksQuantityTraded = 0;
            allBanksDeliveryQuantity = 0;
            allBanksPercentDifference = 0.0;
            retrievesDataCompleted = 0;
            banksLists.clear();
//                }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void bankNiftySuccessJson(JSONObject jsonObject) {
        try {
            JSONObject recordDetails = jsonObject.getJSONObject("records");
            JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
            JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
            timeStampValue = recordDetails.get("timestamp").toString();
            underlyingValue = recordDetails.getDouble("underlyingValue");
            int ulValue = underlyingValue.intValue();
            for (int i = 0; i < filteredDataArray.length(); i++) {
                JSONObject jo = filteredDataArray.getJSONObject(i);

                Double strikePrice = null;
                int sPrice = 0;
                if (strikePrice == null) {
                    JSONObject usBody = jo.getJSONObject("CE");
                    strikePrice = Double.valueOf(usBody.getString("strikePrice"));
                    sPrice = strikePrice.intValue();
                }
                if (sPrice <= ulValue + 700 && sPrice >= ulValue - 700) {
                    JSONObject ceBody = jo.getJSONObject("CE");
                    JSONObject peBody = jo.getJSONObject("PE");

                    if (sPrice > ulValue - 200) {

                        //Add total values for main top card
                        ceTotalTradedVolume += (int) ceBody.get("totalTradedVolume");
                        ceTotalBuyQuantity += (int) ceBody.get("totalBuyQuantity");
                        ceTotalSellQuantity += (int) ceBody.get("totalSellQuantity");
                        ceOpenInterest += (int) ceBody.get("openInterest");

                        bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                ceBody.getString("identifier").substring(25, 32),
                                ceBody.getLong("totalTradedVolume")/1000,
                                ceBody.getLong("totalBuyQuantity")/1000,
                                ceBody.getLong("totalSellQuantity")/1000,
                                ceBody.getLong("openInterest")/1000,
                                ceBody.getDouble("pchangeinOpenInterest")
                        );
                        bankNiftyLists.add(bankNiftyList);
                        bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        movementTrackingNifty(ceBody.getString("identifier").substring(25, 32));
                    }
                    if (sPrice < ulValue - 200) {

                        //Add total values for main top card
                        peTotalTradedVolume += (int) peBody.get("totalTradedVolume");
                        peTotalBuyQuantity += (int) peBody.get("totalBuyQuantity");
                        peTotalSellQuantity += (int) peBody.get("totalSellQuantity");
                        peOpenInterest += (int) peBody.get("openInterest");

                        bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                peBody.getString("identifier").substring(25, 32),
                                peBody.getLong("totalTradedVolume")/1000,
                                peBody.getLong("totalBuyQuantity")/1000,
                                peBody.getLong("totalSellQuantity")/1000,
                                peBody.getLong("openInterest")/1000,
                                peBody.getDouble("pchangeinOpenInterest")
                        );
                        bankNiftyLists.add(bankNiftyList);
                        bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        movementTrackingNifty(peBody.getString("identifier").substring(25, 32));
                    }
                }
            }

            //Add data for "OI History"
            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17), "OI History", (long) (ceOpenInterest / 1000), (long) (peOpenInterest / 1000), (long) (ceTotalTradedVolume / 1000), (long) (peTotalTradedVolume / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("OI History");

            //Add data for "CE History"
            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17), "CE History", (long) (ceTotalBuyQuantity / 1000), (long) (ceTotalSellQuantity / 1000), (long) (ceTotalTradedVolume / 1000), (long) (ceOpenInterest / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("CE History");

            //Add data for "PE History"
            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17), "PE History", (long) (peTotalBuyQuantity / 1000), (long) (peTotalSellQuantity / 1000), (long) (peTotalTradedVolume / 1000), (long) (peOpenInterest / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("PE History");


            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("NiftyOILiveDisplaySP", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("timeStampValue", timeStampValue);
            editor.putString("underlyingValue", String.valueOf(underlyingValue));
            editor.putString("ceTotalTradedVolume", String.valueOf(ceTotalTradedVolume / 1000));
            editor.putString("peTotalTradedVolume", String.valueOf(peTotalTradedVolume / 1000));
            editor.putString("ceTotalBuyQuantity", String.valueOf(ceTotalBuyQuantity / 1000));
            editor.putString("ceTotalSellQuantity", String.valueOf(ceTotalSellQuantity / 1000));
            editor.putString("peTotalBuyQuantity", String.valueOf(peTotalBuyQuantity / 1000));
            editor.putString("peTotalSellQuantity", String.valueOf(peTotalSellQuantity / 1000));
            editor.putString("ceOpenInterest", String.valueOf(ceOpenInterest / 1000));
            editor.putString("peOpenInterest", String.valueOf(peOpenInterest / 1000));
            Gson gson = new Gson();
            try{
                String json = gson.toJson(bankNiftyLists);
                editor.putString("bankNiftyData", json);
                editor.commit();
            }catch (Exception e){
                Log.v("JSON Exception","ConcurrentModificationException");
            }

            ceTotalTradedVolume = 0;
            peTotalTradedVolume = 0;
            ceTotalBuyQuantity = 0;
            ceTotalSellQuantity = 0;
            peTotalBuyQuantity = 0;
            peTotalSellQuantity = 0;
            ceOpenInterest = 0;
            peOpenInterest = 0;
            bankNiftyLists.clear();
        } catch (JSONException e) {
            e.printStackTrace();
//            Toast.makeText(StockDataRetrieveService.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void banksSuccessJsonPM(JSONObject jsonObject) {
        long totalBuyQuantity = 0;
        long totalSellQuantity = 0;
        long tradedVolume = 0;
        long deliveryQuantity = 0;
        String bankName = "";

        try {
            retrievesDataCompleted++;
            String currentTime;


            JSONArray banksArr = jsonObject.getJSONArray("data");
            long last_update_time = banksArr.getJSONObject(0).getLong("last_update_time");
            Date date = new Date(last_update_time * 1000L); // Convert to milliseconds
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String formattedTime = formatter.format(date);
            currentTime = formattedTime.substring(12, 17);
            String ulValue =  banksArr.getJSONObject(0).getString("last_price").substring(0,5);
            long underlyingValue = Long.parseLong(ulValue);
            final String[] banks = {"NIFTYBANK","AUBANK", "AXISBANK", "BANDHANBNK", "FEDERALBNK", "HDFCBANK", "ICICIBANK", "INDUSINDBK", "IDFCFIRSTB", "KOTAKBANK", "PNB", "RBLBANK", "SBIN"};

            for (int i = 1; i < banksArr.length() - 1; i++) {
                JSONObject jo = banksArr.getJSONObject(i);
                bankName = banks[i];
                String totalBuyQuantityStr = jo.getString("total_buy_quantity");
                totalBuyQuantity = parseToLongfrom_(totalBuyQuantityStr.trim());
                String totalSellQuantityStr = jo.getString("total_sell_quantity");
                totalSellQuantity = parseToLongfrom_(totalSellQuantityStr.trim());

                String tradedVolumeStr = jo.getString("volume_traded");
                tradedVolume = parseToLongfrom_(tradedVolumeStr.trim());
                String deliveryQuantityStr = jo.getString("last_traded_quantity");
                deliveryQuantity = parseToLongfrom_(deliveryQuantityStr.trim());

                double percentDiff = getPercentDiff(bankName + "MStr");

                //Live display Data
                banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, underlyingValue, percentDiff);
                banksLists.add(banksList);

                allBanksBuyQuantity += totalBuyQuantity;
                allBanksSellQuantity += totalSellQuantity;
                allBanksQuantityTraded += tradedVolume;
                allBanksDeliveryQuantity += deliveryQuantity;
                allBanksPercentDifference += percentDiff;

                //To store in DB
                banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, underlyingValue, percentDiff);
                banksDao.insertBankData(banksList);

                movementTrackingBanks(bankName);
            }
//                macFTPfile = false;
//                if(retrievesDataCompleted==3){
            banksList = new BanksList(currentTime, "All Banks", allBanksBuyQuantity, allBanksSellQuantity, allBanksQuantityTraded, underlyingValue, allBanksPercentDifference);
            banksDao.insertBankData(banksList);
//                    movementTrackingBanks("All Banks");

            mValTBQAllBanks = Double.parseDouble(df.format(mValTBQAllBanks / 12));
            mValTSQAllBanks = Double.parseDouble(df.format(mValTSQAllBanks / 12));
            mValDPAllBanks = Double.parseDouble(df.format(mValDPAllBanks / 12));
            banksList = new BanksList(currentTime, "All Banksmval", allBanksBuyQuantity, allBanksSellQuantity,
                    mValTBQAllBanks , mValTSQAllBanks , mVAlQTSAllBanks, underlyingValue, mValDPAllBanks );
            banksDao.insertBankData(banksList);

            mStrTBQAllBanks = Double.parseDouble(df.format(mStrTBQAllBanks / 12));
            mStrTSQAllBanks = Double.parseDouble(df.format(mStrTSQAllBanks / 12));
            mStrDPAllBanks = Double.parseDouble(df.format(mStrDPAllBanks / 12));
            //Added mValDPAllBanks instead of VOl column
            banksList = new BanksList(currentTime, "All BanksMStr", allBanksBuyQuantity, allBanksSellQuantity,
                    mStrTBQAllBanks , mStrTSQAllBanks , mValDPAllBanks, underlyingValue, mStrDPAllBanks);
            banksDao.insertBankData(banksList);

            mValTBQAllBanks = 0;
            mValTSQAllBanks = 0;
            mVAlQTSAllBanks = 0;
            mValDPAllBanks = 0;
            mStrTBQAllBanks = 0;
            mStrTSQAllBanks = 0;
            mStrQTSAllBanks = 0;
            mStrDPAllBanks = 0;

            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("AllBanksLiveDisplaySP", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("All Banks", "All Banks");
            editor.putString("allBanksBuyQuantity", String.valueOf(allBanksBuyQuantity));
            editor.putString("allBanksSellQuantity", String.valueOf(allBanksSellQuantity));
            editor.putString("allBanksQuantityTraded", String.valueOf(allBanksQuantityTraded));
            editor.putString("underlyingValue", String.valueOf(underlyingValue));
            editor.putFloat("allBanksDeliveryPercent", Float.parseFloat(df.format(allBanksPercentDifference/12)));
            Gson gson = new Gson();
            String json = gson.toJson(banksLists);
            editor.putString("BanksLiveDisplayData", json);
            editor.commit();

            allBanksBuyQuantity = 0;
            allBanksSellQuantity = 0;
            allBanksQuantityTraded = 0;
            allBanksDeliveryQuantity = 0;
            allBanksPercentDifference = 0.0;
            retrievesDataCompleted = 0;
            banksLists.clear();
//                }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public void bankNiftySuccessJsonPM(JSONObject jsonObject) {
        try {
            JSONArray banksArr = jsonObject.getJSONArray("data");
            long last_update_time = banksArr.getJSONObject(0).getLong("last_update_time");
            Date date = new Date(last_update_time * 1000L); // Convert to milliseconds
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String formattedTime = formatter.format(date);
            timeStampValue = formattedTime.substring(12, 17);
            String ulValues =  banksArr.getJSONObject(0).getString("last_price").substring(0,5);
            int ulValue = Integer.parseInt(ulValues);
            underlyingValue = Double.valueOf(ulValue);
            for (int i = 1; i < banksArr.length(); i++) {
                JSONObject jo = banksArr.getJSONObject(i);
                String security_id = jo.getString("security_id");
                boolean foundData = jo.getBoolean("found");
                if(!foundData)
                    continue;
                String optionName = kvpBankNiftyScripIDsPM.get(security_id);
                if(optionName.contains("CE")){
                    //Add total values for main top card
                    ceTotalTradedVolume += (int) jo.get("volume_traded");
                    ceTotalBuyQuantity += (int) jo.get("total_buy_quantity");
                    ceTotalSellQuantity += (int) jo.get("total_sell_quantity");
                    ceOpenInterest += (int) jo.get("oi");
                } else if (optionName.contains("PE")) {
                    //Add total values for main top card
                    peTotalTradedVolume += (int) jo.get("volume_traded");
                    peTotalBuyQuantity += (int) jo.get("total_buy_quantity");
                    peTotalSellQuantity += (int) jo.get("total_sell_quantity");
                    peOpenInterest += (int) jo.get("oi");
                }

                bankNiftyList = new BankNiftyList(timeStampValue,
                        optionName,
                        jo.getLong("volume_traded")/1000,
                        jo.getLong("total_buy_quantity")/1000,
                        jo.getLong("total_sell_quantity")/1000,
                        jo.getLong("oi")/1000,
                        jo.getDouble("change_percent")
                );
                bankNiftyLists.add(bankNiftyList);
                bankNiftyDao.insertBankNiftyData(bankNiftyList);
                movementTrackingNifty(jo.getString("security_id"));

            }

            //Add data for "OI History"
            bankNiftyList = new BankNiftyList(timeStampValue, "OI History", (long) (ceOpenInterest / 1000), (long) (peOpenInterest / 1000), (long) (ceTotalTradedVolume / 1000), (long) (peTotalTradedVolume / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("OI History");

            //Add data for "CE History"
            bankNiftyList = new BankNiftyList(timeStampValue, "CE History", (long) (ceTotalBuyQuantity / 1000), (long) (ceTotalSellQuantity / 1000), (long) (ceTotalTradedVolume / 1000), (long) (ceOpenInterest / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("CE History");

            //Add data for "PE History"
            bankNiftyList = new BankNiftyList(timeStampValue, "PE History", (long) (peTotalBuyQuantity / 1000), (long) (peTotalSellQuantity / 1000), (long) (peTotalTradedVolume / 1000), (long) (peOpenInterest / 1000), Double.valueOf(underlyingValue));
            bankNiftyDao.insertBankNiftyData(bankNiftyList);
            movementTrackingNifty("PE History");


            // Storing data into SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("NiftyOILiveDisplaySP", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("timeStampValue", timeStampValue);
            editor.putString("underlyingValue", String.valueOf(underlyingValue));
            editor.putString("ceTotalTradedVolume", String.valueOf(ceTotalTradedVolume / 1000));
            editor.putString("peTotalTradedVolume", String.valueOf(peTotalTradedVolume / 1000));
            editor.putString("ceTotalBuyQuantity", String.valueOf(ceTotalBuyQuantity / 1000));
            editor.putString("ceTotalSellQuantity", String.valueOf(ceTotalSellQuantity / 1000));
            editor.putString("peTotalBuyQuantity", String.valueOf(peTotalBuyQuantity / 1000));
            editor.putString("peTotalSellQuantity", String.valueOf(peTotalSellQuantity / 1000));
            editor.putString("ceOpenInterest", String.valueOf(ceOpenInterest / 1000));
            editor.putString("peOpenInterest", String.valueOf(peOpenInterest / 1000));
            Gson gson = new Gson();
            try{
                String json = gson.toJson(bankNiftyLists);
                editor.putString("bankNiftyData", json);
                editor.commit();
            }catch (Exception e){
                Log.v("JSON Exception","ConcurrentModificationException");
            }

            ceTotalTradedVolume = 0;
            peTotalTradedVolume = 0;
            ceTotalBuyQuantity = 0;
            ceTotalSellQuantity = 0;
            peTotalBuyQuantity = 0;
            peTotalSellQuantity = 0;
            ceOpenInterest = 0;
            peOpenInterest = 0;
            bankNiftyLists.clear();
        } catch (JSONException e) {
            e.printStackTrace();
//            Toast.makeText(StockDataRetrieveService.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private double getPercentDiff(String bankName) {
        List<BanksList> banksHistoryPrev = banksDao.getBankHistory(bankName);
        int bankHisLen = banksHistoryPrev.size();
        if (bankHisLen > 0) {
            return Double.parseDouble(df.format(banksHistoryPrev.get(bankHisLen - 1).getPercentDiff()));
        } else {
            return 0.0;
        }

    }

    @Override
    public void onFailureJson(int responseCode, String msg, String type) {
        Toast.makeText(StockDataRetrieveService.this, type + "Error:" + msg, Toast.LENGTH_LONG).show();
    }

    private Long parseToLongfrom_(String trim) {
        if (trim.length() > 2) {
            if (trim.contains(",")) {
                trim = trim.replaceAll(",", "");
            }
            return Long.parseLong(trim) / 1000;
        } else {
            return 0L;
        }
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("service is ", "running");
                    String start = "09:00";
                    Date marketOpen = null;
                    String limit = "15:30";
                    Date marketClose = null;
                    Date now = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
                    String currentTime = dateFormat.format(new Date()).toString();
                    try {
                        marketOpen = dateFormat.parse(start);
                        marketClose = dateFormat.parse(limit);
                        now = dateFormat.parse(currentTime);
                        if (now.after(marketClose) || now.before(marketOpen)) {
                            mHandler.removeCallbacksAndMessages(null);
                        } else {

//                            downloadBanksLiveDataFromMAC_FTP();
//                            downloadBankNiftyOIDataFromMAC_FTP();

                            paytmBanksLiveData();
                            paytmNiftyBankLiveData();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public class LocalBinder extends Binder {
        public StockDataRetrieveService getService() {
            return StockDataRetrieveService.this;
        }
    }

    private void movementTrackingBanks(String bankName) {

        BanksList curBanksList = null;
        BanksList prevBanksList = null;

        try {
            List<BanksList> banksHistoryPrev = banksDao.getBankHistory(bankName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 2) {
                curBanksList = banksHistoryPrev.get(bankHisLen - 1);
                prevBanksList = banksHistoryPrev.get(bankHisLen - 2);
                addmValDataBanks(bankName, curBanksList, prevBanksList);
                addMStrDataBanks(bankName, curBanksList, banksHistoryPrev.get(0));
            } else if (bankHisLen > 1) {
                curBanksList = banksHistoryPrev.get(bankHisLen - 1);
                prevBanksList = banksHistoryPrev.get(0);
                addmValDataBanks(bankName, curBanksList, prevBanksList);
                addMStrDataBanks(bankName, curBanksList, prevBanksList);
            } else {
                curBanksList = banksHistoryPrev.get(0);
                prevBanksList = banksHistoryPrev.get(0);
                addmValDataBanks(bankName, curBanksList, prevBanksList);
                addMStrDataBanks(bankName, curBanksList, prevBanksList);
            }
        } catch (Exception e) {
            Log.v("movementTrackingBanks", e.getMessage());
        }
    }

    private void addmValDataBanks(String bankName, BanksList curBanksList, BanksList prevBanksList) {
        double mValTBQ = getMVal(curBanksList.getTotalBuyQuantity(), prevBanksList.getTotalBuyQuantity());
        mValTBQAllBanks += mValTBQ;
        double mValTSQ = getMVal(curBanksList.getTotalSellQuantity(), prevBanksList.getTotalSellQuantity());
        mValTSQAllBanks += mValTSQ;
        double mValDP = mValTBQ - mValTSQ;
        mValDPtoMStr = mValDP;
        mValDPAllBanks += mValDP;
        double mVAlQTS = getMVal(curBanksList.getQuantityTradedsure(), prevBanksList.getQuantityTradedsure());
        mVAlQTSAllBanks += mVAlQTS;
        double mValUL = curBanksList.getUnderlyingValue();

        BanksList banksList = new BanksList(curBanksList.getTimeStamp(),
                bankName + "mval", curBanksList.getTotalBuyQuantity(), curBanksList.getTotalSellQuantity(),
                mValTBQ, mValTSQ, mVAlQTS, mValUL, mValDP);
        banksDao.insertBankData(banksList);
    }

    private void addMStrDataBanks(String bankName, BanksList curBanksList, BanksList prevBanksList) {
        double mStrTBQ = getMVal(curBanksList.getTotalBuyQuantity(), prevBanksList.getTotalBuyQuantity());
        mStrTBQAllBanks += mStrTBQ;
        double mStrTSQ = getMVal(curBanksList.getTotalSellQuantity(), prevBanksList.getTotalSellQuantity());
        mStrTSQAllBanks += mStrTSQ;
        double mStrDP = mStrTBQ - mStrTSQ;
        mStrDPAllBanks += mStrDP;
//        double mStrQTS = getMVal(curBanksList.getQuantityTradedsure(), prevBanksList.getQuantityTradedsure());
        //Showing  mValPercentDiff in MStr Vol column
        double mStrQTS = mValDPtoMStr;
        mStrQTSAllBanks += mStrQTS;
        double mStrUL = curBanksList.getUnderlyingValue();

        BanksList banksList = new BanksList(curBanksList.getTimeStamp(),
                bankName + "MStr", curBanksList.getTotalBuyQuantity(), curBanksList.getTotalSellQuantity(),
                mStrTBQ, mStrTSQ, mStrQTS, mStrUL, mStrDP);
        banksDao.insertBankData(banksList);
    }

    private void movementTrackingNifty(String oiName) {

        BankNiftyList curNiftyList = null;
        BankNiftyList prevNiftyList = null;

        try {
            List<BankNiftyList> banksHistoryPrev = bankNiftyDao.getBankNiftyHistory(oiName);
            int bankHisLen = banksHistoryPrev.size();
            if (bankHisLen > 2) {
                curNiftyList = banksHistoryPrev.get(bankHisLen - 1);
                prevNiftyList = banksHistoryPrev.get(bankHisLen - 2);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList);
                addMStrDataNifty(oiName, curNiftyList, banksHistoryPrev.get(0));
            } else if (bankHisLen > 1) {
                curNiftyList = banksHistoryPrev.get(bankHisLen - 1);
                prevNiftyList = banksHistoryPrev.get(0);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList);
                addMStrDataNifty(oiName, curNiftyList, prevNiftyList);
            } else {
                curNiftyList = banksHistoryPrev.get(0);
                prevNiftyList = banksHistoryPrev.get(0);
                addmValDataNifty(oiName, curNiftyList, prevNiftyList);
                addMStrDataNifty(oiName, curNiftyList, prevNiftyList);
            }
        } catch (Exception e) {
            Log.v("movementTrackingBanks", e.getMessage());
        }
    }

    private void addmValDataNifty(String oiName, BankNiftyList curNiftyList, BankNiftyList prevNiftyList) {
        double mValTBQ = getMVal(curNiftyList.getCalloi(), prevNiftyList.getCalloi());
        double mValTSQ = getMVal(curNiftyList.getPutoi(), prevNiftyList.getPutoi());
        double mVAlQTS = mValTBQ - mValTSQ;
        mValDPtoMStr = mVAlQTS;
        double mValDQ = getMVal(curNiftyList.getBntotalsellquantity(), prevNiftyList.getBntotalsellquantity());
        double mValDP = curNiftyList.getUnderlyvalue();


        bankNiftyList = new BankNiftyList(curNiftyList.getTimestamp(), oiName + "mval", curNiftyList.getCalloi(),
                mValTBQ, mValTSQ, mVAlQTS, mValDQ, mValDP);
        bankNiftyDao.insertBankNiftyData(bankNiftyList);
    }

    private void addMStrDataNifty(String oiName, BankNiftyList curNiftyList, BankNiftyList prevNiftyList) {
        double mStrTBQ = getMVal(curNiftyList.getCalloi(), prevNiftyList.getCalloi());
        double mStrTSQ = getMVal(curNiftyList.getPutoi(), prevNiftyList.getPutoi());
        double mStrQTS = mStrTBQ - mStrTSQ;
        double mStrDQ = mValDPtoMStr;
        double mStrDP = curNiftyList.getUnderlyvalue();

        bankNiftyList = new BankNiftyList(curNiftyList.getTimestamp(), oiName + "MStr", curNiftyList.getCalloi(),
                mStrTBQ, mStrTSQ, mStrQTS, mStrDQ, mStrDP);
        bankNiftyDao.insertBankNiftyData(bankNiftyList);
    }

    private double getMVal(double curVal, double prevVal) {
        try {
            if (prevVal == 0.0) {
                return 0.0;
            } else {
                double val = (curVal / prevVal);
                val = val * 100;
                double mValD = Double.parseDouble(df.format(val));
                Log.v("mValDoubles", String.valueOf(mValD));
                return mValD;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }



}