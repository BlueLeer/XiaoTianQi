package android.study.leer.coolweather.db;

/**
 * Created by Leer on 2016/11/29.
 */

public class County {
    private int id;
    private String countyName;
    private String countyCode;
    private int myCityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public int getMyCityId() {
        return myCityId;
    }

    public void setMyCityId(int myCityId) {
        this.myCityId = myCityId;
    }
}
