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

import net.zhdev.humantime.shared.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.widget.FrameLayout;

import java.io.FileNotFoundException;

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

        int backgroundType = preferences
                .getInt(Constants.BACKGROUND_TYPE_KEY, Constants.BACKGROUND_TYPE_COLOR);
        if (backgroundType == Constants.BACKGROUND_TYPE_COLOR) {
            int color = preferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                    getResources().getColor(android.R.color.holo_blue_dark));
            mWatchView.setBackground(new ColorDrawable(color));
        } else {
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory
                        .decodeStream(openFileInput(Constants.BACKGROUND_ASSET_FILE_NAME));
                mWatchView.setBackground(new BitmapDrawable(getResources(), bitmap));
            } catch (FileNotFoundException e) {
                mWatchView.setBackground(new ColorDrawable(Color.WHITE));
            }
        }

        int textColor = preferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        mWatchText.setTextColor(textColor);

        int textStyle = preferences.getInt(Constants.TEXT_STYLE_KEY, Constants.TEXT_STYLE_BOLD);
        switch (textStyle) {
            case Constants.TEXT_STYLE_NORMAL:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                break;
            case Constants.TEXT_STYLE_BOLD:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                break;
            case Constants.TEXT_STYLE_ITALIC:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                break;
            case Constants.TEXT_STYLE_BOLD_ITALIC:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                break;
            default:
                mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                break;
        }

        boolean textShadow = preferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
        if (textShadow) {
            mWatchText.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
        } else {
            mWatchText.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
        }

        float textSize = preferences.getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
        mWatchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
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
        listener for SharedPreference changes avoids having to setup a separate callback from the
        service. The changes are retrieved and applied to every view.
         */
        if (!mDisplayDimmed) {
            if (Constants.BACKGROUND_COLOR_KEY.equals(key)) {
                int color = sharedPreferences.getInt(Constants.BACKGROUND_COLOR_KEY, 0);
                mWatchView.setBackground(new ColorDrawable(color));
            } else if (Constants.BACKGROUND_ASSET_LAST_CHANGED_KEY.equals(key)) {
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory
                            .decodeStream(openFileInput(Constants.BACKGROUND_ASSET_FILE_NAME));
                    mWatchView.setBackground(new BitmapDrawable(getResources(), bitmap));
                } catch (FileNotFoundException e) {
                    Log.d("HumanTime", "File not found");
                }
            } else if (Constants.TEXT_COLOR_KEY.equals(key)) {
                int color = sharedPreferences.getInt(Constants.TEXT_COLOR_KEY, 0);
                mWatchText.setTextColor(color);
            } else if (Constants.TEXT_STYLE_KEY.equals(key)) {
                int style = sharedPreferences.getInt(Constants.TEXT_STYLE_KEY, 0);
                switch (style) {
                    case Constants.TEXT_STYLE_NORMAL:
                        mWatchText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                        break;
                    case Constants.TEXT_STYLE_BOLD:
                        mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                        break;
                    case Constants.TEXT_STYLE_ITALIC:
                        mWatchText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                        break;
                    case Constants.TEXT_STYLE_BOLD_ITALIC:
                        mWatchText.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                        break;
                }
            } else if (Constants.TEXT_SHADOW_KEY.equals(key)) {
                boolean showShadow = sharedPreferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
                if (showShadow) {
                    mWatchText.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
                } else {
                    mWatchText.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
                }
            } else if (Constants.TEXT_SIZE_KEY.equals(key)) {
                float textSize = sharedPreferences
                        .getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
                mWatchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            }
        }
    }
}
