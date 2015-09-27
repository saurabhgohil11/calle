package com.evadroid.calle.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.CallDetails;
import com.evadroid.calle.CostType;
import com.evadroid.calle.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;


public class NumberListActivity extends ActionBarActivity {

    final int TYPE_LOCAL_LIST=200;
    final int TYPE_EXCLUDED_LIST=201;
    final int TYPE_STD_LIST=202;

    final int TYPE_UNKNOW=-199;
    int activityType =TYPE_UNKNOW;
    CostType costType;

    ListView numbersListView;
    TextView noItemsTextView;

    View promptsView;

    ArrayAdapter<String> adapter;
    MenuItem modifyListMenuButton;
    ArrayList<String> numberList;
    ArrayList<CallDetails> callDetailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_number_list);

        if(!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String action = getIntent().getAction();

        if(action.equals("com.evadroid.action.EXCLUDED_NUMBERS")){
            activityType =TYPE_EXCLUDED_LIST;
            costType = CostType.FREE;
            setTitle(getResources().getString(R.string.title_excluded_numbers));
        }else if(action.equals("com.evadroid.action.STD_NUMBERS")){
            activityType =TYPE_STD_LIST;
            costType = CostType.STD;
            setTitle(getResources().getString(R.string.title_std_numbers));
        }else if(action.equals("com.evadroid.action.LOCAL_NUMBERS")){
            activityType =TYPE_LOCAL_LIST;
            costType = CostType.LOCAL;
            setTitle(getResources().getString(R.string.title_local_numbers));
        }

        numbersListView = (ListView) findViewById(R.id.numberListView);
        noItemsTextView = (TextView) findViewById(R.id.no_items_text_view);

        numberList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,numberList);
        numbersListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled ( true );
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.number_list_menu, menu);
        modifyListMenuButton = menu.findItem(R.id.action_modify_list);

        if(numberList==null || numberList.isEmpty()){
                modifyListMenuButton.setVisible(false);
        } else{
            modifyListMenuButton.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_a_number) {
            showAddNumberDialog(this);
            return true;
        }
        else if( id == R.id.action_modify_list) {
            Intent i = new Intent(this,ModifyNumberListActivity.class);
            i.putExtra("activity_type", activityType);
            startActivity(i);
        }else if(id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddNumberDialog(final NumberListActivity context) {
        LayoutInflater li = LayoutInflater.from(context);
        promptsView = li.inflate(R.layout.dialog_number_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setTitle("Enter Number to Add: ");
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.number_edit_text);
        final ImageButton selectContactButton = (ImageButton) promptsView.findViewById(R.id.select_contact);

        selectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // BoD con't: CONTENT_TYPE instead of CONTENT_ITEM_TYPE
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
            }
        });

        // set dialog message
        Resources res = getResources();
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(res.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                        String number = userInput.getText().toString();
                        if (number == null || number.isEmpty()) {
                            Toast.makeText(context, R.string.number_too_short, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            PhoneNumberUtil.getInstance().parse(number, AppGlobals.userCountryCode);
                        } catch (NumberParseException e) {
                            Toast.makeText(context, R.string.enter_a_valid_number, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(number, costType);
                        updateListView();
                    }
                });
        alertDialogBuilder.setNegativeButton(res.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        userInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    Cursor c = null;
                    try {
                        c = getContentResolver().query(uri, new String[]{
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.TYPE},
                                null, null, null);

                        if (c != null && c.moveToFirst()) {
                            String number = c.getString(0);
                            int type = c.getInt(1);
                            if(promptsView != null) {
                                EditText userInput = (EditText) promptsView.findViewById(R.id.number_edit_text);
                                userInput.setText(number);
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            }
        }
    }

    public void updateListView(){
        numberList.clear();
        numberList.addAll(AppGlobals.getDataBaseHelper().getUserSpecifiedNumbers(costType));
        adapter.notifyDataSetChanged();
        numbersListView.invalidate();

        if(numberList==null || numberList.isEmpty()){
            noItemsTextView.setVisibility(View.VISIBLE);
            numbersListView.setVisibility(View.GONE);
            if(modifyListMenuButton!=null)
                modifyListMenuButton.setVisible(false);
        } else {
            noItemsTextView.setVisibility(View.GONE);
            numbersListView.setVisibility(View.VISIBLE);
            if(modifyListMenuButton!=null)
                modifyListMenuButton.setVisible(true);
        }
        invalidateOptionsMenu();
    }

}

