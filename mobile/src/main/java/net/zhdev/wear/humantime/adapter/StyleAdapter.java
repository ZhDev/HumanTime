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

package net.zhdev.wear.humantime.adapter;

import net.zhdev.wear.humantime.R;
import net.zhdev.wear.humantime.shared.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * An adapter backed by an array of fixed <code>Style</code> objects. The elements of the adapter
 * can be disabled depending on the font selected because not all the fonts included contain all
 * the possible styles.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class StyleAdapter extends ArrayAdapter<StyleAdapter.Style> {

    private Font mSelectedFont;

    public StyleAdapter(Context context, int resource) {
        super(context, resource);
        Style[] styles = new Style[]{
                Style.getInstance(context, Typeface.NORMAL),
                Style.getInstance(context, Typeface.BOLD),
                Style.getInstance(context, Typeface.ITALIC),
                Style.getInstance(context, Typeface.BOLD_ITALIC)
        };
        addAll(styles);
        mSelectedFont = Font.DEFAULT;
    }

    public void setSelectedFont(Font selectedFont) {
        mSelectedFont = selectedFont;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setText(getItem(position).getRepresentation());
        boolean enabled;
        switch (position) {
            case 1: // BOLD
                enabled = mSelectedFont.hasBoldVersion();
                break;
            case 2: // ITALIC
                enabled = mSelectedFont.hasItalicVersion();
                break;
            case 3: // BOLD_ITALIC
                enabled = mSelectedFont.hasBoldItalicVersion();
                break;
            default:
                enabled = true;
        }
        view.setEnabled(enabled);
        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        switch (position) {
            case 1: // BOLD
                return mSelectedFont.hasBoldVersion();
            case 2: // ITALIC
                return mSelectedFont.hasItalicVersion();
            case 3: // BOLD_ITALIC
                return mSelectedFont.hasBoldItalicVersion();
            default:
                return true;
        }
    }

    public static class Style {

        private int mTextStyle;

        private CharSequence mRepresentation;

        private Style(int textStyle, CharSequence representation) {
            mTextStyle = textStyle;
            mRepresentation = representation;
        }

        public static Style getInstance(Context context, int style) {
            CharSequence representation;
            if (style == Typeface.NORMAL) {
                representation = context.getResources().getText(R.string.normal);
            } else if (style == Typeface.BOLD) {
                representation = context.getResources().getText(R.string.bold);
            } else if (style == Typeface.ITALIC) {
                representation = context.getResources().getText(R.string.italic);
            } else if (style == Typeface.BOLD_ITALIC) {
                representation = context.getResources().getText(R.string.bold_italic);
            } else {
                return null;
            }
            return new Style(style, representation);
        }

        public CharSequence getRepresentation() {
            return mRepresentation;
        }

        public int getTextStyle() {
            return mTextStyle;
        }

        @Override
        public String toString() {
            return mRepresentation.toString();
        }
    }
}
