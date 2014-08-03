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

import net.zhdev.wear.humantime.shared.Constants;
import net.zhdev.wear.humantime.shared.Font;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import me.grantland.widget.AutofitTextView;

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

    private AutofitTextView mTextPreview;

    private Spinner mTextSizeSpinner;

    private Spinner mTextCaseSpinner;

    private Switch mShadowSwitch;

    private ImageButton mBackgroundColorButton;

    private ImageButton mBackgroundImageButton;

    private ImageButton mTextColorButton;

    private ImageButton mPositionButton;

    private Spinner mTextFontSpinner;

    private Spinner mStyleSpinner;

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

        mTextPreview = (AutofitTextView) findViewById(R.id.text_preview);

        mBackgroundColorButton = (ImageButton) findViewById(R.id.bt_background_color);
        mBackgroundColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding an onClickListener to an ImageButton forces the clickable state to always
                // be true, even if the view is disabled
                if (v.isEnabled()) {
                    showColorPicker(DIALOG_BACKGROUND_COLOR_PICKER_TAG);
                }
            }
        });

        mBackgroundImageButton = (ImageButton) findViewById(R.id.bt_background_image);
        mBackgroundImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding an onClickListener to an ImageButton forces the clickable state to always
                // be true, even if the view is disabled
                if (v.isEnabled()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                }
            }
        });

        mTextColorButton = (ImageButton) findViewById(R.id.bt_text_color);
        mTextColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding an onClickListener to an ImageButton forces the clickable state to always
                // be true, even if the view is disabled
                if (v.isEnabled()) {
                    showColorPicker(DIALOG_TEXT_COLOR_PICKER_TAG);
                }
            }
        });

        mTextSizeSpinner = (Spinner) findViewById(R.id.sp_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.text_sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTextSizeSpinner.setAdapter(adapter);

        mTextSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Used to prevent the callback being fired after onCreate
             */
            private boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                } else {
                    float textSize;
                    switch (position) {
                        case 0:
                            textSize = Constants.TEXT_SIZE_LARGE;
                            break;
                        case 1:
                            textSize = Constants.TEXT_SIZE_MEDIUM;
                            break;
                        case 2:
                            textSize = Constants.TEXT_SIZE_SMALL;
                            break;
                        case 3:
                            textSize = Constants.TEXT_SIZE_EXTRA_SMALL;
                            break;
                        default:
                            textSize = Constants.TEXT_SIZE_LARGE;
                            break;
                    }
                    storePreference(Constants.TEXT_SIZE_KEY, textSize);
                    syncData(Constants.TEXT_SIZE_PATH, Constants.TEXT_SIZE_KEY, textSize);
                    loadTextSizePreview(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mStyleSpinner = (Spinner) findViewById(R.id.sp_style);
        final StyleAdapter styleAdapter = new StyleAdapter(this,
                android.R.layout.simple_spinner_item);
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStyleSpinner.setAdapter(styleAdapter);

        mStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Used to prevent the callback being fired after onCreate
             */
            private boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                } else {
                    int style = styleAdapter.getItem(position).getTextStyle();
                    storePreference(Constants.TEXT_STYLE_KEY, style);
                    syncData(Constants.TEXT_STYLE_PATH, Constants.TEXT_STYLE_KEY, style);
                    loadTextStyleAndFontPreview(false, false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mShadowSwitch = (Switch) findViewById(R.id.sw_shadow);
        mShadowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Used to prevent the callback being fired after onCreate
             */
            private boolean firstCall = true;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (firstCall) {
                    firstCall = false;
                } else {
                    storePreference(Constants.TEXT_SHADOW_KEY, isChecked);
                    syncData(Constants.TEXT_SHADOW_PATH, Constants.TEXT_SHADOW_KEY, isChecked);
                    loadTextShadowPreview(false);
                }
            }
        });

        mPositionButton = (ImageButton) findViewById(R.id.bt_position);
        mPositionButton.setOnClickListener(new View.OnClickListener() {
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

        mTextCaseSpinner = (Spinner) findViewById(R.id.sp_caps);
        adapter = ArrayAdapter.createFromResource(this, R.array.text_caps,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTextCaseSpinner.setAdapter(adapter);
        mTextCaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Used to prevent the callback being fired after onCreate
             */
            private boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                } else {
                    // The position matches the fields in Constants
                    storePreference(Constants.TEXT_CASE_KEY, position);
                    syncData(Constants.TEXT_CASE_PATH, Constants.TEXT_CASE_KEY, position);
                    loadTextCasePreview(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTextFontSpinner = (Spinner) findViewById(R.id.sp_font);
        final FontAdapter fontAdapter = new FontAdapter(this, android.R.layout.simple_spinner_item);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTextFontSpinner.setAdapter(fontAdapter);
        mTextFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Used to prevent the callback being fired after onCreate
             */
            private boolean firstCall = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstCall) {
                    firstCall = false;
                } else {
                    // Reset the style because not all custom fonts have them
                    storePreference(Constants.TEXT_STYLE_KEY, Typeface.NORMAL);
                    syncData(Constants.TEXT_STYLE_PATH, Constants.TEXT_STYLE_KEY, Typeface.NORMAL);
                    String fontCode = fontAdapter.getItem(position).getFontCode();
                    storePreference(Constants.TEXT_FONT_KEY, fontCode);
                    syncData(Constants.TEXT_FONT_PATH, Constants.TEXT_FONT_KEY, fontCode);
                    loadTextStyleAndFontPreview(true, false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initWithStoredValues();
        setElementsEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            switch (requestCode) {
                case REQUEST_PICK_IMAGE: // An image has been picked, open the cropper
                    Intent intent = new Intent(this, ImageCropperActivity.class);
                    intent.setData(imageUri);
                    startActivityForResult(intent, REQUEST_CROP_IMAGE);
                    break;
                case REQUEST_CROP_IMAGE: // The image has been cropped, open the cropper
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        storePreference(Constants.BACKGROUND_TYPE_KEY,
                                Constants.BACKGROUND_TYPE_IMAGE);
                        syncData(Constants.BACKGROUND_ASSET_PATH, Constants.BACKGROUND_ASSET_KEY,
                                createAssetFromBitmap(bitmap));
                        deleteData(Constants.BACKGROUND_COLOR_PATH);
                        loadBackgroundPreview();
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
        setImageButtonEnabled(mBackgroundColorButton, enabled);
        setImageButtonEnabled(mBackgroundImageButton, enabled);
        setImageButtonEnabled(mTextColorButton, enabled);
        setImageButtonEnabled(mPositionButton, enabled);
        mTextSizeSpinner.setEnabled(enabled);
        mStyleSpinner.setEnabled(enabled);
        mShadowSwitch.setEnabled(enabled);
        mTextCaseSpinner.setEnabled(enabled);
        mTextFontSpinner.setEnabled(enabled);
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
        loadBackgroundPreview();
        loadTextCasePreview(true);
        loadTextColorPreview();
        loadTextPositionPreview();
        loadTextShadowPreview(true);
        loadTextSizePreview(true);
        loadTextStyleAndFontPreview(true, true);
    }

    private void storePreference(String key, Object value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (Constants.BACKGROUND_TYPE_KEY.equals(key)
                || Constants.BACKGROUND_COLOR_KEY.equals(key)
                || Constants.TEXT_CASE_KEY.equals(key)
                || Constants.TEXT_COLOR_KEY.equals(key)
                || Constants.TEXT_POSITION_KEY.equals(key)
                || Constants.TEXT_STYLE_KEY.equals(key)) {
            editor.putInt(key, (Integer) value);
        } else if (Constants.BACKGROUND_ASSET_LAST_CHANGED_KEY.equals(key)) {
            editor.putLong(key, (Long) value);
        } else if (Constants.TEXT_SHADOW_KEY.equals(key)) {
            editor.putBoolean(key, (Boolean) value);
        } else if (Constants.TEXT_SIZE_KEY.equals(key)) {
            editor.putFloat(key, (Float) value);
        } else if (Constants.TEXT_FONT_KEY.equals(key)) {
            editor.putString(key, (String) value);
        }
        editor.apply();
    }

    /**
     * Syncs a key/value pair at the given path inside Google Play services Wearable Data Layer.
     *
     * @param path  the path where tha data will be stored
     * @param key   the key referencing the data
     * @param value the value of the data
     */
    private void syncData(String path, String key, Object value) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap dataMap = putDataMapRequest.getDataMap();
        if (value instanceof Integer) {
            dataMap.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            dataMap.putBoolean(key, (Boolean) value);
        } else if (value instanceof Asset) {
            dataMap.putAsset(key, (Asset) value);
        } else if (value instanceof Float) {
            dataMap.putFloat(key, (Float) value);
        } else if (value instanceof String) {
            dataMap.putString(key, (String) value);
        } else {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(getGoogleApiClient(), request);
    }

    private void deleteData(String path) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        Wearable.DataApi.deleteDataItems(getGoogleApiClient(), putDataMapRequest.getUri());
    }

    private void loadBackgroundPreview() {
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
    }

    private void loadTextCasePreview(boolean updateSelector) {
        int textCase = mSharedPreferences
                .getInt(Constants.TEXT_CASE_KEY, Constants.TEXT_CASE_NO_CAPS);
        String newText = getString(R.string.sample_time);
        switch (textCase) {
            case Constants.TEXT_CASE_NO_CAPS:
                break;
            case Constants.TEXT_CASE_ALL_CAPS:
                newText = newText.toUpperCase();
                break;
            case Constants.TEXT_CASE_FIRST_CAP:
                newText = newText.substring(0, 1).toUpperCase() + newText.substring(1);
                break;
        }
        mTextPreview.setText(newText);

        if (updateSelector) {
            mTextCaseSpinner.setSelection(textCase);
        }
    }

    private void loadTextColorPreview() {
        int textColor = mSharedPreferences.getInt(Constants.TEXT_COLOR_KEY, Color.WHITE);
        mTextPreview.setTextColor(textColor);
    }

    private void loadTextPositionPreview() {
        int textPosition = mSharedPreferences.getInt(Constants.TEXT_POSITION_KEY,
                Constants.TEXT_POSITION_CENTER_CENTER);
        int gravity;
        switch (textPosition) {
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
        LevelListDrawable levelListDrawable = (LevelListDrawable) mPositionButton.getDrawable();
        // The position matches the order in the LevelListDrawable
        levelListDrawable.setLevel(textPosition);
    }

    private void loadTextShadowPreview(boolean updateSelector) {
        boolean showShadow = mSharedPreferences.getBoolean(Constants.TEXT_SHADOW_KEY, true);
        if (showShadow) {
            mTextPreview.setShadowLayer(3.0F, 3.0F, 3.0F, Color.BLACK);
        } else {
            mTextPreview.setShadowLayer(0.0F, 0.0F, 0.0F, Color.BLACK);
        }
        if (updateSelector) {
            mShadowSwitch.setChecked(showShadow);
        }
    }

    private void loadTextSizePreview(boolean updateSelector) {
        float textSize = mSharedPreferences
                .getFloat(Constants.TEXT_SIZE_KEY, Constants.TEXT_SIZE_LARGE);
        mTextPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        if (updateSelector) {
            if (textSize == Constants.TEXT_SIZE_LARGE) {
                mTextSizeSpinner.setSelection(0);
            } else if (textSize == Constants.TEXT_SIZE_MEDIUM) {
                mTextSizeSpinner.setSelection(1);
            } else if (textSize == Constants.TEXT_SIZE_SMALL) {
                mTextSizeSpinner.setSelection(2);
            } else if (textSize == Constants.TEXT_SIZE_EXTRA_SMALL) {
                mTextSizeSpinner.setSelection(3);
            }
        }
    }

    private void loadTextStyleAndFontPreview(boolean updateStyleSelector,
            boolean updateFontSelector) {
        int textStyle = mSharedPreferences
                .getInt(Constants.TEXT_STYLE_KEY, Typeface.BOLD);
        String textFontCode = mSharedPreferences
                .getString(Constants.TEXT_FONT_KEY, Font.DEFAULT.getFontCode());
        Font font = Font.findFontByCode(textFontCode);
        mTextPreview.setTypeface(font.getTypeface(getApplicationContext(), textStyle));
        mTextPreview.setText(mTextPreview.getText());

        // The style options are only needed to be updated on a font change and the first time the
        // activity loads
        if (updateStyleSelector) {
            mStyleSpinner.setSelection(textStyle);
            StyleAdapter styleAdapter = (StyleAdapter) mStyleSpinner.getAdapter();
            styleAdapter.setSelectedFont(font);
        }
        if (updateFontSelector) {
            FontAdapter fontAdapter = (FontAdapter) mTextFontSpinner.getAdapter();
            int position = fontAdapter.getPosition(font);
            mTextFontSpinner.setSelection(position);
        }
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
            storePreference(Constants.BACKGROUND_TYPE_KEY, Constants.BACKGROUND_TYPE_COLOR);
            storePreference(Constants.BACKGROUND_COLOR_KEY, color);
            syncData(Constants.BACKGROUND_COLOR_PATH, Constants.BACKGROUND_COLOR_KEY, color);
            deleteData(Constants.BACKGROUND_ASSET_PATH);
            loadBackgroundPreview();
        } else if (DIALOG_TEXT_COLOR_PICKER_TAG.equals(tag)) {
            storePreference(Constants.TEXT_COLOR_KEY, color);
            syncData(Constants.TEXT_COLOR_PATH, Constants.TEXT_COLOR_KEY, color);
            loadTextColorPreview();
        }
    }

    @Override
    public void onPositionPicked(DialogFragment dialog, int position) {
        storePreference(Constants.TEXT_POSITION_KEY, position);
        syncData(Constants.TEXT_POSITION_PATH, Constants.TEXT_POSITION_KEY, position);
        loadTextPositionPreview();
    }
}
