package com.sourab.videorecorder;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static com.sourab.videorecorder.R.id.toolbar;

/**
 * Created by amosyuen on 9/22/2016.
 */

public abstract class AbstractDynamicStyledActivity extends AppCompatActivity {

    protected static final String EXTRA_PREFIX = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_STATUS_BAR_COLOR = EXTRA_PREFIX + ".StatusBarColor";
    public static final String EXTRA_TOOLBAR_COLOR = EXTRA_PREFIX + ".ToolbarColor";
    public static final String EXTRA_TOOLBAR_WIDGET_COLOR = EXTRA_PREFIX + ".ToolbarWidgetColor";

    // Enables dynamic coloring
    protected int mStatusBarColor;
    protected int mToolbarColor;
    protected int mToolbarWidgetColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extractIntentParams(getIntent());
        layoutView();
        setupToolbar((Toolbar) findViewById(toolbar));
    }

    @CallSuper
    protected void extractIntentParams(Intent intent) {
        mStatusBarColor = intent.getIntExtra(EXTRA_STATUS_BAR_COLOR,
                Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimaryDark));
        mToolbarColor = intent.getIntExtra(EXTRA_TOOLBAR_COLOR,
                Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimary));
        mToolbarWidgetColor = intent.getIntExtra(EXTRA_TOOLBAR_WIDGET_COLOR,
                Util.getThemeColorAttribute(getTheme(), android.R.attr.textColor));
    }

    protected abstract void layoutView();

    @CallSuper
    protected void setupToolbar(Toolbar toolbar) {
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        toolbar.setBackgroundColor(mToolbarColor);
        toolbarTitle.setTextColor(mToolbarWidgetColor);

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
                menuItemFinishIcon.mutate();
                menuItemFinishIcon.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);

                menuItemFinish.setIcon(menuItemFinishIcon);
            }
        }
        return true;
    }

    @CallSuper
    protected void setNextIntentParams(Intent intent) {
        intent.putExtra(EXTRA_STATUS_BAR_COLOR, mStatusBarColor);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, mToolbarColor);
        intent.putExtra(EXTRA_TOOLBAR_WIDGET_COLOR, mToolbarWidgetColor);
    }
}
