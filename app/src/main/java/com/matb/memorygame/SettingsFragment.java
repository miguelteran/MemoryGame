package com.matb.memorygame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener
{
    private void setPreferenceSummary(Preference preference, String value)
    {
        if (preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0)
            {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        else if (preference instanceof EditTextPreference)
        {
            preference.setSummary(value);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {
        addPreferencesFromResource(R.xml.game_settings);

        // Set preference summaries for each setting
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++)
        {
            Preference preference = preferenceScreen.getPreference(i);
            String value = sharedPreferences.getString(preference.getKey(), "");
            setPreferenceSummary(preference, value);
        }

        Preference preference = findPreference(getString(R.string.matches_to_win));
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o)
    {
        // Make sure user enters valid number for matches to win
        String key = getString(R.string.matches_to_win);
        if (preference.getKey().equals(key))
        {
            String input = (String) o;
            Toast error = Toast.makeText(getContext(),
                    getString(R.string.bad_input_error_message),
                    Toast.LENGTH_SHORT);
            try
            {
                int num_matches = Integer.parseInt(input);
                if (num_matches > 24 || num_matches < 1)
                {
                    error.show();
                    return false;
                }
            }
            catch (NumberFormatException nfe)
            {
                error.show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        Preference preference = findPreference(s);
        if (preference != null)
        {
            String value = sharedPreferences.getString(preference.getKey(), "");
            setPreferenceSummary(preference, value);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
