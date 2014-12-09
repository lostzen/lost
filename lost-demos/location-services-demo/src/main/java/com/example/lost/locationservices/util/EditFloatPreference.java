package com.example.lost.locationservices.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;

public class EditFloatPreference extends EditTextPreference {
    public static final String TAG = EditFloatPreference.class.getSimpleName();

    public EditFloatPreference(Context context) {
        super(context);
        getEditText().setRawInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }

    public EditFloatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getEditText().setRawInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }

    public EditFloatPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getEditText().setRawInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if(getSharedPreferences().contains(getKey())) {
            float floatValue = getPersistedFloat(0f);
            return String.valueOf(floatValue);
        } else {
            return defaultReturnValue;
        }
    }

    @Override
    protected boolean persistString(String value) {
        float floatValue;
        try {
            floatValue = Float.valueOf(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Unable to parse preference value: " + value);
            return true;
        }

        return persistFloat(floatValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
