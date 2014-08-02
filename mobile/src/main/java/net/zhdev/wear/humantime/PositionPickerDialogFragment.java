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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

/**
 * A dialog screen that allows the user to pick the position of the text.
 *
 * @author Julio García Muñoz (ZhDev)
 */
public class PositionPickerDialogFragment extends DialogFragment {

    private PositionPickerDialogListener mPositionPickerDialogListener;

    /**
     * Creates a new instance of this fragment.
     *
     * @return the created instance
     */
    public static PositionPickerDialogFragment newInstance() {
        return new PositionPickerDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mPositionPickerDialogListener = (PositionPickerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement ColorPickerDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Initialize all the views

        View view = inflater.inflate(R.layout.fragment_position_picker_dialog, null);

        final ImageButton topLeft = (ImageButton) view.findViewById(R.id.top_left);
        final ImageButton topCenter = (ImageButton) view.findViewById(R.id.top_center);
        final ImageButton topRight = (ImageButton) view.findViewById(R.id.top_right);
        final ImageButton centerLeft = (ImageButton) view.findViewById(R.id.center_left);
        final ImageButton centerCenter = (ImageButton) view.findViewById(R.id.center_center);
        final ImageButton centerRight = (ImageButton) view.findViewById(R.id.center_right);
        final ImageButton bottomLeft = (ImageButton) view.findViewById(R.id.bottom_left);
        final ImageButton bottomCenter = (ImageButton) view.findViewById(R.id.bottom_center);
        final ImageButton bottomRight = (ImageButton) view.findViewById(R.id.bottom_right);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Constants.TEXT_POSITION_CENTER_CENTER;
                if (v.equals(topLeft)) {
                    position = Constants.TEXT_POSITION_TOP_LEFT;
                } else if (v.equals(topCenter)) {
                    position = Constants.TEXT_POSITION_TOP_CENTER;
                } else if (v.equals(topRight)) {
                    position = Constants.TEXT_POSITION_TOP_RIGHT;
                } else if (v.equals(centerLeft)) {
                    position = Constants.TEXT_POSITION_CENTER_LEFT;
                } else if (v.equals(centerCenter)) {
                    position = Constants.TEXT_POSITION_CENTER_CENTER;
                } else if (v.equals(centerRight)) {
                    position = Constants.TEXT_POSITION_CENTER_RIGHT;
                } else if (v.equals(bottomLeft)) {
                    position = Constants.TEXT_POSITION_BOTTOM_LEFT;
                } else if (v.equals(bottomCenter)) {
                    position = Constants.TEXT_POSITION_BOTTOM_CENTER;
                } else if (v.equals(bottomRight)) {
                    position = Constants.TEXT_POSITION_BOTTOM_RIGHT;
                }
                mPositionPickerDialogListener
                        .onPositionPicked(PositionPickerDialogFragment.this, position);
                PositionPickerDialogFragment.this.getDialog().dismiss();
            }
        };

        topLeft.setOnClickListener(listener);
        topCenter.setOnClickListener(listener);
        topRight.setOnClickListener(listener);
        centerLeft.setOnClickListener(listener);
        centerCenter.setOnClickListener(listener);
        centerRight.setOnClickListener(listener);
        bottomLeft.setOnClickListener(listener);
        bottomCenter.setOnClickListener(listener);
        bottomRight.setOnClickListener(listener);

        builder.setView(view)
                // The negative button closes the dialog
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PositionPickerDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * The <code>Activity</code> that launches this dialog has to implement the
     * <code>PositionPickerDialogListener</code> interface to provide callbacks. It will be checked
     * when the fragment is attached.
     */
    public interface PositionPickerDialogListener {

        public void onPositionPicked(DialogFragment dialog, int position);
    }

}
