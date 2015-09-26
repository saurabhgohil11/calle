package com.evadroid.calle.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.CostType;
import com.evadroid.calle.R;

import java.util.ArrayList;

public class ModifyNumberListActivity extends ActionBarActivity implements ListView.OnItemClickListener{

    int activityType;
    CostType costType;
    final int TYPE_LOCAL_LIST=200;
    final int TYPE_EXCLUDED_LIST=201;
    final int TYPE_STD_LIST=202;

    ArrayAdapter<String> adapter;

    ListView numberListView;
    CheckedTextView selectAllCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_modify_number_list);
        activityType = getIntent().getExtras().getInt("activity_type");
        if(activityType ==TYPE_EXCLUDED_LIST) {
            costType = CostType.FREE;
            setTitle(getResources().getString(R.string.title_delete_excluded_numbers));
        }else if(activityType ==TYPE_STD_LIST) {
            costType = CostType.STD;
            setTitle(getResources().getString(R.string.title_delete_std_numbers));
        }else if(activityType ==TYPE_LOCAL_LIST) {
            costType = CostType.LOCAL;
            setTitle(getResources().getString(R.string.title_delete_local_numbers));
        }

        numberListView = (ListView) findViewById(R.id.modifyNumberListView);
        selectAllCheck = (CheckedTextView) findViewById(R.id.select_all_check);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionbar = getSupportActionBar ();
        actionbar.setDisplayHomeAsUpEnabled ( true );
        ArrayList<String> list;

        list = AppGlobals.getDataBaseHelper(this).getUserSpecifiedNumbers(costType);
        if (list == null || list.isEmpty()) {

        } else {
            adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice,list);
            numberListView.setAdapter(adapter);
            numberListView.setOnItemClickListener(this);
        }
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
            switch (activityType){
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
            if(sparseBooleanArray.get(i)){
                AppGlobals.getDataBaseHelper(this).deleteUserSpecifiedNumber(numberListView.getItemAtPosition(i).toString());
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
        //AppGlobals.log(this, "onItemClicked:" + String.valueOf(position));
        if(numberListView.getCheckedItemCount()==numberListView.getCount()){
            selectAllCheck.setChecked(true);
        }else{
            selectAllCheck.setChecked(false);
        }
        invalidateOptionsMenu();
    }
}
