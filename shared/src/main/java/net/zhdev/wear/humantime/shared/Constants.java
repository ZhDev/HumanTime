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

package net.zhdev.wear.humantime.shared;

import android.view.Gravity;

/**
 * This class provides a set of common constants shared by both the mobile app and the wearable
 * app.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public final class Constants {

    public static final String PREFS_NAME = "human_time_prefs";

    public static final String BACKGROUND_COLOR_KEY = "background_color";

    public static final String BACKGROUND_COLOR_PATH = "/background_color";

    public static final String BACKGROUND_ASSET_KEY = "background_asset";

    public static final String BACKGROUND_ASSET_PATH = "/background_asset";

    public static final String TEXT_COLOR_KEY = "text_color";

    public static final String TEXT_COLOR_PATH = "/text_color";

    public static final String TEXT_STYLE_KEY = "text_style";

    public static final String TEXT_STYLE_PATH = "/text_style";

    public static final String TEXT_SHADOW_KEY = "text_shadow";

    public static final String TEXT_SHADOW_PATH = "/text_shadow";

    public static final String TEXT_SIZE_KEY = "text_size";

    public static final String TEXT_SIZE_PATH = "/text_size";

    public static final float TEXT_SIZE_LARGE = 30.0F;

    public static final float TEXT_SIZE_MEDIUM = 25.0F;

    public static final float TEXT_SIZE_SMALL = 20.0F;

    public static final float TEXT_SIZE_EXTRA_SMALL = 15.0F;

    public static final String TEXT_POSITION_KEY = "text_position";

    public static final String TEXT_POSITION_PATH = "/text_position";

    public static final int TEXT_POSITION_TOP_LEFT = 0;

    public static final int TEXT_POSITION_TOP_CENTER = 1;

    public static final int TEXT_POSITION_TOP_RIGHT = 2;

    public static final int TEXT_POSITION_CENTER_LEFT = 3;

    public static final int TEXT_POSITION_CENTER_CENTER = 4;

    public static final int TEXT_POSITION_CENTER_RIGHT = 5;

    public static final int TEXT_POSITION_BOTTOM_LEFT = 6;

    public static final int TEXT_POSITION_BOTTOM_CENTER = 7;

    public static final int TEXT_POSITION_BOTTOM_RIGHT = 8;

    public static final String TEXT_CASE_KEY = "text_case";

    public static final String TEXT_CASE_PATH = "/text_case";

    public static final int TEXT_CASE_NO_CAPS = 0;

    public static final int TEXT_CASE_ALL_CAPS = 1;

    public static final int TEXT_CASE_FIRST_CAP = 2;

    public static final String BACKGROUND_ASSET_FILE_NAME = "background_image.png";

    public static final String BACKGROUND_ASSET_LAST_CHANGED_KEY = "background_last_changed";

    public static final String BACKGROUND_TYPE_KEY = "background_type";

    public static final int BACKGROUND_TYPE_COLOR = 0;

    public static final int BACKGROUND_TYPE_IMAGE = 1;

    public static final String TEXT_FONT_KEY = "text_font";

    public static final String TEXT_FONT_PATH = "/text_font";

    public static final int positionToGravity(int position) {
        switch (position) {
            case Constants.TEXT_POSITION_TOP_LEFT:
                return Gravity.TOP | Gravity.LEFT;
            case Constants.TEXT_POSITION_TOP_CENTER:
                return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case Constants.TEXT_POSITION_TOP_RIGHT:
                return Gravity.TOP | Gravity.RIGHT;
            case Constants.TEXT_POSITION_CENTER_LEFT:
                return Gravity.CENTER_VERTICAL | Gravity.LEFT;
            case Constants.TEXT_POSITION_CENTER_CENTER:
                return Gravity.CENTER;
            case Constants.TEXT_POSITION_CENTER_RIGHT:
                return Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            case Constants.TEXT_POSITION_BOTTOM_LEFT:
                return Gravity.BOTTOM | Gravity.LEFT;
            case Constants.TEXT_POSITION_BOTTOM_CENTER:
                return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            case Constants.TEXT_POSITION_BOTTOM_RIGHT:
                return Gravity.BOTTOM | Gravity.RIGHT;
            default:
                return Gravity.CENTER;
        }
    }
}
