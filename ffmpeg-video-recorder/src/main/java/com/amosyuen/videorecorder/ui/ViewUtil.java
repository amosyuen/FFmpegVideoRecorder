package com.amosyuen.videorecorder.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Utils for views.
 */
public final class ViewUtil {

    public static boolean isContextConfigurationLandscape(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getContextRotation(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }

    @ColorInt
    public static int getThemeColorAttribute(Resources.Theme theme, @AttrRes int attribute) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int colorInt) {
        drawable.mutate();
        drawable.setColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }
}
