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

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.activity.AddFoodActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.AppSettings;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.User;
import org.deabee.android.db.UserBuilder;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.view.HelloView;
import org.json.JSONArray;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;


public class HelloPresenter {
    private DatabaseHandler dB;
    private final HelloView helloView;
    private User _user;

    public HelloPresenter(final HelloView helloView, DatabaseHandler dbHandler) {
        if(dbHandler.getCurrentUser() != null){
            helloView.startMainView();
        }
        this.helloView = helloView;
        dB = dbHandler;
    }

    public void onNextClicked(
            String userName,
            int d_type,
            String unit,
            int userType) {
        if (!validUsername(userName)) {
            helloView.displayErrorWrongUsername();
        } /*else if (!validAge(age)){
            helloView.displayErrorWrongAge();
        }*/ else {
            saveToDatabase(
                    userName,
                    d_type,
                    unit,
                    userType);
        }

    }

    private boolean validAge(String age) {
        if (TextUtils.isEmpty(age)) {
            return false;
        } else if (!TextUtils.isDigitsOnly(age)) {
            return false;
        } else {
            int finalAge = Integer.parseInt(age);
            return finalAge > 0 && finalAge < 120;
        }
    }

    private boolean validUsername(String username) {
        return !TextUtils.isEmpty(username);
    }

    private void saveToDatabase(
            final String userName,
            final int diabetesType,
            final String unitMeasurement,
            int userType) {
        User user = new UserBuilder()
                .setName(userName)
                .setPreferredLanguage("ru")
                .setDiabetesType(diabetesType)
                .setPreferredUnit(unitMeasurement)
                .setPreferredA1CUnit("percentage")
                .setPreferredWeightUnit("kilograms")
                .setPreferredRange("ADA")
                .setMinRange(70)
                .setMaxRange(180)
                .setUserType(userType)
                .createUser();
        _user = user;
        //dB.addUser(user);
        String server = dB.getAppSettings().IP_ADDRESS;
        saveToWeb(user, server);
    }


    public void saveToWeb(User obj, String server) {
        helloView.showSimpleProgressDialog();
        final String userApi = "http://" + server + ":8090/webdatabase-deabee/api/UserManager/Create";
        // perform HTTP POST request
        try {
            new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
        }
        catch (Exception e){
            Log.e("saveToWeb", e.getMessage());
            e.printStackTrace();
            helloView.removeSimpleProgressDialog();
            helloView.displayErrorWrongAge();
        }

    }
    public JSONObject buidJsonObject(User obj) throws JSONException {

        JSONObject map = new JSONObject();
        map.accumulate("Name", obj.getName());
        //map.accumulate("Preferred_language", obj.getPreferred_language());
        //map.accumulate("Age", obj.getAge());
        //map.accumulate("Gender", obj.getGender());
        //map.accumulate("InsulinName", obj.getInsulinName());
        //map.accumulate("InsulinCompany", obj.getInsulinCompany());
        //map.accumulate("D_type", obj.getD_type());
        map.accumulate("UserTypeResourceId", obj.getUserType());
        map.accumulate("Preferred_unit", obj.getPreferred_unit());
        //map.accumulate("Preferred_unit_a1c", obj.getPreferred_unit_a1c());
        //map.accumulate("Preferred_unit_weight", obj.getPreferred_unit_weight());
        //map.accumulate("Preferred_range", obj.getPreferred_range());
        //map.accumulate("Custom_range_min", obj.getCustom_range_min());
        //map.accumulate("Custom_range_max", obj.getCustom_range_max());
        return map;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        }
        else {
            writer = new BufferedWriter(new OutputStreamWriter(os));
        }
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


    @SuppressLint("StaticFieldLeak")
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
                    JSONObject response = httpPost(urls[0], obj);
                    int id = response.getInt("id");

                    return Integer.toString(id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + urls[0] + obj.toString();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //User user = dB.getCurrentUser();
            _user.setId(Integer.parseInt(result));
            dB.addUser(_user);
            Log.i("[userInfo]", dB.getCurrentUser().toString());
            helloView.removeSimpleProgressDialog();
            helloView.startMainView();
        }
    }



}
