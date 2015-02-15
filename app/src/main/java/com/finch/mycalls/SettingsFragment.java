package com.finch.mycalls;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         initPrefrences();
    }

    private void initPrefrences() {

        EditTextPreference userCircle = (EditTextPreference) findPreference("user_number");

        /*pop pop String userState=AppGlobals.updateUserState(userCircle.getText());
        userCircle.setSummary("Your current user circle is :" +userState);*/

        ListPreference listPref = (ListPreference) findPreference("user_circle");
        listPref.setSummary("Your current user circle is :" + listPref.getEntry());
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        AppGlobals.log(this, "onSharedPreferenceChanged(),key=" + key);
        updatePreference(findPreference(key),key);
        HomeActivity.mHandler.sendEmptyMessage(HomeActivity.UPDATE_VIEWS);
    }

    private void updatePreference(Preference p,String key) {
         if(p instanceof ListPreference){
            ListPreference listPref = (ListPreference) p;
            p.setSummary("Your current user circle is : " + listPref.getEntry());
            AppGlobals.userState=listPref.getValue();
        }
    }
}
