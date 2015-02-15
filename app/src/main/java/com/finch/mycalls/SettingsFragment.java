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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         initPrefrences();
    }

    private void initPrefrences() {
        EditTextPreference localMins = (EditTextPreference) findPreference("total_local_mins");
        EditTextPreference stdMins = (EditTextPreference) findPreference("total_std_mins");
        EditTextPreference roamingMins = (EditTextPreference) findPreference("total_roaming_mins");
        EditTextPreference userCircle = (EditTextPreference) findPreference("user_number");

        localMins.setSummary("Your total free local mins are :" + localMins.getText());
        stdMins.setSummary("Your total free STD mins are :" + stdMins.getText());
        roamingMins.setSummary("Your total free roaming mins are :" + roamingMins.getText());

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
        if(p instanceof EditTextPreference){
            if(key.equals("total_local_mins")){
                p.setSummary("Your total free local mins are : " + ((EditTextPreference)p).getText());
            }else if(key.equals("total_std_mins")){
                p.setSummary("Your total free STD mins are : " + ((EditTextPreference)p).getText());
            }else if(key.equals("total_roaming_mins")){
                p.setSummary("Your total free roaming mins are : " + ((EditTextPreference)p).getText());
            }/* pop pop else if(key.equals("user_number")){
                String userState = AppGlobals.updateUserState(((EditTextPreference)p).getText());
                p.setSummary("Your current user circle is :" + userState);
            }*/
        }else if(p instanceof ListPreference){
            ListPreference listPref = (ListPreference) p;
            p.setSummary("Your current user circle is : " + listPref.getEntry());
            AppGlobals.userState=listPref.getValue();
        }
    }
}
