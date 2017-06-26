package android.study.leer.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leer on 2016/11/29.
 */

public class CoolWeatherDao {
    /*
    数据库名
     */
    private static final String DBName = "cool_weather";
    /*
    数据库版本
     */
    private static final int DB_VERSION = 1;
    SQLiteDatabase db;
    private static CoolWeatherDao sCoolWeatherDao;

    /*
    将数据库构造方法私有化
     */
    private CoolWeatherDao(Context context) {
        CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context, DBName, null, DB_VERSION);
        db = coolWeatherOpenHelper.getWritableDatabase();
    }

    /*
    获取CoolWeatherDB的实例
     */
    public static CoolWeatherDao getInstance(Context context) {
        if (sCoolWeatherDao == null) {
            synchronized (CoolWeatherDao.class) {
                sCoolWeatherDao = new CoolWeatherDao(context);
            }
        }
        return sCoolWeatherDao;
    }

    /*
    将Province存贮到Province表中
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("province", null, values);
        }
    }

    /*
    获取数据库中所有的省份信息
     */
    public List<Province> loadProvinces() {
        List<Province> provinceList = new ArrayList<>();
        Cursor cursor = db.query("province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return provinceList;
    }

    /*
    存贮城市的信息
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("myprovince_id", city.getMyProvinceId());

            db.insert("city", null, values);
        }
    }

    /*
    获取给定省份下所有城市的信息
     */
    public List<City> loadCities(int provinceId) {
        List<City> cityList = new ArrayList<>();
        Cursor cursor = db.query("city", null, "myprovince_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setMyProvinceId(provinceId);
                cityList.add(city);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return cityList;
    }

    /*
    存贮县的信息
     */
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("mycity_id", county.getMyCityId());

            db.insert("county", null, values);
        }
    }

    /*
    获取城市下所有县的信息
     */
    public List<County> loadCounties(int myCityId) {
        List<County> countyList = new ArrayList<>();
        Cursor cursor = db.query("county", null, "mycity_id = ?", new String[]{String.valueOf(myCityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setMyCityId(myCityId);
                countyList.add(county);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return countyList;
    }
}
