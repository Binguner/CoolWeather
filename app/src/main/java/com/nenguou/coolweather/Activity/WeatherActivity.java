package com.nenguou.coolweather.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nenguou.coolweather.R;
import com.nenguou.coolweather.Service.AutoUpdateService;
import com.nenguou.coolweather.Util.HttpCallbackListener;
import com.nenguou.coolweather.Util.HttpUtil;
import com.nenguou.coolweather.Util.Utility;

import org.w3c.dom.Text;

import java.security.AuthProvider;

/**
 * Created by binguner on 2018/2/12.
 */

public class WeatherActivity extends AppCompatActivity {

    private TextView cityTextName,publishText,weatherDespText,temp1Text,temp2Text,currentDataText;
    private Button switchCity,refreshWeather;
    private String countyCode;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        initId();
        setData();
        setListener();
    }

    private void setListener() {
        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
            }
        });

        refreshWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishText.setText("isLoading~~~");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherCode = sharedPreferences.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
            }
        });

    }

    private void setData() {
        if(!TextUtils.isEmpty(countyCode)){
            publishText.setText("Loading~~");
            queryWeatherCode(countyCode);
        }else {
            // 没有县级代号，直接现实本地天气
            showWeather();
        }

    }

    private void showWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityTextName.setText(sharedPreferences.getString("city_name",""));
        temp1Text.setText(sharedPreferences.getString("temp1",""));
        temp2Text.setText(sharedPreferences.getString("temp2",""));
        weatherDespText.setText(sharedPreferences.getString("weather_desp",""));
        publishText.setText("今天" + sharedPreferences.getString("publish_time","") + " 发布" );
        currentDataText.setText(sharedPreferences.getString("current_date"," "));
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinsh(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if(array!= null && array.length == 2 ){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("LoadFailed");
                    }
                });
            }
        });
    }

    private void initId() {
        cityTextName = findViewById(R.id.city_name);
        publishText = findViewById(R.id.publish_text);
        weatherDespText = findViewById(R.id.weather_desp);
        temp1Text = findViewById(R.id.temp1);
        temp2Text = findViewById(R.id.temp2);
        currentDataText = findViewById(R.id.current_date);
        switchCity = findViewById(R.id.switchCity);
        refreshWeather = findViewById(R.id.refreshWeather);
        countyCode = getIntent().getStringExtra("county_code");
        Log.d("WeatherTag2","countyCode is " + countyCode);
    }
}
