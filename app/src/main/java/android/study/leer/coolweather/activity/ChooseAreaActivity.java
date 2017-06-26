package android.study.leer.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.study.leer.coolweather.R;
import android.study.leer.coolweather.db.City;
import android.study.leer.coolweather.db.CoolWeatherDao;
import android.study.leer.coolweather.db.County;
import android.study.leer.coolweather.db.Province;
import android.study.leer.coolweather.util.HttpCallbackListener;
import android.study.leer.coolweather.util.HttpUtil;
import android.study.leer.coolweather.util.Utility;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leer on 2016/11/30.
 */

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private TextView titleView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDao mCoolWeatherDao;
    private List<String> mDataList = new ArrayList<>();//要在ListView中显示的数据
    private ProgressDialog dialog;

    /*
    省列表
     */
    private List<Province> provinceList;
    /*
    市列表
     */
    private List<City> cityList;
    /*
    县列表
     */
    private List<County> countyList;
    /*
    选中的省份
     */
    private Province selectedProvince;
    /*
    选中的市
     */
    private City selectedCity;
    /*
    当前选中的级别
     */
    private int currentLevel = -1;
    /*
    页面是否从WeatherActivity中跳转过来
     */
    private boolean isFromWeatherActivity;
    private int mListViewHeight;
    private int mListViewWidth;
    private ScaleAnimation mScaleAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //判断是否从天气显示页面跳转过来的
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            //结束当前页面,直接进入到天气显示的页面
            finish();
            return;
        }

        setContentView(R.layout.choose_area);
        initView();
        initAnim();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDataList);
        listView.setAdapter(adapter);

        mCoolWeatherDao = CoolWeatherDao.getInstance(this);

        queryProvinces();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(i).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    Log.d("xxxxx", "当前的countyCode是:" + countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    private void initView() {
        titleView = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);

        //测量listView的尺寸
        listView.measure(0,0);
        mListViewHeight = listView.getMeasuredHeight();
        mListViewWidth = listView.getMeasuredWidth();
    }

    private void initAnim() {
        mScaleAnimation = new ScaleAnimation(1,1,0,1);
        mScaleAnimation.setDuration(500);

        Log.d("xxxxxx",mListViewHeight+"");
        Log.d("xxxxxx",mListViewWidth+"");
    }

    /**
     * ListView刷新,同时展示动画
     */
    public void adapterNotify() {
        adapter.notifyDataSetChanged();
        listView.startAnimation(mScaleAnimation);
    }

    /*
    查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        provinceList = mCoolWeatherDao.loadProvinces();
        if (provinceList.size() > 0) {
            mDataList.clear();
            for (Province p : provinceList) {
                mDataList.add(p.getProvinceName());
            }
            adapterNotify();
            listView.setSelection(0);
            titleView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            //从服务器上获取
            queryFromServer(null, "province");
        }
    }

    /*
    查询所选省份下所有的城市,优先从数据库中查询,如果没有查询到就从服务器上查询
     */
    private void queryCities() {
        cityList = mCoolWeatherDao.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            mDataList.clear();
            for (City city : cityList) {
                mDataList.add(city.getCityName());
            }
            adapterNotify();
            listView.setSelection(0);
            titleView.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            //从服务器上获取
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /*
    查询所选市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    public void queryCounties() {
        countyList = mCoolWeatherDao.loadCounties(selectedCity.getId());
        int a = 8;
        if (countyList.size() > 0) {
            mDataList.clear();
            for (County c : countyList) {
                mDataList.add(c.getCountyName());
            }
            adapterNotify();
            listView.setSelection(0);
            titleView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            //从服务器上查询
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /*
    根据传入的代号和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        //加载数据时,显示progressdialog
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.hanleProvinceResponse(mCoolWeatherDao, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(mCoolWeatherDao, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(mCoolWeatherDao, response, selectedCity.getId());
                }

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
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
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在加载...");
            dialog.setCanceledOnTouchOutside(false);
        }

        dialog.show();
    }

    private void closeProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /*
    捕获Back按键,根据当前的级别进行判断
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }
}
