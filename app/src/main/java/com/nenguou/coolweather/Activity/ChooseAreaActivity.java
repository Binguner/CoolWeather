package com.nenguou.coolweather.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nenguou.coolweather.DB.CoolWeatherDB;
import com.nenguou.coolweather.Model.City;
import com.nenguou.coolweather.Model.County;
import com.nenguou.coolweather.Model.Province;
import com.nenguou.coolweather.R;
import com.nenguou.coolweather.Util.HttpCallbackListener;
import com.nenguou.coolweather.Util.HttpUtil;
import com.nenguou.coolweather.Util.Utility;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {

    ListView list_view;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView textView;
    private ArrayAdapter<String> arrayAdapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLevel;

    private TextView title_text;

    private Boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        Log.d("WeatherTag3","Go in again");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("city_selected",false)&& !isFromWeatherActivity){
            Log.d("WeatherTag3",isFromWeatherActivity.toString());
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        initId();
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,dataList);
        list_view.setAdapter(arrayAdapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                   // Log.d("weatherTag4",cityList.get(i).getCityName());
                    selectedCity = cityList.get(i);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(i).getCountyCode();
                    //Log.d("WeatherTag2",countyCode);
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();

    }

    private void initId() {
        title_text = findViewById(R.id.title_text);
        list_view = findViewById(R.id.list_view);
    }

    private void queryProvince(){
        provinceList = coolWeatherDB.loadProvinces();
        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText("China");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    private void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    private void queryCounties(){
//        Log.d("weatherTag4",selectedCity.getCityName());
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        Log.d("weatherTag5",selectedCity.getId()+"~");

        for(County county: countyList){
            Log.d("weatherTag4",county.getCountyName());

        }
        if(countyList.size()>0){
            dataList.clear();
            //Log.d("weatherTag4",countyList.size()+"");
           // Log.d("WeatherTag1","load from database");

            for(County county: countyList){
                //Log.d("WeatherTag1",county.getCountyName());
                dataList.add(county.getCountyName());
               // Log.d("weatherTag4",county.getCountyName()+"");

            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
           // Log.d("WeatherTag1","load from server");

            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    private void queryFromServer(final String code, final String type){
        final String address;
        if(!TextUtils.isEmpty(code)){
            Log.d("WeatherTag","Code is: "+code);
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinsh(String response) {
              //  Log.d("WeatherTag","address is: "+address);

                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    Log.d("WeatherTag","address is: ");

                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    // 通过 runOnUiThread() 方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvince();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                Log.d("WeatherTag","Run here");
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"Load Fialed",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvince();
        }else{
            fileList();
        }
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("isLoading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
