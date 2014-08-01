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

import com.squareup.phrase.Phrase;

import android.content.res.Resources;

import java.util.Locale;

/**
 * <code>TimeConverter</code> converts a time, given in hours and minutes to its textual
 * representation in a human readable way.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class TimeConverter {

    private Resources mResources;

    private Locale mLocale;

    public TimeConverter(Resources resources) {
        if (resources == null) {
            throw new IllegalArgumentException("Null parameters");
        }
        mResources = resources;
        mLocale = resources.getConfiguration().locale;
    }

    private static int roundToNearestMultipleOfFive(int number) {
        int temp = number % 5;
        int floor = number - temp;

        return temp < 3 ? floor : floor + 5;
    }

    /**
     * Converts a time, given in hours and minutes, to a textual representation rounded to 5-minute
     * steps
     *
     * @param hours   the time hours
     * @param minutes the time minutes
     * @return the textual representation of the time
     */
    public String convertTime(int hours, int minutes) {
        hours = hours % 12;
        if (hours == 0) {
            hours = 12;
        }
        minutes = roundToNearestMultipleOfFive(minutes);
        if (minutes > 30) {
            hours = (hours % 12) + 1;
            if (minutes == 60) {
                minutes = 0;
            }
        } else if (mLocale.getLanguage().equals(Locale.GERMAN.getLanguage()) && minutes >= 20
                && minutes <= 30) {
            // German time uses a different scheme, after the 20 minute mark the time is subtracted
            // from the following hour, so we have to counter for that.
            hours = (hours % 12) + 1;
        }

        String textHours = getHoursText(hours);
        // German time changes the word for the number 1 if it's o'clock
        if (hours == 1 && minutes == 0
                && mLocale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
            textHours = textHours.substring(0, textHours.length() - 1);
        }

        Phrase phrase = null;
        switch (minutes) {
            case 0:
                phrase = Phrase.from(mResources, R.string.o_clock);
                break;
            case 5:
                phrase = Phrase.from(mResources, R.string.five_past);
                break;
            case 10:
                phrase = Phrase.from(mResources, R.string.ten_past);
                break;
            case 15:
                phrase = Phrase.from(mResources, R.string.quarter_past);
                break;
            case 20:
                phrase = Phrase.from(mResources, R.string.twenty_past);
                break;
            case 25:
                phrase = Phrase.from(mResources, R.string.twenty_five_past);
                break;
            case 30:
                phrase = Phrase.from(mResources, R.string.half_past);
                break;
            case 35:
                phrase = Phrase.from(mResources, R.string.twenty_five_to);
                break;
            case 40:
                phrase = Phrase.from(mResources, R.string.twenty_to);
                break;
            case 45:
                phrase = Phrase.from(mResources, R.string.quarter_to);
                break;
            case 50:
                phrase = Phrase.from(mResources, R.string.ten_to);
                break;
            case 55:
                phrase = Phrase.from(mResources, R.string.five_to);
                break;
            default:
                return mResources.getString(R.string.nan);

        }

        return phrase.put("hours", textHours).format().toString();
    }

    private String getHoursText(int hours) {
        String text;
        switch (hours) {
            case 1:
                text = mResources.getString(R.string.one);
                break;
            case 2:
                text = mResources.getString(R.string.two);
                break;
            case 3:
                text = mResources.getString(R.string.three);
                break;
            case 4:
                text = mResources.getString(R.string.four);
                break;
            case 5:
                text = mResources.getString(R.string.five);
                break;
            case 6:
                text = mResources.getString(R.string.six);
                break;
            case 7:
                text = mResources.getString(R.string.seven);
                break;
            case 8:
                text = mResources.getString(R.string.eight);
                break;
            case 9:
                text = mResources.getString(R.string.nine);
                break;
            case 10:
                text = mResources.getString(R.string.ten);
                break;
            case 11:
                text = mResources.getString(R.string.eleven);
                break;
            case 12:
                text = mResources.getString(R.string.twelve);
                break;
            default:
                return mResources.getString(R.string.nan);
        }

        return this.mResources.getQuantityString(R.plurals.hours, hours, text);
    }
}