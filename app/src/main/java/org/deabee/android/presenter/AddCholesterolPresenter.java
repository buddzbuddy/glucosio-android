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

import org.deabee.android.activity.AddCholesterolActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.CholesterolReading;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.GlucoseReading;
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

public class AddCholesterolPresenter extends AddReadingPresenter {
    private DatabaseHandler dB;
    private AddCholesterolActivity activity;

    public AddCholesterolPresenter(AddCholesterolActivity addCholesterolActivity) {
        this.activity = addCholesterolActivity;
        dB = new DatabaseHandler(addCholesterolActivity.getApplicationContext());
    }

    public void dialogOnAddButtonPressed(String time, String date, String totalCho, String LDLCho, String HDLCho) {
        if (validateDate(date) && validateTime(time) && validateCholesterol(totalCho) && validateCholesterol(LDLCho) && validateCholesterol(HDLCho)) {
            CholesterolReading cReading = generateCholesterolReading(totalCho, LDLCho, HDLCho);
            dB.addCholesterolReading(cReading);
            saveToWeb(cReading);
            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    public void dialogOnAddButtonPressed(String time, String date, String totalCho, String LDLCho, String HDLCho, long oldId) {
        if (validateDate(date) && validateTime(time) && validateCholesterol(totalCho) && validateCholesterol(LDLCho) && validateCholesterol(HDLCho)) {
            CholesterolReading cReading = generateCholesterolReading(totalCho, LDLCho, HDLCho);
            dB.editCholesterolReading(oldId, cReading);
            activity.finishActivity();
        } else {
            activity.showErrorMessage();

        }
    }

    private CholesterolReading generateCholesterolReading(String totalCho, String LDLCho, String HDLCho) {
        Date finalDateTime = getReadingTime();
        double totalChoFinal = ReadingTools.safeParseDouble(totalCho);
        double LDLChoFinal = ReadingTools.safeParseDouble(LDLCho);
        double HDLChoFinal = ReadingTools.safeParseDouble(HDLCho);
        return new CholesterolReading(totalChoFinal, LDLChoFinal, HDLChoFinal, finalDateTime);
    }

    public String getUnitMeasurement() {
        return dB.getCurrentUser().getPreferred_unit();
    }

    public CholesterolReading getCholesterolReadingById(Long id) {
        return dB.getCholesterolReading(id);
    }

    // Validator
    private boolean validateCholesterol(String reading) {
        return validateText(reading);
    }


    public void saveToWeb(CholesterolReading obj) {
        String server = dB.getAppSettings().IP_ADDRESS;
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/CholesterolReadingResources";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
        }

    }
    public JSONObject buidJsonObject(CholesterolReading obj) throws JSONException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject map = new JSONObject();
        map.accumulate("created", inputFormat.format(obj.getCreated()).split(" ")[0] + "T" + inputFormat.format(obj.getCreated()).split(" ")[1] + "+06:00");
        map.accumulate("totalReading", obj.getTotalReading());
        map.accumulate("LDLReading", obj.getLDLReading());
        map.accumulate("HDLReading", obj.getHDLReading());
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
