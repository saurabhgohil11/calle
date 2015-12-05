package com.evadroid.calle.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.CostType;
import com.evadroid.calle.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;


public class NumberListActivity extends AppCompatActivity {

    final int TYPE_LOCAL_LIST = 200;
    final int TYPE_EXCLUDED_LIST = 201;
    final int TYPE_STD_LIST = 202;

    final int TYPE_UNKNOW = -199;
    int activityType = TYPE_UNKNOW;
    CostType costType;

    ListView numberListView;
    TextView noItemsTextView;

    View promptsView;

    ArrayAdapter<String> adapter;
    MenuItem modifyListMenuButton;
    ArrayList<String> numberList;
    SparseBooleanArray mCheckedItems;
    CheckedTextView mSelectAllCheck;
    ActionBarCallBack mActionBarCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_number_list);

        if (!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String action = getIntent().getAction();

        if (action.equals("com.evadroid.action.EXCLUDED_NUMBERS")) {
            activityType = TYPE_EXCLUDED_LIST;
            costType = CostType.FREE;
            setTitle(getResources().getString(R.string.title_excluded_numbers));
        } else if (action.equals("com.evadroid.action.STD_NUMBERS")) {
            activityType = TYPE_STD_LIST;
            costType = CostType.STD;
            setTitle(getResources().getString(R.string.title_std_numbers));
        } else if (action.equals("com.evadroid.action.LOCAL_NUMBERS")) {
            activityType = TYPE_LOCAL_LIST;
            costType = CostType.LOCAL;
            setTitle(getResources().getString(R.string.title_local_numbers));
        }

        numberListView = (ListView) findViewById(R.id.numberListView);
        noItemsTextView = (TextView) findViewById(R.id.no_items_text_view);
        mSelectAllCheck = (CheckedTextView) findViewById(R.id.select_all_check);

        mCheckedItems = new SparseBooleanArray();
        numberList = new ArrayList<>();
        adapter = new NumberListAdapter(this, R.layout.number_list_item, numberList);
        numberListView.setAdapter(adapter);
        numberListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mCheckedItems = new SparseBooleanArray(numberListView.getCount());
                mCheckedItems.put(position, true);
                mActionBarCallback = new ActionBarCallBack();
                startActionMode(mActionBarCallback);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        updateListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.number_list_menu, menu);
        modifyListMenuButton = menu.findItem(R.id.action_modify_list);

        if (numberList == null || numberList.isEmpty()) {
            modifyListMenuButton.setVisible(false);
        } else {
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
        } else if (id == R.id.action_modify_list) {
            mCheckedItems = new SparseBooleanArray(numberListView.getCount());
            mActionBarCallback = new ActionBarCallBack();
            startActionMode(mActionBarCallback);
        } else if (id == android.R.id.home) {
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
                        String number = userInput.getText().toString().replaceAll("\\s", "");
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
                            if (promptsView != null) {
                                EditText userInput = (EditText) promptsView.findViewById(R.id.number_edit_text);
                                userInput.setText(number.replaceAll("\\s", ""));
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

    public void onSelectAllClicked(View v) {
        if (mSelectAllCheck.isChecked()) {
            mSelectAllCheck.setChecked(false);
            mCheckedItems.clear();
        } else {
            mSelectAllCheck.setChecked(true);
            int size = numberListView.getCount();
            while (size > 0) {
                size--;
                mCheckedItems.put(size, true);
            }
        }
        adapter.notifyDataSetInvalidated();
        mActionBarCallback.actionMode.invalidate();
    }

    public void updateListView() {
        numberList.clear();
        numberList.addAll(AppGlobals.getDataBaseHelper().getUserSpecifiedNumbers(costType));
        adapter.notifyDataSetChanged();
        numberListView.invalidate();

        if (numberList == null || numberList.isEmpty()) {
            noItemsTextView.setVisibility(View.VISIBLE);
            numberListView.setVisibility(View.GONE);
            if (modifyListMenuButton != null)
                modifyListMenuButton.setVisible(false);
        } else {
            noItemsTextView.setVisibility(View.GONE);
            numberListView.setVisibility(View.VISIBLE);
            if (modifyListMenuButton != null)
                modifyListMenuButton.setVisible(true);
        }
        invalidateOptionsMenu();
    }

    //adapter to handle checkedTextView states
    public class NumberListAdapter extends ArrayAdapter<String> {

        List<String> items;
        int itemResource;
        LayoutInflater inflater;
        Drawable checkIndicator;

        public NumberListAdapter(Context ctx, int resource, List<String> objects) {
            super(ctx, resource, objects);
            this.items = objects;
            this.itemResource = resource;
            this.inflater = ((NumberListActivity) ctx).getLayoutInflater();
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(itemResource, parent, false);
            }
            CheckedTextView checkView = (CheckedTextView) convertView;

            checkView.setText(items.get(pos));
            if (mCheckedItems.get(pos))
                checkView.setChecked(true);
            else
                checkView.setChecked(false);

            int[] attrs = {android.R.attr.listChoiceIndicatorMultiple};
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
            checkIndicator = ta.getDrawable(0);
            ta.recycle();
            if (mActionBarCallback != null)
                checkView.setCheckMarkDrawable(checkIndicator);
            else
                checkView.setCheckMarkDrawable(null);

            return convertView;
        }
    }

    //action mode to delete items
    public class ActionBarCallBack implements ListView.OnItemClickListener,
            ActionMode.Callback {

        ActionMode actionMode;
        AdapterView.OnItemClickListener previousListener;
        AdapterView.OnItemLongClickListener previousLongClickListener;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.modify_number_list_menu, menu);
            previousListener = numberListView.getOnItemClickListener();
            previousLongClickListener = numberListView.getOnItemLongClickListener();
            numberListView.setOnItemClickListener(this);
            numberListView.setOnItemLongClickListener(null);
            mSelectAllCheck.setVisibility(View.VISIBLE);
            int selectedCount = mCheckedItems.size();
            if (selectedCount == 0) {
                menu.findItem(R.id.action_delete_items).setEnabled(false);
            } else {
                menu.findItem(R.id.action_delete_items).setEnabled(true);
            }
            if (selectedCount == numberListView.getCount()) {
                mSelectAllCheck.setChecked(true);
            } else {
                mSelectAllCheck.setChecked(false);
            }
            actionMode.setTitle(selectedCount + " Items selected");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedCount = mCheckedItems.size();

            if (selectedCount == 0) {
                menu.findItem(R.id.action_delete_items).setEnabled(false);
            } else {
                menu.findItem(R.id.action_delete_items).setEnabled(true);
            }
            actionMode.setTitle(selectedCount + " Items selected");
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_items:
                    showConfirmationDialog1();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mSelectAllCheck.setVisibility(View.GONE);
            numberListView.setOnItemClickListener(previousListener);
            numberListView.setOnItemLongClickListener(previousLongClickListener);
            mCheckedItems.clear();
            mActionBarCallback = null;
            updateListView();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            CheckedTextView checkView = (CheckedTextView) view;
            boolean state = checkView.isChecked();
            checkView.setChecked(!state);

            if (!mCheckedItems.get(position))
                mCheckedItems.put(position, true);
            else
                mCheckedItems.delete(position);

            if (mCheckedItems.size() == numberListView.getCount()) {
                mSelectAllCheck.setChecked(true);
            } else {
                mSelectAllCheck.setChecked(false);
            }
            actionMode.invalidate();
        }

        private void showConfirmationDialog1() {
            new AlertDialog.Builder(NumberListActivity.this)
                    .setTitle("Delete Numbers")
                    .setMessage(mCheckedItems.size() + " Number(s) will be deleted.")
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
            for (int i = 0; i < count; i++) {
                if (mCheckedItems.get(i)) {
                    AppGlobals.getDataBaseHelper(getApplicationContext()).deleteUserSpecifiedNumber(numberListView.getItemAtPosition(i).toString());
                }
            }
            actionMode.finish();
        }
    }
}




