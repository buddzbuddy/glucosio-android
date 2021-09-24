package org.deabee.android.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.object.ProductResourceModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import static com.android.volley.VolleyLog.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepFragmentFood extends Fragment implements BlockingStep {

    private String server;
    public void setServer(String server){
        this.server = server;
    }

    private String formattedFoodWithBU = "";

    public void setFormattedFoodWithBU(String formattedFoodWithBU) {
        this.formattedFoodWithBU = formattedFoodWithBU;
    }
    public String getFormattedFoodWithBU(){
        return formattedFoodWithBU;
    }

    @Override
    @UiThread
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        callback.getStepperLayout().showProgress("Считаю ХЕ...");
        //RequestQueue initialized
        String url = "http://" + server + ":8090/webdatabase-deabee/api/FoodReadingExplain/CalcBUStr?formattedFoods=" + getFoodFormatted();
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.accumulate("formattedFoods", getFoodFormatted());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("result")){
                        setFormattedFoodWithBU(response.getString("text"));
                        callback.goToNextStep();
                        callback.getStepperLayout().hideProgress();
                    }
                    else {
                        callback.getStepperLayout().hideProgress();
                        Toast.makeText(getActivity(), "result-false: " + response.toString(), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    callback.getStepperLayout().hideProgress();
                    e.printStackTrace();
                    callback.getStepperLayout().updateErrorState(new VerificationError("VolleyJSONParse: " + e.getMessage()));
                    Toast.makeText(getActivity(), "JSONException -> " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                callback.getStepperLayout().hideProgress();
                Writer writer = new StringWriter();
                error.printStackTrace(new PrintWriter(writer));
                String s = writer.toString();
                String errorMessage = "Volley: " + error.toString() + "; jsonRequest: " + jsonRequest.toString() + "; trace: " + s;
                Log.i(TAG, errorMessage);
                callback.getStepperLayout().updateErrorState(new VerificationError(errorMessage));
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        mRequestQueue.add(objectRequest);
    }

    @Override
    @UiThread
    public void onCompleteClicked(final StepperLayout.OnCompleteClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.complete();
            }
        }, 5000L);
    }

    @Override
    @UiThread
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
        callback.goToPrevStep();
    }

    AutoCompleteTextView productNameTextView_1;
    AutoCompleteTextView productNameTextView_2;
    AutoCompleteTextView productNameTextView_3;
    AutoCompleteTextView productNameTextView_4;
    AutoCompleteTextView productNameTextView_5;
    AutoCompleteTextView productNameTextView_6;
    AutoCompleteTextView productNameTextView_7;
    AutoCompleteTextView productNameTextView_8;

    TextView productGramTextView_1;
    TextView productGramTextView_2;
    TextView productGramTextView_3;
    TextView productGramTextView_4;
    TextView productGramTextView_5;
    TextView productGramTextView_6;
    TextView productGramTextView_7;
    TextView productGramTextView_8;

    private LinearLayout ll_1;
    private LinearLayout ll_2;
    private boolean ll_2_expanded = false;
    private LinearLayout ll_3;
    private boolean ll_3_expanded = false;
    private LinearLayout ll_4;
    private boolean ll_4_expanded = false;
    private LinearLayout ll_5;
    private boolean ll_5_expanded = false;
    private LinearLayout ll_6;
    private boolean ll_6_expanded = false;
    private LinearLayout ll_7;
    private boolean ll_7_expanded = false;
    private LinearLayout ll_8;
    private boolean ll_8_expanded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_food, container, false);

        //initialize your UI
        //TODO: Init fields
        productNameTextView_1 = v.findViewById(R.id.productNameTextView_1);
        productNameTextView_2 = v.findViewById(R.id.productNameTextView_2);
        productNameTextView_3 = v.findViewById(R.id.productNameTextView_3);
        productNameTextView_4 = v.findViewById(R.id.productNameTextView_4);
        productNameTextView_5 = v.findViewById(R.id.productNameTextView_5);
        productNameTextView_6 = v.findViewById(R.id.productNameTextView_6);
        productNameTextView_7 = v.findViewById(R.id.productNameTextView_7);
        productNameTextView_8 = v.findViewById(R.id.productNameTextView_8);

        productGramTextView_1 = v.findViewById(R.id.productGramTextView_1);
        productGramTextView_2 = v.findViewById(R.id.productGramTextView_2);
        productGramTextView_3 = v.findViewById(R.id.productGramTextView_3);
        productGramTextView_4 = v.findViewById(R.id.productGramTextView_4);
        productGramTextView_5 = v.findViewById(R.id.productGramTextView_5);
        productGramTextView_6 = v.findViewById(R.id.productGramTextView_6);
        productGramTextView_7 = v.findViewById(R.id.productGramTextView_7);
        productGramTextView_8 = v.findViewById(R.id.productGramTextView_8);

        ll_1 = v.findViewById(R.id.ll_1);
        ll_2 = v.findViewById(R.id.ll_2);
        ll_3 = v.findViewById(R.id.ll_3);
        ll_4 = v.findViewById(R.id.ll_4);
        ll_5 = v.findViewById(R.id.ll_5);
        ll_6 = v.findViewById(R.id.ll_6);
        ll_7 = v.findViewById(R.id.ll_7);
        ll_8 = v.findViewById(R.id.ll_8);

        loadFoodSugguestions();
        return v;
    }

    @SuppressLint("StaticFieldLeak")
    private void loadFoodSugguestions(){
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                try {
                    String jsonURL = "http://" + server + ":8090/webdatabase-deabee/odata/ProductResources";
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare(HttpRequest.Method.GET).sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                try {
                    onTaskCompleted(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void onTaskCompleted(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONArray dataArray = jsonObject.getJSONArray("value");
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataobj = dataArray.getJSONObject(i);
            names.add(dataobj.getString("Name"));
        }
        final ArrayAdapter<String> foodArrayAdapter = new ArrayAdapter<String>(getActivity(),   android.R.layout.simple_list_item_1, names);
        productNameTextView_1.setAdapter(foodArrayAdapter);
        productNameTextView_2.setAdapter(foodArrayAdapter);
        productNameTextView_3.setAdapter(foodArrayAdapter);
        productNameTextView_4.setAdapter(foodArrayAdapter);
        productNameTextView_5.setAdapter(foodArrayAdapter);
        productNameTextView_6.setAdapter(foodArrayAdapter);
        productNameTextView_7.setAdapter(foodArrayAdapter);
        productNameTextView_8.setAdapter(foodArrayAdapter);

        productGramTextView_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_2_expanded) {
                        expand(ll_2);
                        ll_2_expanded = true;
                    }
                }
                else {
                    if(ll_2_expanded) {
                        collapse(ll_2);
                        ll_2_expanded = false;
                    }
                }
            }
        });

        productGramTextView_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_3_expanded) {
                        expand(ll_3);
                        ll_3_expanded = true;
                    }
                }
                else {
                    if(ll_3_expanded) {
                        collapse(ll_3);
                        ll_3_expanded = false;
                    }
                }
            }
        });

        productGramTextView_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_4_expanded) {
                        expand(ll_4);
                        ll_4_expanded = true;
                    }
                }
                else {
                    if(ll_4_expanded) {
                        collapse(ll_4);
                        ll_4_expanded = false;
                    }
                }
            }
        });

        productGramTextView_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_5_expanded) {
                        expand(ll_5);
                        ll_5_expanded = true;
                    }
                }
                else {
                    if(ll_5_expanded) {
                        collapse(ll_5);
                        ll_5_expanded = false;
                    }
                }
            }
        });

        productGramTextView_5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_6_expanded) {
                        expand(ll_6);
                        ll_6_expanded = true;
                    }
                }
                else {
                    if(ll_6_expanded) {
                        collapse(ll_6);
                        ll_6_expanded = false;
                    }
                }
            }
        });

        productGramTextView_6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_7_expanded) {
                        expand(ll_7);
                        ll_7_expanded = true;
                    }
                }
                else {
                    if(ll_7_expanded) {
                        collapse(ll_7);
                        ll_7_expanded = false;
                    }
                }
            }
        });

        productGramTextView_7.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    if(!ll_8_expanded) {
                        expand(ll_8);
                        ll_8_expanded = true;
                    }
                }
                else {
                    if(ll_8_expanded) {
                        collapse(ll_8);
                        ll_8_expanded = false;
                    }
                }
            }
        });

    }

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewPager.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public String getFoodStr(){
        return getFormattedFoodWithBU();

    }
    public String getFoodFormatted(){
        String foodStr = "";

        if(!productNameTextView_1.getText().toString().isEmpty() && !productGramTextView_1.getText().toString().isEmpty()) {
            foodStr += productNameTextView_1.getText().toString() + ":" + productGramTextView_1.getText().toString();
        }
        if(!productNameTextView_2.getText().toString().isEmpty() && !productGramTextView_2.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_2.getText().toString() + ":" + productGramTextView_2.getText().toString();
        }
        if(!productNameTextView_3.getText().toString().isEmpty() && !productGramTextView_3.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_3.getText().toString() + ":" + productGramTextView_3.getText().toString();
        }
        if(!productNameTextView_4.getText().toString().isEmpty() && !productGramTextView_4.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_4.getText().toString() + ":" + productGramTextView_4.getText().toString();
        }
        if(!productNameTextView_5.getText().toString().isEmpty() && !productGramTextView_5.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_5.getText().toString() + ":" + productGramTextView_5.getText().toString();
        }
        if(!productNameTextView_6.getText().toString().isEmpty() && !productGramTextView_6.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_6.getText().toString() + ":" + productGramTextView_6.getText().toString();
        }
        if(!productNameTextView_7.getText().toString().isEmpty() && !productGramTextView_7.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_7.getText().toString() + ":" + productGramTextView_7.getText().toString();
        }
        if(!productNameTextView_8.getText().toString().isEmpty() && !productGramTextView_8.getText().toString().isEmpty()) {
            foodStr += "|" + productNameTextView_8.getText().toString() + ":" + productGramTextView_8.getText().toString();
        }
        return foodStr;
    }
    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        if(productNameTextView_1.getText().toString().isEmpty()){
            return new VerificationError("Заполните название еды!");
        }
        if(productGramTextView_1.getText().toString().isEmpty()){
            return new VerificationError("Заполните граммаж еды!");
        }
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }
}
