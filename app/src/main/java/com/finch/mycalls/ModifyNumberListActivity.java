package com.finch.mycalls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ModifyNumberListActivity extends ActionBarActivity implements ListView.OnItemClickListener{

    int type;
    final int TYPE_LOCAL_LIST=200;
    final int TYPE_EXCLUDED_LIST=201;
    final int TYPE_STD_LIST=202;

    final int TYPE_UNKNOW=-199;

    ArrayAdapter<String> adapter;

    ListView numberListView;
    CheckedTextView selectAllCheck;
    ArrayList<CallDetails> callDetailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide notification bar
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_modify_number_list);
        type = getIntent().getExtras().getInt("activity_type");
        if(type==TYPE_EXCLUDED_LIST){
            setTitle("Delete Excluded Numbers");
        }else if(type==TYPE_STD_LIST){
            setTitle("Delete STD Numbers");
        }else if(type==TYPE_LOCAL_LIST){
            setTitle("Delete Local Numbers");
        }

        numberListView = (ListView) findViewById(R.id.modifyNumberListView);
        selectAllCheck = (CheckedTextView) findViewById(R.id.select_all_check);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled ( true );
        ArrayList<String> list = null;
        switch (type) {
            case TYPE_EXCLUDED_LIST:
                list = AppGlobals.dbHelper.getExcludedNumbers();
                break;
            case TYPE_STD_LIST:
                list = AppGlobals.dbHelper.getSTDNumbers();
                break;
            case TYPE_LOCAL_LIST:
                list = AppGlobals.dbHelper.getLocalNumbers();
                break;
        }
        if (list == null || list.isEmpty()) {

        } else {
            adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,list);
            numberListView.setAdapter(adapter);
            numberListView.setOnItemClickListener(this);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify_number_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int selectedCount=numberListView.getCheckedItemCount();
        if(selectedCount==0){
            menu.findItem(R.id.action_delete_items).setEnabled(false);
        }
        else{
            menu.findItem(R.id.action_delete_items).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_items) {
            switch (type){
                case TYPE_LOCAL_LIST:
                case TYPE_STD_LIST:
                case TYPE_EXCLUDED_LIST:
                    showConfirmationDialog1();
                    break;
            }
            return true;
        }else if(id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationDialog1() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Numbers")
                .setMessage(numberListView.getCheckedItemCount() + " Number(s) will be deleted.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedItems();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSelectedItems() {
        int count = numberListView.getCount();
        SparseBooleanArray sparseBooleanArray = numberListView.getCheckedItemPositions();
        for(int i=0;i<count ; i++){
            if(sparseBooleanArray.get(i) == true){
                switch(type){
                    case TYPE_EXCLUDED_LIST:
                        AppGlobals.dbHelper.deleteNumberFromExcluded(numberListView.getItemAtPosition(i).toString());
                        break;
                    case TYPE_STD_LIST:
                        AppGlobals.dbHelper.deleteNumberFromSTD(numberListView.getItemAtPosition(i).toString());
                        break;
                    case TYPE_LOCAL_LIST:
                        AppGlobals.dbHelper.deleteNumberFromLocal(numberListView.getItemAtPosition(i).toString());
                        break;
                }
            }
        }
        this.finish();
    }

    public void onSelectAllClicked(View v){
        if(selectAllCheck.isChecked()){
            selectAllCheck.setChecked(false);
            int size = numberListView.getCount();
            while(size>0){
                size--;
                numberListView.setItemChecked(size,false);
            }
        }else{
            selectAllCheck.setChecked(true);
            int size = numberListView.getCount();
            while(size>0){
                size--;
                numberListView.setItemChecked(size,true);
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AppGlobals.log(this, "onItemClicked:" + String.valueOf(position));
        if(numberListView.getCheckedItemCount()==numberListView.getCount()){
            selectAllCheck.setChecked(true);
        }else{
            selectAllCheck.setChecked(false);
        }
        invalidateOptionsMenu();
    }
}
