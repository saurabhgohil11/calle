package com.evadroid.calle.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.evadroid.calle.R;

public class ContactOptionsPopup {
    public static volatile ContactOptionsPopup instance;
    public static Context mContext;
    String mPhoneNumber;

    public static ContactOptionsPopup getInstance(Context c) {
        if (instance == null) {
            instance = new ContactOptionsPopup(c);
        }
        mContext = c;
        return instance;
    }

    ContactOptionsPopup(Context c) {
        mContext = c;
    }

    public void show(View v, final String phoneNumber, boolean isListView) {
        mPhoneNumber = phoneNumber;
        if (mPhoneNumber == null || mPhoneNumber.isEmpty())
            return;

        LayoutInflater layoutInflater
                = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.contact_popup_options, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        Button callBtn = (Button) popupView.findViewById(R.id.call_button);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                popupWindow.dismiss();
            }
        });
        Button msgBtn = (Button) popupView.findViewById(R.id.msg_button);
        msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", mPhoneNumber, null));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                popupWindow.dismiss();
            }
        });
        if (isListView) {
            int yOffset = v.getHeight() - (int) mContext.getResources().getDimension(R.dimen.log_list_item_padding_top_bottom);
            int xOffset = (int) mContext.getResources().getDimension(R.dimen.log_list_item_contact_image_size)
                    + (int) mContext.getResources().getDimension(R.dimen.log_list_item_padding_side);
            popupWindow.showAsDropDown(v, xOffset + 5, -yOffset);
        } else {
            popupWindow.showAsDropDown(v);
        }
    }

}
