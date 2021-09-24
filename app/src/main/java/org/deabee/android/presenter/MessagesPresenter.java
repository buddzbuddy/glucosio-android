package org.deabee.android.presenter;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.deabee.android.activity.MessagesActivity;
import org.deabee.android.db.DatabaseHandler;

public class MessagesPresenter {
    private MessagesActivity activity;
    private DatabaseHandler db;
    public MessagesPresenter(MessagesActivity activity) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
    }
}
