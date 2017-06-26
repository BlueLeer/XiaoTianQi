package android.study.leer.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Leer on 2016/11/30.
 */

/*
发送Http地址,并且得到返回的消息
 */

public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener){
        new Thread(){
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    StringBuffer response = new StringBuffer();
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    if(listener != null){
                        //回调Onfinish()方法
                        listener.onFinish(response.toString());
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    if(listener != null){
                        //回调onError方法
                        listener.onError(e);
                    }
                }
            }
        }.start();
    }
}
