package com.sureit.stockops.Util;

import static com.sureit.stockops.Util.Constants.access_token_pm;
import static com.sureit.stockops.view.BanksListActivity.paytmURL;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaytmBanksLiveData {
    private StockDataRetrieveService stockDataRetrieveService;

    public void retrieveData(Context context) throws Exception {
        String banksID = "NSE:25:INDEX,NSE:21238:EQUITY,NSE:5900:EQUITY,NSE:2263:EQUITY,NSE:1023:EQUITY,NSE:1333:EQUITY,NSE:11184:EQUITY,NSE:4963:EQUITY,NSE:5258:EQUITY,NSE:1922:EQUITY,NSE:10666:EQUITY,NSE:18391:EQUITY,NSE:3045:EQUITY";
        RequestQueue queue = Volley.newRequestQueue(context);
        stockDataRetrieveService = new StockDataRetrieveService();
        String banks_url = paytmURL+banksID;
        jsonRequestUrl(access_token_pm, (RequestQueue) queue, banks_url, "banks");
        queue.start();
//        String nifty_url = paytmURL+stockDataRetrieveService.importKeysFromFTP();
//        jsonRequestUrl(access_token_pm, (RequestQueue) queue, nifty_url, "nifty");
//        queue.start();
    }

    private void jsonRequestUrl(String access_token_pm, RequestQueue queue, String ul_url, String requestType) {
        JsonObjectRequest requestUl = new JsonObjectRequest(Request.Method.GET, ul_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(requestType.equals("banks"))
                    stockDataRetrieveService.banksSuccessJsonPM(response);
                else if(requestType.equals("nifty"))
                    stockDataRetrieveService.bankNiftySuccessJsonPM(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authority", "www.nseindia.com");
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                params.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
                params.put("Accept-Encoding", "none");
                params.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
                params.put("Connection", "keep-alive");
                params.put("x-jwt-token", access_token_pm);
                params.put("cookie", String.valueOf(params));
                return params;
            }
        };
        queue.add(requestUl);
    }

}
