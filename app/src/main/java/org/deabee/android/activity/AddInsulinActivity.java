package org.deabee.android.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.presenter.AddGlucosePresenter;
import org.deabee.android.presenter.AddInsulinPresenter;
import org.deabee.android.tools.FormatDateTime;
import org.deabee.android.tools.LabelledSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AddInsulinActivity extends AddReadingActivity {
    private LabelledSpinner readingTypeSpinner;
    private TextView readingTextView;
    private RadioGroup radioInsulinTypeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_insulin);
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        this.retrieveExtra();

        AddInsulinPresenter presenter = new AddInsulinPresenter(this);
        setPresenter(presenter);
        presenter.setReadingTimeNow();

        readingTypeSpinner = findViewById(R.id.insulin_add_reading_type);
        String server = presenter.dB.getAppSettings().IP_ADDRESS;
        loadMealtimeItems(server);
        //readingTypeSpinner.setItemsArray(R.array.dialog_add_measured_list);
        readingTextView = findViewById(R.id.insulin_add_unit);
        radioInsulinTypeGroup = findViewById(R.id.insulinTypeRadioGroup);

        this.createDateTimeViewAndListener();
        this.createFANViewAndListener();

// If an id is passed, open the activity in edit mode
        Calendar cal = Calendar.getInstance();
        FormatDateTime dateTime = new FormatDateTime(getApplicationContext());
        this.getAddDateTextView().setText(dateTime.getDate(cal));
        this.getAddTimeTextView().setText(dateTime.getTime(cal));
    }

    @SuppressLint("StaticFieldLeak")
    private void loadMealtimeItems(final String server){
        showSimpleProgressDialog(this, "Загрузка данных...","...",false);
        String jsonURL = "http://" + server + ":8090/webdatabase-deabee/odata/MealTimeResources";
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                HashMap<String, String> map=new HashMap<>();
                try {
                    String jsonURL = "http://" + server + ":8090/webdatabase-deabee/odata/MealTimeResources";
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare(HttpRequest.Method.GET)/*.withData(map)*/.sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted(result);
            }
        }.execute();
    }
    public ArrayList<MealTimeModel> mealtimeItems = new ArrayList<>();
    public void onTaskCompleted(String response) {
        removeSimpleProgressDialog();
        mealtimeItems = parseMealtimeItems(response);
        ArrayList<String> mealtimeNames = new ArrayList<>();
        for (int i = 0; i < mealtimeItems.size(); i++){
            mealtimeNames.add(mealtimeItems.get(i).getName());
        }
        readingTypeSpinner.setItemsArray(mealtimeNames);
    }
    private static ProgressDialog mProgressDialog;
    public ArrayList<MealTimeModel> parseMealtimeItems(String response) {
        ArrayList<MealTimeModel> mealTimeModelArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray dataArray = jsonObject.getJSONArray("value");

            for (int i = 0; i < dataArray.length(); i++) {

                MealTimeModel mealTimeModel = new MealTimeModel();
                JSONObject dataobj = dataArray.getJSONObject(i);
                mealTimeModel.setName(dataobj.getString("Name"));
                mealTimeModel.setId(dataobj.getInt("Id"));
                mealTimeModelArrayList.add(mealTimeModel);

            }
            /*if (jsonObject.getString("status").equals("true")) {


            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mealTimeModelArrayList;
    }
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

    @Override
    protected void dialogOnAddButtonPressed() {
        AddInsulinPresenter presenter = (AddInsulinPresenter) getPresenter();
        String readingType = readingTypeSpinner.getSpinner().getSelectedItem().toString();

        RadioButton insulinShort = findViewById(R.id.insulinShort);
        RadioButton insulinLevemir = findViewById(R.id.insulinLevemir);
        Integer insulinType = 0;

        int selectedId = radioInsulinTypeGroup.getCheckedRadioButtonId();

        if(selectedId == insulinShort.getId()) {
            insulinType = 1;
        } else if (selectedId == insulinLevemir.getId()) {
            insulinType = 2;
        }

        presenter.dialogOnAddButtonPressed(this.getAddTimeTextView().getText().toString(),
                this.getAddDateTextView().getText().toString(), readingTextView.getText().toString(),
                insulinType, readingType);
    }
    public void showErrorMessage() {
        View rootLayout = findViewById(android.R.id.content);
        Snackbar.make(rootLayout, getString(R.string.dialog_error2), Snackbar.LENGTH_SHORT).show();
    }
    public void showDuplicateErrorMessage() {
        View rootLayout = findViewById(android.R.id.content);
        Snackbar.make(rootLayout, getString(R.string.dialog_error_duplicate), Snackbar.LENGTH_SHORT).show();
    }
}
