package com.evadroid.calle;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.utils.DateTimeUtils;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.List;

public class SimpleRecyclerViewAdapter extends
        RecyclerView.Adapter<SimpleRecyclerViewAdapter.ListItemViewHolder> {

    private List<CallDetails> items;
    private Context mContext;
    Dialog logDetailDialog;

    boolean showCostType;

    SimpleRecyclerViewAdapter(Context context, List<CallDetails> modelData, boolean showCostType) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
        this.showCostType = showCostType;
        this.mContext = context;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.list_item_log,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, final int position) {
        CallDetails call = items.get(position);
        viewHolder.data = call;

        if (showCostType) {
            viewHolder.type.setVisibility(View.VISIBLE);
            viewHolder.number.setVisibility(View.GONE);
        } else {
            viewHolder.type.setVisibility(View.GONE);
            viewHolder.number.setVisibility(View.VISIBLE);
        }

        viewHolder.number.setText(call.getPhoneNumber());

        if (call.getCachedContactName() != null && !call.getCachedContactName().isEmpty()) {
            viewHolder.name.setText(call.getCachedContactName());
            viewHolder.contactImageButton.setText(String.valueOf(call.getCachedContactName().toUpperCase().charAt(0)));
            viewHolder.phoneNumberType.setVisibility(View.VISIBLE);
        } else {
            viewHolder.name.setText(call.getPhoneNumber());
            viewHolder.contactImageButton.setText("?");
            viewHolder.number.setVisibility(View.GONE);
            if (showCostType) {
                viewHolder.phoneNumberType.setVisibility(View.VISIBLE);
            } else {
                viewHolder.phoneNumberType.setVisibility(View.GONE);
            }
        }

        viewHolder.backgroundIndex = AppGlobals.getRandomBackgroundIndex();
        viewHolder.contactImageButton.setBackgroundResource(AppGlobals.bgColoredImages[viewHolder.backgroundIndex]);

        if (call.getPhoneNumberType() == PhoneNumberUtil.PhoneNumberType.MOBILE) {
            viewHolder.phoneNumberType.setImageResource(R.drawable.ic_type_mobile);
        } else {
            viewHolder.phoneNumberType.setImageResource(R.drawable.ic_type_fixedline);
        }

        String time = DateTimeUtils.timeToRelativeString(call.getDate());
        if (time != null)
            viewHolder.time.setText(time);
        else
            viewHolder.time.setText("");

        String duration;
        if (AppGlobals.isMinuteMode) {
            duration = DateTimeUtils.timeToRoundedString(call.getDuration());
        } else {
            duration = DateTimeUtils.timeToString(call.getDuration());
        }
        if (duration != null)
            viewHolder.duration.setText(duration);
        else
            viewHolder.duration.setText("");

        String type = call.getCostAndCallTypeString();
        if (type != null)
            viewHolder.type.setText(type);
        else
            viewHolder.type.setText("");


        viewHolder.upperLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogDetailDialog(viewHolder.data, viewHolder.backgroundIndex);
            }
        });
        viewHolder.upperLayout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                showContextMenu(viewHolder.data);
                return true;
            }
        });
    }

    private void showContextMenu(final CallDetails callDetails) {
        final CostType userSpecifiedCostType = AppGlobals.getDataBaseHelper().getUserSpecifiedNumberType(callDetails.getPhoneNumber());
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        Resources res = mContext.getResources();
        CharSequence fakeMneuItems[] = new CharSequence[]{
                res.getString(R.string.action_delete),
                res.getString(R.string.action_add_to_local),
                res.getString(R.string.action_add_to_std),
                res.getString(R.string.action_add_to_free),
                res.getString(R.string.action_copy_number)};
        if (userSpecifiedCostType != null) {
            switch (userSpecifiedCostType) {
                case LOCAL:
                    fakeMneuItems[1] = res.getString(R.string.action_remove_from_local);
                    break;
                case STD:
                    fakeMneuItems[2] = res.getString(R.string.action_remove_from_std);
                    break;
                case FREE:
                    fakeMneuItems[3] = res.getString(R.string.action_remove_from_free);
                    break;
            }
        }

        b.setItems(fakeMneuItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onOptionsItemSelected(which);
            }

            private void onOptionsItemSelected(int item) {
                String msg;
                String userSpecifiedNumber = AppGlobals.getDataBaseHelper().isUserSpecifiedNumberExists(callDetails.getPhoneNumber());
                switch (item) {
                    case 3:  //action_add_to_free
                        if (userSpecifiedCostType == CostType.FREE) {
                            AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                            msg = String.format(mContext.getResources().getString(R.string.removed_from_free), callDetails.getPhoneNumber());
                        } else {
                            AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.FREE);
                            msg = String.format(mContext.getResources().getString(R.string.added_to_free), callDetails.getPhoneNumber());
                        }

                        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                        AppGlobals.sendUpdateMessage();
                        break;
                    case 1:  //action_add_to_local
                        if (userSpecifiedCostType == CostType.LOCAL) {
                            AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                            msg = String.format(mContext.getResources().getString(R.string.removed_from_local), callDetails.getPhoneNumber());
                        } else {
                            AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.LOCAL);
                            msg = String.format(mContext.getResources().getString(R.string.added_to_local), callDetails.getPhoneNumber());
                        }
                        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                        AppGlobals.sendUpdateMessage();
                        break;
                    case 2:  //action_add_to_std
                        if (userSpecifiedCostType == CostType.STD) {
                            AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                            msg = String.format(mContext.getResources().getString(R.string.removed_from_std), callDetails.getPhoneNumber());
                        } else {
                            AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.STD);
                            msg = String.format(mContext.getResources().getString(R.string.added_to_std), callDetails.getPhoneNumber());
                        }
                        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                        AppGlobals.sendUpdateMessage();
                        break;
                    case 0:  //action_delete
                        int rowsDeleted = AppGlobals.getDataBaseHelper().deleteNumberFromLogs(callDetails.callID);
                        if (rowsDeleted > 0) {
                            Toast.makeText(mContext, R.string.delete_success, Toast.LENGTH_SHORT).show();
                            AppGlobals.sendUpdateMessage();
                        } else {
                            Toast.makeText(mContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 4:  //action_copy_number
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AppGlobals.mContext
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData
                                .newPlainText("phonenumber", callDetails.getPhoneNumber());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, R.string.copy_successful, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        String title = (callDetails.getCachedContactName() == null || callDetails.getCachedContactName().isEmpty()) ?
                callDetails.getNationalNumber() : callDetails.getCachedContactName();
        b.setTitle(Html.fromHtml("<font color='#00796B'>" + title + "</font>"));
        b.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void dismissDialog() {
        if (logDetailDialog != null)
            logDetailDialog.dismiss();
    }

    public void showLogDetailDialog(final CallDetails callDetails, int bgIndex) {
        final CostType userSpecifiedCostType = AppGlobals.getDataBaseHelper().getUserSpecifiedNumberType(callDetails.getPhoneNumber());

        //AppGlobals.log(this, "showLogDetailDialog: " + callDetails.toString());
        logDetailDialog = new Dialog(mContext);
        logDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        logDetailDialog.setContentView(R.layout.dialog_log_detail);
        logDetailDialog.show();

        RelativeLayout rootView = (RelativeLayout) logDetailDialog.findViewById(R.id.log_detail_dialog_layout);
        rootView.setBackgroundResource(AppGlobals.funkyColors[bgIndex]);

        final TextView mCallerName = (TextView) logDetailDialog.findViewById(R.id.caller_name);
        final TextView mCallerNumber = (TextView) logDetailDialog.findViewById(R.id.caller_number);
        final TextView mPhoneType = (TextView) logDetailDialog.findViewById(R.id.number_type);
        final TextView mCostType = (TextView) logDetailDialog.findViewById(R.id.cost_type);
        final TextView mDuration = (TextView) logDetailDialog.findViewById(R.id.duration);
        final TextView mDate = (TextView) logDetailDialog.findViewById(R.id.date);
        final TextView mLocation = (TextView) logDetailDialog.findViewById(R.id.location);
        final ImageView mCallTypeIcon = (ImageView) logDetailDialog.findViewById(R.id.call_type_image);

        if (callDetails.getCachedContactName() != null && !callDetails.getCachedContactName().isEmpty()) {
            mCallerName.setText(callDetails.getCachedContactName());
            mCallerNumber.setText(callDetails.getPhoneNumber());
        } else {
            mCallerName.setText(callDetails.getPhoneNumber());
            mCallerNumber.setVisibility(View.GONE);
            mCallTypeIcon.setVisibility(View.GONE);
        }
        mPhoneType.setText(callDetails.getPhoneNumberTypeToDisplay());

        if (callDetails.getCallType() == CallType.INCOMING) {
            mCallTypeIcon.setImageResource(R.drawable.ic_incoming);
        } else if (callDetails.getCallType() == CallType.OUTGOING) {
            mCallTypeIcon.setImageResource(R.drawable.ic_outgoing);
        } else if (callDetails.getCallType() == CallType.MISSED) {
            mCallTypeIcon.setImageResource(R.drawable.ic_missed);
        }


        //String costType = DateTimeUtils.toDisplayCase(callDetails.getCostType().toString().toLowerCase());
        StringBuffer costString = new StringBuffer();
        if (userSpecifiedCostType != null)
            costString.append(mContext.getResources().getString(R.string.user_specified) + " ");
        costString.append(callDetails.getCostAndCallTypeString());
        mCostType.setText(costString);

        String time = DateTimeUtils.timeToDateString(callDetails.getDate());
        if (time != null)
            mDate.setText(time);
        else
            mDate.setText("");

        String duration = DateTimeUtils.timeToString(callDetails.getDuration());
        if (duration != null)
            mDuration.setText(duration);
        else
            mDuration.setText("");

        mLocation.setText(callDetails.getNumberRegionToDisplay());

        Button callButton = (Button) logDetailDialog.findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callDetails.getPhoneNumber()));
                mContext.startActivity(intent);
            }
        });

        Button smsButton = (Button) logDetailDialog.findViewById(R.id.message_button);
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", callDetails.getPhoneNumber(), null));
                mContext.startActivity(intent);
            }
        });

        final ImageButton mMenuButton = (ImageButton) logDetailDialog.findViewById(R.id.option_menu_button);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, mMenuButton);
                popup.getMenuInflater().inflate(R.menu.log_detail_dialog_menu, popup.getMenu());
                if (userSpecifiedCostType != null) {
                    switch (userSpecifiedCostType) {
                        case LOCAL:
                            popup.getMenu().getItem(1).setTitle(R.string.action_remove_from_local);
                            break;
                        case STD:
                            popup.getMenu().getItem(2).setTitle(R.string.action_remove_from_std);
                            break;
                        case FREE:
                            popup.getMenu().getItem(3).setTitle(R.string.action_remove_from_free);
                            break;
                    }
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        onOptionsItemSelected(item);
                        return true;
                    }

                    private void onOptionsItemSelected(MenuItem item) {
                        String msg;
                        String userSpecifiedNumber = AppGlobals.getDataBaseHelper().isUserSpecifiedNumberExists(callDetails.getPhoneNumber());
                        switch (item.getItemId()) {
                            case R.id.action_add_to_free:
                                if (userSpecifiedCostType == CostType.FREE) {
                                    AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                                    msg = String.format(mContext.getResources().getString(R.string.removed_from_free), callDetails.getPhoneNumber());
                                } else {
                                    AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.FREE);
                                    msg = String.format(mContext.getResources().getString(R.string.added_to_free), callDetails.getPhoneNumber());
                                    mCostType.setText(mContext.getResources().getString(R.string.user_specified) + " " + mContext.getResources().getString(R.string.free));
                                }

                                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                                AppGlobals.sendUpdateMessage();
                                dismissDialog();
                                break;
                            case R.id.action_add_to_local:
                                if (userSpecifiedCostType == CostType.LOCAL) {
                                    AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                                    msg = String.format(mContext.getResources().getString(R.string.removed_from_local), callDetails.getPhoneNumber());
                                } else {
                                    AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.LOCAL);
                                    msg = String.format(mContext.getResources().getString(R.string.added_to_local), callDetails.getPhoneNumber());
                                    mCostType.setText(mContext.getResources().getString(R.string.user_specified) + " " + mContext.getResources().getString(R.string.local));
                                }
                                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                                AppGlobals.sendUpdateMessage();
                                dismissDialog();
                                break;
                            case R.id.action_add_to_std:
                                if (userSpecifiedCostType == CostType.STD) {
                                    AppGlobals.getDataBaseHelper().deleteUserSpecifiedNumber(userSpecifiedNumber);
                                    msg = String.format(mContext.getResources().getString(R.string.removed_from_std), callDetails.getPhoneNumber());
                                } else {
                                    AppGlobals.getDataBaseHelper().addUserSpecifiedNumber(callDetails.getPhoneNumber(), CostType.STD);
                                    msg = String.format(mContext.getResources().getString(R.string.added_to_std), callDetails.getPhoneNumber());
                                    mCostType.setText(mContext.getResources().getString(R.string.user_specified) + " " + AppGlobals.mContext.getResources().getString(R.string.std));
                                }
                                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                                AppGlobals.sendUpdateMessage();
                                dismissDialog();
                                break;
                            case R.id.action_delete_log:
                                int rowsDeleted = AppGlobals.getDataBaseHelper().deleteNumberFromLogs(callDetails.callID);
                                if (rowsDeleted > 0) {
                                    Toast.makeText(mContext, R.string.delete_success, Toast.LENGTH_SHORT).show();
                                    AppGlobals.sendUpdateMessage();
                                    dismissDialog();
                                } else {
                                    Toast.makeText(mContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                                }

                                break;
                            /*case R.id.action_hide_log:

                                break;*/
                            case R.id.action_copy_number:
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AppGlobals.mContext
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData
                                        .newPlainText("phonenumber", callDetails.getPhoneNumber());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(mContext, R.string.copy_successful, Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                popup.show();
            }
        });
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        CallDetails data;  //data
        RelativeLayout upperLayout;

        TextView name;  //shows name or number if name is not avialable
        TextView number;
        ImageView phoneNumberType; //fixed line or mobile icon
        Button contactImageButton; //shows name text
        TextView time;
        TextView duration;
        TextView type;
        int backgroundIndex;

        public ListItemViewHolder(View itemView) {
            super(itemView);

            upperLayout = (RelativeLayout) itemView.findViewById(R.id.log_upper_item_container);
            name = (TextView) itemView.findViewById(R.id.nameorNumberItem);
            number = (TextView) itemView.findViewById(R.id.phoneNumberItem);
            phoneNumberType = (ImageView) itemView.findViewById(R.id.phoneNumeberTypeItem);
            contactImageButton = (Button) itemView.findViewById(R.id.contactImageButton);
            time = (TextView) itemView.findViewById(R.id.timeItem);
            duration = (TextView) itemView.findViewById(R.id.durationItem);
            type = (TextView) itemView.findViewById(R.id.typeItem);
        }

    }
}