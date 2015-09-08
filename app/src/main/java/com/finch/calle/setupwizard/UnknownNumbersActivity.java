package com.finch.calle.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;

import com.finch.calle.AppGlobals;
import com.finch.calle.CallDetails;
import com.finch.calle.CostType;
import com.finch.calle.DataBaseHelper;
import com.finch.calle.HomeActivity;
import com.finch.calle.R;

import java.util.ArrayList;

public class UnknownNumbersActivity extends ActionBarActivity {

    ListView unknownListView;
    UnknownListViewAdapter adapter;
    DataBaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unknown_numbers);
        dbHelper = AppGlobals.getDataBaseHelper(this);
        if (dbHelper == null) {
            dbHelper = new DataBaseHelper(this);
        }
        ArrayList<CallDetails> unknwonList = dbHelper.getUnknownLogsHistory();
        unknownListView = (ListView) findViewById(R.id.unknown_list_view);
        adapter = new UnknownListViewAdapter(this,unknwonList);
        unknownListView.setAdapter(adapter);
    }

    public void onDoneClicked(View v) {
        ArrayList<CallDetails> dataList = adapter.getAllData();
        for(int i=0;i<dataList.size();i++) {
            if (dataList.get(i).getCostType()!= null && dataList.get(i).getCostType() != CostType.UNKNOWN)
                dbHelper.addUserSpecifiedNumber(dataList.get(i).getPhoneNumber(), dataList.get(i).getCostType());
        }
        this.finish();
        Intent i = new Intent(this,HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
