package com.amosyuen.videorecorder.activity;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.amosyuen.videorecorder.BuildConfig;
import com.amosyuen.videorecorder.R;
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
        setStatusBarColor(getStatusBarColor());

        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        toolbar.setBackgroundColor(getToolbarColor());
        toolbarTitle.setTextColor(getToolbarWidgetColor());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        }
    }
}
