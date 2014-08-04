/*
 * Copyright 2014 Julio García Muñoz (ZhDev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.zhdev.wear.humantime;

import net.zhdev.wear.humantime.shared.Constants;
import net.zhdev.wear.humantime.shared.Font;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * <p><code>WatchFaceActivity</code> implements the UI for a watch face that shows the time in a
 * "human speech" way, using a textual representation such as "half past twelve".</p>
 *
 * <p>The watch face can be configured through the companion app installed in the handheld device.
 * The parameters that can be configured are the background (using a solid color or an image), the
 * text color, the text style and the text shadow.</p>
 *
 * <p>The activity follows Android Wear conventions, changing the UI when the screen is dimmed on
 * the device, displaying a simplified version: black background, normal style and no shadow.</p>
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class WatchFaceActivity extends Activity implements DisplayManager.DisplayListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private LinearLayout mWatchContainer;

    private HumanTextClock mWatchTime;

    private ShortDateClock mWatchDate;

    private boolean mDisplayDimmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face);

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(this, new Handler(getMainLooper()));

        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        mWatchContainer = (LinearLayout) findViewById(R.id.watch_view);
        mWatchTime = (HumanTextClock) findViewById(R.id.watch_time);
        mWatchDate = (ShortDateClock) findViewById(R.id.watch_date);

        loadSavedValues();
    }

    @Override
    protected void onDestroy() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.unregisterDisplayListener(this);
        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    /**
     * Loads the saved settings used by the watch face or uses the default ones if it's the first
     * time running the app.
     */
    private void loadSavedValues() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        loadBackground(preferences);
        loadTextCase(preferences);
        loadTextColor(preferences);
        loadTextPosition(preferences);
        loadTextShadow(preferences);
        loadTextSize(preferences);
        loadTextStyleAndFont(preferences);
        loadDate(preferences);
    }

    @Override
    public void onDisplayAdded(int displayId) {

    }

    @Override
    public void onDisplayRemoved(int displayId) {

    }

    @Override
    public void onDisplayChanged(int displayId) {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        int state = displayManager.getDisplay(displayId).getState();

        switch (state) {
            case Display.STATE_DOZING: // The UI is simplified
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWatchContainer.setBackground(new ColorDrawable(Color.BLACK));
                        mWatchTime.setTextColor(Color.WHITE);
                        mWatchTime.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
                        if (mWatchDate.getVisibility() == View.VISIBLE) {
                            mWatchDate.setTextColor(Color.WHITE);
                            mWatchDate.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
                        }
                    }
                });
                mDisplayDimmed = true;
                break;
            case Display.STATE_OFF:
                break;
            default:
                mDisplayDimmed = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadSavedValues();
                    }
                });
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
        This method is used to listen for changes coming from the DataLayerListenerService. Since
        the values have to be stored anyways to be used every time the screen wakes up, setting up a
        listener for changes on SharedPreferences avoids having to setup a separate callback from
        the service. The changes are retrieved and applied to every view.
         */
        if (!mDisplayDimmed) {
            if (Constants.BACKGROUND_COLOR_KEY.equals(key)) {
                loadBackground(sharedPreferences);
            } else if (Constants.BACKGROUND_ASSET_LAST_CHANGED_KEY.equals(key)) {
                loadBackground(sharedPreferences);
            } else if (Constants.TEXT_COLOR_KEY.equals(key)) {
                loadTextColor(sharedPreferences);
            } else if (Constants.TEXT_STYLE_KEY.equals(key)
                    || Constants.TEXT_FONT_KEY.equals(key)) {
                loadTextStyleAndFont(sharedPreferences);
            } else if (Constants.TEXT_SHADOW_KEY.equals(key)) {
                loadTextShadow(sharedPreferences);
            } else if (Constants.TEXT_SIZE_KEY.equals(key)) {
                loadTextSize(sharedPreferences);
            } else if (Constants.TEXT_POSITION_KEY.equals(key)) {
                loadTextPosition(sharedPreferences);
            } else if (Constants.TEXT_CASE_KEY.equals(key)) {
                loadTextCase(sharedPreferences);
            } else if (Constants.DATE_KEY.equals(key)) {
                loadDate(sharedPreferences);
            }
        }
    }

    private void loadBackground(SharedPreferences preferences) {
        int backgroundType = preferences.getInt(Constants.BACKGROUND_TYPE_KEY,
                Constants.BACKGROUND_TYPE_COLOR);
        Drawable drawable = null;
        int color;
        if (backgroundType == Constants.BACKGROUND_TYPE_COLOR) {
            color = preferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                    getResources().getColor(android.R.color.holo_blue_dark));
            drawable = new ColorDrawable(color);
        } else if (backgroundType == Constants.BACKGROUND_TYPE_IMAGE) {
            try {
                InputStream inputStream = openFileInput(Constants.BACKGROUND_ASSET_FILE_NAME);
                drawable = new BitmapDrawable(getResources(), inputStream);
            } catch (FileNotFoundException e) {
                color = preferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                        getResources().getColor(android.R.color.holo_blue_dark));
                drawable = new ColorDrawable(color);
            }
        }
        mWatchContainer.setBackground(drawable);
    }

    private void loadTextCase(SharedPreferences preferences) {
        int textCase = preferences.getInt(Constants.TEXT_CASE_KEY, Constants.TEXT_CASE_NO_CAPS);
        mWatchTime.setTextCase(textCase);
    }

    private void loadTextColor(SharedPreferences preferences) {
        int textColor = preferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        mWatchTime.setTextColor(textColor);
        mWatchDate.setTextColor(textColor);
    }

    private void loadTextPosition(SharedPreferences preferences) {
        int textPosition = preferences.getInt(Constants.TEXT_POSITION_KEY,
                Constants.TEXT_POSITION_CENTER_CENTER);
        int gravity = Constants.positionToGravity(textPosition);
        mWatchTime.setGravity(gravity);
        mWatchDate.setGravity(gravity);
        mWatchContainer.setGravity(gravity);
    }

    private void loadTextShadow(SharedPreferences preferences) {
        boolean showShadow = preferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
        if (showShadow) {
            mWatchTime.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
            mWatchDate.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
        } else {
            mWatchTime.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
            mWatchDate.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
        }
    }

    private void loadTextSize(SharedPreferences preferences) {
        float textSize = preferences.getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
        mWatchTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    private void loadTextStyleAndFont(SharedPreferences preferences) {
        int textStyle = preferences.getInt(Constants.TEXT_STYLE_KEY, Typeface.BOLD);
        String textFontCode = preferences
                .getString(Constants.TEXT_FONT_KEY, Font.DEFAULT.getFontCode());
        Font font = Font.findFontByCode(textFontCode);
        // A change of font implies a change of style, if we receive the change of font first we
        // might be in an invalid state
        if (font.hasStyle(textStyle)) {
            mWatchTime.setTypeface(font.getTypeface(getApplicationContext(), textStyle));
            mWatchTime.setText(mWatchTime.getText());
            mWatchDate.setTypeface(font.getTypeface(getApplicationContext(), textStyle));
            mWatchDate.setText(mWatchDate.getText());
        }
    }

    private void loadDate(SharedPreferences preferences) {
        boolean showDate = preferences.getBoolean(Constants.DATE_KEY, false);
        if (showDate) {
            mWatchDate.setVisibility(View.VISIBLE);
        } else {
            mWatchDate.setVisibility(View.GONE);
        }
    }
}
