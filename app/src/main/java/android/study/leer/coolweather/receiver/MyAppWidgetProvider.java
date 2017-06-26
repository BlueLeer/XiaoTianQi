package android.study.leer.coolweather.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.study.leer.coolweather.service.AppWidgetService;

/** 桌面小部件处理相关功能的代码
 * Created by Leer on 2017/3/13.
 */

public class MyAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }


    //第一个窗体小部件被创建的时候调用此方法
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(context,AppWidgetService.class));
    }

    //创建多一个窗体小部件调用的方法,也就是说创建每一个窗体小部件的时候都会调用
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context,AppWidgetService.class));
    }

    //当窗体小部件被拖拽导致尺寸发生变化的时候调用此方法
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        context.startService(new Intent(context,AppWidgetService.class));
    }

    //当一个窗体小部件被删除的时候调用此方法
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    //当所有的窗体小部件被删除的时候调用此方法
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context,AppWidgetService.class));
    }
}
