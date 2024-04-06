package com.sharmaji.spideystream.utils;
import android.content.Context;
import android.content.SharedPreferences;
import  com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class PrefsHandler {
    public  static String SHARED_PREFS_NAME = "SpideyPrefs";
    public  static String HOSTS_PREFS = "HostsPrefs";
    public  static String LAST_BEST_HOST_PREFS = "Last_Best_HostsPrefs";

    public static void setHosts(Context context, List<String> hosts){
        Gson gson = new Gson();
        String json = gson.toJson(hosts);
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(HOSTS_PREFS,json).apply();
    }

    public static List<String> getHosts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(HOSTS_PREFS, null);
        Gson gson = new Gson();
        List<String> models = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        return models;
    }
    public static void setLastBestHosts(Context context, String host){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(LAST_BEST_HOST_PREFS,host).apply();
    }
    public static String getLastBestHost(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_BEST_HOST_PREFS, "https://vidsrc.net");
    }


}
