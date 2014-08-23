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

import com.edmodo.cropper.CropImageView;

import net.zhdev.wear.humantime.shared.Constants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ImageCropperActivity provides a screen to resize and crop an image. The original image will we
 * passed to the <code>Activity</code> in the <code>Intent</code> data and the resulting image will
 * be stored in persistent memory and its Uri will be passed back as the activity result.
 *
 * @author Julio García Muñoz (ZhDev)
 * @see com.edmodo.cropper.CropImageView
 */
public class ImageCropperActivity extends Activity {

    public static final String EXTRA_CACHED_IMAGE_PATH = "cached_image_name";

    /**
     * Default image size for wearables
     */
    private static final int IMAGE_SIZE = 320;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String imagePath = getIntent().getStringExtra(EXTRA_CACHED_IMAGE_PATH);
        if (imagePath == null) {
            throw new IllegalArgumentException("Missing image path in the intent extras");
        }

        setContentView(R.layout.activity_image_cropper);

        final CropImageView imageCropper = (CropImageView) findViewById(R.id.image_cropper);
        final File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Invalid file");
        }
        Bitmap bitmap = getScaledBitmapFromFile(imagePath);
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(imagePath);
        } catch (IOException e) {
            // If it goes wrong at most we lose the image orientation
        }
        imageCropper.setImageBitmap(bitmap, exifInterface);

        Button buttonOK = (Button) findViewById(R.id.button_ok);
        // Store the image and pass the resulting Uri back to the calling Activity
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the squared cropped image
                Bitmap bitmap = imageCropper.getCroppedImage();
                // Resize it to the specific size ox 320x320 for the watch face
                Bitmap resizedBitmap = ThumbnailUtils
                        .extractThumbnail(bitmap, IMAGE_SIZE, IMAGE_SIZE,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                // Store the image
                FileOutputStream outputStream = null;
                try {
                    outputStream = openFileOutput(Constants.BACKGROUND_ASSET_FILE_NAME,
                            MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                }
                boolean saved = resizedBitmap
                        .compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                if (saved) {
                    Intent intent = new Intent();
                    intent.setData(
                            Uri.fromFile(getFileStreamPath(Constants.BACKGROUND_ASSET_FILE_NAME)));
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                imageFile.delete();
                finish();
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.button_cancel);
        // Send back RESULT_CANCELED
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                imageFile.delete();
                finish();
            }
        });
    }

    /**
     * Loads a scaled bitmap from a file with a factor of reduction in a power of 2 to make it as
     * close as possible to the display size (minus system decorations).
     *
     * @param imagePath the absolute path of the image file to be loaded
     * @return a scaled bitmap
     */
    private Bitmap getScaledBitmapFromFile(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, options);

        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int allowedWidth = metrics.widthPixels;
        int allowedHeight = metrics.heightPixels;

        int sampleSize = 1;
        if (bitmapWidth > allowedWidth || bitmapHeight > allowedHeight) {
            bitmapWidth /= 2;
            bitmapHeight /= 2;
            while ((bitmapWidth / sampleSize) > allowedWidth
                    || (bitmapHeight / sampleSize) > allowedHeight) {
                sampleSize *= 2;
            }
        }

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }
}
