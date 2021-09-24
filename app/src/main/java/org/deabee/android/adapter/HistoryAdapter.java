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

package org.deabee.android.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.deabee.android.Constants;
import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.presenter.HistoryPresenter;
import org.deabee.android.tools.GlucoseRanges;
import org.deabee.android.tools.GlucosioConverter;
import org.deabee.android.tools.NumberFormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final int metricId;
    private final Context mContext;
    private final HistoryPresenter presenter;

    private JSONArray weightReadings;
    private JSONArray glucoseReadings;
    private JSONArray ketoneReadings;
    private JSONArray pressureReadings;
    private JSONArray cholesterolReadings;
    private JSONArray hb1acReadings;
    private JSONArray foodReadings;
    private JSONArray insulinReadings;

    private final NumberFormat numberFormat = NumberFormatUtils.createDefaultNumberFormat();

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(
            Context context,
            HistoryPresenter presenter,
            int metricId,
            JSONArray glucoseReadings,
            JSONArray weightReadings,
            JSONArray ketoneReadings,
            JSONArray pressureReadings,
            JSONArray cholesterolReadings,
            JSONArray hb1acReadings,
            JSONArray foodReadings,
            JSONArray insulinReadings
            ) {
        this.mContext = context;
        this.presenter = presenter;
        this.metricId = metricId;
        this.glucoseReadings = glucoseReadings;
        this.weightReadings = weightReadings;
        this.ketoneReadings = ketoneReadings;
        this.pressureReadings = pressureReadings;
        this.cholesterolReadings = cholesterolReadings;
        this.hb1acReadings = hb1acReadings;
        this.foodReadings = foodReadings;
        this.insulinReadings = insulinReadings;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history_item, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView readingTextView = holder.mView.findViewById(R.id.item_history_reading);
        TextView datetimeTextView = holder.mView.findViewById(R.id.item_history_time);
        TextView typeTextView = holder.mView.findViewById(R.id.item_history_type);
        TextView idTextView = holder.mView.findViewById(R.id.item_history_id);
        TextView notesTextView = holder.mView.findViewById(R.id.item_history_notes);

        GlucoseRanges ranges = new GlucoseRanges(mContext);
        switch (metricId) {
            // Glucose
            case 0:
                try {
                    JSONObject glObj = glucoseReadings.getJSONObject(position);

                    idTextView.setText(glObj.get("id").toString());

                    double glucoseReading = glObj.getDouble("reading");//glucoseReadingArray.get(position);

                    String color = ranges.colorFromReading(glucoseReading);//glucoseReadingArray.get(position)

                    if (Constants.Units.MG_DL.equals(presenter.getUnitMeasuerement())) {
                        String reading = numberFormat.format(glucoseReading);
                        readingTextView.setText(mContext.getString(R.string.mg_dL_value, reading));
                    } else {
                        double mmol = GlucosioConverter.glucoseToMmolL(glucoseReading);
                        String reading = numberFormat.format(mmol);
                        readingTextView.setText(mContext.getString(R.string.mmol_L_value, reading));
                    }

                    readingTextView.setTextColor(ranges.stringToColor(color));
                    datetimeTextView.setText(presenter.convertDate(glObj.getString("created")));//glucoseDateTime.get(position)
                    typeTextView.setText(glObj.getString("reading_type"));//glucoseReadingType.get(position)
                    String notes = glObj.getString("notes");//glucoseNotes.get(position)
                    if (!notes.isEmpty()) {
                        notesTextView.setText(notes);
                        notesTextView.setVisibility(View.VISIBLE);
                    } else {
                        notesTextView.setText("");
                        notesTextView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            // A1C
            case 1:
                try {
                    JSONObject hbObj = hb1acReadings.getJSONObject(position);
                    idTextView.setText(hbObj.get("id").toString());
                    double readingVal = hbObj.getDouble("reading");
                    if ("percentage".equals(presenter.getA1cUnitMeasurement())) {
                        readingTextView.setText(numberFormat.format(readingVal) + " %");
                    } else {
                        double ifcc = GlucosioConverter.a1cNgspToIfcc(readingVal);
                        String reading = numberFormat.format(ifcc);
                        readingTextView.setText(mContext.getString(R.string.mmol_mol_value, reading));
                    }
                    datetimeTextView.setText(presenter.convertDate(hbObj.getString("created")));
                    typeTextView.setText("");
                    typeTextView.setVisibility(View.GONE);
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            // Cholesterol
            case 2:
                try {
                    JSONObject chObj = cholesterolReadings.getJSONObject(position);
                    idTextView.setText(chObj.get("id").toString());
                    String reading = numberFormat.format(chObj.getDouble("totalReading"));
                    readingTextView.setText(mContext.getString(R.string.mg_dL_value, reading));
                    datetimeTextView.setText(presenter.convertDate(chObj.getString("created")));
                    typeTextView.setText("LDL: " + numberFormat.format(chObj.getDouble("LDLReading")) +
                            " - " + "HDL: " + numberFormat.format(chObj.getDouble("HDLReading")));
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            // Pressure
            case 3:
                try {
                    JSONObject pObj = pressureReadings.getJSONObject(position);
                    idTextView.setText(pObj.get("id").toString());
                    readingTextView.setText(numberFormat.format(pObj.getDouble("maxReading")) + "/" +
                            numberFormat.format(pObj.getDouble("minReading")) + "  mm/Hg");
                    datetimeTextView.setText(presenter.convertDate(pObj.getString("created")));
                    typeTextView.setText("");
                    typeTextView.setVisibility(View.GONE);
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            //Ketones
            case 4:
                try {
                    JSONObject kObj = ketoneReadings.getJSONObject(position);
                    idTextView.setText(kObj.get("id").toString());
                    readingTextView.setText(numberFormat.format(kObj.getDouble("reading")) + " ммоль");
                    datetimeTextView.setText(presenter.convertDate(kObj.getString("created")));
                    typeTextView.setText("");
                    typeTextView.setVisibility(View.GONE);
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            // Weight
            case 5:
                try {
                    JSONObject wObj = weightReadings.getJSONObject(position);

                    idTextView.setText(wObj.get("id").toString());

                    readingTextView.setText("вес(кг): " + numberFormat.format(wObj.getDouble("weight")) + ", рост(см): " + numberFormat.format(wObj.getDouble("height")));

                    datetimeTextView.setText(presenter.convertDate(wObj.getString("created")));
                    typeTextView.setText("");
                    typeTextView.setVisibility(View.GONE);
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            // Food
            case 6:
                try {
                    JSONObject fObj = foodReadings.getJSONObject(position);
                    idTextView.setText(fObj.get("id").toString());

                    readingTextView.setText(fObj.getString("mealtime") + " - " + fObj.getString("productName") + " " + fObj.getDouble("reading") + " гр");
                    datetimeTextView.setText(presenter.convertDate(fObj.getString("created")));
                    typeTextView.setText(numberFormat.format(fObj.getDouble("breadUnit")) + " ХЕ");
                    readingTextView.setTextColor(ranges.stringToFoodColor(fObj.getString("qualityColor")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 7:
                try {
                    JSONObject iObj = insulinReadings.getJSONObject(position);
                    idTextView.setText(iObj.get("id").toString());
                    Integer insulinType = iObj.getInt("insulinType");
                    String insulinTypeText = insulinType == 1 ? "На еду" : "Левемир";
                    readingTextView.setText(insulinTypeText + " - " + iObj.getDouble("reading") + " ед");

                    datetimeTextView.setText(presenter.convertDate(iObj.getString("created")));
                    typeTextView.setText(iObj.getString("mealTime"));
                    readingTextView.setTextColor(ContextCompat.getColor(mContext, R.color.glucosio_text_dark));
                } catch (JSONException e){
                    e.printStackTrace();
                }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        switch (metricId) {
            // Glucose
            case 0:
                return glucoseReadings.length();
            // HB1AC
            case 1:
                return hb1acReadings.length();
            // Cholesterol
            case 2:
                return cholesterolReadings.length();
            // Pressure
            case 3:
                return pressureReadings.length();
            //Ketones
            case 4:
                return ketoneReadings.length();
            // Weight
            case 5:
                return weightReadings.length();
            // Food
            case 6:
                return foodReadings.length();
            case 7:
                return insulinReadings.length();
            default:
                return 0;
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }
}
