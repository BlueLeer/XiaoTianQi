package android.study.leer.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.study.leer.coolweather.db.City;
import android.study.leer.coolweather.db.CoolWeatherDao;
import android.study.leer.coolweather.db.County;
import android.study.leer.coolweather.db.Province;
import android.study.leer.coolweather.db.WeatherInfo;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

/**
 * Created by Leer on 2016/11/30.
 */

/*
解析网站发回来的省市县的信息
 */
public class Utility {
    /*
    处理发回来的省份的信息
     */
    public synchronized static boolean hanleProvinceResponse(CoolWeatherDao coolWeatherDao, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length != 0) {
                for (String p : allProvinces) {
                    Province province = new Province();
                    String[] proNameAndCode = p.split("\\|");
                    province.setProvinceCode(proNameAndCode[0]);
                    province.setProvinceName(proNameAndCode[1]);
                    coolWeatherDao.saveProvince(province);
                }
                return true;//接收的结果不为空并且成功的解析出了省份信息并存贮到数据库中了
            }
        }

        return false;//接收的结果为空或其他的原因,此时返回false
    }

    /*
    处理返回来的城市的信息
     */
    public static boolean handleCityResponse(CoolWeatherDao coolWeatherDao, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length != 0) {
                for (String c : allCities) {
                    String[] cityNameAndCode = c.split("\\|");
                    City city = new City();
                    city.setMyProvinceId(provinceId);
                    city.setCityCode(cityNameAndCode[0]);
                    city.setCityName(cityNameAndCode[1]);
                    coolWeatherDao.saveCity(city);
                }
                return true;
            }
        }

        return false;
    }

    /*
    处理返回来的县的信息
     */
    public static boolean handleCountyResponse(CoolWeatherDao coolWeatherDao, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String co : allCounties) {
                    String[] countyCodeAndName = co.split("\\|");
                    County county = new County();
                    county.setMyCityId(cityId);
                    county.setCountyCode(countyCodeAndName[0]);
                    county.setCountyName(countyCodeAndName[1]);
                    coolWeatherDao.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /*
    解析服务器返回来的JSON数据,并将解析的数据存贮到本地
     */
    public static void handleWeatherResponse(Context context, String response) {
////        {"city":"朝阳","cityid":"101010300","temp1":"-2℃","temp2":"16℃",
////                "weather":"晴","img1":"d0.gif","img2":"n0.gif","ptime":"18:a00"}}
//        try {
//            JSONObject jsonObject = new JSONObject(response);
//
//            JSONObject tadayWeatherObject = jsonObject.getJSONObject("result");//一对大括号相当于一组json数据
//
////            实况温度下面的信息:
//            JSONObject jsonSK = tadayWeatherObject.getJSONObject("sk");
//            String publishTime = jsonSK.getString("time");//更新时间
//            String currentTem = jsonSK.getString("temp");//当前温度
//            String currentWindDirection = jsonSK.getString("wind_direction");//当前风向
//            String currentWindStrength = jsonSK.getString("wind_strength");//当前风力
//
//
//            JSONObject weatherInfo = tadayWeatherObject.getJSONObject("today");
//            String cityName = weatherInfo.getString("city");
//            String temp1 = weatherInfo.getString("temperature");
//            String weatherDesp = weatherInfo.getString("weather");
//            saveWeatherInfo(context,
//                    cityName,//城市名称
//                    currentTem,//当前的温度
//                    temp1,//
//                    weatherDesp,//天气的描述信息
//                    publishTime);//发布时间
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Gson gson = new Gson();
        WeatherInfo weatherInfo = gson.fromJson(response, WeatherInfo.class);
        //将所得的天气数据存入sp当中
        saveWeatherInfo(context, weatherInfo);
    }

    public static void saveWeatherInfo(Context context, WeatherInfo weatherInfo) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        //先将以前存贮在sp当中的天气信息删除掉
        editor.clear().commit();
        //在sp中维护是否初次进入到应用
        editor.putBoolean("city_selected", true);
        String temp = weatherInfo.result.sk.temp;//实时温度
        String time = weatherInfo.result.sk.time;//发布时间
        String temperature = weatherInfo.result.today.temperature;//全天天气温度范围
        String weather = weatherInfo.result.today.weather;//天气
        String wind = weatherInfo.result.today.wind;//风力及风向
        String city = weatherInfo.result.today.city;//当前城市
        String dressing_index = weatherInfo.result.today.dressing_index;//穿衣指数
        String dressing_advice = weatherInfo.result.today.dressing_advice;//穿衣建议
        String uv_index = weatherInfo.result.today.uv_index;//紫外线指数
        String travel_index = weatherInfo.result.today.travel_index;//旅游建议
        String exercise_index = weatherInfo.result.today.exercise_index;//是否适合户外运动
        String weather_icon1 = weatherInfo.result.today.weather_id.fa;
        String weather_icon2 = weatherInfo.result.today.weather_id.fb;

        editor.putString("temp", temp);
        editor.putString("time", time);
        editor.putString("temperature", temperature);
        editor.putString("weather", weather);
        editor.putString("wind", wind);
        editor.putString("city", city);
        editor.putString("dressing_index", dressing_index);
        editor.putString("dressing_advice", dressing_advice);
        editor.putString("uv_index", uv_index);
        editor.putString("travel_index", travel_index);
        editor.putString("exercise_index", exercise_index);
        editor.putString("weather_icon1", weather_icon1);
        editor.putString("weather_icon2", weather_icon2);

        //未来第一天的天气
        String future1_temperature = weatherInfo.result.future.get(1).temperature;
        String future1_weather = weatherInfo.result.future.get(1).weather;
        String future1_weather_icon1 = weatherInfo.result.future.get(1).weather_id.fa;
        String future1_weather_icon2 = weatherInfo.result.future.get(1).weather_id.fb;
        String future1_wind = weatherInfo.result.future.get(1).wind;
        String future1_week = weatherInfo.result.future.get(1).week;
        String future1_date = weatherInfo.result.future.get(1).date;
        editor.putString("future1_temperature", future1_temperature);
        editor.putString("future1_weather", future1_weather);
        editor.putString("future1_weather_icon1", future1_weather_icon1);
        editor.putString("future1_weather_icon2", future1_weather_icon2);
        editor.putString("future1_wind", future1_wind);
        editor.putString("future1_week", future1_week);
        editor.putString("future1_date", future1_date);

        //未来第二天的天气
        String future2_temperature = weatherInfo.result.future.get(2).temperature;
        String future2_weather = weatherInfo.result.future.get(2).weather;
        String future2_weather_icon1 = weatherInfo.result.future.get(2).weather_id.fa;
        String future2_weather_icon2 = weatherInfo.result.future.get(2).weather_id.fb;
        String future2_wind = weatherInfo.result.future.get(2).wind;
        String future2_week = weatherInfo.result.future.get(2).week;
        String future2_date = weatherInfo.result.future.get(2).date;

        editor.putString("future2_temperature", future2_temperature);
        editor.putString("future2_weather", future2_weather);
        editor.putString("future2_weather_icon1", future2_weather_icon1);
        editor.putString("future2_weather_icon2", future2_weather_icon2);
        editor.putString("future2_wind", future2_wind);
        editor.putString("future2_week", future2_week);
        editor.putString("future2_date", future2_date);

        //未来第三天的天气
        String future3_temperature = weatherInfo.result.future.get(3).temperature;
        String future3_weather = weatherInfo.result.future.get(3).weather;
        String future3_weather_icon1 = weatherInfo.result.future.get(3).weather_id.fa;
        String future3_weather_icon2 = weatherInfo.result.future.get(3).weather_id.fb;
        String future3_wind = weatherInfo.result.future.get(3).wind;
        String future3_week = weatherInfo.result.future.get(3).week;
        String future3_date = weatherInfo.result.future.get(3).date;

        editor.putString("future3_temperature", future3_temperature);
        editor.putString("future3_weather", future3_weather);
        editor.putString("future3_weather_icon1", future3_weather_icon1);
        editor.putString("future3_weather_icon2", future3_weather_icon2);
        editor.putString("future3_wind", future3_wind);
        editor.putString("future3_week", future3_week);
        editor.putString("future3_date", future3_date);

        editor.commit();

    }

    /*
    将服务器返回的所有天气信息存贮到SharePreferences文件中
     */

//    public static void saveWeatherInfo(Context context, String cityName, String currentTemp, String temp1, String weatherDesp, String publishTime) {
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
//        //应该先将数据清空,保证只存放一个城市的天气信息
//        editor.clear().commit();
//
//
//        editor.putBoolean("city_selected", true);
//        editor.putString("city_name", cityName);
//
//        editor.putString("temp1", temp1);
//        editor.putString("weather_desp", weatherDesp);
//        editor.putString("current_temp", currentTemp);
//        editor.putString("publish_time", publishTime);
//        editor.commit();
//
//        try {
//            OutputStream os = context.openFileOutput(context.getDir("weather",Context.MODE_PRIVATE).getPath(),Context.MODE_PRIVATE);
//            ObjectInputStream ois = new ObjectInputStream(os);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }
}
