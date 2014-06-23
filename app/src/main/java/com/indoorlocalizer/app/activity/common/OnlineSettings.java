package com.indoorlocalizer.app.activity.common;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.indoorlocalizer.app.R;

public class OnlineSettings extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_online);
    }
}