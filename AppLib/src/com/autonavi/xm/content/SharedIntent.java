
package com.autonavi.xm.content;

public class SharedIntent {

    private static final String INTENT_PREFIX = "com.autonavi.xm.";

    private static final String ACTION_PREFIX = INTENT_PREFIX + "action.";

    private static final String EXTRA_PREFIX = INTENT_PREFIX + "extra.";

    /**
     * 在地图上显示某个点
     * 
     * <pre>
     * Extras:
     * {@link #EXTRA_NAME}       非必须
     * {@link #EXTRA_LONGITUDE}  必须
     * {@link #EXTRA_LATITUDE}   必须
     * {@link #EXTRA_GEO_TYPE}   非必须
     * </pre>
     * 
     * 常量值：{@value}
     */
    public static final String ACTION_SHOW_ON_MAP = ACTION_PREFIX + "SHOW_ON_MAP";

    /**
     * 导航到某个点<br>
     * 
     * <pre>
     * Extras:
     * {@link #EXTRA_NAME}       非必须
     * {@link #EXTRA_LONGITUDE}  必须
     * {@link #EXTRA_LATITUDE}   必须
     * {@link #EXTRA_GEO_TYPE}   非必须
     * {@link #EXTRA_PROMPT}     非必须
     * </pre>
     * 
     * 常量值：{@value}
     */
    public static final String ACTION_NAVIGATE = ACTION_PREFIX + "NAVIGATE";

    /**
     * 系统功能键广播<br>
     * 常量值：{@value}
     */
    public static final String ACTION_FUNCATION_KEY = "android.intent.action.FUNCATION_KEY";

    /**
     * 名称，类型：String<br>
     * 常量值：{@value}
     */
    public static final String EXTRA_NAME = EXTRA_PREFIX + "NAME";

    /**
     * 经度，类型：float <i>(导航内部可以采用int类型)</i><br>
     * 常量值：{@value}
     */
    public static final String EXTRA_LONGITUDE = EXTRA_PREFIX + "LONGITUDE";

    /**
     * 纬度，类型：float <i>(导航内部可以采用int类型)</i><br>
     * 常量值：{@value}
     */
    public static final String EXTRA_LATITUDE = EXTRA_PREFIX + "LATITUDE";

    /**
     * 经纬度类型，类型：int<br>
     * 常量值：{@value}
     */
    public static final String EXTRA_GEO_TYPE = EXTRA_PREFIX + "GEO_TYPE";

    /**
     * 是否提示，类型：boolean<br>
     * 常量值：{@value}
     */
    public static final String EXTRA_PROMPT = EXTRA_PREFIX + "PROMPT";

    /**
     * 索引，类型：int<br>
     * 常量值：{@value}
     */
    public static final String EXTRA_INDEX = EXTRA_PREFIX + "INDEX";

    /**
     * Fragment保存的状态<br>
     * 常量值：{@value}
     */
    public static final String EXTRA_SAVED_INSTANCE_STATE = EXTRA_PREFIX + "SAVED_INSTANCE_STATE";

    /**
     * 导航
     */
    public static final String PACKAGE_NAVIGATION = INTENT_PREFIX + "navigation";

    /**
     * 系统设置
     */
    public static final String PACKAGE_SYSTEM_SETTINGS = INTENT_PREFIX + "settings";

    /**
     * 人工导航
     */
    public static final String PACKAGE_IDRIVE = INTENT_PREFIX + "idrive";

    /**
     * 电话
     */
    public static final String PACKAGE_PHONE = INTENT_PREFIX + "phone";

    /**
     * 应用商店
     */
    public static final String PACKAGE_APPMANAGER = INTENT_PREFIX + "appmanager";

    /**
     * 音乐
     */
    public static final String ACTION_MEDIA_MUSIC = ACTION_PREFIX + "MP3_PLAYER_LIST";

    /**
     * 打开音乐列表界面传递的boolean值:是否从图片应用打开音乐列表界面
     */
    public static final String EXTRA_IS_OPEN_FROM_PHOTOVIEWER = "extra_is_open_from_photoviewer";

    /**
     * 蓝牙音乐
     */
    public static final String ACTION_MEDIA_MUSIC_BLUETOOTH = ACTION_PREFIX
            + "START_BLUETOOTH_MUSIC";

    /**
     * 视频
     */
    public static final String PACKAGE_MEDIA_VIDEO = INTENT_PREFIX + "media.video";

    /**
     * T服务
     */
    public static final String PACKAGE_TELEMATICS = INTENT_PREFIX + "telematics.";

    /**
     * T服务:新闻
     */
    public static final String PACKAGE_TELEMATICS_NEWS = PACKAGE_TELEMATICS + "news";

    /**
     * T服务:天气预报
     */
    public static final String PACKAGE_TELEMATICS_WEATHER = PACKAGE_TELEMATICS + "weather";

    /**
     * T服务:酒店预订
     */
    public static final String PACKAGE_TELEMATICS_HOTEL = PACKAGE_TELEMATICS + "hotel";

    /**
     * T服务:股票资讯
     */
    public static final String PACKAGE_TELEMATICS_STOCK = PACKAGE_TELEMATICS + "stock";

    /**
     * 主菜单
     */
    public static final String PACKAGE_HOME = INTENT_PREFIX + "home";

    /**
     * 图片浏览
     */
    public static final String PACKAGE_PHOTO_VIEWER = INTENT_PREFIX + "photoviewer";

    /**
     * 机票服务
     */
    public static final String PACKAGE_TELEMATICS_FLIGHT = PACKAGE_TELEMATICS + "flight";

    /**
     * 日程表(提醒事项)
     */
    public static final String PACKAGE_SCHEDULE = INTENT_PREFIX + "schedule";

    /**
     * 音乐电台
     */
    public static final String PACKAGE_INTERNET_RADIO = INTENT_PREFIX + "internet.radio";

    /**
     * 应用商店的前台升级服务
     */
    public static final String ACTION_APPMANAGER_SERVICE = ACTION_PREFIX
            + "START_APPMANAGER_SERVICE";

    /**
     * 应用商店卸载的action
     */
    public static final String ACTION_APP_MANAGER_UNINSTALL = ACTION_PREFIX
            + "APP_MANAGER_UNINSTALL";

    /**
     * 应用商店有新的应用安装
     */
    public static final String ACTION_APP_MANAGER_NEW_APP_INSTALL = ACTION_PREFIX
            + "APP_MANAGER_NEW_APP_INSTALL";

    /**
     * 打开系统设置:日期和时间界面的action
     */
    public static final String ACTION_SYSTEM_SETTINGS_DATE_TIME = ACTION_PREFIX
            + "SYSTEM_SETTINGS_DATE_TIME";

    /**
     * 打开系统设置:蓝牙设置界面
     */
    public static final String ACTION_BLUETOOTH_SETTINGS = ACTION_PREFIX + "BLUETOOTH_SETTINGS";

    /**
     * 打开系统设置:wifi设置界面
     */
    public static final String ACTION_WIFI_SETTINGS = ACTION_PREFIX + "WIFI_SETTINGS";

    public static final String ACTION_MULTIMEDIA = ACTION_PREFIX + "MULTIMEDIA";

    /**
     * 应用商店有多少应用需要更新的action
     */
    public static final String ACTION_APP_MANAGER_TO_HOME_APP_UPDATE_NUMBER = ACTION_PREFIX
            + "APP_MANAGER_TO_HOME_APP_UPDATE_NUMBER";

    /**
     * 应用商店有多少应用需要更新的extra
     */
    public static final String EXTRA_APP_MANAGER_TO_HOME_APP_UPDATE_NUMBER = EXTRA_PREFIX
            + "APP_MANAGER_TO_HOME_APP_UPDATE_NUMBER";

    /**
     * 手机伴侣的后台服务
     */
    public static final String ACTION_APP_SEND_TO_CAR_SERVICE = ACTION_PREFIX
            + "APP_SEND_TO_CAR_SERVICE";

    /**
     * 视频获取音频通道焦点时,向音频发送获取焦点的广播通知
     */
    public static final String MUSIC_AUDIO_FOCUS = "com.autonavi.xm.media.music.musicaudiofocus";

    /**
     * 开启音乐服务的action
     */
    public static final String ACTION_START_MUSIC_PLAYER_SERVICE = "com.autonavi.xm.media.music.action.MUSIC_PLAYER_SERVICE";

    /**
     * 打开人工导航设置界面的action
     */
    public static final String ACTION_IDRIVE_SETTINGS = ACTION_PREFIX + "idrive_settings";

    /**
     * 人工导航,拨打电话,挂断电话后,获取目的地的actions
     */
    public static final String ACTION_GET_CUR_DESTINATION = ACTION_PREFIX + "GET_CUR_DESTINATION";

    /**
     * 拨打电话的action
     */
    public static final String ACTION_PHONE_CALL = ACTION_PREFIX + "CALL";

    /**
     * 拨打电话传递的类型
     */
    public static final String EXTRA_CALL_TYPE = ACTION_PREFIX + "EXTRA_CALL_TYPE";

    public static final String EXTRA_PHONE_OFF_HOOK = ACTION_PREFIX + "EXTRA_PHONE_OFF_HOOK";
}
