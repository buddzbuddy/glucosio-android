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

package org.deabee.android.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import org.deabee.android.R;
import org.deabee.android.db.WeightReading;
import org.deabee.android.presenter.AddWeightPresenter;
import org.deabee.android.tools.FormatDateTime;
import org.deabee.android.tools.GlucosioConverter;

import java.util.Calendar;

public class AddWeightActivity extends AddReadingActivity {

    private TextView weightReadingTextView;
    private TextView heightReadingTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean needUnitConversion = false;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_weight);
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        this.retrieveExtra();

        AddWeightPresenter presenter = new AddWeightPresenter(this);
        this.setPresenter(presenter);
        presenter.setReadingTimeNow();

        weightReadingTextView = findViewById(R.id.weight_add_value);
        heightReadingTextView = findViewById(R.id.height_add_value);

        this.createDateTimeViewAndListener();
        this.createFANViewAndListener();

        double weightVal = 0;
        double heightVal = 0;
        // If an id is passed, open the activity in edit mode
        FormatDateTime formatDateTime = new FormatDateTime(getApplicationContext());
        if (this.isEditing()) {
            setTitle(R.string.title_activity_add_weight_edit);
            WeightReading readingToEdit = presenter.getWeightReadingById(this.getEditId());
            weightVal = readingToEdit.getWeight();
            heightVal = readingToEdit.getHeight();
            weightReadingTextView.setText(numberFormat.format(weightVal));
            heightReadingTextView.setText(numberFormat.format(heightVal));
            Calendar cal = Calendar.getInstance();
            cal.setTime(readingToEdit.getCreated());
            this.getAddDateTextView().setText(formatDateTime.getDate(cal));
            this.getAddTimeTextView().setText(formatDateTime.getTime(cal));
            presenter.updateReadingSplitDateTime(readingToEdit.getCreated());
        } else {
            this.getAddDateTextView().setText(formatDateTime.getCurrentDate());
            this.getAddTimeTextView().setText(formatDateTime.getCurrentTime());
        }
    }

    @Override
    protected void dialogOnAddButtonPressed() {
        AddWeightPresenter presenter = (AddWeightPresenter) this.getPresenter();
        if (this.isEditing()) {
            presenter.dialogOnAddButtonPressed(this.getAddTimeTextView().getText().toString(),
                    this.getAddDateTextView().getText().toString(), weightReadingTextView.getText().toString(), heightReadingTextView.getText().toString(), this.getEditId());
        } else {
            presenter.dialogOnAddButtonPressed(this.getAddTimeTextView().getText().toString(),
                    this.getAddDateTextView().getText().toString(), weightReadingTextView.getText().toString(), heightReadingTextView.getText().toString());

        }
    }

    public void showErrorMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.dialog_error2), Toast.LENGTH_SHORT).show();
    }
}
