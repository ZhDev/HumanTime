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

import android.content.Context;
import android.graphics.Typeface;

/**
 * <code>Font</code> holds static references to available fonts.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public final class Font {

    public static final Font DEFAULT = new Font("default", "Default", true, true, true);

    public static final Font CRAFTY_GIRLS = new Font("crafty-girls", "Crafty Girls", false, false,
            false);

    public static final Font DANCING_SCRIPT = new Font("dancing-script", "Dancing Script", true,
            false, false);

    public static final Font LOBSTER_TWO = new Font("lobster-two", "Lobster Two", true, true, true);

    public static final Font PRESS_START_2P = new Font("press-start-2p", "Press Start 2P", false,
            false, false);

    private final String mFontCode;

    private final String mDisplayName;

    private final boolean mHasBoldVersion;

    private final boolean mHasItalicVersion;

    private final boolean mHasBoldItalicVersion;

    private Font(String fontCode, String displayName, boolean hasBoldVersion,
            boolean hasItalicVersion, boolean hasBoldItalicVersion) {
        mFontCode = fontCode;
        mDisplayName = displayName;
        mHasBoldVersion = hasBoldVersion;
        mHasItalicVersion = hasItalicVersion;
        mHasBoldItalicVersion = hasBoldItalicVersion;
    }

    public static Font findFontByCode(String code) {
        if (CRAFTY_GIRLS.mFontCode.equals(code)) {
            return CRAFTY_GIRLS;
        } else if (DANCING_SCRIPT.mFontCode.equals(code)) {
            return DANCING_SCRIPT;
        } else if (LOBSTER_TWO.mFontCode.equals(code)) {
            return LOBSTER_TWO;
        } else if (PRESS_START_2P.mFontCode.equals(code)) {
            return PRESS_START_2P;
        } else {
            return DEFAULT;
        }
    }

    public boolean hasBoldVersion() {
        return mHasBoldVersion;
    }

    public boolean hasItalicVersion() {
        return mHasItalicVersion;
    }

    public boolean hasBoldItalicVersion() {
        return mHasBoldItalicVersion;
    }

    public String getFontCode() {
        return mFontCode;
    }

    public Typeface getTypeface(Context context, int style) {
        if (mFontCode == "default") {
            return Typeface.create(Typeface.DEFAULT, style);
        } else {
            return FontCache.getFont(context, getFontPath(style));
        }
    }

    private String getFontPath(int style) {
        StringBuilder builder = new StringBuilder("fonts/");
        builder.append(mFontCode);
        switch (style) {
            case Typeface.BOLD:
                builder.append("-bold");
                break;
            case Typeface.ITALIC:
                builder.append("-italic");
                break;
            case Typeface.BOLD_ITALIC:
                builder.append("-bolditalic");
                break;
            default:
                break;
        }
        return builder.append(".ttf").toString();
    }

    @Override
    public String toString() {
        return mDisplayName;
    }

}
