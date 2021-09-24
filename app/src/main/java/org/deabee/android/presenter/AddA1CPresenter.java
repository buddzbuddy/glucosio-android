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

import android.os.AsyncTask;
import android.util.Log;

import org.deabee.android.activity.AddA1CActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.CholesterolReading;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.HB1ACReading;
import org.deabee.android.tools.GlucosioConverter;
import org.deabee.android.tools.ReadingTools;
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
import java.util.Date;

public class AddA1CPresenter extends AddReadingPresenter {
    public DatabaseHandler dB;
    private AddA1CActivity activity;

    public AddA1CPresenter(AddA1CActivity addA1CActivity) {
        this.activity = addA1CActivity;
        dB = new DatabaseHandler(addA1CActivity.getApplicationContext());
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading) {
        if (validateDate(date) && validateTime(time) && validateA1C(reading)) {

            HB1ACReading hReading = generateHB1ACReading(reading);
            dB.addHB1ACReading(hReading);
            saveToWeb(hReading);
            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading, long oldId) {
        if (validateDate(date) && validateTime(time) && validateText(reading)) {

            HB1ACReading hReading = generateHB1ACReading(reading);
            dB.editHB1ACReading(oldId, hReading);

            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    private HB1ACReading generateHB1ACReading(String reading) {
        Date finalDateTime = getReadingTime();

        double finalReading;
        if ("percentage".equals(getA1CUnitMeasuerement())) {
            finalReading = ReadingTools.safeParseDouble(reading);
        } else {
            finalReading = GlucosioConverter.a1cIfccToNgsp(ReadingTools.safeParseDouble(reading));
        }

        return new HB1ACReading(finalReading, finalDateTime);
    }

    public String getA1CUnitMeasuerement() {
        return dB.getCurrentUser().getPreferred_unit_a1c();
    }

    public HB1ACReading getHB1ACReadingById(Long id) {
        return dB.getHB1ACReadingById(id);
    }

    // Validator
    private boolean validateA1C(String reading) {
        return validateText(reading);
    }


    public void saveToWeb(HB1ACReading obj) {
        String server = dB.getAppSettings().IP_ADDRESS;
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/HB1ACReadingResources";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
        }

    }
    public JSONObject buidJsonObject(HB1ACReading obj) throws JSONException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject map = new JSONObject();
        map.accumulate("created", inputFormat.format(obj.getCreated()).split(" ")[0] + "T" + inputFormat.format(obj.getCreated()).split(" ")[1] + "+06:00");
        map.accumulate("reading", obj.getReading());
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
