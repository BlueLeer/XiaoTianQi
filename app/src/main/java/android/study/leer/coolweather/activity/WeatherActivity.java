package android.study.leer.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.study.leer.coolweather.R;
import android.study.leer.coolweather.receiver.MyAppWidgetProvider;
import android.study.leer.coolweather.util.DayWords;
import android.study.leer.coolweather.util.HttpCallbackListener;
import android.study.leer.coolweather.util.HttpUtil;
import android.study.leer.coolweather.util.Utility;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Leer on 2016/12/1.
 */

public class WeatherActivity extends Activity implements View.OnClickListener {
    private RelativeLayout weatherInfoLayout;
    /*
    用于显示城市名
     */
    private TextView cityNameText;
    /*
    用于显示发布日期
     */
    private TextView publishText;
    /*
    用于显示天气描述信息
     */
    private TextView weatherDespText;
    /*
    用于显示气温1
     */
    private TextView tempText;
    /*
    用于显示气温2
     */
//    private TextView temp2Text;
    /*
    用于显示当前的日期
     */
//    private TextView currentDataText;
    /*
    重新选择要显示天气的城市
     */
    private Button changeCityButton;
    /*
    刷新城市的天气信息
     */
    private Button refreshButton;
    private ScrollView mSl_weather_detail;
    private LinearLayout mLl_weather_nor;
    //天气界面常规情况下显示的视图的高度
    private int mMLl;

    private boolean isWeatherDetailOpen = false;
    private TextView mUv_index;
    private TextView mExercise_index;
    private TextView mTravel_index;
    private TextView mDressing_index;
    private TextView mDressing_advice;
    private TextView mDay_weather;
    private TextView mTv_date1;
    private TextView mTv_date2;
    private TextView mTv_date3;
    private ImageView mIv_icon1;
    private ImageView mIv_icon2;
    private ImageView mIv_icon3;
    private AssetManager mAM;
    private TextView mTv_day_words;
    private TextView mTv_wind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        //初始化控件
        initView();

        mAM = getAssets();

        changeCityButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号时,就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryCountyCode(countyCode);
        } else {
            //没有天气代号时,就直接显示本地天气
            showWeather();
        }
    }

    private void initView() {
        //顶部标题栏的城市名
        cityNameText = (TextView) findViewById(R.id.city_name);
        //标题栏上的改变城市按钮
        changeCityButton = (Button) findViewById(R.id.change_city);
        //标题栏上的刷新按钮
        refreshButton = (Button) findViewById(R.id.refresh);
        //天气的发布日期
        publishText = (TextView) findViewById(R.id.publish_time);
        //具体天气的跟布局
        weatherInfoLayout = (RelativeLayout) findViewById(R.id.weather_info_layout);
        //天气
        weatherDespText = (TextView) findViewById(R.id.weather);
        //温度
        tempText = (TextView) findViewById(R.id.temp);

        //风力
        mTv_wind = (TextView) findViewById(R.id.tv_wind);

        //展示全天的天气信息
        mDay_weather = (TextView) findViewById(R.id.day_weather);

        //常规要展示的天气的信息的根布局
        mLl_weather_nor = (LinearLayout) findViewById(R.id.ll_weather_nor);

        //底部天气的更多细节
        mSl_weather_detail = (ScrollView) findViewById(R.id.sl_weather_detail);

        mLl_weather_nor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMLl = mLl_weather_nor.getHeight();
            }
        });

        //紫外线指数
        mUv_index = (TextView) findViewById(R.id.uv_index);
        //户外运动
        mExercise_index = (TextView) findViewById(R.id.exercise_index);
        //旅游建议
        mTravel_index = (TextView) findViewById(R.id.travel_index);
        //穿衣指数
        mDressing_index = (TextView) findViewById(R.id.dressing_index);
        //穿衣建议
        mDressing_advice = (TextView) findViewById(R.id.dressing_advice);

        mTv_date1 = (TextView) findViewById(R.id.tv_date1);
        mTv_date2 = (TextView) findViewById(R.id.tv_date2);
        mTv_date3 = (TextView) findViewById(R.id.tv_date3);

        mIv_icon1 = (ImageView) findViewById(R.id.iv_icon1);
        mIv_icon2 = (ImageView) findViewById(R.id.iv_icon2);
        mIv_icon3 = (ImageView) findViewById(R.id.iv_icon3);

        mTv_day_words = (TextView) findViewById(R.id.tv_day_words);
    }

    private float mStartY = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                float endY = ev.getY();
                float disY = endY - mStartY;
                if (disY < -40 && !isWeatherDetailOpen) {
                    showWeatherDetail();
                    return false;
                } else if (disY > 40 && isWeatherDetailOpen) {
                    closeWeatherDetail();
                    return false;
                }
                mStartY = -1;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mStartY = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//            case MotionEvent.ACTION_UP:
//                float endY = event.getY();
//                float disY = endY - mStartY;
//                if (disY < -40 && !isWeatherDetailOpen) {
//                    showWeatherDetail();
//                } else if (disY > 40 && isWeatherDetailOpen) {
//                    closeWeatherDetail();
//                }
//                mStartY = -1;
//                break;
//        }
        return true;
    }

    //关闭更多具体的天气信息
    private void closeWeatherDetail() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, mMLl);
        valueAnimator.setDuration(1000);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewHelper.setTranslationY(mLl_weather_nor, -mMLl + value);
                mSl_weather_detail.setVisibility(View.VISIBLE);
                ViewHelper.setTranslationY(mSl_weather_detail, value);
                isWeatherDetailOpen = false;
            }
        });

    }

    //展示更多具体的天气信息
    private void showWeatherDetail() {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, -mMLl);
        valueAnimator.setDuration(1000);
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewHelper.setTranslationY(mLl_weather_nor, value);
                mSl_weather_detail.setVisibility(View.VISIBLE);
                ViewHelper.setTranslationY(mSl_weather_detail, mMLl + value);
                isWeatherDetailOpen = true;
            }
        });

    }


    /*
        查询县级代号对应的天气代号
         */
    private void queryCountyCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
//        Log.d("weather","weather_code:"+);
    }

    /*
    查询天气代号对应的天气
     */
    private void queryWeatherCode(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    //用聚合数据通过城市名称查询天气信息
    private void queryWeatherCity(String weatherCity) {
        String encodeCityName = null;
        try {
            encodeCityName = URLEncoder.encode(weatherCity, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String address = "http://v.juhe.cn/weather/index?format=2&cityname=" + encodeCityName + "&key=f17c7d9899a84c15bba87d8d8a635551";
        Log.d("address:", address);
        queryFromServer(address, "weatherInfo");
    }

    /*
    根据传入的地址和类型去向服务器查询天气代号或者天气
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherCode(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回来的天气信息
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response);
                        JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
                        //读取城市名称
                        String cityName = weatherInfo.getString("city");
                        queryWeatherCity(cityName);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if ("weatherInfo".equals(type)) {
                    //将查询出来的数据进行解析,然后存放在SP当中
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    Log.d("xxxxx", response);
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
                        publishText.setText("同步失败!");
                    }
                });
            }
        });
    }

    /*
    从SharedPreference文件中读取存储的天气信息,并显示到界面上
     */
    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //显示城市名
        cityNameText.setText(preferences.getString("city", "Error"));
        //显示城市温度
        tempText.setText(preferences.getString("temp", "Error"));
        //全天温度范围
        mDay_weather.setText(preferences.getString("temperature", "Error"));
        //显示天气信息
        weatherDespText.setText(preferences.getString("weather", "Error"));
        //显示发布时间
        publishText.setText("今天" + preferences.getString("time", "Error") + "发布");

        //显示风向
        mTv_wind.setText(preferences.getString("wind","Error"));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        mUv_index.setText(preferences.getString("uv_index", "Error"));
        mExercise_index.setText(preferences.getString("exercise_index", "Error"));
        mTravel_index.setText(preferences.getString("travel_index", "Error"));
        mDressing_index.setText(preferences.getString("dressing_index", "Error"));
        mDressing_advice.setText(preferences.getString("dressing_advice", "Error"));

        mTv_date1.setText("明天");
        mTv_date2.setText(dateFormat(preferences.getString("future2_date", "Error")));
        mTv_date3.setText(dateFormat(preferences.getString("future3_date", "Error")));

        String img1 = preferences.getString("future1_weather_icon1", "0");
        String img2 = preferences.getString("future2_weather_icon1", "0");
        String img3 = preferences.getString("future3_weather_icon1", "0");
        InputStream inputStream1 = null;
        InputStream inputStream2 = null;
        InputStream inputStream3 = null;
        try {
            inputStream1 = mAM.open("a" + img1 + ".png");
            inputStream2 = mAM.open("a" + img2 + ".png");
            inputStream3 = mAM.open("a" + img3 + ".png");

            Bitmap bitmap_icon1 = BitmapFactory.decodeStream(inputStream1);
            mIv_icon1.setImageBitmap(bitmap_icon1);

            Bitmap bitmap_icon2 = BitmapFactory.decodeStream(inputStream2);
            mIv_icon2.setImageBitmap(bitmap_icon2);

            Bitmap bitmap_icon3 = BitmapFactory.decodeStream(inputStream3);
            mIv_icon3.setImageBitmap(bitmap_icon3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream1.close();
                inputStream2.close();
                inputStream3.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //更新每日寄语
        mTv_day_words.setText(DayWords.getDayWord());

        Intent intent = new Intent("REFRESH");
        sendBroadcast(intent);
    }

    private String dateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfM = new SimpleDateFormat("M");
        SimpleDateFormat sdfD = new SimpleDateFormat("d");

        try {
            Date parse = sdf.parse(date);
            String month = sdfM.format(parse);
            String day = sdfD.format(parse);

            Log.d("1111", month + "月 " + day + "日");

            return month + "月" + day + "日";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCity = prefs.getString("city", "");
                if (!TextUtils.isEmpty(weatherCity)) {
                    queryWeatherCity(weatherCity);
                }
                break;
            default:
                break;

        }

    }

}
