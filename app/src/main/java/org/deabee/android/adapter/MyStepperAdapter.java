package org.deabee.android.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import org.deabee.android.db.GlucoseReading;
import org.deabee.android.fragment.StepFragmentComplete;
import org.deabee.android.fragment.StepFragmentFood;
import org.deabee.android.fragment.StepFragmentGlucose;
import org.deabee.android.fragment.StepFragmentInsulin;
import org.deabee.android.tools.GlucosioConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyStepperAdapter extends AbstractFragmentStepAdapter {
    private static final String CURRENT_STEP_POSITION_KEY = "position";


    private String server;
    private int patientId;
    public void setServer(String server){
        this.server = server;
    }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public MyStepperAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }
    public GlucoseReading getGlucoseReading() {
        DateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();
        try {
            date = inputFormat.parse(step0.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new GlucoseReading(GlucosioConverter.glucoseToMgDl(step0.getGlucose()), step0.getMealtime(), date, "");
    }
    StepFragmentGlucose step0;
    StepFragmentInsulin step1;
    StepFragmentFood step2;
    @Override
    public Step createStep(int position) {

        Bundle b = new Bundle();
        b.putInt(CURRENT_STEP_POSITION_KEY, position);
        switch (position)
        {
            case 0:
                step0 = new StepFragmentGlucose();
                step0.setArguments(b);
                step0.setServer(server);
                step0.setPatientId(patientId);
                return step0;
            case 1:
                step1 = new StepFragmentInsulin();
                step1.setArguments(b);
                return step1;
            case 2:
                step2 = new StepFragmentFood();
                step2.setArguments(b);
                step2.setServer(server);
                return step2;
            case 3:
                StepFragmentComplete step3 = new StepFragmentComplete();
                step3.setArguments(b);
                step3.setServer(server);
                step3.setPatientId(patientId);
                step3.setAll(step0, step1, step2);
                return step3;
            default:
                StepFragmentGlucose stepDefault = new StepFragmentGlucose();
                stepDefault.setArguments(b);

                return stepDefault;
        }


    }

    @Override
    public int getCount() {
        return 4;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        //Override this method to set Step title for the Tabs, not necessary for other stepper types
        StepViewModel.Builder builder = new StepViewModel.Builder(context);
        //builder.setBackButtonLabel("НАЗАД").setEndButtonLabel("ДАЛЕЕ");
        switch (position)
        {
            case 0:
                builder.setTitle("Глюкоза");
                break;
            case 1:
                builder.setTitle("Инсулин");
                break;
            case 2:
                builder.setTitle("Еда");
                break;
            case 3:
                builder.setTitle("Финиш");
                break;
            default:
                builder.setTitle("UNKNOWN TAB");
                break;
        }
        return builder.create();
    }
}
