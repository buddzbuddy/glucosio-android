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

import org.deabee.android.activity.AddFoodActivity;
import org.deabee.android.activity.AddWeightActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.FoodReading;
import org.deabee.android.db.WeightReading;
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
import java.util.Date;

public class AddFoodPresenter extends AddReadingPresenter {
    private DatabaseHandler dB;
    private AddFoodActivity activity;

    public AddFoodPresenter(AddFoodActivity addFoodActivity) {
        this.activity = addFoodActivity;
        dB = new DatabaseHandler(addFoodActivity.getApplicationContext());
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading, String productName, String mealTime, String color) {
        if (validateDate(date) && validateTime(time) && validateFood(reading)) {

            FoodReading fReading = generateFoodReading(reading, productName, mealTime, color);
            dB.addFoodReading(fReading);

            saveToWeb(fReading, dB.getAppSettings().IP_ADDRESS);
            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    public void dialogOnAddButtonPressed(String time, String date, String reading, long oldId, String productName, String mealTime, String color) {
        if (validateDate(date) && validateTime(time) && validateFood(reading)) {

            FoodReading fReading = generateFoodReading(reading, productName, mealTime, color);
            dB.editFoodReading(oldId, fReading);

            activity.finishActivity();
        } else {
            activity.showErrorMessage();
        }
    }

    public FoodReading generateFoodReading(String reading, String productName, String mealTime, String color) {
        Date finalDateTime = getReadingTime();

        return new FoodReading(reading, finalDateTime, productName, mealTime, color);
    }

    public FoodReading getFoodReadingById(Long id) {
        return dB.getFoodReadingById(id);
    }

    // Validator
    private boolean validateFood(String reading) {
        return validateText(reading);
    }

    public void saveToWeb(FoodReading obj, String server) {
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/FoodReadingResources";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
        }

    }
    public JSONObject buidJsonObject(FoodReading obj) throws JSONException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject map = new JSONObject();
        map.accumulate("reading", Double.parseDouble(obj.getReading()));
        map.accumulate("created", inputFormat.format(obj.getCreated()).split(" ")[0] + "T" + inputFormat.format(obj.getCreated()).split(" ")[1] + "+06:00");
        map.accumulate("UserResourceId", dB.getCurrentUser().getId());
        for(int i = 0; i < activity.FOODS.size(); i++) {
            ProductResourceModel foodDb = activity.FOODS.get(i);
            Boolean same = foodDb.getName().equals(obj.getProductName());
            if(same) {
                map.accumulate("ProductResourceId", foodDb.getId());
                break;
            }
        }
        for(int i = 0; i < activity.mealTimeArrayList.size(); i++) {
            MealTimeModel mealTimeDb = activity.mealTimeArrayList.get(i);
            if(mealTimeDb.getName().equals(obj.getMealTime())) {
                map.accumulate("MealTimeResourceId", mealTimeDb.getId());
                break;
            }
        }
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
