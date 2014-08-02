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

import net.zhdev.wear.humantime.shared.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * <code>FontAdapter</code> is an ArrayAdapter that populates with an array of available fonts.
 * Each element will give a preview of the font with its name.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class FontAdapter extends ArrayAdapter<Font> {

    public FontAdapter(Context context, int resource) {
        super(context, resource);
        addAll(Font.DEFAULT, Font.CRAFTY_GIRLS, Font.DANCING_SCRIPT, Font.LOBSTER_TWO,
                Font.PRESS_START_2P);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setTypeface(getItem(position).getTypeface(view.getContext(), Typeface.NORMAL));

        return view;
    }
}
