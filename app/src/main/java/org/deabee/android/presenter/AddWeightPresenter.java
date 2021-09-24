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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.deabee.android.activity.AddWeightActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.PressureReading;
import org.deabee.android.db.WeightReading;
import org.deabee.android.tools.GlucosioConverter;
import org.deabee.android.tools.ReadingTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddWeightPresenter extends AddReadingPresenter {
    private DatabaseHandler dB;
    private AddWeightActivity activity;

    public AddWeightPresenter(AddWeightActivity addWeightActivity) {
        this.activity = addWeightActivity;
        dB = new DatabaseHandler(addWeightActivity.getApplicationContext());
    }

    public void dialogOnAddButtonPressed(String time, String date, String weight, String height) {
        if (validateDate(date) && validateTime(time) && validateWeight(weight) && validateWeight(height)) {

            WeightReading wReading = generateWeightReading(weight, height);
            dB.addWeightReading(wReading);
            saveToWeb(wReading);
            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    public void dialogOnAddButtonPressed(String time, String date, String weight, String height, long oldId) {
        if (validateDate(date) && validateTime(time) && validateWeight(weight) && validateWeight(height)) {

            WeightReading wReading = generateWeightReading(weight, height);
            dB.editWeightReading(oldId, wReading);

            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    private WeightReading generateWeightReading(String weight, String height) {
        Date finalDateTime = getReadingTime();

        double finalWeight = ReadingTools.safeParseDouble(weight);
        double finalHeight = ReadingTools.safeParseDouble(height);

        return new WeightReading(finalWeight, finalHeight, finalDateTime);
    }

    // Getters and Setters

    public String getWeightUnitMeasuerement() {
        return dB.getCurrentUser().getPreferred_unit_weight();
    }

    public WeightReading getWeightReadingById(Long id) {
        return dB.getWeightReadingById(id);
    }

    // Validator
    private boolean validateWeight(String reading) {
        return validateText(reading);
    }


    public void saveToWeb(WeightReading obj) {
        String server = dB.getAppSettings().IP_ADDRESS;
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/WeightReadingResources";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
        }

    }
    public JSONObject buidJsonObject(WeightReading obj) throws JSONException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject map = new JSONObject();
        map.accumulate("created", inputFormat.format(obj.getCreated()).split(" ")[0] + "T" + inputFormat.format(obj.getCreated()).split(" ")[1] + "+06:00");
        map.accumulate("weight", obj.getWeight());
        map.accumulate("height", obj.getHeight());
        map.accumulate("UserResourceId", dB.getCurrentUser().getId());
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
