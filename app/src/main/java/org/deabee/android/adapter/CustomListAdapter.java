package org.deabee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.deabee.android.R;
import org.deabee.android.object.MessageItem;
import android.view.View;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {
    private ArrayList<MessageItem> listData;
    private LayoutInflater layoutInflater;
    public CustomListAdapter(Context aContext, ArrayList<MessageItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }
    @Override
    public int getCount() {
        return listData.size();
    }
    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View v, ViewGroup vg) {
        MessageItemView msgItemView;
        if (v == null) {
            v = layoutInflater.inflate(R.layout.message_list_row, null);
            msgItemView = new MessageItemView();
            msgItemView.sender = (TextView) v.findViewById(R.id.sender);
            msgItemView.message_text = (TextView) v.findViewById(R.id.message_text);
            msgItemView.createdAt = (TextView) v.findViewById(R.id.createdAt);
            v.setTag(msgItemView);
        } else {
            msgItemView = (MessageItemView) v.getTag();
        }
        msgItemView.sender.setText(listData.get(position).getSender());
        msgItemView.message_text.setText(listData.get(position).getMessage());
        msgItemView.createdAt.setText(listData.get(position).getCreatedAt().toString());
        return v;
    }
    static class MessageItemView {
        TextView sender;
        TextView message_text;
        TextView createdAt;
    }
}