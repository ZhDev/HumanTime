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

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * An adapter backed by an array of fixed <code>Case</code> objects.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class CaseAdapter extends ArrayAdapter<CaseAdapter.Case> {

    public CaseAdapter(Context context, int resource) {
        super(context, resource);
        Case[] cases = new Case[]{

        };
        addAll(cases);
    }

    public static class Case {

        private int mCase;

        private String mDescription;

        public Case(int textCase, String description) {
            mCase = textCase;
            mDescription = description;
        }

        public int getCase() {
            return mCase;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Case textCase = (Case) o;

            return mCase == textCase.mCase;
        }

        @Override
        public int hashCode() {
            return mCase;
        }

        @Override
        public String toString() {
            return mDescription;
        }
    }

}
