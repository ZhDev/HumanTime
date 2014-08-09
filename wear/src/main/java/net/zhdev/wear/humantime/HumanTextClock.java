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

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;

/**
 * <code>HumanTextClock</code> is a clock that displays the time in a "human way", representing the
 * time as a text string such as "quarter to
 * five".
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class HumanTextClock extends AutoFitTextClock {

    private TimeConverter mTimeConverter;

    private int mTextCase;

    public HumanTextClock(Context context) {
        this(context, null);
    }

    public HumanTextClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HumanTextClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTimeConverter = new TimeConverter(context.getResources());
        mTextCase = Constants.TEXT_CASE_NO_CAPS;
    }

    private static String setCorrectCase(String text, int textCase) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Invalid text");
        }
        switch (textCase) {
            case Constants.TEXT_CASE_NO_CAPS:
                return text.toLowerCase();
            case Constants.TEXT_CASE_ALL_CAPS:
                return text.toUpperCase();
            case Constants.TEXT_CASE_FIRST_CAP:
                return text.substring(0, 1).toUpperCase() + text.substring(1);
            default:
                return text;
        }
    }

    @Override
    protected String getDateTimeText(Calendar calendar) {
        String time = mTimeConverter
                .convertTime(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE))
                .replace(' ', '\n');

        return setCorrectCase(time, mTextCase);
    }

    /**
     * @see net.zhdev.wear.humantime.shared.Constants
     */
    public void setTextCase(int textCase) {
        mTextCase = textCase;
    }


}
