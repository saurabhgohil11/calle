package com.evadroid.calle.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.evadroid.calle.R;

public class NumberPickerPreference extends DialogPreference {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;

    private int mSelectedValue;
    private final int mMinValue;
    private final int mMaxValue;
    private NumberPicker mNumberPicker;

    public NumberPickerPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);

        mMinValue = a.getInt(R.styleable.NumberPickerPreference_minValue, NumberPickerPreference.MIN_VALUE);
        mMaxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, NumberPickerPreference.MAX_VALUE);

        a.recycle();
    }

    @Override
    protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
        mSelectedValue = restoreValue ? this.getPersistedInt(0) : (Integer) defaultValue;
        this.updateSummary();
    }

    @Override
    protected Object onGetDefaultValue(final TypedArray a, final int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mNumberPicker = new NumberPicker(this.getContext());
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mSelectedValue);
        mNumberPicker.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout linearLayout = new LinearLayout(this.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(mNumberPicker);

        builder.setView(linearLayout);
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult) {
        if (positiveResult && this.shouldPersist()) {
            mSelectedValue = mNumberPicker.getValue();
            this.persistInt(mSelectedValue);
            this.updateSummary();
        }
    }

    private void updateSummary() {
        super.setSummary(String.format(getContext().getResources().getString(R.string.summary_modify_cycle),mSelectedValue));
    }

}
