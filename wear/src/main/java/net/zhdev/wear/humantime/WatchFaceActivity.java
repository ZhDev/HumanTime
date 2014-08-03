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
import android.view.Gravity;
import android.widget.FrameLayout;

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

    private FrameLayout mWatchView;

    private HumanTextClock mWatchText;

    private boolean mDisplayDimmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face);

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(this, new Handler(getMainLooper()));

        getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        mWatchView = (FrameLayout) findViewById(R.id.watch_view);
        mWatchText = (HumanTextClock) findViewById(R.id.watch_text);

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
        loadTextStyle(preferences);
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
                        mWatchView.setBackground(new ColorDrawable(Color.BLACK));
                        mWatchText.setTextColor(Color.WHITE);
                        mWatchText.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
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
            } else if (Constants.TEXT_STYLE_KEY.equals(key)) {
                loadTextStyle(sharedPreferences);
            } else if (Constants.TEXT_SHADOW_KEY.equals(key)) {
                loadTextShadow(sharedPreferences);
            } else if (Constants.TEXT_SIZE_KEY.equals(key)) {
                loadTextSize(sharedPreferences);
            } else if (Constants.TEXT_POSITION_KEY.equals(key)) {
                loadTextPosition(sharedPreferences);
            } else if (Constants.TEXT_CASE_KEY.equals(key)) {
                loadTextCase(sharedPreferences);
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
        mWatchView.setBackground(drawable);
    }

    private void loadTextCase(SharedPreferences preferences) {
        int textCase = preferences.getInt(Constants.TEXT_CASE_KEY, Constants.TEXT_CASE_NO_CAPS);
        mWatchText.setTextCase(textCase);
    }

    private void loadTextColor(SharedPreferences preferences) {
        int textColor = preferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        mWatchText.setTextColor(textColor);
    }

    private void loadTextPosition(SharedPreferences preferences) {
        int textPosition = preferences.getInt(Constants.TEXT_POSITION_KEY,
                Constants.TEXT_POSITION_CENTER_CENTER);
        int gravity;
        switch (textPosition) {
            case Constants.TEXT_POSITION_TOP_LEFT:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_TOP_CENTER:
                gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case Constants.TEXT_POSITION_TOP_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case Constants.TEXT_POSITION_CENTER_LEFT:
                gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_CENTER_CENTER:
                gravity = Gravity.CENTER;
                break;
            case Constants.TEXT_POSITION_CENTER_RIGHT:
                gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                break;
            case Constants.TEXT_POSITION_BOTTOM_LEFT:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_BOTTOM_CENTER:
                gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case Constants.TEXT_POSITION_BOTTOM_RIGHT:
                gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            default:
                gravity = Gravity.CENTER;
                break;
        }
        mWatchText.setGravity(gravity);
    }

    private void loadTextShadow(SharedPreferences preferences) {
        boolean showShadow = preferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
        if (showShadow) {
            mWatchText.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
        } else {
            mWatchText.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
        }
    }

    private void loadTextSize(SharedPreferences preferences) {
        float textSize = preferences.getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
        mWatchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    private void loadTextStyle(SharedPreferences preferences) {
        int textStyle = preferences.getInt(Constants.TEXT_STYLE_KEY, Constants.TEXT_STYLE_BOLD);
        switch (textStyle) {
            case Constants.TEXT_STYLE_BOLD_ITALIC:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                break;
            case Constants.TEXT_STYLE_BOLD:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                break;
            case Constants.TEXT_STYLE_ITALIC:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                break;
            default:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                break;
        }
    }
}
