package android.study.leer.coolweather.db;

/**
 * Created by Leer on 2016/11/29.
 */

public class City {
    private int id;
    private String cityName;
    private String cityCode;
    private int myProvinceId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public int getMyProvinceId() {
        return myProvinceId;
    }

    public void setMyProvinceId(int myProvinceId) {
        this.myProvinceId = myProvinceId;
    }
}
