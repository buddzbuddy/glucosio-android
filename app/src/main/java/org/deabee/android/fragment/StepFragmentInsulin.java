package org.deabee.android.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepFragmentInsulin extends Fragment implements Step {


    public double getInsulin_food() {
        return Double.parseDouble(step_add_insulin_food.getText().toString());
    }

    public double getInsulin_levemir() {
        return step_add_insulin_levemir.getText().toString().isEmpty() ? 0 : Double.parseDouble(step_add_insulin_levemir.getText().toString());
    }
    TextView step_add_insulin_food;
    TextView step_add_insulin_levemir;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_insulin, container, false);
        step_add_insulin_food = v.findViewById(R.id.step_add_insulin_food);
        step_add_insulin_levemir = v.findViewById(R.id.step_add_insulin_levemir);
        //initialize your UI
        //TODO: Init fields


        return v;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        if(step_add_insulin_food.getText().toString().isEmpty()){
            return new VerificationError("Заполните инсулин на еду!");
        }
        int val = Integer.parseInt(step_add_insulin_food.getText().toString().trim());
        if(val > 25 || val < 0.5){
            return new VerificationError("Инсулин на еду указан некорректно");
        }
        if(!step_add_insulin_levemir.getText().toString().isEmpty()){
            val = Integer.parseInt(step_add_insulin_levemir.getText().toString().trim());
            if(val > 25 || val < 0.5){
                return new VerificationError("Инсулин левемир указан некорректно");
            }
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

        //Toast.makeText(getActivity(), "error onStepSelected! -> " + error.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}
