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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Calendar;
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

    private boolean mAnimationsEnabled;

    private TimeConverter mTimeConverter;

    private int mTextCase;

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

    private void init(Context context) {
        // The View doesn't work well in edit mode, it's better to use tools:text in the layout
        // editor
        if (isInEditMode()) {
            return;
        }

        setText("");
        mAnimationsEnabled = true;
        mTimeConverter = new TimeConverter(context.getResources());
        mCurrentText = "";
        mAnimationIn = AnimationUtils.loadAnimation(context, R.anim.push_in_right);
        mAnimationOut = AnimationUtils.loadAnimation(context, R.anim.push_out_left);
        mAnimationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateText();
                startAnimation(mAnimationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTextCase = Constants.TEXT_CASE_NO_CAPS;
        createTime(mTimeZone);
    }

    private int countLines(String text) {
        if (text == null) {
            return 0;
        }
        return text.length() - text.replace("\n", "").length() + 1;
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

        String time = mTimeConverter
                .convertTime(mTime.get(Calendar.HOUR), mTime.get(Calendar.MINUTE))
                .replace(' ', '\n');

        // We assume that the time patterns in strings.xml are always in lowercase
        switch (mTextCase) {
            case Constants.TEXT_CASE_NO_CAPS:
                break;
            case Constants.TEXT_CASE_ALL_CAPS:
                time = time.toUpperCase();
                break;
            case Constants.TEXT_CASE_FIRST_CAP:
                time = time.substring(0, 1).toUpperCase() + time.substring(1);
                break;
        }

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
            if (mAnimationsEnabled) {
                startAnimation(mAnimationOut);
            } else {
                updateText();
            }
        }
    }

    /**
     * @see net.zhdev.wear.humantime.shared.Constants
     */
    public void setTextCase(int textCase) {
        mTextCase = textCase;
        if (mAttached) {
            onTimeChanged();
        }
    }

    /**
     * Updates the View with the new text stored, fixing the autofit proportions.
     */
    private void updateText() {
        int pastMaxLines = countLines(getText().toString());
        int newMaxLines = countLines(mCurrentText);
        if ((pastMaxLines != newMaxLines)) {
            if (pastMaxLines > newMaxLines) {
                setText(mCurrentText);
                setMaxLines(newMaxLines);
            } else {
                setMaxLines(newMaxLines);
                setText(mCurrentText);
            }
        } else {
            setText(mCurrentText);
        }
    }

    public void setAnimationsEnabled(boolean animationsEnabled) {
        mAnimationsEnabled = animationsEnabled;
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
}
