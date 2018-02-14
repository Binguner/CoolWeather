package com.nenguou.coolweather.Util;

/**
 * Created by binguner on 2018/2/10.
 */

public interface HttpCallbackListener {
    void onFinsh(String response);
    void onError(Exception e);
}
