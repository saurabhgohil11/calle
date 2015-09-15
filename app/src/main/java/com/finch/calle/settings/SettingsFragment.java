package com.finch.calle.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.finch.calle.AppGlobals;
import com.finch.calle.DataBaseHelper;
import com.finch.calle.HomeActivity;
import com.finch.calle.R;

import java.io.File;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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

        //EditTextPreference userCircle = (EditTextPreference) findPreference("user_number");

        ListPreference listPref = (ListPreference) findPreference("user_circle");
        listPref.setSummary(getResources().getString(R.string.summary_user_circle) + listPref.getEntry());
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
        //AppGlobals.log(this, "onSharedPreferenceChanged(),key=" + key);
        updatePreference(findPreference(key), key);
        if (key.equals("enable_mins_toast")) {
            AppGlobals.isEnableToast = sharedPreferences.getBoolean(key,false);
            return;
        }

        if (key.equals("user_circle")) {
            final ProgressDialog progressDialog;
            final DataBaseHelper dbHelper = AppGlobals.getDataBaseHelper(getActivity());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            Resources res = getResources();
            alertDialogBuilder.setTitle(res.getString(R.string.dialog_title_user_circle_changed));
            alertDialogBuilder.setMessage(res.getString(R.string.body_user_circle_changed));

            alertDialogBuilder.setPositiveButton(res.getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            final AlertDialog alertDialog = alertDialogBuilder.create();
            if (dbHelper == null ) {
                new DataBaseHelper(getActivity()).updateLogsOnCircleChange();
                alertDialog.show();
            } else {
                progressDialog = ProgressDialog.show(getActivity(), "Please wait!","Updating...",true);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        alertDialog.show();
                    }
                });

                new Thread() {
                    public void run() {
                        try {
                            dbHelper.updateLogsOnCircleChange();
                        } catch (Exception e) {
                            AppGlobals.log(this, "Error in updating via thread when state changed.");
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                        if(HomeActivity.mHandler!=null)
                            HomeActivity.mHandler.sendEmptyMessage(HomeActivity.UPDATE_VIEWS);
                    }
                }.start();
            }

        } else if (key.equals("mode_of_calcualation")) {
            AppGlobals.isMinuteMode = AppGlobals.MODE_MINUTES.equals(sharedPreferences.getString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_MINUTES));
        }

        if(HomeActivity.mHandler!=null)
            HomeActivity.mHandler.sendEmptyMessage(HomeActivity.UPDATE_VIEWS);
    }

    private void updatePreference(Preference p,String key) {
         if(p instanceof ListPreference){
            ListPreference listPref = (ListPreference) p;
            p.setSummary(getResources().getString(R.string.summary_user_circle) + listPref.getEntry());
            AppGlobals.userState=listPref.getValue();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String key = preference.getKey();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog;
        if(key != null) {
            switch (key) {
                case "reset_app":
                    Resources res = getResources();
                    alertDialogBuilder.setTitle(res.getString(R.string.title_reset_application));
                    alertDialogBuilder.setMessage(res.getString(R.string.reset_confirmation_msg));

                    alertDialogBuilder.setPositiveButton(res.getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    getPreferenceManager().getSharedPreferences().edit().clear().commit();
                                    clearApplicationData();
                                    Toast.makeText(getActivity(), getResources().getString(R.string.reset_complete), Toast.LENGTH_LONG).show();
                                    getActivity().finish();
                                    AppGlobals.reset();
                                    startActivity(new Intent(getActivity(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            });
                    alertDialogBuilder.setNegativeButton(res.getString(R.string.cancel),null);
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    break;
                case "about":
                    showAboutDialog();
                    break;
                /*case "extract_database":
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();
                    FileChannel source;
                    FileChannel destination;
                    String currentDBPath = "/data/" + "com.finch.mycalls" + "/databases/" + "calleapp";
                    String backupDBPath = "calleapp";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);
                    try {
                        source = new FileInputStream(currentDB).getChannel();
                        destination = new FileOutputStream(backupDB).getChannel();
                        destination.transferFrom(source, 0, source.size());
                        source.close();
                        destination.close();
                        Toast.makeText(getActivity(), "DB Exported to" + backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "Failed to export!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    break;
                case "number_test":
                    LayoutInflater li = LayoutInflater.from(getActivity());
                    View promptsView = li.inflate(R.layout.dialog_number_test, null);
                    alertDialogBuilder.setTitle("Enter Number to Add: ");
                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView.findViewById(R.id.number_edit_text);
                    final EditText igog = (EditText) promptsView.findViewById(R.id.ic_og_edit_text);
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    String number = userInput.getText().toString();
                                    String numbertype = igog.getText().toString();
                                    if (number == null || number.isEmpty() || numbertype == null || numbertype.isEmpty()) {
                                        Toast.makeText(getActivity(),"Number or type can't be empty",Toast.LENGTH_SHORT).show();
                                    } else {
                                        CallDetails cd =new CallDetails();
                                        cd.duration=150;
                                        cd.date = new Date().getTime();
                                        cd.cachedContactName = "Misa Misa";
                                        cd.phoneNumber = number;
                                        cd.isRoaming = numbertype.contains("r");
                                        cd.isHidden = false;
                                        if(numbertype.contains("i"))
                                                cd.callType = CallType.INCOMING;
                                        if(numbertype.contains("o"))
                                                cd.callType = CallType.OUTGOING;

                                        PhoneNumber n = new PhoneNumber(getActivity(),AppGlobals.dbHelper,cd.phoneNumber);
                                        cd.costType = n.getCostType();
                                        cd.nationalNumber = n.getNationalNumber();
                                        cd.phoneNumberType = n.getPhoneNumberType();
                                        cd.numberLocation = n.getPhoneNumberLocation();

                                        AppGlobals.dbHelper.addToLogsHistory(cd);
                                    }

                                }
                            });
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    break;*/
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showAboutDialog() {
        final Dialog aboutDialog = new Dialog(getActivity());
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setContentView(R.layout.dialog_about);
        TextView credits = (TextView) aboutDialog.findViewById(R.id.credits_text_view);
        credits.setMovementMethod(LinkMovementMethod.getInstance());
        aboutDialog.show();
    }

    public void clearApplicationData() {
        File cache = getActivity().getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    //AppGlobals.log(this, "** File /data/data/APP_PACKAGE/" + s + " DELETED **");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
