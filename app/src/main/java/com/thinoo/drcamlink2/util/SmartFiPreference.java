package com.thinoo.drcamlink2.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SmartFiPreference {
    public static final String SF_TOKEN = "smartfi.token";
    public static final String SF_HOSPITAL_ID = "smartfi.hospital.id";
    public static final String SF_DOCTOR_ID = "smartfi.doctor.id";
    public static final String SF_PATIENT_ID = "smartfi.patient.id";
    public static final String SF_PATIENT_CHART = "smartfi.patient.chart";
    public static final String SF_DOCTOR_PW = "smartfi.doctor.password";

    public static void defaultPreference(Context context){

    }

    public static String getSfToken(Context con){
        return getString(con, SF_TOKEN, "AUTH_tk11c6caf3f4ce4a71a30aa5b1a0cc30d6");
    }

    public static void setSfToken(Context con, String tk){
        setString(con, SF_TOKEN, tk);
    }

    public static final String getHospitalId(Context con){
        //return getString(con, SF_HOSPITAL_ID, "000000000001");
        return getString(con, SF_HOSPITAL_ID, "kimcy");
    }

    public static void setHospitalId(Context con, String id){
        setString(con, SF_HOSPITAL_ID, id);
    }

    public static final String getDoctorId(Context con){
        return getString(con, SF_DOCTOR_ID, "test7");
    }

    public static void setDoctorId(Context con, String id){
        setString(con, SF_DOCTOR_ID, id);
    }

    public static final String getPatientId(Context con){
        return getString(con, SF_PATIENT_ID, "sf-patient");
    }

    public static void setPatientId(Context con, String id){
        setString(con, SF_PATIENT_ID, id);
    }

    public static final String getPatientChart(Context con){
        return getString(con, SF_PATIENT_CHART, "101010");
    }

    public static void setPatientChart(Context con, String chartNum){
        setString(con, SF_PATIENT_CHART, chartNum);
    }


    public static final String getSfDoctorPw(Context con){
        return getString(con, SF_DOCTOR_PW, "7777777");
    }

    public static void setSfDoctorPw(Context con, String pw){
        setString(con, SF_PATIENT_CHART, pw);
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
