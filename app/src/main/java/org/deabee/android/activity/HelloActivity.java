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

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.deabee.android.GlucosioApplication;
import org.deabee.android.HttpRequest;
import org.deabee.android.R;
import org.deabee.android.analytics.Analytics;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.object.MealTimeModel;
import org.deabee.android.presenter.HelloPresenter;
import org.deabee.android.tools.LabelledSpinner;
import org.deabee.android.tools.LocaleHelper;
import org.deabee.android.tools.network.GlucosioExternalLinks;
import org.deabee.android.view.HelloView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HelloActivity extends AppCompatActivity implements HelloView {

    /*@BindView(R.id.activity_hello_spinner_country)
    LabelledSpinner countrySpinner;

    @BindView(R.id.activity_hello_spinner_language)
    LabelledSpinner languageSpinner;*/

    @BindView(R.id.activity_hello_spinner_diabetes_type)
    LabelledSpinner typeSpinner;

    @BindView(R.id.activity_hello_spinner_preferred_unit)
    LabelledSpinner unitSpinner;

    @BindView(R.id.activity_hello_button_start)
    Button startButton;

    @BindView(R.id.activity_hello_username)
    TextView userNameTextView;

    @BindView(R.id.activity_hello_userType)
    LabelledSpinner userTypeSpinner;


    private HelloPresenter presenter;

    private List<String> localesWithTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        ButterKnife.bind(this);

        // Prevent SoftKeyboard to pop up on start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        GlucosioApplication application = (GlucosioApplication) getApplication();
        presenter = application.createHelloPresenter(this);
        //presenter.loadDatabase();

        final LocaleHelper localeHelper = application.getLocaleHelper();
        //initCountrySpinner(localeHelper);
        //initLanguageSpinner(localeHelper);

        userTypeSpinner.setItemsArray(R.array.pref_userTypeTexts);
        unitSpinner.setItemsArray(R.array.helloactivity_preferred_glucose_unit);
        typeSpinner.setItemsArray(R.array.helloactivity_diabetes_type);

        initStartButton();

        //showAnalyticsExplanationDialog();
/*
        Log.i("HelloActivity", "Setting screen name: hello");
        for(String ip : new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.pref_deabeeServers)))) {
            CheckAvailableServer r = new CheckAvailableServer(ip, 8090);
            r.execute();
        }*/
    }
/*
    private void initLanguageSpinner(final LocaleHelper localeHelper) {
        localesWithTranslation = localeHelper.getLocalesWithTranslation(getResources());

        List<String> displayLanguages = new ArrayList<>(localesWithTranslation.size());
        for (String language : localesWithTranslation) {
            if (language.length() > 0) {
                displayLanguages.add(localeHelper.getDisplayLanguage(language));
            }
        }

        //languageSpinner.setItemsArray(displayLanguages);

        final Locale deviceLocale = localeHelper.getDeviceLocale();
        String displayLanguage = localeHelper.getDisplayLanguage(deviceLocale.toString());

        setSelection(displayLanguage, languageSpinner);
    }
*/
    /*private void setSelection(final String label, final LabelledSpinner labelledSpinner) {
        if (label != null) {
            int position = ((ArrayAdapter) labelledSpinner.getSpinner().getAdapter()).getPosition(label);
            labelledSpinner.setSelection(position);
        }
    }*/

    private void initStartButton() {
        final Drawable pinkArrow = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_navigate_next_pink_24px, null);
        if (pinkArrow != null) {
            pinkArrow.setBounds(0, 0, 60, 60);
            startButton.setCompoundDrawables(null, null, pinkArrow, null);
        }
    }
/*
    private void initCountrySpinner(final LocaleHelper localeHelper) {
        // Get countries list from locale
        ArrayList<String> countries = new ArrayList<>();
        Locale[] locales = Locale.getAvailableLocales();

        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();

            if ((country.trim().length() > 0) && (!countries.contains(country))) {
                countries.add(country);
            }
        }

        Collections.sort(countries);

        // Populate Spinners with array
        //countrySpinner.setItemsArray(countries);

        // Get locale country name and set the spinner
        String localCountry = localeHelper.getDeviceLocale().getDisplayCountry();

        //setSelection(localCountry, countrySpinner);
    }
*/
    @OnClick(R.id.activity_hello_button_start)
    void onStartClicked() {
        presenter.onNextClicked(
                userNameTextView.getText().toString(),
                typeSpinner.getSpinner().getSelectedItemPosition() + 1,
                unitSpinner.getSpinner().getSelectedItem().toString(),
                userTypeSpinner.getSpinner().getSelectedItemPosition() + 1);
    }

    @OnClick(R.id.helloactivity_textview_terms)
    void onTermsAndConditionClick() {
        ExternalLinkActivity.launch(
            this,
            getString(R.string.preferences_terms),
            GlucosioExternalLinks.TERMS);
    }

    public void displayErrorWrongAge() {
        Toast.makeText(getApplicationContext(), getString(R.string.helloactivity_age_invalid), Toast.LENGTH_SHORT).show();
    }
    public void displayErrorWrongUsername() {
        Toast.makeText(getApplicationContext(), getString(R.string.helloactivity_username_invalid), Toast.LENGTH_SHORT).show();
    }
    public void displayError(String errorText) {
        Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
    }

    public void startMainView() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
/*
    private void showAnalyticsExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.analytics_usage)
                .setMessage(R.string.analytics_usage_overview)
                .setCancelable(false)
                .setNegativeButton(R.string.optout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        sharedPref.edit().putBoolean("pref_analytics_opt_in", false).apply();
                    }
                })
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        sharedPref.edit().putBoolean("pref_analytics_opt_in", true).apply();
                        GlucosioApplication application = (GlucosioApplication) getApplication();
                        Analytics analytics = application.getAnalytics();
                        analytics.reportScreen("Hello Activity");
                    }
                })
                .show();
    }

*/

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    public void showSimpleProgressDialog() {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(this, "Добро пожаловать!", "Осталось несколько секунд...");
                mProgressDialog.setCancelable(false);
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

    private ProgressDialog mProgressDialog;
    public void removeSimpleProgressDialog() {
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
}
