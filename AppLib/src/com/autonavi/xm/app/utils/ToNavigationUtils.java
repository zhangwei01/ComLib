
package com.autonavi.xm.app.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.autonavi.xm.content.SharedIntent;

/**
 * 打开导航地图的工具类
 * 
 * @author zhangwei01
 * @since 2014年9月12日 下午4:24:07
 */
public class ToNavigationUtils {
    private static final String TAG = "ToNavigationUtils";

    public static void startNavigation(Context context, String name, double longitude,
            double latitude) {
        startALiNavigation(context, name, longitude, latitude);
    }

    public static void startNavigation(Context context, String name, String lon, String lat) {
        startALiNavigation(context, name, lon, lat);
    }

    public static void startNavigationShowOnMap(Context context, String name, double longitude,
            double latitude) {
        startALiNavigationShowOnMap(context, name, longitude, latitude);
    }

    public static void startNavigationShowOnMap(Context context, String name, String lon, String lat) {
        startALiNavigationShowOnMap(context, name, lon, lat);
    }

    /**
     * 打开阿里导航并进行导航
     * 
     * @param name 位置名称
     * @param longitude 经度
     * @param latitude 纬度
     */
    public static void startALiNavigation(Context context, String name, double longitude,
            double latitude) {
        Intent intent = new Intent("com.autonavi.xm.action.call.NAVIGATE");
        intent.putExtra("mName", name);
        intent.putExtra("mEndLng", longitude);
        intent.putExtra("mEndLat", latitude);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * 打开阿里导航并进行导航
     * 
     * @param name 位置名称
     * @param longitude 经度
     * @param latitude 纬度
     */
    public static void startALiNavigation(Context context, String name, String lon, String lat) {
        double longitude = longitudeStringToDouble(lon);
        double latitude = latitudeStringToDouble(lat);
        startALiNavigation(context, name, longitude, latitude);
    }

    /**
     * 打开阿里导航并在地图上显示该点
     * 
     * @param name 位置名称
     * @param longitude 经度
     * @param latitude 纬度
     */
    public static void startALiNavigationShowOnMap(Context context, String name, double longitude,
            double latitude) {
        Intent intent = new Intent("com.autonavi.xm.action.call.SHOW_ON_MAP");
        intent.putExtra("mName", name);
        intent.putExtra("mStartLng", longitude);
        intent.putExtra("mStartLat", latitude);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * 打开阿里导航并在地图上显示该点
     * 
     * @param name 位置名称
     * @param longitude 经度
     * @param latitude 纬度
     */
    public static void startALiNavigationShowOnMap(Context context, String name, String lon,
            String lat) {
        double longitude = longitudeStringToDouble(lon);
        double latitude = latitudeStringToDouble(lat);
        startALiNavigationShowOnMap(context, name, longitude, latitude);
    }

    public static void startDragonNavi(Context context, String name, double longitude,
            double latitude) {
        Intent intent = new Intent(SharedIntent.ACTION_NAVIGATE);
        intent.putExtra(SharedIntent.EXTRA_NAME, name);
        intent.putExtra(SharedIntent.EXTRA_LONGITUDE, longitude);
        intent.putExtra(SharedIntent.EXTRA_LATITUDE, latitude);
        intent.putExtra(SharedIntent.EXTRA_PROMPT, true);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            //ignore
        }
    }

    public static void startDragonNavi(Context context, String name, String lon, String lat) {
        double longitude = longitudeStringToDouble(lon);
        double latitude = latitudeStringToDouble(lat);
        startDragonNavi(context, name, longitude, latitude);
    }

    public static void startDragonNaviShowOnMap(Context context, String name, double longitude,
            double latitude) {
        Intent intent = new Intent(SharedIntent.ACTION_SHOW_ON_MAP);
        intent.putExtra(SharedIntent.EXTRA_NAME, name);
        intent.putExtra(SharedIntent.EXTRA_LONGITUDE, longitude);
        intent.putExtra(SharedIntent.EXTRA_LATITUDE, latitude);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            //ignore
        }
    }

    public static void startDragonNaviShowOnMap(Context context, String name, String lon, String lat) {
        double longitude = longitudeStringToDouble(lon);
        double latitude = latitudeStringToDouble(lat);
        startDragonNaviShowOnMap(context, name, longitude, latitude);
    }

    /**
     * 将经度从String转换为double
     * 
     * @param lon 经度
     */
    private static double longitudeStringToDouble(String lon) {
        if (TextUtils.isEmpty(lon)) {
            LogUtils.i(TAG, "data error-->lon:" + lon);
            return 0;
        }

        double longitude = 0;
        if (lon.contains(".")) {
            //double型
            longitude = Double.parseDouble(lon);
        } else {
            //int型
            int xLength = lon.length();
            String xx;
            if (xLength > 3) {
                xx = "118." + lon.substring(3, xLength);
            } else {
                xx = lon;
            }
            longitude = Double.parseDouble(xx);
        }
        return longitude;
    }

    /**
     * 将纬度从String转换为double
     * 
     * @param lon 纬度
     */
    private static double latitudeStringToDouble(String lat) {
        if (TextUtils.isEmpty(lat)) {
            LogUtils.i(TAG, "data error-->lat:" + lat);
            return 0;
        }

        double latitude = 0;
        if (lat.contains(".")) {
            //double型
            latitude = Double.parseDouble(lat);
        } else {
            //int型
            int yLength = lat.length();
            String yy;
            if (yLength > 2) {
                yy = "24." + lat.substring(2, yLength);
            } else {
                yy = lat;
            }
            latitude = Double.parseDouble(yy);
        }
        return latitude;
    }
}
