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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import me.grantland.widget.AutofitTextView;

/**
 * <p><code>HumanTextClock</code> is a modified version of {@link android.widget.TextClock}. It can
 * display the time in a "human way", representing the time as a text string such as "quarter to
 * five". The widget listens for changes in the time and the time zone.</p>
 *
 * <p>It inherits from {@link me.grantland.widget.AutofitTextView} instead of
 * <code>TextView</code>,
 * so it can fit automatically within given bounds.</p>
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class HumanTextClock extends AutofitTextView {

    private boolean mAttached;

    private String mCurrentText;

    private Animation mAnimationIn;

    private Animation mAnimationOut;

    private Calendar mTime;

    private String mTimeZone;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra("time-zone");
                createTime(timeZone);
            }
            onTimeChanged();
        }
    };

    /**
     * Creates a new clock using the default patterns for the current locale.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    @SuppressWarnings("UnusedDeclaration")
    public HumanTextClock(Context context) {
        this(context, null);
    }

    /**
     * Creates a new clock inflated from XML. This object's properties are
     * intialized from the attributes specified in XML.
     *
     * This constructor uses a default style of 0, so the only attribute values
     * applied are those in the Context's Theme and the given AttributeSet.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view
     */
    @SuppressWarnings("UnusedDeclaration")
    public HumanTextClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new clock inflated from XML. This object's properties are
     * intialized from the attributes specified in XML.
     *
     * @param context  The Context the view is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view
     * @param defStyle The default style to apply to this view. If 0, no style
     *                 will be applied (beyond what is included in the theme). This may
     *                 either be an attribute resource, whose value will be retrieved
     *                 from the current theme, or an explicit style resource
     */
    public HumanTextClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private static int roundToNearestMultipleOfFive(int number) {
        int temp = number % 5;
        int floor = number - temp;

        return temp < 3 ? floor : floor + 5;
    }

    private void init(Context context) {
        // The View doesn't work well in edit mode, it's better to use tools:text in the layout
        // editor
        if (isInEditMode()) {
            return;
        }
        setText("");
        mCurrentText = "";
        mAnimationIn = AnimationUtils.loadAnimation(context, R.anim.push_in_right);
        mAnimationOut = AnimationUtils.loadAnimation(context, R.anim.push_out_left);
        mAnimationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setText(mCurrentText);
                startAnimation(mAnimationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        createTime(mTimeZone);
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            mTime = Calendar.getInstance();
        }
    }

    /**
     * Indicates which time zone is currently used by this view.
     *
     * @return The ID of the current time zone or null if the default time zone,
     * as set by the user, must be used
     * @see TimeZone
     * @see java.util.TimeZone#getAvailableIDs()
     * @see #setTimeZone(String)
     */
    public String getTimeZone() {
        return mTimeZone;
    }

    /**
     * Sets the specified time zone to use in this clock. When the time zone
     * is set through this method, system time zone changes (when the user
     * sets the time zone in settings for instance) will be ignored.
     *
     * @param timeZone The desired time zone's ID as specified in {@link TimeZone}
     *                 or null to user the time zone specified by the user
     *                 (system time zone)
     * @see #getTimeZone()
     * @see java.util.TimeZone#getAvailableIDs()
     * @see TimeZone#getTimeZone(String)
     */
    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;

        createTime(timeZone);
        onTimeChanged();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;

            registerReceiver();
            createTime(mTimeZone);
            if (isInEditMode()) {
                return;
            }
            onTimeChanged();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            unregisterReceiver();
            mAttached = false;
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    /**
     * Updates the time. Every there is a time change the time is transformed into its textual
     * representation for the current locale and the <code>View</code> is updated.
     */
    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        String time = null;
        int hours = mTime.get(Calendar.HOUR);
        int minutes = roundToNearestMultipleOfFive(mTime.get(Calendar.MINUTE));

        if (minutes > 30) {
            hours = (hours % 12) + 1;
            if (minutes == 60) {
                minutes = 0;
            }
        }

        if (hours == 0) {
            hours = 12;
        }

        Phrase phrase = null;

        if (minutes == 0) {
            phrase = Phrase.from(getResources(), R.string.o_clock);
        } else if (minutes == 15) {
            phrase = Phrase.from(getResources(), R.string.quarter_past);
        } else if (minutes == 30) {
            if (getResources().getConfiguration().locale.equals(Locale.GERMAN)) {
                hours++;
            }
            phrase = Phrase.from(getResources(), R.string.half_past);
        } else if (minutes == 45) {
            phrase = Phrase.from(getResources(), R.string.quarter_to);
        } else if (minutes < 30) {
            phrase = Phrase.from(getResources(), R.string.any_past);
        } else {
            minutes = 60 - minutes;
            phrase = Phrase.from(getResources(), R.string.any_to);
        }

        time = phrase.put("hours", getHoursText(hours))
                .putOptional("minutes", getMinutesText(minutes))
                .format()
                .toString()
                .replace(' ', '\n');

        setTime(time);
    }

    /**
     * Updates the time in the <code>View</code> if it is different for the one currently being
     * displayed. It applies an animation for the transition.
     *
     * @param time the text representing the new time
     */
    private void setTime(String time) {
        if (!time.equals(mCurrentText)) {
            mCurrentText = time;
            startAnimation(mAnimationOut);
        }
    }

    private String getHoursText(int hours) {
        Resources res = getResources();
        String text = null;
        switch (hours) {
            case 1:
                text = res.getString(R.string.one);
                break;
            case 2:
                text = res.getString(R.string.two);
                break;
            case 3:
                text = res.getString(R.string.three);
                break;
            case 4:
                text = res.getString(R.string.four);
                break;
            case 5:
                text = res.getString(R.string.five);
                break;
            case 6:
                text = res.getString(R.string.six);
                break;
            case 7:
                text = res.getString(R.string.seven);
                break;
            case 8:
                text = res.getString(R.string.eight);
                break;
            case 9:
                text = res.getString(R.string.nine);
                break;
            case 10:
                text = res.getString(R.string.ten);
                break;
            case 11:
                text = res.getString(R.string.eleven);
                break;
            case 12:
                text = res.getString(R.string.twelve);
                break;
        }
        if (isInEditMode()) {
            return text;
        }
        return res.getQuantityString(R.plurals.hours, hours, text);
    }

    private String getMinutesText(int minutes) {
        Resources res = getResources();
        switch (minutes) {
            case 5:
                return res.getString(R.string.five);
            case 10:
                return res.getString(R.string.ten);
            case 20:
                return res.getString(R.string.twenty);
            case 25:
                return res.getString(R.string.twenty_five);
            default:
                return res.getString(R.string.nan);
        }
    }
}