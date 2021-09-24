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

package org.deabee.android.presenter;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.deabee.android.Constants;
import org.deabee.android.activity.AddGlucoseActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.FoodReading;
import org.deabee.android.db.GlucoseReading;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.object.ProductResourceModel;
import org.deabee.android.tools.GlucosioConverter;
import org.deabee.android.tools.ReadingTools;
import org.deabee.android.tools.SplitDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddGlucosePresenter extends AddReadingPresenter {
    private static final int UNKNOWN_ID = -1;
    private DatabaseHandler dB;
    private AddGlucoseActivity activity;
    private ReadingTools rTools;

    public AddGlucosePresenter(AddGlucoseActivity addGlucoseActivity) {
        this.activity = addGlucoseActivity;
        dB = new DatabaseHandler(addGlucoseActivity.getApplicationContext());
        rTools = new ReadingTools();
    }

    public void updateSpinnerTypeTime() {
        setReadingTimeNow();
        activity.updateSpinnerTypeTime(timeToSpinnerType());
    }

    private int timeToSpinnerType() {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date formatted = Calendar.getInstance().getTime();

        SplitDateTime addSplitDateTime = new SplitDateTime(formatted, inputFormat);
        int hour = Integer.parseInt(addSplitDateTime.getHour());

        return hourToSpinnerType(hour);
    }

    public int hourToSpinnerType(int hour) {
        return rTools.hourToSpinnerType(hour);
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading, String type, String notes) {
        dialogOnAddButtonPressed(time, date, reading, type, notes, UNKNOWN_ID);
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading, String type, String notes, long oldId) {
        if (validateDate(date) && validateTime(time) && validateGlucose(reading) && validateType(type)) {
            Date finalDateTime = getReadingTime();
            Number number = ReadingTools.parseReading(reading);
            if (number == null) {
                activity.showErrorMessage();
            } else {
                boolean isReadingAdded = createReading(type, notes, oldId, finalDateTime, number);
                if (!isReadingAdded) {
                    activity.showDuplicateErrorMessage();
                } else {
                    saveToWeb(gReading);
                    activity.finishActivity();
                }
            }
        } else {
            activity.showErrorMessage();
        }
    }
    GlucoseReading gReading;
    private boolean createReading(String type, String notes, long oldId, Date finalDateTime, Number number) {
        boolean isReadingAdded;
        double readingValue;
        if (Constants.Units.MG_DL.equals(getUnitMeasurement())) {
            readingValue = number.doubleValue();
        } else {
            readingValue = GlucosioConverter.glucoseToMgDl(number.doubleValue());
        }
        gReading = new GlucoseReading(readingValue, type, finalDateTime, notes);
        if (oldId == UNKNOWN_ID) {
            isReadingAdded = dB.addGlucoseReading(gReading);
        } else {
            isReadingAdded = dB.editGlucoseReading(oldId, gReading);
        }
        return isReadingAdded;
    }

    public Integer retrieveSpinnerID(String measuredTypeText, List<String> measuredTypelist) {
        int measuredId = 0;
        boolean isFound = false;
        for (String measuredType : measuredTypelist) {
            if (measuredType.equals(measuredTypeText)) {
                isFound = true;
                break;
            }
            measuredId++;
        }
        // if type is not found, it's return null
        return isFound ? measuredId : null;
    }

    public String getUnitMeasurement() {
        return dB.getCurrentUser().getPreferred_unit();
    }

    public GlucoseReading getGlucoseReadingById(Long id) {
        return dB.getGlucoseReadingById(id);
    }

    // Validator
    private boolean validateGlucose(String reading) {
        if (validateText(reading)) {
            if (Constants.Units.MG_DL.equals(getUnitMeasurement())) {
                // We store data in db in mg/dl
                Double readingValue = ReadingTools.safeParseDouble(reading);
                //TODO: Add custom ranges
                return readingValue > 19 && readingValue < 601;
            } else if (Constants.Units.MMOL_L.equals(getUnitMeasurement())) {
                // Convert mmol/L Unit
                Double readingValue = ReadingTools.safeParseDouble(reading);
                return readingValue > 1.0545 && readingValue < 33.3555;
            } else {
                // IT return always true: we don't have ranges yet.
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isFreeStyleLibreEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sharedPref.getBoolean("pref_freestyle_libre", false);
    }

    private boolean validateType(String type) {
        return validateText(type);
    }


    public void saveToWeb(GlucoseReading obj) {
        String server = dB.getAppSettings().IP_ADDRESS;
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/GlucoseReadingResources";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
        }

    }
    public JSONObject buidJsonObject(GlucoseReading obj) throws JSONException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject map = new JSONObject();
        map.accumulate("reading", obj.getReading());
        map.accumulate("created", inputFormat.format(obj.getCreated()).split(" ")[0] + "T" + inputFormat.format(obj.getCreated()).split(" ")[1] + "+06:00");
        map.accumulate("UserResourceId", dB.getCurrentUser().getId());
        map.accumulate("reading_type", obj.getReading_type());
        map.accumulate("notes", obj.getNotes());
        Log.i("JSON", map.toString());
        return map;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }
    private JSONObject httpPost(String myUrl, JSONObject obj) throws IOException, JSONException {
        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. add JSON content to POST request body
        setPostRequestContent(conn, obj);

        // 3. make POST request to the given URL
        //conn.connect();
        String response = sendAndReadString(conn);
        // 4. return response message
        return new JSONObject(response);
    }

    public String sendAndReadString(HttpURLConnection conn) throws IOException{
        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response=new StringBuilder();
        for(String line;(line=br.readLine())!=null;)response.append(line+"\n");
        return response.toString();
    }


    public class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        private JSONObject obj;
        public HTTPAsyncTask(JSONObject obj){
            this.obj = obj;
        }
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    httpPost(urls[0], obj);
                    return "OK";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i("[FoodReadingSaved]", result);
        }
    }
}
