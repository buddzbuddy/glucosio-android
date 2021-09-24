/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.deabee.android.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.FoodReading;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.object.ProductResourceModel;
import org.deabee.android.presenter.AddFoodPresenter;
import org.deabee.android.tools.FormatDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AddFoodActivity extends AddReadingActivity {
    public ArrayList<ProductResourceModel> FOODS;
    private Spinner mealTimeSpinner;
    private AutoCompleteTextView foodEditText;
    private TextView readingTextView;
    private DatabaseHandler dB;
    private String color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_food);
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        dB = new DatabaseHandler(getApplicationContext());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        this.retrieveExtra();

        AddFoodPresenter presenter = new AddFoodPresenter(this);
        this.setPresenter(presenter);
        presenter.setReadingTimeNow();

        readingTextView = findViewById(R.id.food_add_value);

        this.createDateTimeViewAndListener();
        this.createFANViewAndListener();

        String foodVal = "";
        // If an id is passed, open the activity in edit mode
        FormatDateTime formatDateTime = new FormatDateTime(getApplicationContext());
        if (this.isEditing()) {
            setTitle("Изменить еду");
            FoodReading readingToEdit = presenter.getFoodReadingById(this.getEditId());
            foodVal = readingToEdit.getReading();
            readingTextView.setText(foodVal);
            Calendar cal = Calendar.getInstance();
            cal.setTime(readingToEdit.getCreated());
            this.getAddDateTextView().setText(formatDateTime.getDate(cal));
            this.getAddTimeTextView().setText(formatDateTime.getTime(cal));
            presenter.updateReadingSplitDateTime(readingToEdit.getCreated());
        } else {
            this.getAddDateTextView().setText(formatDateTime.getCurrentDate());
            this.getAddTimeTextView().setText(formatDateTime.getCurrentTime());
        }
        mealTimeSpinner = (Spinner) findViewById(R.id.mealTimeSpinner);
        String server = dB.getAppSettings().IP_ADDRESS;
        loadMealtimeItems(server);
        foodEditText = (AutoCompleteTextView) findViewById(R.id.foodAutoCompleteTextView);
        loadFoodSugguestions(server);
    }
    private static ProgressDialog mProgressDialog;
    public ArrayList<MealTimeModel> mealTimeArrayList;
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<String> foodNames = new ArrayList<String>();
    @SuppressLint("StaticFieldLeak")
    private void loadMealtimeItems(final String server){

        showSimpleProgressDialog(this, "Загрузка данных...","Справочник употребления пищи",false);

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
    public void onTaskCompleted(String response) {
        removeSimpleProgressDialog();

        mealTimeArrayList = parseMealtimeItems(response);
        // Application of the Array to the Spinner

        for (int i = 0; i < mealTimeArrayList.size(); i++){
            names.add(mealTimeArrayList.get(i).getName());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, names);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        mealTimeSpinner.setAdapter(spinnerArrayAdapter);
    }


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

    public boolean isSuccess(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("status").equals("true")) {
                return true;
            } else {

                return false;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getErrorCode(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("message");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "No data";
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

    @SuppressLint("StaticFieldLeak")
    private void loadFoodSugguestions(final String server){
        showSimpleProgressDialog(this, "Загрузка данных...","Справочник продуктов",false);
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                try {
                    String jsonURL2 = "http://" + server + ":8090/webdatabase-deabee/odata/ProductResources?$expand=QualityLevelResource";
                    HttpRequest req = new HttpRequest(jsonURL2);
                    response = req.prepare(HttpRequest.Method.GET).sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted2(result);
            }
        }.execute();
    }

    public void onTaskCompleted2(String response) {
        FOODS = parseInfo2(response);
        // Application of the Array to the Spinner

        for (int i = 0; i < FOODS.size(); i++){
            foodNames.add(FOODS.get(i).getName());
        }

        final ArrayAdapter<String> foodArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_list_item_1, foodNames);
        foodEditText.setAdapter(foodArrayAdapter);
        foodEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                for (int i = 0; i < FOODS.size(); i++){
                    if(FOODS.get(i).getName().equals(foodEditText.getText().toString())){
                        color = FOODS.get(i).getColor();
                        Log.d("COLOR", color);
                        break;
                    }
                }
            }
        });

        removeSimpleProgressDialog();
    }

    public ArrayList<ProductResourceModel> parseInfo2(String response) {
        ArrayList<ProductResourceModel> productModelArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray dataArray = jsonObject.getJSONArray("value");

            for (int i = 0; i < dataArray.length(); i++) {

                ProductResourceModel productModel = new ProductResourceModel();
                JSONObject dataobj = dataArray.getJSONObject(i);
                productModel.setName(dataobj.getString("Name"));
                productModel.setId(dataobj.getString("Id"));

                if(!dataobj.isNull("QualityLevelResource")){
                    productModel.setColor(dataobj.getJSONObject("QualityLevelResource").getString("Color"));
                }
                else {
                    productModel.setColor("gray");
                }

                productModelArrayList.add(productModel);

            }
            /*if (jsonObject.getString("status").equals("true")) {


            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return productModelArrayList;
    }



    @Override
    protected void dialogOnAddButtonPressed() {
        AddFoodPresenter presenter = (AddFoodPresenter) this.getPresenter();
        if (this.isEditing()) {
            presenter.dialogOnAddButtonPressed(this.getAddTimeTextView().getText().toString(),
                    this.getAddDateTextView().getText().toString(), readingTextView.getText().toString(), this.getEditId(), foodEditText.getText().toString(), mealTimeSpinner.getSelectedItem().toString(), color);
        } else {
            presenter.dialogOnAddButtonPressed(this.getAddTimeTextView().getText().toString(),
                    this.getAddDateTextView().getText().toString(), readingTextView.getText().toString(), foodEditText.getText().toString(), mealTimeSpinner.getSelectedItem().toString(), color);



        }
    }

    public void showErrorMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.dialog_error2), Toast.LENGTH_SHORT).show();
    }

}
