package com.juan.consumo_movil;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class LocalAttendanceManager {

    private static final String PREF_NAME = "attendance_prefs";
    private static final String KEY_ATTENDANCE = "attendance_ids";

    public static void saveAttendance(Context context, String activityId, String attendanceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> attendances = new HashSet<>(prefs.getStringSet(KEY_ATTENDANCE, new HashSet<>()));
        attendances.add(activityId + "|" + attendanceId);
        prefs.edit().putStringSet(KEY_ATTENDANCE, attendances).apply();
    }

    public static void removeAttendance(Context context, String activityId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> attendances = new HashSet<>(prefs.getStringSet(KEY_ATTENDANCE, new HashSet<>()));
        attendances.removeIf(s -> s.startsWith(activityId));
        prefs.edit().putStringSet(KEY_ATTENDANCE, attendances).apply();
    }

    public static Set<String> getAllAttendances(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_ATTENDANCE, new HashSet<>());
    }
}