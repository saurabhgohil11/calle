package com.evadroid.calle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WarningLimitsActivity extends AppCompatActivity {
    EditText stdLocalMins;
    EditText stdMins;
    EditText localMins;
    EditText roamingMins;
    EditText isdMins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning_limit_dialog);
        stdLocalMins = (EditText) findViewById(R.id.std_local_limit_edit_text);
        stdMins = (EditText) findViewById(R.id.std_limit_edit_text);
        localMins = (EditText) findViewById(R.id.local_limit_edit_text);
        roamingMins = (EditText) findViewById(R.id.roaming_limit_edit_text);
        isdMins = (EditText) findViewById(R.id.isd_limit_edit_text);
        stdLocalMins.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (stdMins.length() > 0 || localMins.length() >0) {
                        Toast.makeText(getApplicationContext(),R.string.warning_can_not_set_both_std_local,Toast.LENGTH_LONG).show();
                        stdMins.setText("");
                        localMins.setText("");
                    }

                }
            }
        });
        stdMins.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (stdLocalMins.length() > 0) {
                        Toast.makeText(getApplicationContext(),R.string.warning_can_not_set_both_std_local,Toast.LENGTH_LONG).show();
                        stdLocalMins.setText("");
                    }
                }
            }
        });
        localMins.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (stdLocalMins.length() > 0) {
                        Toast.makeText(getApplicationContext(),R.string.warning_can_not_set_both_std_local,Toast.LENGTH_LONG).show();
                        stdLocalMins.setText("");
                    }
                }
            }
        });
        loadFromPreference();
    }

    private void loadFromPreference() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        setTextForView(sp.getInt(AppGlobals.PKEY_STD_LOCAL_LIMIT, -1), stdLocalMins);
        setTextForView(sp.getInt(AppGlobals.PKEY_STD_LIMIT, -1), stdMins);
        setTextForView(sp.getInt(AppGlobals.PKEY_LOCAL_LIMIT, -1), localMins);
        setTextForView(sp.getInt(AppGlobals.PKEY_ROAMING_LIMIT, -1), roamingMins);
        setTextForView(sp.getInt(AppGlobals.PKEY_ISD_LIMIT, -1), isdMins);
    }

    private void setTextForView(int mins, EditText t) {
        if (t == null) return;
        if (mins == -1 || mins == 0)
            t.setText(null);
        else
            t.setText(String.valueOf(mins));
    }

    public void onSetClick(View v) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = sp.edit();
        boolean shouldFinish = true;

        int mins = getMinsFromView(stdLocalMins);
        if (mins >= 0)
            e.putInt(AppGlobals.PKEY_STD_LOCAL_LIMIT, mins);
        else if (mins == -99)
            e.remove(AppGlobals.PKEY_STD_LOCAL_LIMIT);
        else
            shouldFinish = false;

        mins = getMinsFromView(stdMins);
        if (mins >= 0)
            e.putInt(AppGlobals.PKEY_STD_LIMIT, mins);
        else if (mins == -99)
            e.remove(AppGlobals.PKEY_STD_LIMIT);
        else
            shouldFinish = false;

        mins = getMinsFromView(localMins);
        if (mins >= 0)
            e.putInt(AppGlobals.PKEY_LOCAL_LIMIT, mins);
        else if (mins == -99)
            e.remove(AppGlobals.PKEY_LOCAL_LIMIT);
        else
            shouldFinish = false;

        mins = getMinsFromView(roamingMins);
        if (mins >= 0)
            e.putInt(AppGlobals.PKEY_ROAMING_LIMIT, mins);
        else if (mins == -99)
            e.remove(AppGlobals.PKEY_ROAMING_LIMIT);
        else
            shouldFinish = false;

        mins = getMinsFromView(isdMins);
        if (mins >= 0)
            e.putInt(AppGlobals.PKEY_ISD_LIMIT, mins);
        else if (mins == -99)
            e.remove(AppGlobals.PKEY_ISD_LIMIT);
        else
            shouldFinish = false;
        e.commit();

        if (shouldFinish) {
            AppGlobals.sendUpdateMessage();
            finish();
        }
    }

    private int getMinsFromView(EditText text) {
        if (text == null) return -1;
        String s = text.getText().toString();
        if (s == null || s.isEmpty()) return -99;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = getResources().getString(R.string.please_enter_correct_mins) + text.getHint();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    public void onCancelClick(View v) {
        finish();
    }
}
