package org.deabee.android.fragment;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.db.InsulinReading;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.presenter.AddInsulinPresenter;
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

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepFragmentComplete extends Fragment implements BlockingStep {
    private StepFragmentGlucose step0;
    private StepFragmentInsulin step1;
    private StepFragmentFood step2;
    private String server;
    public void setServer(String server){
        this.server = server;
    }
    private int patientId;
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    public void setAll(StepFragmentGlucose step0, StepFragmentInsulin step1, StepFragmentFood step2) {
        this.step0 = step0;
        this.step1 = step1;
        this.step2 = step2;
    }

    TextView complete_add_glucose;
    TextView complete_add_insulin_food;
    TextView complete_add_insulin_levemir;
    TextView complete_add_food;
    TextView complete_add_mealtimeTextView;
    TextView complete_add_dateTextView;
    @Override
    @UiThread
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.goToNextStep();
            }
        }, 2000L);
    }
    private String httpPost(String myUrl) throws IOException, JSONException {
        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. add JSON content to POST request body
        setPostRequestContent(conn, buidJsonObject());

        // 3. make POST request to the given URL
        //conn.connect();
        String response = sendAndReadString(conn);
        // 4. return response message
        //return new JSONObject(response);
        return response;
    }
    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(StepFragmentComplete.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }
    public String sendAndReadString(HttpURLConnection conn) throws IOException{
        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response=new StringBuilder();
        for(String line;(line=br.readLine())!=null;)response.append(line+"\n");
        return response.toString();
    }
    public JSONObject buidJsonObject() throws JSONException {
        JSONObject map = new JSONObject();
        map.accumulate("glucose", complete_add_glucose.getText().toString());
        map.accumulate("insulin_food", complete_add_insulin_food.getText().toString());
        map.accumulate("insulin_levemir", complete_add_insulin_levemir.getText().toString());
        map.accumulate("mealtime", complete_add_mealtimeTextView.getText().toString());
        map.accumulate("food", step2.getFoodFormatted());
        map.accumulate("notes", "");
        map.accumulate("date", complete_add_dateTextView.getText().toString());
        Log.i("JSON", map.toString());
        return map;
    }
    @Override
    @UiThread
    @SuppressLint("StaticFieldLeak")
    public void onCompleteClicked(final StepperLayout.OnCompleteClickedCallback callback) {
        callback.getStepperLayout().showProgress("Сохраняю данные...");
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                try {
                    String jsonURL = "http://" + server + ":8090/webdatabase-deabee/api/AddFullReeadings/Save/" + patientId;
                    response = httpPost(jsonURL);
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("result")) {
                        callback.complete();
                    } else {

                        String errorMessage = "";
                        try {
                            errorMessage = jsonObject.getString("errorMessage") + "; StackTrace: " + jsonObject.getString("trace");
                        } catch (Exception e) {
                            errorMessage = response;
                        }
                        //callback.getStepperLayout().updateErrorState(new VerificationError(errorMessage));
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    callback.getStepperLayout().hideProgress();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("response", response);
                }
            }
        }.execute();
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.complete();
                callback.getStepperLayout().hideProgress();
            }
        }, 5000L);*/
    }

    @Override
    @UiThread
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        //Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
        callback.goToPrevStep();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_complete, container, false);
        complete_add_glucose = v.findViewById(R.id.complete_add_glucose);
        complete_add_insulin_food = v.findViewById(R.id.complete_add_insulin_food);
        complete_add_insulin_levemir = v.findViewById(R.id.complete_add_insulin_levemir);
        complete_add_food = v.findViewById(R.id.foodTextView);
        complete_add_mealtimeTextView = v.findViewById(R.id.complete_add_mealtimeTextView);
        complete_add_dateTextView = v.findViewById(R.id.complete_add_date);
        //initialize your UI
        //TODO: Init fields
        complete_add_glucose.setText(step0.getGlucose() + "");
        complete_add_insulin_food.setText(step1.getInsulin_food() + "");
        complete_add_insulin_levemir.setText(step1.getInsulin_levemir() + "");
        Log.i("step2.getFoodStr()", step2.getFoodStr());
        complete_add_food.setText(step2.getFoodStr());
        complete_add_mealtimeTextView.setText(step0.getMealtime());
        complete_add_dateTextView.setText(step0.getDate());

        return v;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        /*if(true){
            return new VerificationError("Заполните все поля!");
        }*/
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
        complete_add_glucose.setText(step0.getGlucose() + "");
        complete_add_insulin_food.setText(step1.getInsulin_food() + "");
        complete_add_insulin_levemir.setText(step1.getInsulin_levemir() + "");
        complete_add_food.setText(step2.getFoodStr());
        complete_add_mealtimeTextView.setText(step0.getMealtime());
        complete_add_dateTextView.setText(step0.getDate());
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText

        //Toast.makeText(getActivity(), "error onStepSelected! -> " + error.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}
