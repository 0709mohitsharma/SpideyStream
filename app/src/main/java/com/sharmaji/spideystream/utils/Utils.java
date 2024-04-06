package com.sharmaji.spideystream.utils;

import android.content.ClipboardManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {
    public static String getClipboardText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            return ""+clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        }
        return "";
    }
    public static String getCurrentTimeAndDate(Context context) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String time = timeFormat.format(calendar.getTime());
        String date = dateFormat.format(calendar.getTime());
        return time + " | " + date;
    }
}
