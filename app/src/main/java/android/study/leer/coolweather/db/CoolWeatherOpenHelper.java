package android.study.leer.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Leer on 2016/11/29.
 */

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {
    //键表语句
    private static final String CREATE_PROVINCE = "create table province(id integer primary key autoincrement,province_name text,province_code text)";
    private static final String CREATE_CITY = "create table city(id integer primary key autoincrement,city_name text,city_code text,myprovince_id integer)";
    private static final String CREATE_COUNTY = "create table county(id integer primary key autoincrement,county_name text,county_code text,mycity_id integer)";

    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PROVINCE);//创建省份表
        sqLiteDatabase.execSQL(CREATE_CITY);//创建省份下的城市的表
        sqLiteDatabase.execSQL(CREATE_COUNTY);//创建城市下县级的表
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
