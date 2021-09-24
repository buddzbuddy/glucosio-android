package org.deabee.android.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.adapter.CustomListAdapter;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.object.MessageItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MessagesActivity extends AppCompatActivity {
    private ListView lv;
    private DatabaseHandler dB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        lv = findViewById(R.id.userlist);
        dB = new DatabaseHandler(getApplicationContext());
        Toolbar toolbar = findViewById(R.id.activity_messages_toolbar);
        String server = dB.getAppSettings().IP_ADDRESS;
        loadJSON(server, dB.getCurrentUser().getId());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
            //getSupportActionBar().setTitle("Сообщения");
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void loadJSON(final String server, final Integer userId){

        showSimpleProgressDialog(this, "Загрузка данных...","Сообщения...",false);

        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                try {
                    String jsonURL = "http://" + server + ":8090/webdatabase-deabee/api/Messages/GetList/" + userId.toString();
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare(HttpRequest.Method.GET)/*.withData(map)*/.sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                //do something with response
                Log.d("newwwss",result);
                onTaskCompleted(result,jsoncode);
            }
        }.execute();
    }
    public void onTaskCompleted(String response, int serviceCode) {
        Log.d("responsejson", response.toString());
        switch (serviceCode) {
            case jsoncode:

                //if (isSuccess(response)) {
                removeSimpleProgressDialog();  //will remove progress dialog

                lv.setAdapter(new CustomListAdapter(this, parseInfo(response)));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                        MessageItem user = (MessageItem) lv.getItemAtPosition(position);
                        Toast.makeText(MessagesActivity.this, "Selected :" + " " + user.getSender()+", "+ user.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }


    public ArrayList<MessageItem> parseInfo(String response) {
        ArrayList<MessageItem> messageItems = new ArrayList<>();
        try {
            JSONArray dataArray = new JSONArray(response);
            for (int i = 0; i < dataArray.length(); i++) {
                MessageItem messageItem = new MessageItem();
                JSONObject dataobj = dataArray.getJSONObject(i);
                messageItem.setSender(dataobj.getString("sender"));
                messageItem.setMessage(dataobj.getString("message"));
                messageItem.setCreatedAt(dataobj.getString("createdAt"));
                messageItems.add(messageItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("responseString:",response);
        }
        return messageItems;
    }

    private final int jsoncode = 1;
    private static ProgressDialog mProgressDialog;

    public static void removeSimpleProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();

        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showSimpleProgressDialog(Context context, String title,
                                                String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, title, msg);
                mProgressDialog.setCancelable(isCancelable);
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
