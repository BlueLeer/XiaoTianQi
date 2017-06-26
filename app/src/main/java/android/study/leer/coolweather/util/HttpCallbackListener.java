package android.study.leer.coolweather.util;

/**
 * Created by Leer on 2016/11/30.
 */

/*
该接口下的方法在HttpUtil类中被回调
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
