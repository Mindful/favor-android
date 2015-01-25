package com.favor;

import android.os.Bundle;
import android.preference.PreferenceFragment;


public class CoreMenuFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.core_settings);
    }
}
