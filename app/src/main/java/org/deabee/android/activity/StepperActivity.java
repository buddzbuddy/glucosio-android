package org.deabee.android.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.R;
import org.deabee.android.adapter.MyStepperAdapter;
import org.deabee.android.db.DatabaseHandler;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StepperActivity extends AppCompatActivity implements StepperLayout.StepperListener {
    private DatabaseHandler dB;
    private StepperLayout mStepperLayout;
    MyStepperAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stepper);
        mStepperLayout = findViewById(R.id.stepperLayout);
        mAdapter = new MyStepperAdapter(getSupportFragmentManager(), this);
        dB = new DatabaseHandler(getApplicationContext());
        mAdapter.setServer(dB.getAppSettings().IP_ADDRESS);
        mAdapter.setPatientId(dB.getCurrentUser().getId());
        mStepperLayout.setAdapter(mAdapter);
        mStepperLayout.setListener(this);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onCompleted(View completeButton) {
        //Toast.makeText(this, "Отправляю в базу!", Toast.LENGTH_SHORT).show();
        dB.addGlucoseReading(mAdapter.getGlucoseReading());
        finishActivity();
    }

    @Override
    public void onError(VerificationError verificationError) {
        Toast.makeText(this, "Ошибка! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        //Toast.makeText(this, "onStepSelected! -> " + newStepPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReturn() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void finishActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
