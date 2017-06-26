package android.study.leer.coolweather.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leer on 2017/6/25.
 */

public class WeatherInfo {
    public Result result;

    public class Result {
        public SK sk;
        public Today today;
        public ArrayList<Future> future;

    }

    //实况天气
    public class SK {
        public String temp;
        public String time;
    }

    public class Today {
        public String temperature;//全天天气温度范围
        public String weather;//天气
        public WeatherIcon weather_id;//描述天气的图片编号
        public String wind;
        public String city;
        public String dressing_index;//穿衣指数
        public String dressing_advice;//穿衣建议
        public String uv_index;//紫外线指数
        public String travel_index;//旅游建议
        public String exercise_index;//是否适合户外运动
    }

    //未来天气情况
    public class Future {
        public String temperature;
        public String weather;
        public WeatherIcon weather_id;
        public String wind;
        public String week;
        public String date;

    }

    public class WeatherIcon {
        public String fa;
        public String fb;
    }

}
