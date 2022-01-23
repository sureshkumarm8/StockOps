package com.sureit.stockops.Util;

import static com.sureit.stockops.view.BanksListActivity.deleteCache;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sureit.stockops.R;
import com.sureit.stockops.data.BankNiftyList;
import com.sureit.stockops.data.BanksList;
import com.sureit.stockops.db.BankNiftyDao;
import com.sureit.stockops.db.BanksDao;
import com.sureit.stockops.db.BanksDatabase;
import com.sureit.stockops.db.BanksViewModel;
import com.sureit.stockops.view.BanksListActivity;
import com.sureit.stockops.view.PostVolleyJsonRequest;
import com.sureit.stockops.view.VolleyJsonRespondsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import am.appwise.components.ni.NoInternetDialog;

public class StockDataRetrieveService extends Service implements VolleyJsonRespondsListener{

    public static final int interval_min = 60 * 1000;  //interval between two services(Here Service run every 1 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling
    LocalBinder localBinder = new LocalBinder();

    private static final String LOG_TAG = "StockRetrieveService";
    private List<BankNiftyList> bankNiftyLists;
    private List<BanksList> banksLists;
    BanksListActivity banksListActivityObj;

    BanksDao banksDao;
    BankNiftyDao bankNiftyDao;

    public static final String URL_BankNifty = "https://www.nseindia.com/api/option-chain-indices?symbol=BANKNIFTY";
    public static final String URL_BanksTradeInfo = "https://www.nseindia.com/api/quote-equity?symbol=";
    public static final String URL_NSE = "https://www.nseindia.com/";
    public static String URL_MacFTPServer_BanksLiveData = "http://192.168.43.251:1313/Desktop/Suresh/Stock/liveQuotesData/banksData";
    public static String URL_MacFTPServer_BankNiftyOIData = "http://192.168.43.251:1313/Desktop/Suresh/Stock/liveQuotesData/bankNifty.json";
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

    TextView strikePriceTVBanks;
    TextView ceOpenInterestBanks;
    TextView peOpenInterestBanks;
    TextView timeStampBanks;
    private Double underlyingValue;
    private String timeStampValue;
    private BankNiftyList bankNiftyList;
    private BanksList banksList;
    List<String> banksRetryList = new ArrayList<>();

    private boolean bnkT = false;
    private boolean mainT = false;
    private boolean retry = false;
    List<String> bankNiftyOIdata = new ArrayList<>();
    private boolean macFTPfile = false;

    long allBanksBuyQuantity;
    long allBanksSellQuantity;
    long allBanksQuantityTraded;
    long allBanksDeliveryQuantity;
    double allBanksDeliveryPercent;
    private int retrievesDataCompleted=0;
    private int minsCount=0;
    private Context mContextSTRS;
    private BanksViewModel viewModel;

    public StockDataRetrieveService(){
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


        //Calling NiftyOI 1st time
        downloadBankNiftyOIDataFromMAC_FTP();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_id";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "my_channel_title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("@string/app_name")
                    .setContentText("Stock Data Retrieve Service Running in Background")
                    .setColor(ContextCompat.getColor(this, R.color.cardview_dark_background))
                    .setBadgeIconType(R.mipmap.ic_launcher_bull_bear)
                    .build();

            if (mTimer != null) // Cancel if already existed
                mTimer.cancel();
            else
                mTimer = new Timer();   //recreate new
            mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, interval_min);   //Schedule task

            startForeground(1, notification);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
            for(int i=1; i<= 3; i++){
                new PostVolleyJsonRequest(localBinder.getService(), StockDataRetrieveService.this,"Banks", URL_MacFTPServer_BanksLiveData +i+".json", null);
            }
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
            new PostVolleyJsonRequest(StockDataRetrieveService.this, StockDataRetrieveService.this,"BankNifty", URL_MacFTPServer_BankNiftyOIData, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSuccessJson(String response, String type) {
//        if(type.equals("StrikePrice")){
//            JSONObject jsonObject = null;
//            try {
//                jsonObject = new JSONObject(response);
//                JSONArray indices = jsonObject.getJSONArray("data");
//                JSONObject jo = indices.getJSONObject(3);
//                strikePriceTVBanks.setText(jo.getString("last"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
        if (type.equals("BankNifty")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject recordDetails = jsonObject.getJSONObject("records");
                JSONObject filteredDetails = jsonObject.getJSONObject("filtered");
                JSONArray filteredDataArray = filteredDetails.getJSONArray("data");
                timeStampValue = recordDetails.get("timestamp").toString();
                underlyingValue = recordDetails.getDouble("underlyingValue");
                int ulValue = underlyingValue.intValue();
                for (int i = 35; i < 86; i++) {
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

                        if(sPrice>ulValue-200) {

                            //Add total values for main top card
                            ceTotalTradedVolume += (int) ceBody.get("totalTradedVolume");
                            ceTotalBuyQuantity += (int) ceBody.get("totalBuyQuantity");
                            ceTotalSellQuantity += (int) ceBody.get("totalSellQuantity");
                            ceOpenInterest += (int) ceBody.get("openInterest");

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                    ceBody.getString("identifier").substring(25, 32),
                                    ceBody.getLong("totalTradedVolume"),
                                    ceBody.getLong("totalBuyQuantity"),
                                    ceBody.getLong("totalSellQuantity"),
                                    ceBody.getLong("openInterest"),
                                    ceBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        }
                        if(sPrice<ulValue-200) {

                            //Add total values for main top card
                            peTotalTradedVolume += (int) peBody.get("totalTradedVolume");
                            peTotalBuyQuantity += (int) peBody.get("totalBuyQuantity");
                            peTotalSellQuantity += (int) peBody.get("totalSellQuantity");
                            peOpenInterest += (int) peBody.get("openInterest");

                            bankNiftyList = new BankNiftyList(timeStampValue.substring(12, 17),
                                    peBody.getString("identifier").substring(25, 32),
                                    peBody.getLong("totalTradedVolume"),
                                    peBody.getLong("totalBuyQuantity"),
                                    peBody.getLong("totalSellQuantity"),
                                    peBody.getLong("openInterest"),
                                    peBody.getDouble("pchangeinOpenInterest")
                            );
                            bankNiftyLists.add(bankNiftyList);
                            bankNiftyDao.insertBankNiftyData(bankNiftyList);
                        }
                    }
                }

                //Add data for "OI History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"OI History", (long) (ceOpenInterest / 1000), (long) (peOpenInterest / 1000), (long) (ceTotalTradedVolume / 1000),(long) (peTotalTradedVolume / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);

                //Add data for "CE History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"CE History", (long) (ceTotalBuyQuantity / 1000), (long) (ceTotalSellQuantity / 1000), (long) (ceTotalTradedVolume / 1000),(long) (ceOpenInterest / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);

                //Add data for "PE History"
                bankNiftyList = new BankNiftyList(timeStampValue.substring(12,17),"PE History", (long) (peTotalBuyQuantity / 1000), (long) (peTotalSellQuantity / 1000), (long) (peTotalTradedVolume / 1000),(long) (peOpenInterest / 1000),Double.valueOf(underlyingValue));
                bankNiftyDao.insertBankNiftyData(bankNiftyList);


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
                String json = gson.toJson(bankNiftyLists);
                editor.putString("bankNiftyData", json);
                editor.commit();

                ceTotalTradedVolume=0;
                peTotalTradedVolume=0;
                ceTotalBuyQuantity=0;
                ceTotalSellQuantity=0;
                peTotalBuyQuantity=0;
                peTotalSellQuantity=0;
                ceOpenInterest=0;
                peOpenInterest=0;
                bankNiftyLists.clear();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(StockDataRetrieveService.this, "BankNifty Main Data Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, e.getMessage(), e);
            }

        } else {
            long totalBuyQuantity = 0;
            long totalSellQuantity = 0;
            long tradedVolume = 0;
            long deliveryQuantity = 0;
            double deliveryPercent = 0.0;
            String bankName = "";
//        if(!macFTPfile) {
//            try {
//                Thread.sleep(2000);
//                JSONObject jsonObject = new JSONObject(response);
//
//                JSONObject marketDeptOrderBook = jsonObject.getJSONObject("marketDeptOrderBook");
//                String totalBuyQuantityStr = marketDeptOrderBook.getString("totalBuyQuantity");
//                totalBuyQuantity = Long.parseLong(totalBuyQuantityStr.trim()) / 1000;
//                String totalSellQuantityStr = marketDeptOrderBook.getString("totalSellQuantity");
//                totalSellQuantity = Long.parseLong(totalSellQuantityStr.trim()) / 1000;
//
//                JSONObject securityWiseDP = jsonObject.getJSONObject("securityWiseDP");
//                String quantityTradedStr = securityWiseDP.getString("tradedVolume");
//                tradedVolume = Long.parseLong(quantityTradedStr.trim()) / 1000;
//                String deliveryQuantityStr = securityWiseDP.getString("deliveryQuantity");
//                deliveryQuantity = Long.parseLong(deliveryQuantityStr.trim()) / 1000;
//
//                banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity);
//                banksLists.add(banksList);
//
//                //add new data to database
//                long tsLong = System.currentTimeMillis() / 1000;
//                String ts = Long.toString(tsLong);
//                BanksDao banksDao = BanksDatabase.getInstance(getApplicationContext()).notes();
//                banksList = new BanksList(ts, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity);
//                banksDao.insertBankNiftyData(banksList);
//
//            } catch (JSONException | InterruptedException e) {
//                e.printStackTrace();
//                Log.e(LOG_TAG, e.getMessage(), e);
//            }
//        }else{
            try {
                retrievesDataCompleted++;
                //add new data to database
//                SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
////                String currentTime = dateFormat.format(new Date()).toString();
                String currentTime;

//                Thread.sleep(2000);
                JSONObject jsonObject = new JSONObject(response);
                currentTime = jsonObject.getString("lastUpdateTime").substring(12,17);
                JSONArray banksArr = jsonObject.getJSONArray("data");
                for (int i = 0; i < banksArr.length(); i++) {
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

                    String deliveryToTradedQuantityStr = jo.getString("deliveryToTradedQuantity");
                    try {
                        deliveryPercent = Double.parseDouble(deliveryToTradedQuantityStr.trim());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        parseToLongfrom_(deliveryToTradedQuantityStr.trim());
                    }

                    //Live display Data
                    banksList = new BanksList(bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity, deliveryPercent);
                    banksLists.add(banksList);

                    allBanksBuyQuantity+=totalBuyQuantity;
                    allBanksSellQuantity+=totalSellQuantity;
                    allBanksQuantityTraded+=tradedVolume;
                    allBanksDeliveryQuantity+=deliveryQuantity;
                    allBanksDeliveryPercent+=deliveryPercent;

                    //To store in DB
                    banksList = new BanksList(currentTime, bankName, totalBuyQuantity, totalSellQuantity, tradedVolume, deliveryQuantity, deliveryPercent);
                    banksDao.insertBankData(banksList);
                }
                macFTPfile = false;
                if(retrievesDataCompleted==3){
                    banksList = new BanksList(currentTime, "All Banks", allBanksBuyQuantity, allBanksSellQuantity, allBanksQuantityTraded, allBanksDeliveryQuantity, allBanksDeliveryPercent);
                    banksDao.insertBankData(banksList);

                    // Storing data into SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("AllBanksLiveDisplaySP", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("All Banks", "All Banks");
                    editor.putString("allBanksBuyQuantity", String.valueOf(allBanksBuyQuantity));
                    editor.putString("allBanksSellQuantity", String.valueOf(allBanksSellQuantity));
                    editor.putString("allBanksQuantityTraded", String.valueOf(allBanksQuantityTraded));
                    editor.putString("allBanksDeliveryQuantity", String.valueOf(allBanksDeliveryQuantity));
                    editor.putFloat("allBanksDeliveryPercent", (float) allBanksDeliveryPercent);
                    Gson gson = new Gson();
                    String json = gson.toJson(banksLists);
                    editor.putString("BanksLiveDisplayData", json);
                    editor.commit();
//                    banksListActivityObj.liveDisplayUI();

                    allBanksBuyQuantity=0;
                    allBanksSellQuantity=0;
                    allBanksQuantityTraded=0;
                    allBanksDeliveryQuantity=0;
                    allBanksDeliveryPercent=0.0;
                    retrievesDataCompleted=0;
                    banksLists.clear();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, e.getMessage(), e);
            }
//        }
        }
    }

    @Override
    public void onFailureJson(int responseCode, String msg, String type) {
        Toast.makeText(StockDataRetrieveService.this, type + "Error:" + msg, Toast.LENGTH_LONG).show();
        if (type.equals("BankNifty"))
            timeStampBanks.setText(msg);
        strikePriceTVBanks.setText(msg);
        ceOpenInterestBanks.setText(msg);
        peOpenInterestBanks.setText(msg);
        banksRetryList.add(type);
//        if(!retry){
//            retry=true;
//            Toast.makeText(BanksListActivity.this, "banksRetryList :" + banksRetryList.toString(), Toast.LENGTH_LONG).show();
////            retryFailedBanksTradeInfo(banksRetryList);
//        }
    }

    private Long parseToLongfrom_(String trim) {
        if(trim.length()>2){
            trim = trim.replaceAll(",","");
            return Long.parseLong(trim)/1000;
        }else{
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
                    Log.d("service is ","running");
                    String start = "09:15";
                    Date marketOpen=null;
                    String limit = "15:17";
                    Date marketClose=null;
                    Date now = null;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
                    String currentTime = dateFormat.format(new Date()).toString();
                    try {
                        marketOpen = dateFormat.parse(start);
                        marketClose = dateFormat.parse(limit);
                        now = dateFormat.parse(currentTime);
                        if (now.after(marketClose)||now.before(marketOpen)){
                            mHandler.removeCallbacksAndMessages(null);
                        }else{
                            downloadBanksLiveDataFromMAC_FTP();
//                            Toast.makeText(StockDataRetrieveService.this, "1 min service", Toast.LENGTH_SHORT).show();
                            minsCount++;
                            if(minsCount == 5){
                                downloadBankNiftyOIDataFromMAC_FTP();
//                                Toast.makeText(StockDataRetrieveService.this, "5 min service", Toast.LENGTH_SHORT).show();
                                minsCount=0;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public class LocalBinder extends Binder {
        StockDataRetrieveService getService() {
            return StockDataRetrieveService.this;
        }
    }

}