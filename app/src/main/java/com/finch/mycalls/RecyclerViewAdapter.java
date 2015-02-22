package com.finch.mycalls;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

/**
 * Created by Saurabh on 22-02-2015.
 */
public class RecyclerViewAdapter extends
        RecyclerSwipeAdapter<RecyclerViewAdapter.ListItemViewHolder> {

    private List<CallDetails> items;

    RecyclerViewAdapter(List<CallDetails> modelData) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.list_item_swipe,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            ListItemViewHolder viewHolder, int position) {
        CallDetails call = items.get(position);
        viewHolder.name.setText(call.number);
        viewHolder.number.setText(call.calltype);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe_item;
    }


    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.nameItem);
            number = (TextView) itemView.findViewById(R.id.numberItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(view.getContext(), "onItemSelected: " , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}