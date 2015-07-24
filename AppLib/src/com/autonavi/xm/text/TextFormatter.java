
package com.autonavi.xm.text;

import android.content.Context;

import com.autonavi.xm.app.lib.R;

public class TextFormatter {

    public static String formatDistance(Context context, int meter) {
        return formatDistance(context, meter, false);
    }

    public static String formatToShortDistance(Context context, int meter) {
        return formatDistance(context, meter, true);
    }

    public static String formatDistance(Context context, int meter, boolean isShort) {
        if (Math.abs(meter) < 1000) {
            return context.getString(R.string.format_distance_meter, meter);
        } else {
            float km = meter / 1000f;
            return context.getString(
                    (isShort || km >= 1000.0f) ? R.string.format_distance_kilometer_short
                            : R.string.format_distance_kilometer, km);
        }
    }

    public static String formatElapsedTime(Context context, int hour, int minute, int second,
            int millis) {
        second += millis / 1000;
        minute += second / 60;
        minute += hour * 60;
        return formatElapsedTime(context, minute);
    }

    public static String formatElapsedTime(Context context, int minute) {
        return context.getString(R.string.format_short_time, minute / 60, minute % 60);
    }

    /**
     * 格式化速度<br>
     * 例：1.1km/h or 1.1m/s
     * 
     * @param context Context
     * @param speed 速度值
     * @param kmPerHour true为km/h，false为m/s
     * @return 格式化后的字符串
     */
    public static String formatSpeed(Context context, float speed, boolean kmPerHour) {
        return context.getString(kmPerHour ? R.string.format_km_per_hour
                : R.string.format_m_per_second, speed);
    }

    /**
     * 格式化速度<br>
     * 例：1km/h or 1m/s
     * 
     * @param context Context
     * @param speed 速度值
     * @param kmPerHour true为km/h，false为m/s
     * @return 格式化后的字符串
     */
    public static String formatToShortSpeed(Context context, int speed, boolean kmPerHour) {
        return context.getString(kmPerHour ? R.string.format_km_per_hour_short
                : R.string.format_m_per_second_short, speed);
    }

}
