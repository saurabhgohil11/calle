package com.evadroid.calle.setupwizard;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.DataBaseHelper;
import com.evadroid.calle.R;

import java.util.Date;



public class SetupActivity extends AppCompatActivity {

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    Integer[] colors;
    static String[] stateIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        boolean permissionGranted = AppGlobals.checkForPermissions(SetupActivity.this);
        if (!permissionGranted) {
            requestPermissions();
        }

        if(!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Integer color1 = getResources().getColor(R.color.color1);
        Integer color2 = getResources().getColor(R.color.color2);
        Integer color3 = getResources().getColor(R.color.color3);

        Integer[] colors_temp = {color1, color2, color3};
        colors = colors_temp;

        stateIDs = getResources().getStringArray(R.array.circle_state_codes);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SetupPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.setPageTransformer(true, new FloatViewsTransform());

        final ImageButton nextButton = (ImageButton)findViewById(R.id.next_button);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            final RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup);
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position < (mPagerAdapter.getCount() -1) && position < (colors.length - 1)) {
                    mPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]));
                } else {
                    mPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        radioGroup.check(R.id.radioButton1);
                        break;
                    case 1:
                        radioGroup.check(R.id.radioButton2);
                        break;
                    case 2:
                        radioGroup.check(R.id.radioButton3);
                        break;
                }
                if(position==mPagerAdapter.getCount()-1) {
                    nextButton.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                } else {
                    nextButton.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SetupActivity.this,Manifest.permission.READ_CALL_LOG)) {
            ActivityCompat.requestPermissions(SetupActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS},
                    AppGlobals.MY_PERMISSIONS_REQUEST);
        } else {
            ActivityCompat.requestPermissions(SetupActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS},
                    AppGlobals.MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppGlobals.MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(SetupActivity.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void onFinishClicked(View v) {
        AppGlobals.log(this, "onFinishClicked()");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        EditText mobileNumber = (EditText) findViewById(R.id.mobile_number_edit_text);
        Spinner state = (Spinner) findViewById(R.id.spinner_state);
        Spinner date =  (Spinner) findViewById(R.id.spinner_date);
        RadioGroup mode = (RadioGroup) findViewById(R.id.group_mode);
        CheckBox tncCheckBox = (CheckBox) findViewById(R.id.tnc_checkbox);

        if(mobileNumber.getText().toString().length()<10) {
            Toast.makeText(this, R.string.enter_a_valid_mobile_number, Toast.LENGTH_LONG).show();
            return;
        }

        if(!tncCheckBox.isChecked()) {
            Toast.makeText(this, R.string.please_accept_tnc, Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences.Editor e = sp.edit();

        e.putString(AppGlobals.PKEY_USER_CIRCLE,stateIDs[state.getSelectedItemPosition()]);
        e.putInt(AppGlobals.PKEY_BILL_CYCLE, Integer.parseInt(date.getSelectedItem().toString()));
        if(((RadioButton)(mode.getChildAt(0))).isChecked()){
            e.putString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_MINUTES);
        }else{
            e.putString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_SECONDS);
        }
        e.putBoolean(AppGlobals.PKEY_FIRST_TIME, true);
        e.putLong(AppGlobals.PKEY_INSTALLATION_DATE, new Date().getTime());
        e.commit();
        startActivity(new Intent(this,LogAnalyzerActivity.class));
        finish();
    }

    public void onNextClicked(View v) {
        int i = mPager.getCurrentItem();
        AppGlobals.log(this, "onNextClicked()"+i);
        if(i+1<mPagerAdapter.getCount())
            mPager.setCurrentItem(i+1);
    }

    public void onShowMinutesHelp(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog alertDialog;
        Resources res = getResources();
        alertDialogBuilder.setTitle(res.getString(R.string.dialog_title_mode_of_calculation));
        alertDialogBuilder.setMessage(res.getString(R.string.help_mode_of_calculation));

        alertDialogBuilder.setPositiveButton(res.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public static class SetupWizardFragment extends Fragment {
        private static final String FRAGNO = "frag_no";
        private static final int FRAG_WELCOME = 0;
        private static final int  FRAG_CIRCLE_CYCLE_MODE= 2;
        private static final int FRAG_INFO_1 = 1;

        int fragmentNum;
        DataBaseHelper dbHelper;

        public static SetupWizardFragment newInstance(int fragmentNum) {
            SetupWizardFragment fragment = new SetupWizardFragment();
            Bundle args = new Bundle();
            args.putInt(FRAGNO, fragmentNum);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                fragmentNum = getArguments().getInt(FRAGNO);
            }
            dbHelper = AppGlobals.getDataBaseHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            switch(fragmentNum){
                case FRAG_WELCOME:
                    rootView = inflater.inflate(R.layout.fragment_setup_welcome, container, false);
                    break;
                case FRAG_INFO_1:
                    rootView = inflater.inflate(R.layout.fragment_setup_info_1, container, false);
                    break;
                case FRAG_CIRCLE_CYCLE_MODE:
                    rootView = inflater.inflate(R.layout.fragment_setup_plan, container, false);
                    if(rootView == null) break;

                    final EditText mobileNumber = (EditText) rootView.findViewById(R.id.mobile_number_edit_text);
                    final Spinner stateSpinner = (Spinner) rootView.findViewById(R.id.spinner_state);
                    final TextView tncTextView = (TextView) rootView.findViewById(R.id.tnc_text_view);
                    tncTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    mobileNumber.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) { }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String number = mobileNumber.getText().toString();
                            if(number!=null && number.length()>=4) {

                                if(dbHelper == null) {
                                    dbHelper = new DataBaseHelper(getActivity());
                                }
                                String numberState = dbHelper.getMobileNumberState(Long.parseLong(number));
                                if (numberState != null && !numberState.isEmpty()) {
                                    int index = -1;
                                    for(int i=0;i<stateIDs.length;i++){
                                        if(numberState.equals(stateIDs[i])){
                                            index = i;
                                            break;
                                        }
                                    }
                                    if(index>-1) {
                                        stateSpinner.setSelection(index,true);
                                    }
                                }
                            }
                        }
                    });
                    break;

            }
            return rootView;
        }
    }

    public class SetupPagerAdapter extends FragmentPagerAdapter {

        public SetupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            return SetupWizardFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 3;
        }
    }

    public class FloatViewsTransform implements ViewPager.PageTransformer {

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(1);

            } else if (position <= 1) { // [-1,1]

                //add allviews and give them different translation
                //lower float value gives faster transition

                //page 1
                TextView w1=(TextView)view.findViewById(R.id.setup_page_1_app_name);
                TextView w2=(TextView)view.findViewById(R.id.setup_page_1_text);
                ImageView v1 = (ImageView) view.findViewById(R.id.welcome_image);
                if(v1!=null) v1.setAlpha(1-Math.abs(position));
                if(w1!=null) w1.setTranslationX((position) * (pageWidth / 1));
                if(w2!=null) w2.setTranslationX((position) * (pageWidth / 0.4f));

                //page 2
                TextView page2title=(TextView)view.findViewById(R.id.info_title_page2);
                TextView page2text=(TextView)view.findViewById(R.id.info_text_page2);
                ImageView page2Image = (ImageView) view.findViewById(R.id.info_image_page2);
                if(page2Image!=null) page2Image.setAlpha(0.8f-Math.abs(position));
                if(page2title!=null) page2title.setTranslationX((position) * (pageWidth / 0.5f));
                if(page2text!=null) page2text.setTranslationX((position) * (pageWidth / 0.4f));

                //page 3
                LinearLayout finish = (LinearLayout) view.findViewById(R.id.finish_button);
                if(finish!=null) {
                    finish.setAlpha(1-Math.abs(position));
                    finish.setTranslationX((position) * (pageWidth / 0.3f));
                }

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(1);
            }

        }
    }
}
