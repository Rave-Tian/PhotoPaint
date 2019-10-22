package com.example.photopaint.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class BuildVars {

    public static boolean DEBUG_VERSION = false;
    public static boolean DEBUG_PRIVATE_VERSION = false;
    public static boolean LOGS_ENABLED = false;
    public static boolean USE_CLOUD_STRINGS = true;
    public static boolean CHECK_UPDATES = false;
    public static int BUILD_VERSION = 1608;
    public static String BUILD_VERSION_STRING = "5.7.0";
    public static int APP_ID = 800906; //obtain your own APP_ID at https://core.telegram.org/api/obtaining_api_id
    public static String APP_HASH = "ff0354a8224b666e3486875143ce0278"; //obtain your own APP_HASH at https://core.telegram.org/api/obtaining_api_id
    public static String HOCKEY_APP_HASH = "00000000000000000000000000000000";
    public static String HOCKEY_APP_HASH_DEBUG = "00000000000000000000000000000000";
    public static String SMS_HASH = ""; //https://developers.google.com/identity/sms-retriever/overview
    public static String PLAYSTORE_APP_URL = "";

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION);
        }
    }
}