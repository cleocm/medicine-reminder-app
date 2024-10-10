package com.example.medicinereminderapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class NotificationUtils {
    private static final String PREFS_NAME = "MedicineReminders";
    private static final String KEY_PREFIX = "MedicineReminder_";

    public static void saveWorkRequestId(Context context, String medicineName, UUID workRequestId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PREFIX + medicineName, workRequestId.toString());
        editor.apply();
    }

    public static UUID getWorkRequestId(Context context, String medicineName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String workRequestIdString = sharedPreferences.getString(KEY_PREFIX + medicineName, null);
        if (workRequestIdString != null) {
            return UUID.fromString(workRequestIdString);
        }
        return null;
    }

    public static void removeWorkRequestId(Context context, String medicineName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PREFIX + medicineName);
        editor.apply();
    }

    public static void clearAllReminders(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // clear all the data in SharedPreferences
        editor.apply();
    }
}
