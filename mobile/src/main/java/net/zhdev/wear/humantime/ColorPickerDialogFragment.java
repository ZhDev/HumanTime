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

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * A dialog screen that allows the user to pick a color using a color wheel, a saturation bar and a
 * value bar. The initial color that is displayed in the color picker can be given when the
 * instance is created.
 *
 * @author Julio García Muñoz (ZhDev)
 * @see com.larswerkman.holocolorpicker.ColorPicker
 */
public class ColorPickerDialogFragment extends DialogFragment {

    private ColorPicker mColorPicker;

    private ColorPickerDialogListener mColorPickerDialogListener;

    /**
     * Creates a new instance of this fragment, setting its initial color.
     *
     * @param oldColor the initial color to be be displayed in the the center of the color picker
     * @return the created instance
     */
    public static ColorPickerDialogFragment newInstance(int oldColor) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt("old_color", oldColor);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mColorPickerDialogListener = (ColorPickerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement ColorPickerDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int oldColor = getArguments().getInt("old_color");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Initialize all the views

        View view = inflater.inflate(R.layout.fragment_color_picker_dialog, null);
        mColorPicker = (ColorPicker) view.findViewById(R.id.color_picker);

        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturation_bar);
        mColorPicker.addSaturationBar(saturationBar);

        ValueBar valueBar = (ValueBar) view.findViewById(R.id.value_bar);
        mColorPicker.addValueBar(valueBar);

        mColorPicker.setColor(oldColor);
        mColorPicker.setOldCenterColor(oldColor);

        builder.setView(view)
                // The positive button returns the picked color to the attached listener
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mColorPickerDialogListener.onColorPicked(ColorPickerDialogFragment.this,
                                mColorPicker.getColor());
                    }
                })
                        // Th negative button closes the dialog
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ColorPickerDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * The <code>Activity</code> that launches this dialog has to implement the
     * <code>ColorPickerDialogListener</code> interface to provide callbacks. It will be checked
     * when the fragment is attached.
     */
    public interface ColorPickerDialogListener {

        public void onColorPicked(DialogFragment dialog, int color);
    }

}
