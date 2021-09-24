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

package org.deabee.android.fragment;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.stepstone.stepper.VerificationError;

import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.activity.AddA1CActivity;
import org.deabee.android.activity.AddCholesterolActivity;
import org.deabee.android.activity.AddFoodActivity;
import org.deabee.android.activity.AddGlucoseActivity;
import org.deabee.android.activity.AddKetoneActivity;
import org.deabee.android.activity.AddPressureActivity;
import org.deabee.android.activity.AddWeightActivity;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.adapter.HistoryAdapter;
import org.deabee.android.listener.ItemClickSupport;
import org.deabee.android.presenter.HistoryPresenter;
import org.deabee.android.tools.FormatDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import static com.android.volley.VolleyLog.TAG;

public class HistoryFragment extends Fragment {

    private static final String INTENT_EXTRA_PAGER = "pager";
    private static final String INTENT_EXTRA_EDITING_ID = "edit_id";
    private static final String INTENT_EXTRA_EDITING = "editing";
    private static final String INTENT_EXTRA_DROPDOWN = "history_dropdown";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private HistoryPresenter presenter;
    private LinearLayout glucoseLegend;
    private Spinner historySpinner;
    private BottomSheetDialog mBottomSheetDialog;
    private Boolean isToolbarScrolling = true;
    private int historyDropdownPosition = 0;


    TextView diary_req_date;

    TextView diary_mealtime_1;
    TextView diary_glucose_1;
    TextView diary_insulin_1;
    TextView diary_food_1;
    TextView diary_bu_1;
    TextView diary_notes_1;

    TextView diary_mealtime_2;
    TextView diary_glucose_2;
    TextView diary_insulin_2;
    TextView diary_food_2;
    TextView diary_bu_2;
    TextView diary_notes_2;

    TextView diary_mealtime_3;
    TextView diary_glucose_3;
    TextView diary_insulin_3;
    TextView diary_food_3;
    TextView diary_bu_3;
    TextView diary_notes_3;

    TextView diary_mealtime_4;
    TextView diary_glucose_4;
    TextView diary_insulin_4;
    TextView diary_food_4;
    TextView diary_bu_4;
    TextView diary_notes_4;

    TextView diary_mealtime_5;
    TextView diary_glucose_5;
    TextView diary_insulin_5;
    TextView diary_food_5;
    TextView diary_bu_5;
    TextView diary_notes_5;

    private String server;
    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mFragmentView;
        presenter = new HistoryPresenter(this);
        userId = presenter.dB.getCurrentUser().getId();
        server = presenter.dB.getAppSettings().IP_ADDRESS;
        mFragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        final String server = presenter.dB.getAppSettings().IP_ADDRESS;
        createDateTimeViewAndListener(mFragmentView);
        diary_mealtime_1 = mFragmentView.findViewById(R.id.diary_mealtime_1);
        diary_glucose_1 = mFragmentView.findViewById(R.id.diary_glucose_1);
        diary_insulin_1 = mFragmentView.findViewById(R.id.diary_insulin_1);
        diary_food_1 = mFragmentView.findViewById(R.id.diary_food_1);
        diary_bu_1 = mFragmentView.findViewById(R.id.diary_bu_1);
        diary_notes_1 = mFragmentView.findViewById(R.id.diary_notes_1);

        diary_mealtime_2 = mFragmentView.findViewById(R.id.diary_mealtime_2);
        diary_glucose_2 = mFragmentView.findViewById(R.id.diary_glucose_2);
        diary_insulin_2 = mFragmentView.findViewById(R.id.diary_insulin_2);
        diary_food_2 = mFragmentView.findViewById(R.id.diary_food_2);
        diary_bu_2 = mFragmentView.findViewById(R.id.diary_bu_2);
        diary_notes_2 = mFragmentView.findViewById(R.id.diary_notes_2);

        diary_mealtime_3 = mFragmentView.findViewById(R.id.diary_mealtime_3);
        diary_glucose_3 = mFragmentView.findViewById(R.id.diary_glucose_3);
        diary_insulin_3 = mFragmentView.findViewById(R.id.diary_insulin_3);
        diary_food_3 = mFragmentView.findViewById(R.id.diary_food_3);
        diary_bu_3 = mFragmentView.findViewById(R.id.diary_bu_3);
        diary_notes_3 = mFragmentView.findViewById(R.id.diary_notes_3);

        diary_mealtime_4 = mFragmentView.findViewById(R.id.diary_mealtime_4);
        diary_glucose_4 = mFragmentView.findViewById(R.id.diary_glucose_4);
        diary_insulin_4 = mFragmentView.findViewById(R.id.diary_insulin_4);
        diary_food_4 = mFragmentView.findViewById(R.id.diary_food_4);
        diary_bu_4 = mFragmentView.findViewById(R.id.diary_bu_4);
        diary_notes_4 = mFragmentView.findViewById(R.id.diary_notes_4);

        diary_mealtime_5 = mFragmentView.findViewById(R.id.diary_mealtime_5);
        diary_glucose_5 = mFragmentView.findViewById(R.id.diary_glucose_5);
        diary_insulin_5 = mFragmentView.findViewById(R.id.diary_insulin_5);
        diary_food_5 = mFragmentView.findViewById(R.id.diary_food_5);
        diary_bu_5 = mFragmentView.findViewById(R.id.diary_bu_5);
        diary_notes_5 = mFragmentView.findViewById(R.id.diary_notes_5);


        return mFragmentView;

        /*mRecyclerView = (RecyclerView) mFragmentView.findViewById(R.id.fragment_history_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mLayoutManager = new LinearLayoutManager(super.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        historySpinner = (Spinner) mFragmentView.findViewById(R.id.history_spinner);
        glucoseLegend = (LinearLayout) mFragmentView.findViewById(R.id.fragment_history_legend);

        // use a linear layout manager
        // Set array and adapter for graphSpinner
        String[] selectorArray = getActivity().getResources().getStringArray(R.array.fragment_history_selector);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, selectorArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        historySpinner.setAdapter(dataAdapter);

        historySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!presenter.isdbEmpty()) {
                    if (position != 0) {
                        glucoseLegend.setVisibility(View.GONE);
                    } else {
                        glucoseLegend.setVisibility(View.VISIBLE);
                    }


                    //loadAllReadingsFromWEB(server, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                showBottomSheetDialog(v);
                return true;
            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mRecyclerView.removeOnLayoutChangeListener(this);
                updateToolbarBehaviour();
            }
        });

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(INTENT_EXTRA_DROPDOWN)) {
                historySpinner.setSelection(extras.getInt(INTENT_EXTRA_DROPDOWN));
            }
        }

        return mFragmentView;*/
    }
    private int cYear;
    private int cMonth;
    private int cDay;
    public void createDateTimeViewAndListener(final View vw) {
        diary_req_date = vw.findViewById(R.id.diary_req_date);
        final DecimalFormat df = new DecimalFormat("00");
        final Calendar now = Calendar.getInstance();
        cYear = now.get(Calendar.YEAR);
        cMonth = now.get(Calendar.MONTH);
        cDay = now.get(Calendar.DAY_OF_MONTH);
        String y = cYear + "";
        String month = df.format(cMonth + 1);
        String day = df.format(cDay);
        String date = day + "." + month + "." + y;
        //diary_req_date.setText(date);
        diary_req_date.setOnClickListener(new View.OnClickListener() {
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
                                diary_req_date.setText(date);
                                //Toast.makeText(vw.getContext(), "Selected " + date, Toast.LENGTH_SHORT).show();
                                showSimpleProgressDialog(vw.getContext(), "Дневник самоконтроля", "Загрузка с сервера...", false);
                                GetDiaryHttp(vw, userId, date);
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
    private void GetDiaryHttp(final View vw, final int patientId, final String date)
    {
        final String url = "http://" + server + ":8090/webdatabase-deabee/api/Diary/Get?patientId=" + patientId + "&dateStr=" + date;
        RequestQueue mRequestQueue = Volley.newRequestQueue(vw.getContext());

        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response.length() == 0){
                    Toast.makeText(vw.getContext(), "Не могу скачать дневник по параметрам: patientId=" + patientId + "&dateStr=" + date, Toast.LENGTH_SHORT).show();
                }
                try {
                    SetDiaryToView(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(vw.getContext(), "Ошибка записи дневника по параметрам: patientId=" + patientId + "&dateStr=" + date, Toast.LENGTH_SHORT).show();
                }
                removeSimpleProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG, "onErrorResponse: " + error.toString());
                Log.d(TAG, url);
                Toast.makeText(getActivity(), "Volley: " + error.toString() + " URL: " + url, Toast.LENGTH_LONG).show();

                removeSimpleProgressDialog();
            }
        });
        mRequestQueue.add(arrayRequest);
    }
    private void SetDiaryToView(JSONArray diaryItems) throws JSONException {
        //1
        JSONObject item = diaryItems.getJSONObject(0);
        String time = item.isNull("time") ? "-:-" : item.getString("time");
        String mealtime = item.isNull("mealtime") ? "-" : item.getString("mealtime");
        diary_mealtime_1.setText(time + " " + mealtime);
        diary_glucose_1.setText(item.isNull("glucose") ? "-" : item.getString("glucose"));
        diary_insulin_1.setText(item.isNull("insulin") ? "-" : item.getString("insulin"));
        diary_food_1.setText(item.isNull("food") ? "-" : item.getString("food"));
        diary_bu_1.setText(item.isNull("breadUnitTotal") ? "-" : item.getString("breadUnitTotal"));
        diary_notes_1.setText(item.isNull("notes") ? "-" : item.getString("notes"));
        //2
        item = diaryItems.getJSONObject(1);
        time = item.isNull("time") ? "-:-" : item.getString("time");
        mealtime = item.isNull("mealtime") ? "-" : item.getString("mealtime");
        diary_mealtime_2.setText(time + " " + mealtime);
        diary_glucose_2.setText(item.isNull("glucose") ? "-" : item.getString("glucose"));
        diary_insulin_2.setText(item.isNull("insulin") ? "-" : item.getString("insulin"));
        diary_food_2.setText(item.isNull("food") ? "-" : item.getString("food"));
        diary_bu_2.setText(item.isNull("breadUnitTotal") ? "-" : item.getString("breadUnitTotal"));
        diary_notes_2.setText(item.isNull("notes") ? "-" : item.getString("notes"));
        //3
        item = diaryItems.getJSONObject(2);
        time = item.isNull("time") ? "-:-" : item.getString("time");
        mealtime = item.isNull("mealtime") ? "-" : item.getString("mealtime");
        diary_mealtime_3.setText(time + " " + mealtime);
        diary_glucose_3.setText(item.isNull("glucose") ? "-" : item.getString("glucose"));
        diary_insulin_3.setText(item.isNull("insulin") ? "-" : item.getString("insulin"));
        diary_food_3.setText(item.isNull("food") ? "-" : item.getString("food"));
        diary_bu_3.setText(item.isNull("breadUnitTotal") ? "-" : item.getString("breadUnitTotal"));
        diary_notes_3.setText(item.isNull("notes") ? "-" : item.getString("notes"));
        //4
        item = diaryItems.getJSONObject(3);
        time = item.isNull("time") ? "-:-" : item.getString("time");
        mealtime = item.isNull("mealtime") ? "-" : item.getString("mealtime");
        diary_mealtime_4.setText(time + " " + mealtime);
        diary_glucose_4.setText(item.isNull("glucose") ? "-" : item.getString("glucose"));
        diary_insulin_4.setText(item.isNull("insulin") ? "-" : item.getString("insulin"));
        diary_food_4.setText(item.isNull("food") ? "-" : item.getString("food"));
        diary_bu_4.setText(item.isNull("breadUnitTotal") ? "-" : item.getString("breadUnitTotal"));
        diary_notes_4.setText(item.isNull("notes") ? "-" : item.getString("notes"));
        //5
        item = diaryItems.getJSONObject(4);
        time = item.isNull("time") ? "-:-" : item.getString("time");
        mealtime = item.isNull("mealtime") ? "-" : item.getString("mealtime");
        diary_mealtime_5.setText(time + " " + mealtime);
        diary_glucose_5.setText(item.isNull("glucose") ? "-" : item.getString("glucose"));
        diary_insulin_5.setText(item.isNull("insulin") ? "-" : item.getString("insulin"));
        diary_food_5.setText(item.isNull("food") ? "-" : item.getString("food"));
        diary_bu_5.setText(item.isNull("breadUnitTotal") ? "-" : item.getString("breadUnitTotal"));
        diary_notes_5.setText(item.isNull("notes") ? "-" : item.getString("notes"));
    }
    private static ProgressDialog mProgressDialog;
    public static void removeSimpleProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();

        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showSimpleProgressDialog(Context context, String title,
                                                String msg, boolean isCancelable) {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, title, msg);
                mProgressDialog.setCancelable(isCancelable);
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }

        } catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int userId;
    @SuppressLint("StaticFieldLeak")
    private void loadAllReadingsFromWEB(final String server, final int position){

        showSimpleProgressDialog(getContext(), "Загрузка данных...","Глюкоза",false);

        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                try {
                    String jsonURL = "http://" + server + ":8090/webdatabase-deabee/api/HistoryReadings/GetAll/" + userId;
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare(HttpRequest.Method.GET)/*.withData(map)*/.sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted(result, position);
            }
        }.execute();
    }
    public void onTaskCompleted(String response, int position) {
        try {
            removeSimpleProgressDialog();
            JSONObject allReadings = new JSONObject(response);

            final Context context = getActivity().getApplicationContext();
            mAdapter = new HistoryAdapter(
                    context,
                    presenter,
                    position,
                    allReadings.getJSONArray("glucoseReadings"),
                    allReadings.getJSONArray("weightReadings"),
                    allReadings.getJSONArray("ketoneReadings"),
                    allReadings.getJSONArray("pressureReadings"),
                    allReadings.getJSONArray("cholesterolReadings"),
                    allReadings.getJSONArray("hb1acReadings"),
                    allReadings.getJSONArray("foodReadings"),
                    allReadings.getJSONArray("insulinReadings")
            );
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            historyDropdownPosition = position;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void showBottomSheetDialog(final View itemView) {
        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.fragment_history_bottom_sheet, null);
        LinearLayout edit = (LinearLayout) sheetView.findViewById(R.id.fragment_history_bottom_sheet_edit);
        LinearLayout delete = (LinearLayout) sheetView.findViewById(R.id.fragment_history_bottom_sheet_delete);
        final TextView idTextView = (TextView) itemView.findViewById(R.id.item_history_id);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int historyTypePosition = (int) historySpinner.getSelectedItemId();
                Intent intent;
                switch (historyTypePosition) {
                    // HAB1C
                    case 1:
                        intent = new Intent(getActivity(), AddA1CActivity.class);
                        break;
                    // Cholesterol
                    case 2:
                        intent = new Intent(getActivity(), AddCholesterolActivity.class);
                        break;
                    // Pressure
                    case 3:
                        intent = new Intent(getActivity(), AddPressureActivity.class);
                        break;
                    // Ketone
                    case 4:
                        intent = new Intent(getActivity(), AddKetoneActivity.class);
                        break;
                    // Weight
                    case 5:
                        intent = new Intent(getActivity(), AddWeightActivity.class);
                        break;
                    // Food
                    case 6:
                        intent = new Intent(getActivity(), AddFoodActivity.class);
                        break;
                    // Glucose
                    default:
                        intent = new Intent(getActivity(), AddGlucoseActivity.class);
                        break;
                }

                intent.putExtra(INTENT_EXTRA_EDITING_ID, Long.parseLong(idTextView.getText().toString()));
                intent.putExtra(INTENT_EXTRA_EDITING, true);
                intent.putExtra(INTENT_EXTRA_DROPDOWN, historyDropdownPosition);
                // History page is 1
                intent.putExtra(INTENT_EXTRA_PAGER, 1);
                startActivity(intent);
                mBottomSheetDialog.dismiss();
                getActivity().finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.dismiss();
                TextView idTextView = (TextView) itemView.findViewById(R.id.item_history_id);
                final long idToDelete = Long.parseLong(idTextView.getText().toString());
                final CardView item = (CardView) itemView.findViewById(R.id.item_history);
                item.animate().alpha(0.0f).setDuration(2000);
                Snackbar.make(((MainActivity) getActivity()).getFabView(), R.string.fragment_history_snackbar_text, Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        switch (event) {
                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                // Do nothing, see Undo onClickListener
                                break;
                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                presenter.onDeleteClicked(idToDelete, historySpinner.getSelectedItemPosition());
                                break;
                            default:
                                break;
                        }
                    }
                }).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.clearAnimation();
                        item.setAlpha(1.0f);
                        mAdapter.notifyDataSetChanged();
                    }
                }).setActionTextColor(ContextCompat.getColor(getContext(), R.color.glucosio_accent)).show();

            }
        });

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }

    public void updateToolbarBehaviour() {
        if (mAdapter != null) {
            if (mLayoutManager.findLastCompletelyVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                isToolbarScrolling = false;
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).turnOffToolbarScrolling();
                }
            } else {
                if (!isToolbarScrolling) {
                    isToolbarScrolling = true;
                    ((MainActivity) getActivity()).turnOnToolbarScrolling();
                }
            }
        }
    }

    public String convertDate(String date) {
        FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());
        return dateTime.convertDateTime(date);
    }

    public void notifyAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    public void reloadFragmentAdapter() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).reloadFragmentAdapter();
            ((MainActivity) getActivity()).checkIfEmptyLayout();
        }
    }

    public int getHistoryDropdownPosition() {
        return historyDropdownPosition;
    }
}
