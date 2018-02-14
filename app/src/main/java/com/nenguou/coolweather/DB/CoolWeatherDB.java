package com.nenguou.coolweather.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nenguou.coolweather.Model.City;
import com.nenguou.coolweather.Model.County;
import com.nenguou.coolweather.Model.Province;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by binguner on 2018/2/10.
 */

public class CoolWeatherDB {
    /**
     * Database name
     */
    public static final String DB_NAME = "cool_weather";
    /**
     * Database version
     */
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase sqLiteDatabase;

    /**
     * 将构造方法私有化
     */
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        sqLiteDatabase = coolWeatherOpenHelper.getWritableDatabase();
    }

    /**
     * 获取 CoolWeather 实例
     */
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 将 Province 实例存储到数据库
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            contentValues.put("province_code", province.getProvinceCode());
            sqLiteDatabase.insert("Province", null, contentValues);
        }
    }

    /**
     * 从数据库读取全国所有身份信息
     */
    public List<Province> loadProvinces() {
        List<Province> provinces = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                provinces.add(province);
            } while (cursor.moveToNext());
        }
        return provinces;
    }

    /**
     * 将 City 实例 存储到数据库
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("city_code", city.getCityCode());
            contentValues.put("province_id", city.getProvinceId());
            sqLiteDatabase.insert("City", null, contentValues);
        }
    }

    /**
     * 从数据库中读取某省下所有的城市信息
     */
    public List<City> loadCities(int provinceId) {
        List<City> cities = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()){
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("idf")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                cities.add(city);
            } while (cursor.moveToNext());
        }
        return cities;
    }

    /**
     * 将 County 实例存储到数据库
     */
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("county_name", county.getCountyName());
            contentValues.put("county_code", county.getCountyCode());
            contentValues.put("city_id", county.getCityId());
            sqLiteDatabase.insert("County", null, contentValues);
        }
    }

    /**
     * 从数据库中读取某城市下所有县到信息
     */
    public List<County> loadCounty(int cityId) {
        Log.d("weatherTag5",cityId+"..");

        List<County> counties = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                counties.add(county);
            } while (cursor.moveToNext());
        }
        return counties;
    }
}
