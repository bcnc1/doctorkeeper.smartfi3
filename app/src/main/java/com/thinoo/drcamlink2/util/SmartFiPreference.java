package com.thinoo.drcamlink2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SmartFiPreference {
    public static final String SF_TOKEN = "smartfi.token";

    public static void defaultPreference(Context context){

    }

    public static String getSfToken(Context con){
        return getString(con, SF_TOKEN, null);
    }

    public static void setSfToken(Context con, String tk){
        setString(con, SF_TOKEN, tk);
    }

    public static void setInt(Context context, String key, int value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, default_value);
    }

    public static void setBoolean(Context context, String key, boolean value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, default_value);
    }

    public static void setString(Context context, String key, String value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key, String default_value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, default_value);
    }
}
