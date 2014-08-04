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

package net.zhdev.wear.humantime.adapter;

import net.zhdev.wear.humantime.R;
import net.zhdev.wear.humantime.shared.Constants;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * An adapter backed by an array of fixed <code>Size</code> objects.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class SizeAdapter extends ArrayAdapter<SizeAdapter.Size> {

    public SizeAdapter(Context context, int resource) {
        super(context, resource);
        Size[] sizes = new Size[]{
                new Size(Constants.TEXT_SIZE_LARGE, context.getString(R.string.large)),
                new Size(Constants.TEXT_SIZE_MEDIUM, context.getString(R.string.medium)),
                new Size(Constants.TEXT_SIZE_SMALL, context.getString(R.string.small)),
                new Size(Constants.TEXT_SIZE_EXTRA_SMALL, context.getString(R.string.extra_small))
        };
        addAll(sizes);
    }

    public static class Size {

        private final String mDescription;

        private float mSize;

        public Size(float size, String description) {
            mSize = size;
            mDescription = description;
        }

        public float getSize() {
            return mSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Size size = (Size) o;

            return Float.compare(size.mSize, mSize) == 0;
        }

        @Override
        public int hashCode() {
            return (mSize != +0.0f ? Float.floatToIntBits(mSize) : 0);
        }

        @Override
        public String toString() {
            return mDescription;
        }
    }
}
