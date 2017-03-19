package com.amosyuen.videorecorder.activity;


import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.activity.params.ActivityThemeParamsI;

/**
 * Activity that sets the colors based on params
 */

public abstract class AbstractDynamicStyledActivity extends AppCompatActivity {

    protected abstract boolean extractIntentParams();

    protected abstract ActivityThemeParamsI getThemeParams();

    protected abstract void layoutView();

    @CallSuper
    protected void setupToolbar(Toolbar toolbar) {
        ActivityThemeParamsI themeParams = getThemeParams();
        setStatusBarColor(themeParams.getStatusBarColor());

        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        toolbar.setBackgroundColor(themeParams.getToolbarColor());
        toolbarTitle.setTextColor(themeParams.getToolbarWidgetColor());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
