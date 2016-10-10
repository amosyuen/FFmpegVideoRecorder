package com.amosyuen.videorecorder;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.amosyuen.videorecorder.util.ActivityThemeParams;
import com.amosyuen.videorecorder.util.Util;

/**
 * Activity that sets the colors based on params
 */

public abstract class AbstractDynamicStyledActivity extends AppCompatActivity {

    public static final String ACTIVITY_THEME_PARAMS_KEY =
            BuildConfig.APPLICATION_ID + ".ActivityThemeParams";

    protected ActivityThemeParams mThemeParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!extractIntentParams()) {
            return;
        }
        layoutView();
        setupToolbar((Toolbar) findViewById(R.id.toolbar));
    }

    protected boolean extractIntentParams() {
        Intent intent = getIntent();
        mThemeParams = intent.hasExtra(ACTIVITY_THEME_PARAMS_KEY)
                ? (ActivityThemeParams) intent.getSerializableExtra(ACTIVITY_THEME_PARAMS_KEY)
                : new ActivityThemeParams.Builder().build();
        return true;
    }

    protected ActivityThemeParams getThemeParams() {
        return mThemeParams;
    }

    @ColorInt
    protected int getStatusBarColor() {
        int color = getThemeParams().getStatusBarColor();
        return color == 0
                ? Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimaryDark) : color;
    }

    @ColorInt
    protected int getToolbarColor() {
        int color = getThemeParams().getToolbarColor();
        return color == 0 ? Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimary) : color;
    }

    @ColorInt
    protected int getToolbarWidgetColor() {
        int color = getThemeParams().getToolbarWidgetColor();
        return color == 0
                ? Util.getThemeColorAttribute(getTheme(), android.R.attr.textColor) : color;
    }

    @ColorInt
    protected int getProgressColor() {
        int color = getThemeParams().getProgressColor();
        return color == 0
                ? Util.getThemeColorAttribute(getTheme(), R.attr.colorAccent) : color;
    }

    protected abstract void layoutView();

    @CallSuper
    protected void setupToolbar(Toolbar toolbar) {
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        toolbar.setBackgroundColor(getToolbarColor());
        toolbarTitle.setTextColor(getToolbarWidgetColor());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    @CallSuper
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuItem menuItemFinish = menu.findItem(R.id.menu_finish);
        if (menuItemFinish != null) {
            Drawable menuItemFinishIcon = menuItemFinish.getIcon();
            if (menuItemFinishIcon != null) {
                menuItemFinish.setIcon(
                        Util.tintDrawable(menuItemFinishIcon, getToolbarWidgetColor()));
            }
        }
        return true;
    }
}
