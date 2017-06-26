package android.study.leer.coolweather.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.study.leer.coolweather.R;
import android.study.leer.coolweather.receiver.MyAppWidgetProvider;
import android.study.leer.coolweather.util.HttpCallbackListener;
import android.study.leer.coolweather.util.HttpUtil;
import android.study.leer.coolweather.util.Utility;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Leer on 2017/3/13.
 */
public class AppWidgetService extends Service {

    private static final String TAG = "AppWidgetService";
    private Timer mTimer;
    private TimerTaskReceiver mTimerTaskReceiver;
    private String mPublishTime;
    private String mCurrentTemp;
    private CharSequence mCurrentCity;
    private AssetManager mAM;
    private Bitmap mBitmap_icon;

    @Override
    public void onCreate() {
        super.onCreate();

        mAM = getAssets();
        startTimerTask();

        IntentFilter filter = new IntentFilter();
        //屏幕点亮/关闭的广播,用来停止或者开启后台天气更新的任务
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction("REFRESH");
        mTimerTaskReceiver = new TimerTaskReceiver();

        registerReceiver(mTimerTaskReceiver, filter);
    }

    private void startTimerTask() {
        mTimer = new Timer();
        //三个参数的意思分别是:一个用于执行定时任务的任务,什么时候第一次执行0代表立刻执行,执行任务的间隔数
        mTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppWidgetService.this);
                        String cityName = sp.getString("city", "南昌");

                        String encodeCityName = null;
                        try {
                            //聚合数据需要先将城市名转换成二进制数据编码
                            encodeCityName = URLEncoder.encode(cityName, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        String address = "http://v.juhe.cn/weather/index?format=2&cityname=" + encodeCityName + "&key=f17c7d9899a84c15bba87d8d8a635551";
                        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
                            @Override
                            public void onFinish(String response) {
                                Utility.handleWeatherResponse(AppWidgetService.this, response);
                                mCurrentCity = sp.getString("city", "Error");
                                mPublishTime = sp.getString("time", "Error");
                                mCurrentTemp = sp.getString("temp", "Error") + "℃";
                                String weatherIcon = sp.getString("weather_icon1", "a00");

                                InputStream inputStream = null;
                                try {
                                    inputStream = mAM.open("a" + weatherIcon + ".png");
                                    mBitmap_icon = BitmapFactory.decodeStream(inputStream);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                updateWidget();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        updateWidget();
                        Log.i(TAG, "定时更新任务正在运行.............");
                    }
                },
                0, 3000 * 60 * 30);//半个小时更新一次
    }

    private class TimerTaskReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                //开启桌面小部件的定时更新任务
                startTimerTask();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                //关闭桌面小部件的定时更新任务
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }else if(action.equals("REFRESH")){
                updateWidget();
            }
        }
    }

    private void updateWidget() {
        //获取WidgetManager对象
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        //使用RemoteViews远程更新控件
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget);
        if (mBitmap_icon != null) {
            remoteViews.setImageViewBitmap(R.id.iv_icon, mBitmap_icon);
        }
        remoteViews.setTextViewText(R.id.tv_publis_time, mPublishTime);
        remoteViews.setTextViewText(R.id.tv_temp, mCurrentTemp);
        remoteViews.setTextViewText(R.id.tv_city, mCurrentCity);


        //点击小部件的进程总数和剩余控件总数的textview处,跳转到应用的主界面处
        Intent i = new Intent("android.intent.action.HOME");
        i.addCategory("android.intent.category.DEFAULT");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.fl_icon, pendingIntent);

//        让AppWidgetManager更新小部件的内容
        ComponentName componentName = new ComponentName(this, MyAppWidgetProvider.class);
        awm.updateAppWidget(componentName, remoteViews);
    }

    @Override
    public void onDestroy() {
        if (mTimerTaskReceiver != null) {
            unregisterReceiver(mTimerTaskReceiver);
        }
        mTimer.cancel();
        mTimer = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
