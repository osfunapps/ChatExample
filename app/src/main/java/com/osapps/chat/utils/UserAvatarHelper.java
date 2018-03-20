package com.osapps.chat.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;

/**
 * Created by aniket on 05/09/17.
 */

public class UserAvatarHelper {

    private static final Integer [] COLORS = new Integer[] {
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5,
            0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50,
            0xFF8BC34A, 0xFFCDDC39, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722,
            0xFF795548, 0xFF9E9E9E, 0xFF607D8B};

    private static Integer getUserAvatarBackgroundColor (String username) {
        return COLORS[username.length() % COLORS.length];
    }

    private static String getUsernameInitials (String username) {
        if (username.isEmpty()) {
            return "?";
        }

        String [] splitUsername = username.split(".");
        Integer splitCount = splitUsername.length;
        if (splitCount > 1 && !splitUsername[0].isEmpty() && !splitUsername[splitCount-1].isEmpty()) {
            String firstInitial = splitUsername[0].substring(0, 1);
            String secondInitial = splitUsername[splitCount-1].substring(0, 1);
            return (firstInitial + secondInitial).toUpperCase();
        } else {
            if (username.length() > 1) {
                return username.substring(0, 2).toUpperCase();
            } else {
                return username.substring(0, 1).toUpperCase();
            }
        }
    }

    public static Drawable getTextDrawable(String username, Context context) {

        return TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF)
                .endConfig()
                .buildRoundRect(getUsernameInitials(username), getUserAvatarBackgroundColor(username), getRadius(context));
    }


    public static Integer getRadius(Context context) {
        return 4 * (int)context.getResources().getDisplayMetrics().density;
    }
}
