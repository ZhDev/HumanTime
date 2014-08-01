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

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

/**
 * <code>FontAdapter</code> is an ArrayAdapter that populates with an array of available fonts.
 * Each element will give a preview of the font with its name.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class FontAdapter extends ArrayAdapter<CharSequence> {

    public FontAdapter(Context context, int resource) {
        super(context, resource);
        addAll(context.getResources().getTextArray(R.array.text_fonts));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        String fontPath;
        switch (position) {
            case 0:
                fontPath = null;
                break;
            case 1:
                fontPath = "fonts/crafty-girls.ttf";
                break;
            case 2:
                fontPath = "fonts/dancing-script.ttf";
                break;
            case 3:
                fontPath = "fonts/lobster-two.ttf";
                break;
            case 4:
                fontPath = "fonts/press-start-2p.ttf";
                break;
            default:
                fontPath = null;
                break;
        }
        Typeface typeface;
        if (fontPath != null) {
            typeface = FontCache.getFont(view.getContext(), fontPath);
        } else {
            typeface = Typeface.DEFAULT;
        }
        ((TextView) view).setTypeface(typeface);

        return view;
    }
}
