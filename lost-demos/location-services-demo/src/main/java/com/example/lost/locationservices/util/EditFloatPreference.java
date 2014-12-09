package com.example.lost.locationservices.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;

public class EditFloatPreference extends EditTextPreference {
    public static final String TAG = EditFloatPreference.class.getSimpleName();

    public EditFloatPreference(Context context) {
        super(context);
    }

    public EditFloatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditFloatPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
