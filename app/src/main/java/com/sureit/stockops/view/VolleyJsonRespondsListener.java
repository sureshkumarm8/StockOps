package com.sureit.stockops.view;

public interface VolleyJsonRespondsListener {

    public void onSuccessJson(String response, String type);

    public void onFailureJson(int responseCode, String msg, String responseMessage);
}
