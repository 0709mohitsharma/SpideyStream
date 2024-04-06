package com.sharmaji.spideystream.utils;

import android.content.ClipboardManager;
import android.content.Context;

public class Utils {
    public static String getClipboardText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            return ""+clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        }
        return "";
    }
}
