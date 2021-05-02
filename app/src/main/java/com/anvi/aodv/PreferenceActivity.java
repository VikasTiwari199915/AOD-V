package com.anvi.aodv;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        SwitchPreference clockEnabled= (SwitchPreference) findPreference("clock_enabled");
        clockEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(PreferenceActivity.this, "Clock Enabled=>"+newValue, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        ListPreference clockFont= (ListPreference) findPreference("clock_font_list");
        clockFont.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(PreferenceActivity.this, "Clock Font=>"+newValue, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
