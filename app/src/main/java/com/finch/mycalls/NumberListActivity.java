package com.finch.mycalls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class NumberListActivity extends ActionBarActivity {

    final int TYPE_LOCAL_LIST=200;
    final int TYPE_EXCLUDED_LIST=201;
    final int TYPE_STD_LIST=202;

    final int TYPE_UNKNOW=-199;
    int type=TYPE_UNKNOW;

    ListView numbersListView;
    TextView noItemsTextView;

    ArrayAdapter<String> adapter;
    MenuItem modifyListMenuButton;
    ArrayList<String> numberList;
    ArrayList<CallDetails> callDetailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide notification bar
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_number_list);
        String action = getIntent().getAction();

        if(action.equals("com.app.postpaidcallapp.action.EXCLUDED_NUMBERS")){
            type=TYPE_EXCLUDED_LIST;
            setTitle("Excluded Numbers");
        }else if(action.equals("com.app.postpaidcallapp.action.STD_NUMBERS")){
            type=TYPE_STD_LIST;
            setTitle("STD Numbers");
        }else if(action.equals("com.app.postpaidcallapp.action.LOCAL_NUMBERS")){
            type=TYPE_LOCAL_LIST;
            setTitle("Local Numbers");
        }

        numbersListView = (ListView) findViewById(R.id.numberListView);
        noItemsTextView = (TextView) findViewById(R.id.no_items_text_view);

        numberList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,numberList);
        numbersListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled ( true );
//        updateListView();
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
            i.putExtra("activity_type",type);
            startActivity(i);
        }else if(id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddNumberDialog(final NumberListActivity context) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_number_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setTitle("Enter Number to Add: ");
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.number_edit_text);


        // set dialog message
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
                                String number = userInput.getText().toString();
                                if (number == null || number.isEmpty()) {
                                    Toast.makeText(context,"Number can't be empty",Toast.LENGTH_SHORT).show();
                                }else{
                                    switch (type){
                                        case TYPE_EXCLUDED_LIST:
                                            AppGlobals.dbHelper.addExcludedNumber(number);
                                            break;
                                        case TYPE_STD_LIST:
                                            AppGlobals.dbHelper.addSTDNumber(number);
                                            break;
                                        case TYPE_LOCAL_LIST:
                                            AppGlobals.dbHelper.addLocalNumber(number);
                                            break;
                                    }
                                    updateListView();
                                }

                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
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

    public void updateListView(){
        switch (type){
            case TYPE_EXCLUDED_LIST:
                numberList.clear();
                numberList.addAll(AppGlobals.dbHelper.getExcludedNumbers());
                adapter.notifyDataSetChanged();
                break;
            case TYPE_STD_LIST:
                numberList.clear();
                numberList.addAll(AppGlobals.dbHelper.getSTDNumbers());
                adapter.notifyDataSetChanged();
                break;
            case TYPE_LOCAL_LIST:
                numberList.clear();
                numberList.addAll(AppGlobals.dbHelper.getLocalNumbers());
                adapter.notifyDataSetChanged();
                break;
        }
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


