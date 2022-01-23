package com.sureit.stockops.view;

import android.app.Activity;
import android.app.Service;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sureit.stockops.Util.StockDataRetrieveService;
import com.sureit.stockops.data.BanksList;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostVolleyJsonRequest {
    private Service serviceStock;
    private String  type;
    private Activity act;
    private VolleyJsonRespondsListener volleyJsonRespondsListener;
    private String networkurl;
    private JSONObject jsonObject = null;
    private String params;
    private BanksList banksList;
    private List<BanksList> banksLists = new ArrayList<>();
    public PostVolleyJsonRequest(Activity act, VolleyJsonRespondsListener volleyJsonRespondsListener,String  type, String netnetworkUrl, String params) {
        this.act = act;
        this.volleyJsonRespondsListener = volleyJsonRespondsListener;
        this.type = type;
        this.networkurl = netnetworkUrl;
        this.params = params;
        sendRequest();
    }

    public PostVolleyJsonRequest(StockDataRetrieveService serviceStock, VolleyJsonRespondsListener volleyJsonRespondsListener, String  type, String netnetworkUrl, String params) {
        this.serviceStock = serviceStock;
        this.volleyJsonRespondsListener = volleyJsonRespondsListener;
        this.type = type;
        this.networkurl = netnetworkUrl;
        this.params = params;
        sendRequest();
    }

    private void sendRequest() {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, networkurl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("response", "response " + response);
                    volleyJsonRespondsListener.onSuccessJson(response, type);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        Thread.sleep(2000);
                        NetworkResponse response = error.networkResponse;
                        Log.e("response", "response " + response);
                        if (response != null) {
                            int code = response.statusCode;

                            String errorMsg = new String(response.data);
                            Log.e("response", "response" + errorMsg);

                            volleyJsonRespondsListener.onFailureJson(code, errorMsg, type);
                        } else {
                            String errorMsg = error.getMessage();
                            volleyJsonRespondsListener.onFailureJson(0, errorMsg,type);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            })  {

                //This is for Headers If You Needed
                @Override
                public Map<String, String> getHeaders(){
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authority", "www.nseindia.com");
                    params.put("Content-Type", "application/json; charset=UTF-8");
                    params.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
                    params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    params.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
                    params.put("Accept-Encoding", "none");
                    params.put("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
                    params.put("Connection", "keep-alive");
                    params.put("cookie", String.valueOf(params));
                    return params;
                }};

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue;
        if(act==null){
            requestQueue = Volley.newRequestQueue(serviceStock);
        }else{
            requestQueue = Volley.newRequestQueue(act);
        }
        requestQueue.add(stringRequest);
    }
    }


