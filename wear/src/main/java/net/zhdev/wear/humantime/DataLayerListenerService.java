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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import net.zhdev.humantime.shared.Constants;

import android.content.SharedPreferences;
import android.graphics.Color;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * <code>DataLayerListenerService</code> is a service that automatically binds and listens for
 * events from the Wear API on the background.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final int TIMEOUT_MS = 5000;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            // Every time a data path is changed the listener retrieves the data and updates the
            // stored preferences
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAME,
                        MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                DataItem dataItem = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                String uriPath = dataItem.getUri().getPath();

                if (Constants.BACKGROUND_COLOR_PATH.equals(uriPath)) {
                    int color = dataMap.getInt(Constants.BACKGROUND_COLOR_KEY);
                    editor.putInt(Constants.BACKGROUND_COLOR_KEY, color);
                    editor.putInt(Constants.BACKGROUND_TYPE_KEY, Constants.BACKGROUND_TYPE_COLOR);
                } else if (Constants.BACKGROUND_ASSET_PATH.equals(uriPath)) {
                    Asset asset = dataMap.getAsset(Constants.BACKGROUND_ASSET_KEY);
                    boolean fileSaved = saveFileFromAsset(asset);
                    if (fileSaved) {
                        editor.putLong(Constants.BACKGROUND_ASSET_LAST_CHANGED_KEY,
                                System.currentTimeMillis());
                        editor.putInt(Constants.BACKGROUND_TYPE_KEY,
                                Constants.BACKGROUND_TYPE_IMAGE);
                    } else {
                        editor.putInt(Constants.BACKGROUND_COLOR_KEY, Color.BLACK);
                        editor.putInt(Constants.BACKGROUND_TYPE_KEY,
                                Constants.BACKGROUND_TYPE_COLOR);
                    }

                } else if (Constants.TEXT_COLOR_PATH.equals(uriPath)) {
                    int color = dataMap.getInt(Constants.TEXT_COLOR_KEY);
                    editor.putInt(Constants.TEXT_COLOR_KEY, color);
                } else if (Constants.TEXT_STYLE_PATH.equals(uriPath)) {
                    int style = dataMap.getInt(Constants.TEXT_STYLE_KEY);
                    editor.putInt(Constants.TEXT_STYLE_KEY, style);
                } else if (Constants.TEXT_SHADOW_PATH.equals(uriPath)) {
                    boolean showShadow = dataMap.getBoolean(Constants.TEXT_SHADOW_KEY);
                    editor.putBoolean(Constants.TEXT_SHADOW_KEY, showShadow);
                } else if (Constants.TEXT_SIZE_PATH.equals(uriPath)) {
                    float size = dataMap.getFloat(Constants.TEXT_SIZE_KEY);
                    editor.putFloat(Constants.TEXT_SIZE_KEY, size);
                } else if (Constants.TEXT_POSITION_PATH.equals(uriPath)) {
                    int position = dataMap.getInt(Constants.TEXT_POSITION_KEY);
                    editor.putInt(Constants.TEXT_POSITION_KEY, position);
                }

                editor.apply();
            }
        }
    }


    /**
     * Saves into persistent memory an asset put into the Wear Data Layer
     *
     * @param asset the <code>Asset</code> that will be saves into a file
     * @return true if the asset was correctly retrieved and stored, false otherwise
     */
    private boolean saveFileFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = mGoogleApiClient
                .blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return false;
        }

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset)
                .await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            return false;
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = openFileOutput(Constants.BACKGROUND_ASSET_FILE_NAME, MODE_PRIVATE);
            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = assetInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes);
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

}
