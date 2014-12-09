package com.example.lost.locationservices.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class IntListPreference extends ListPreference {
    public static final String TAG = IntListPreference.class.getSimpleName();

    public IntListPreference(Context context) {
        super(context);
    }

    public IntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if(getSharedPreferences().contains(getKey())) {
            int intValue = getPersistedInt(0);
            return String.valueOf(intValue);
        } else {
            return defaultReturnValue;
        }
    }

    @Override
    protected boolean persistString(String value) {
        int intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Unable to parse preference value: " + value);
            return true;
        }

        return persistInt(intValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final String s = a.getString(index);

        // Workaround for Robolectric which loads integer resources as hex strings.
        if (s.startsWith("0x")) {
            return Integer.valueOf(s.substring(2), 16).toString();
        }

        return s;
    }
}
