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

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.tundem.aboutlibraries.Libs;
import com.tundem.aboutlibraries.ui.LibsActivity;

import net.zhdev.humantime.shared.Constants;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * MainActivity implements a settings screen where the user can select different aspects of how the
 * companion watch face app is displayed.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class MainActivity extends WearApiActivity
        implements ColorPickerDialogFragment.ColorPickerDialogListener,
        PositionPickerDialogFragment.PositionPickerDialogListener {

    private static final String DIALOG_BACKGROUND_COLOR_PICKER_TAG = "background";

    private static final String DIALOG_TEXT_COLOR_PICKER_TAG = "text";

    private static final String DIALOG_POSITION_PICKER_TAG = "position";

    private static final int REQUEST_PICK_IMAGE = 0;

    private static final int REQUEST_CROP_IMAGE = 1;

    private SharedPreferences mSharedPreferences;

    private TextView mTextPreview;

    private Spinner mSize;

    private Spinner mCaps;

    private CheckBox mItalic;

    private CheckBox mBold;

    private Switch mShadow;

    private ImageButton mBackgroundColor;

    private ImageButton mBackgroundImage;

    private ImageButton mTextColor;

    private ImageButton mPosition;

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_activity_main);

        mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        mTextPreview = (TextView) findViewById(R.id.text_preview);

        mBackgroundColor = (ImageButton) findViewById(R.id.bt_background_color);
        mBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    // Adding an onClickListener to an ImageButton forces the clickable state to always
                    // be true, even if the view is disabled
                    showColorPicker(DIALOG_BACKGROUND_COLOR_PICKER_TAG);
                }
            }
        });

        mBackgroundImage = (ImageButton) findViewById(R.id.bt_background_image);
        mBackgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    // Adding an onClickListener to an ImageButton forces the clickable state to always
                    // be true, even if the view is disabled
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                }
            }
        });

        mTextColor = (ImageButton) findViewById(R.id.bt_text_color);
        mTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding an onClickListener to an ImageButton forces the clickable state to always
                // be true, even if the view is disabled
                if (v.isEnabled()) {
                    showColorPicker(DIALOG_TEXT_COLOR_PICKER_TAG);
                }
            }
        });

        mSize = (Spinner) findViewById(R.id.sp_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.text_sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSize.setAdapter(adapter);

        mItalic = (CheckBox) findViewById(R.id.cb_italic);
        mBold = (CheckBox) findViewById(R.id.cb_bold);

        mShadow = (Switch) findViewById(R.id.sw_shadow);

        mPosition = (ImageButton) findViewById(R.id.bt_position);
        mPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding an onClickListener to an ImageButton forces the clickable state to always
                // be true, even if the view is disabled
                if (v.isEnabled()) {
                    PositionPickerDialogFragment fragment = PositionPickerDialogFragment
                            .newInstance();
                    fragment.show(getFragmentManager(), DIALOG_POSITION_PICKER_TAG);
                }
            }
        });

        mCaps = (Spinner) findViewById(R.id.sp_caps);
        adapter = ArrayAdapter.createFromResource(this, R.array.text_caps,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCaps.setAdapter(adapter);

        initWithStoredValues();

        setElementsEnabled(false);

        // Set some of the listeners after setting the restored values to prevent their side calls
        // when their values are initially set

        mSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float size;
                switch (position) {
                    case 0:
                        size = Constants.TEXT_SIZE_LARGE;
                        break;
                    case 1:
                        size = Constants.TEXT_SIZE_MEDIUM;
                        break;
                    case 2:
                        size = Constants.TEXT_SIZE_SMALL;
                        break;
                    case 3:
                        size = Constants.TEXT_SIZE_EXTRA_SMALL;
                        break;
                    default:
                        size = Constants.TEXT_SIZE_LARGE;
                        break;
                }
                setTextSize(size, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CompoundButton.OnCheckedChangeListener checkedListener
                = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTextStyle(mBold.isChecked(), mItalic.isChecked(), true);
            }
        };

        mItalic.setOnCheckedChangeListener(checkedListener);
        mBold.setOnCheckedChangeListener(checkedListener);

        mShadow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTextShadow(isChecked, true);
            }
        });

        mCaps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTextCase(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            switch (requestCode) {
                case REQUEST_PICK_IMAGE: // Open the image picker
                    Intent intent = new Intent(this, ImageCropperActivity.class);
                    intent.setData(imageUri);
                    startActivityForResult(intent, REQUEST_CROP_IMAGE);
                    break;
                case REQUEST_CROP_IMAGE: // The image has been picked, open the cropper
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), inputStream);
                        mTextPreview.setBackground(drawable);
                        mSharedPreferences.edit().putInt(Constants.BACKGROUND_TYPE_KEY,
                                Constants.BACKGROUND_TYPE_IMAGE).apply();
                        putData(Constants.BACKGROUND_ASSET_PATH, Constants.BACKGROUND_ASSET_KEY,
                                createAssetFromBitmap(drawable.getBitmap()));
                        deleteData(Constants.BACKGROUND_COLOR_PATH);
                    } catch (FileNotFoundException e) {
                        Log.w("Human Time",
                                "The background file was created, but it can't be found");
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        setElementsEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        setElementsEnabled(false);
    }

    /**
     * Sets the enabled state of all the views in the activity that receive direct interaction from
     * the user.
     *
     * @param enabled true if the views should be enabled, false otherwise
     */
    private void setElementsEnabled(boolean enabled) {
        setImageButtonEnabled(mBackgroundColor, enabled);
        setImageButtonEnabled(mBackgroundImage, enabled);
        setImageButtonEnabled(mTextColor, enabled);
        setImageButtonEnabled(mPosition, enabled);
        mSize.setEnabled(enabled);
        mItalic.setEnabled(enabled);
        mBold.setEnabled(enabled);
        mShadow.setEnabled(enabled);
        mCaps.setEnabled(enabled);
    }

    /**
     * Sets the enabled state of an <code>ImageButton</code>, simulating the behavior of a state
     * list drawable.
     *
     * @param imageButton the button that will change its state
     * @param enabled     true if the button should be enabled, false otherwise
     * @see android.widget.ImageButton
     * @see #onCreate(android.os.Bundle)
     */
    public void setImageButtonEnabled(ImageButton imageButton, boolean enabled) {
        Drawable drawable = imageButton.getDrawable();
        if (enabled) {
            drawable.clearColorFilter();
        } else {
            drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
        imageButton.setEnabled(enabled);
    }

    /**
     * Initializes all the views using the default values if it's the first time being run or
     * retrieving the values stored.
     */
    private void initWithStoredValues() {
        int backgroundType = mSharedPreferences
                .getInt(Constants.BACKGROUND_TYPE_KEY, Constants.BACKGROUND_TYPE_COLOR);
        Drawable drawable = null;
        int color;
        if (backgroundType == Constants.BACKGROUND_TYPE_COLOR) {
            color = mSharedPreferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                    getResources().getColor(android.R.color.holo_blue_dark));
            drawable = new ColorDrawable(color);
        } else if (backgroundType == Constants.BACKGROUND_TYPE_IMAGE) {
            try {
                InputStream inputStream = openFileInput(Constants.BACKGROUND_ASSET_FILE_NAME);
                drawable = new BitmapDrawable(getResources(), inputStream);
            } catch (FileNotFoundException e) {
                color = mSharedPreferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                        getResources().getColor(android.R.color.holo_blue_dark));
                drawable = new ColorDrawable(color);
            }
        }
        mTextPreview.setBackground(drawable);

        color = mSharedPreferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        mTextPreview.setTextColor(color);

        int textStyle = mSharedPreferences
                .getInt(Constants.TEXT_STYLE_KEY, Constants.TEXT_STYLE_BOLD);
        boolean bold;
        boolean italic;
        switch (textStyle) {
            case Constants.TEXT_STYLE_BOLD_ITALIC:
                bold = true;
                italic = true;
                break;
            case Constants.TEXT_STYLE_BOLD:
                bold = true;
                italic = false;
                break;
            case Constants.TEXT_STYLE_ITALIC:
                bold = false;
                italic = true;
                break;
            default:
                bold = false;
                italic = false;
                break;
        }
        setTextStyle(bold, italic, false);
        mBold.setChecked(bold);
        mItalic.setChecked(italic);

        boolean textShadow = mSharedPreferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
        setTextShadow(textShadow, false);
        mShadow.setChecked(textShadow);

        float textSize = mSharedPreferences
                .getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
        int selection = 0;
        if (textSize == Constants.TEXT_SIZE_LARGE) {
            selection = 0;
        } else if (textSize == Constants.TEXT_SIZE_MEDIUM) {
            selection = 1;
        } else if (textSize == Constants.TEXT_SIZE_SMALL) {
            selection = 2;
        } else if (textSize == Constants.TEXT_SIZE_EXTRA_SMALL) {
            selection = 3;
        }
        mSize.setSelection(selection);
        setTextSize(textSize, false);

        int textPosition = mSharedPreferences
                .getInt(Constants.TEXT_POSITION_KEY, Constants.TEXT_POSITION_CENTER_CENTER);
        ((LevelListDrawable) mPosition.getDrawable()).setLevel(textPosition);
        setTextPosition(textPosition, false);

        int textCase = mSharedPreferences
                .getInt(Constants.TEXT_CASE_KEY, Constants.TEXT_CASE_NO_CAPS);
        mCaps.setSelection(textCase);
        setTextCase(textCase, false);
    }

    /**
     * Shows a dialog that lets the user pick a color to be used as the background of an element.
     *
     * @param tag the text representing the element that will get its color changed
     */
    private void showColorPicker(String tag) {
        int oldColor = 0;
        if (DIALOG_BACKGROUND_COLOR_PICKER_TAG.equals(tag)) {
            oldColor = mSharedPreferences.getInt(Constants.BACKGROUND_COLOR_KEY,
                    getResources().getColor(android.R.color.holo_blue_dark));
        } else if (DIALOG_TEXT_COLOR_PICKER_TAG.equals(tag)) {
            oldColor = mSharedPreferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        }
        ColorPickerDialogFragment fragment = ColorPickerDialogFragment.newInstance(oldColor);
        fragment.show(getFragmentManager(), tag);
    }

    /**
     * Sets the text style that should be applied both in the preview watch face and syncing the
     * data with the Wearable Data Layer.
     *
     * @param bold    true if the text should be displayed using a bold typeface, false otherwise
     * @param italic  true if the text should be displayed using an italic typeface, false
     *                otherwise
     * @param putData true if the action should be synced with the Wearable Data Layer, false
     *                otherwise
     */
    private void setTextStyle(boolean bold, boolean italic, boolean putData) {
        String path = Constants.TEXT_STYLE_PATH;
        String key = Constants.TEXT_STYLE_KEY;
        int style;
        if (bold && italic) {
            mTextPreview.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            style = Constants.TEXT_STYLE_BOLD_ITALIC;
        } else if (bold) {
            mTextPreview.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            style = Constants.TEXT_STYLE_BOLD;
        } else if (italic) {
            mTextPreview.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            style = Constants.TEXT_STYLE_ITALIC;
        } else {
            mTextPreview.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            style = Constants.TEXT_STYLE_NORMAL;
        }
        if (putData) {
            putData(path, key, style);
        }
    }

    /**
     * Sets the enabled state of the text shadow, both in the preview watch face and syncing the
     * data with the Wearable Data Layer.
     *
     * @param enableShadow true if the shadow is enabled, false otherwise
     * @param putData      true if the action should be synced with the Wearable Data Layer, false
     *                     otherwise
     */
    private void setTextShadow(boolean enableShadow, boolean putData) {
        if (enableShadow) {
            mTextPreview.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
        } else {
            mTextPreview.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
        }
        if (putData) {
            putData(Constants.TEXT_SHADOW_PATH, Constants.TEXT_SHADOW_KEY, enableShadow);
        }
    }

    /**
     * Sets the size of the text, both in the preview watch face and syncing the data with the
     * Wearable Data Layer.
     *
     * @param size    the dimension, in SP
     * @param putData true if the action should be synced with the Wearable Data Layer, false
     *                otherwise
     */
    private void setTextSize(float size, boolean putData) {
        mTextPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (putData) {
            putData(Constants.TEXT_SIZE_PATH, Constants.TEXT_SIZE_KEY, size);
        }
    }

    /**
     * Sets the position of the text on the screen, both in the preview watch face and syncing the
     * data with the Wearable Data Layer.
     *
     * @param position the position where the text should be aligned
     * @param putData  true if the action should be synced with the Wearable Data Layer, false
     *                 otherwise
     */
    private void setTextPosition(int position, boolean putData) {
        int gravity;
        switch (position) {
            case Constants.TEXT_POSITION_TOP_LEFT:
                gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_TOP_CENTER:
                gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case Constants.TEXT_POSITION_TOP_RIGHT:
                gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case Constants.TEXT_POSITION_CENTER_LEFT:
                gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_CENTER_CENTER:
                gravity = Gravity.CENTER;
                break;
            case Constants.TEXT_POSITION_CENTER_RIGHT:
                gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                break;
            case Constants.TEXT_POSITION_BOTTOM_LEFT:
                gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case Constants.TEXT_POSITION_BOTTOM_CENTER:
                gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case Constants.TEXT_POSITION_BOTTOM_RIGHT:
                gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            default:
                gravity = Gravity.CENTER;
                break;
        }
        mTextPreview.setGravity(gravity);
        if (putData) {
            putData(Constants.TEXT_POSITION_PATH, Constants.TEXT_POSITION_KEY, position);
        }
    }

    /**
     * Sets the text case to be lowercase, uppercase or only the first letter should be capitalized
     *
     * @param textCase the text case from {@link net.zhdev.humantime.shared.Constants}
     * @param putData  true if the action should be synced with the Wearable Data Layer, false
     *                 otherwise
     */
    private void setTextCase(int textCase, boolean putData) {
        String newText = mTextPreview.getText().toString();
        switch (textCase) {
            case Constants.TEXT_CASE_NO_CAPS:
                newText = newText.toLowerCase();
                break;
            case Constants.TEXT_CASE_ALL_CAPS:
                newText = newText.toUpperCase();
                break;
            case Constants.TEXT_CASE_FIRST_CAP:
                newText = newText.substring(0, 1).toUpperCase() + newText.substring(1)
                        .toLowerCase();
                break;
            default:
                newText = newText.toLowerCase();
                break;
        }
        mTextPreview.setText(newText);
        if (putData) {
            putData(Constants.TEXT_CASE_PATH, Constants.TEXT_CASE_KEY, textCase);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent intent = new Intent(getApplicationContext(), LibsActivity.class);
            intent.putExtra(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));
            intent.putExtra(Libs.BUNDLE_VERSION, true);
            intent.putExtra(Libs.BUNDLE_LICENSE, true);
            intent.putExtra(Libs.BUNDLE_TITLE, getString(R.string.about));

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorPicked(DialogFragment dialog, int color) {
        String tag = dialog.getTag();
        if (DIALOG_BACKGROUND_COLOR_PICKER_TAG.equals(tag)) {
            mSharedPreferences.edit().putInt(Constants.BACKGROUND_TYPE_KEY,
                    Constants.BACKGROUND_TYPE_COLOR).apply();
            mTextPreview.setBackgroundColor(color);
            putData(Constants.BACKGROUND_COLOR_PATH, Constants.BACKGROUND_COLOR_KEY, color);
            deleteData(Constants.BACKGROUND_ASSET_PATH);
        } else if (DIALOG_TEXT_COLOR_PICKER_TAG.equals(tag)) {
            mTextPreview.setTextColor(color);
            putData(Constants.TEXT_COLOR_PATH, Constants.TEXT_COLOR_KEY, color);
        }
    }

    @Override
    public void onPositionPicked(DialogFragment dialog, int position) {
        LevelListDrawable levelListDrawable = (LevelListDrawable) mPosition.getDrawable();
        levelListDrawable.setLevel(position);
        setTextPosition(position, true);
    }

    /**
     * Puts a key/value pair at the given path inside Google Play services Wearable Data Layer and
     * stores it in the app shared preferences.
     *
     * @param path  the path where tha data will be stored
     * @param key   the key referencing the data
     * @param value the value of the data
     */
    private void putData(String path, String key, Object value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap dataMap = putDataMapRequest.getDataMap();
        if (value instanceof Integer) {
            int casted = (Integer) value;
            dataMap.putInt(key, casted);
            editor.putInt(key, casted);
        } else if (value instanceof Boolean) {
            boolean casted = (Boolean) value;
            dataMap.putBoolean(key, casted);
            editor.putBoolean(key, casted);
        } else if (value instanceof Asset) {
            dataMap.putAsset(key, (Asset) value);
            editor.putLong(key, System.currentTimeMillis());
        } else if (value instanceof Float) {
            float casted = (Float) value;
            dataMap.putFloat(key, casted);
            editor.putFloat(key, casted);
        } else {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        editor.apply();
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(getGoogleApiClient(), request);
    }

    private void deleteData(String path) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        Wearable.DataApi.deleteDataItems(getGoogleApiClient(), putDataMapRequest.getUri());
    }
}
