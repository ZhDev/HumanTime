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
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * <code>ShortDateClock</code> is a clock than only provides a short, localized version of the
 * current date. It only includes the day of the week and the day of the month.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class ShortDateClock extends AutoFitTextClock {

    private SimpleDateFormat mFormatter;

    public ShortDateClock(Context context) {
        this(context, null);
    }

    public ShortDateClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortDateClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Locale locale = context.getResources().getConfiguration().locale;
        String datePattern = DateFormat.getBestDateTimePattern(locale, "cccd");
        mFormatter = new SimpleDateFormat(datePattern, locale);
    }

    protected String getDateTimeText(Calendar calendar) {
        return mFormatter.format(calendar.getTime());
    }

}
