package org.deabee.android.fragment;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.R;
import org.deabee.android.activity.AddReadingActivity;
import org.deabee.android.activity.StepperActivity;
import org.deabee.android.presenter.AddReadingPresenter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;

import butterknife.BindView;

import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepFragmentGlucose extends Fragment implements BlockingStep {
    private String server;
    public void setServer(String server){
        this.server = server;
    }
    private int patientId;
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    public double getGlucose() {
        return Double.parseDouble(step_add_glucose.getText().toString());
    }
    public String getMealtime(){
        return step_mealTimeSpinner.getSelectedItem().toString();
    }
    public String getDate() {
        return addDateTextView.getText().toString();
    }

    TextView step_add_glucose;
    private Spinner step_mealTimeSpinner;
    private TextView addDateTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_glucose, container, false);
        step_add_glucose = v.findViewById(R.id.step_add_glucose);
        step_mealTimeSpinner = v.findViewById(R.id.step_mealTimeSpinner);
        createDateTimeViewAndListener(v);
        //initialize your UI
        //TODO: Init fields


        return v;
    }
    private int cYear;
    private int cMonth;
    private int cDay;
    public void createDateTimeViewAndListener(final View vw) {
        addDateTextView = vw.findViewById(R.id.step_add_date);
        final DecimalFormat df = new DecimalFormat("00");
        final Calendar now = Calendar.getInstance();
        cYear = now.get(Calendar.YEAR);
        cMonth = now.get(Calendar.MONTH);
        cDay = now.get(Calendar.DAY_OF_MONTH);
        String y = cYear + "";
        String month = df.format(cMonth + 1);
        String day = df.format(cDay);
        String date = day + "." + month + "." + y;
        addDateTextView.setText(date);
        addDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(
                        v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                cYear = year;
                                cMonth = monthOfYear;
                                cDay = dayOfMonth;
                                String y = cYear + "";
                                String month = df.format(cMonth + 1);
                                String day = df.format(cDay);

                                String date = day + "." + month + "." + y;
                                addDateTextView.setText(date);
                            }
                        },
                        cYear,
                        cMonth,
                        cDay
                );
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                dpd.show();
            }
        });
    }
    @Override
    public VerificationError verifyStep() {
        if(step_add_glucose.getText().toString().trim().isEmpty()){
            return new VerificationError("Укажите глюкозу");
        }
        int val = Integer.parseInt(step_add_glucose.getText().toString().trim());
        if(val > 15 || val < 1){
            return new VerificationError("Глюкоза указана некорректно");
        }
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
        Toast.makeText(getActivity(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
    @UiThread
    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        callback.getStepperLayout().showProgress("Проверка...");
        //RequestQueue initialized
        String url = "http://" + server + ":8090/webdatabase-deabee/api/AddFullReeadings/CheckMealtime/" + patientId;
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.accumulate("mealtime", getMealtime());
            jsonRequest.accumulate("date", getDate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("result")){
                        callback.goToNextStep();
                    }
                    else {
                        String errorMessage = "Показатели '" + getMealtime() + "' уже введены! Укажите другое время!";
                        callback.getStepperLayout().updateErrorState(new VerificationError(errorMessage));
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String errorMessage ="VolleyJSONParse: " + e.getMessage();
                            callback.getStepperLayout().updateErrorState(new VerificationError(errorMessage));
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                }
                callback.getStepperLayout().hideProgress();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG, "onErrorResponse: " + error.toString());
                Log.d(TAG, jsonRequest.toString());
                callback.getStepperLayout().updateErrorState(new VerificationError("Volley: " + error.toString()));
                Toast.makeText(getActivity(), "Volley: " + error.toString(), Toast.LENGTH_SHORT).show();

                callback.getStepperLayout().hideProgress();
            }
        });
        mRequestQueue.add(objectRequest);
    }
    @UiThread
    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

    }
    @UiThread
    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {

    }
}
