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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import org.deabee.android.Constants;
import org.deabee.android.GlucosioApplication;
import org.deabee.android.R;
import org.deabee.android.analytics.Analytics;
import org.deabee.android.db.AppSettings;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.User;
import org.deabee.android.presenter.HelloPresenter;
import org.deabee.android.tools.GlucoseRanges;
import org.deabee.android.tools.GlucosioConverter;
import org.deabee.android.tools.InputFilterMinMax;
import org.deabee.android.tools.LocaleHelper;
import org.deabee.android.tools.ReadingTools;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreferencesActivity extends AppCompatActivity {

    @NonNull
    private static String[] getEntryValues(List<String> list) {
        String[] result = new String[list.size()];
        return list.toArray(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        getFragmentManager().beginTransaction()
                .replace(R.id.preferencesFrame, new MyPreferenceFragment()).commit();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(getString(R.string.action_settings));
        }

        // Obtain the Analytics shared Tracker instance.
        GlucosioApplication application = (GlucosioApplication) getApplication();
        Analytics analytics = application.getAnalytics();
        Log.i("PreferencesActivity", "Setting screen name: preferences");
        analytics.reportScreen("Preferences");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        private DatabaseHandler dB;
        private User user;
        private ListPreference languagePref;
        private ListPreference userTypePref;
        //private ListPreference genderPref;
        private ListPreference diabetesTypePref;
        private ListPreference unitPrefGlucose;
        private ListPreference insulinNamePref;
        private ListPreference insulinCompanyPref;
        private ListPreference unitPrefA1c;
        private ListPreference unitPrefWeight;
        private ListPreference rangePref;
        //private EditText ageEditText;
        private EditText minEditText;
        private EditText maxEditText;
        private EditTextPreference usernamePref;
        private EditTextPreference minRangePref;
        private EditTextPreference maxRangePref;
        private SwitchPreference dyslexiaModePref;
        private SwitchPreference freestyleLibrePref;
        private SwitchPreference analyticsOptInPref;
        private User updatedUser;
        private LocaleHelper localeHelper;


        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            final GlucosioApplication app = (GlucosioApplication) getActivity().getApplicationContext();
            dB = app.getDBHandler();
            localeHelper = app.getLocaleHelper();
            user = dB.getCurrentUser();//dB.getUser(1);
            updatedUser = new User(user);
            usernamePref = (EditTextPreference) findPreference("pref_username");
            userTypePref = (ListPreference) findPreference("pref_userType");
            //genderPref = (ListPreference) findPreference("pref_gender");
            languagePref = (ListPreference) findPreference("pref_language");
            diabetesTypePref = (ListPreference) findPreference("pref_diabetes_type");
            unitPrefGlucose = (ListPreference) findPreference("pref_unit_glucose");
            insulinNamePref = (ListPreference) findPreference("pref_insulinName");
            insulinCompanyPref = (ListPreference) findPreference("pref_insulinCompany");
            unitPrefA1c = (ListPreference) findPreference("pref_unit_a1c");
            unitPrefWeight = (ListPreference) findPreference("pref_unit_weight");
            rangePref = (ListPreference) findPreference("pref_range");
            minRangePref = (EditTextPreference) findPreference("pref_range_min");
            maxRangePref = (EditTextPreference) findPreference("pref_range_max");
            dyslexiaModePref = (SwitchPreference) findPreference("pref_font_dyslexia");
            freestyleLibrePref = (SwitchPreference) findPreference("pref_freestyle_libre");
            analyticsOptInPref = (SwitchPreference) findPreference( "pref_analytics_opt_in");

            usernamePref.setText(user.getName());

            try {
                String userTypeText = "";
                Integer userTypeId = user.getUserType();
                userTypePref.setValue(String.valueOf(userTypeId));

                ArrayList<String> userTypeValues = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.pref_userTypeValues)));
                ArrayList<String> userTypeTexts = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.pref_userTypeTexts)));


                if(userTypeId > 0) {
                    userTypePref.setSummary(userTypeTexts.get(userTypeValues.indexOf(userTypeId.toString())));
                }
            } catch (Exception e) {
                Log.e("USER_TYPE_PARSE", e.getMessage());
                e.printStackTrace();
            }

            //genderPref.setValue(user.getGender());
            diabetesTypePref.setValue(String.valueOf(user.getD_type()));
            if (user.getInsulinName() != null) {
                insulinNamePref.setValue(user.getInsulinName());
            }
            if (user.getInsulinCompany() != null) {
                insulinCompanyPref.setValue(user.getInsulinCompany());
            }
            unitPrefGlucose.setValue(getGlucoseUnitValue(user.getPreferred_unit()));
            unitPrefA1c.setValue(getA1CUnitValue(user.getPreferred_unit_a1c()));
            unitPrefWeight.setValue(getUnitWeight(user.getPreferred_unit_weight()));
            rangePref.setValue(user.getPreferred_range());

            if (Constants.Units.MG_DL.equals(user.getPreferred_unit())) {
                maxRangePref.setDefaultValue(user.getCustom_range_max());
                minRangePref.setDefaultValue(user.getCustom_range_min());
            } else {
                maxRangePref.setDefaultValue(GlucosioConverter.glucoseToMmolL(user.getCustom_range_max()));
                minRangePref.setDefaultValue(GlucosioConverter.glucoseToMmolL(user.getCustom_range_min()));
            }

            if (!"custom".equals(rangePref.getValue())) {
                minRangePref.setEnabled(false);
                maxRangePref.setEnabled(false);
            } else {
                minRangePref.setEnabled(true);
                maxRangePref.setEnabled(true);
            }

            final Preference aboutPref = findPreference("about_settings");
            usernamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().trim().equals("")) {
                        return false;
                    }
                    updatedUser.setName(newValue.toString());
                    updateDB();
                    return true;
                }
            });
            userTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setUserType(Integer.parseInt(newValue.toString()));
                    updateDB();
                    return true;
                }
            });
            /*genderPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setGender(newValue.toString());
                    updateDB();
                    return true;
                }
            });*/
            diabetesTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String[] typesArray = getResources().getStringArray(R.array.helloactivity_diabetes_type);
                    String selectedType = newValue.toString();

                    if (selectedType.equals(typesArray[0])) {
                        updatedUser.setD_type(1);
                        updateDB();
                    } else if (selectedType.equals(typesArray[1])) {
                        updatedUser.setD_type(2);
                        updateDB();
                    } else if (selectedType.equals(typesArray[2])) {
                        updatedUser.setD_type(3);
                        updateDB();
                    } else {
                        updatedUser.setD_type(4);
                        updateDB();
                    }

                    return true;
                }
            });
            insulinNamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setInsulinName(newValue.toString());
                    updateDB();
                    return true;
                }
            });

            insulinCompanyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updatedUser.setInsulinCompany(newValue.toString());
                    updateDB();
                    return true;
                }
            });
            unitPrefGlucose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String preferredUnit = getResources().getString(R.string.helloactivity_spinner_preferred_glucose_unit_1).equals(newValue.toString()) ?
                            Constants.Units.MG_DL :
                            Constants.Units.MMOL_L;
                    updatedUser.setPreferred_unit(preferredUnit);
                    updateDB();
                    return true;
                }
            });
            unitPrefA1c.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.preferences_spinner_preferred_a1c_unit_1))) {
                        updatedUser.setPreferred_unit_a1c("percentage");
                    } else {
                        updatedUser.setPreferred_unit_a1c("mmol/mol");
                    }
                    updateDB();
                    return true;
                }
            });
            unitPrefWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals(getResources().getString(R.string.preferences_spinner_preferred_weight_unit_1))) {
                        updatedUser.setPreferred_unit_weight("kilograms");
                    } else {
                        updatedUser.setPreferred_unit_weight("pounds");
                    }
                    updateDB();
                    return true;
                }
            });
            rangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String selectedPreset = newValue.toString();
                    updatedUser.setPreferred_range(selectedPreset);

                    // look up the min/max values of the selected preset
                    if (!selectedPreset.equals("Custom range")) {
                        int rangeMin = GlucoseRanges.getPresetMin(selectedPreset);
                        int rangeMax = GlucoseRanges.getPresetMax(selectedPreset);
                        // min/max ranges are stored in mg/dl format
                        updatedUser.setCustom_range_min(rangeMin);
                        updatedUser.setCustom_range_max(rangeMax);
                    }

                    updateDB();
                    return true;
                }
            });

            minRangePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    minEditText.setText("");
                    return false;
                }
            });

            minRangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (TextUtils.isEmpty(newValue.toString().trim())) {
                        return false;
                    }
                    double glucoseDouble = ReadingTools.safeParseDouble(newValue.toString());
                    if (user.getPreferred_unit().equals(Constants.Units.MG_DL)) {
                        updatedUser.setCustom_range_min(glucoseDouble);
                    } else {
                        updatedUser.setCustom_range_min(GlucosioConverter.glucoseToMgDl(glucoseDouble));
                    }
                    updateDB();
                    return true;
                }
            });

            maxRangePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    maxEditText.setText("");
                    return false;
                }
            });
            maxRangePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (TextUtils.isEmpty(newValue.toString().trim())) {
                        return false;
                    }
                    if (user.getPreferred_unit().equals(Constants.Units.MG_DL)) {
                        updatedUser.setCustom_range_max(ReadingTools.safeParseDouble(newValue.toString()));
                    } else {
                        updatedUser.setCustom_range_max(GlucosioConverter.glucoseToMgDl(ReadingTools.safeParseDouble(newValue.toString())));
                    }
                    updateDB();
                    return true;
                }
            });
            dyslexiaModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // EXPERIMENTAL PREFERENCE
                    // Display Alert
                    showExperimentalDialog(true);
                    return true;
                }
            });
            freestyleLibrePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!((SwitchPreference) preference).isChecked()) {
                        // EXPERIMENTAL PREFERENCE
                        // Display Alert
                        showExperimentalDialog(false);
                    }
                    return true;
                }
            });
            analyticsOptInPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference preference, Object newValue) {
                     return true;
                 }
            });
            //ageEditText = agePref.getEditText();
            minEditText = minRangePref.getEditText();
            maxEditText = maxRangePref.getEditText();

            //ageEditText.setFilters(new InputFilter[]{new InputFilterMinMax(1, 110)});

            updateDB();

            aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent aboutActivity = new Intent(getActivity(), AboutActivity.class);
                    getActivity().startActivity(aboutActivity);
                    return false;
                }
            });

        }

        private String getA1CUnitValue(final String a1CUnit) {
            @StringRes int unitResId = "percentage".equals(a1CUnit) ?
                    R.string.preferences_spinner_preferred_a1c_unit_1 :
                    R.string.preferences_spinner_preferred_a1c_unit_2;
            return getResources().getString(unitResId);
        }

        private String getGlucoseUnitValue(final String glucoseUnit) {
            @StringRes int unitResId = Constants.Units.MG_DL.equals(glucoseUnit) ?
                    R.string.helloactivity_spinner_preferred_glucose_unit_1 :
                    R.string.helloactivity_spinner_preferred_glucose_unit_2;
            return getResources().getString(unitResId);
        }

        private String getUnitWeight(final String unit_weight) {
            @StringRes int unitResId = "kilograms".equals(unit_weight) ?
                    R.string.preferences_spinner_preferred_weight_unit_1 :
                    R.string.preferences_spinner_preferred_weight_unit_2;
            return getResources().getString(unitResId);
        }

        /*private void initLanguagePreference() {
            List<String> valuesLanguages = localeHelper.getLocalesWithTranslation(getResources());

            List<String> displayLanguages = new ArrayList<>(valuesLanguages.size());
            for (String language : valuesLanguages) {
                if (language.length() > 0) {
                    displayLanguages.add(localeHelper.getDisplayLanguage(language));
                }
            }
        }*/

        private void updateDB() {
            dB.updateUser(updatedUser);
            usernamePref.setSummary(String.valueOf(user.getName()));
            languagePref.setSummary(String.valueOf(user.getPreferred_language()));

            try {
                String userTypeText = "";
                Integer userTypeId = user.getUserType();

                ArrayList<String> userTypeValues = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.pref_userTypeValues)));
                ArrayList<String> userTypeTexts = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.pref_userTypeTexts)));


                if(userTypeId > 0) {
                    userTypePref.setSummary(userTypeTexts.get(userTypeValues.indexOf(userTypeId.toString())));
                }
            } catch (Exception e) {
                Log.e("USER_TYPE_PREF_UPDATE", e.getMessage());
                e.printStackTrace();
            }

            insulinNamePref.setSummary(String.valueOf(user.getInsulinName()));
            insulinCompanyPref.setSummary(String.valueOf(user.getInsulinCompany()));
            diabetesTypePref.setSummary(getResources().getStringArray(R.array.helloactivity_diabetes_type)[user.getD_type() - 1]);
            unitPrefGlucose.setSummary(getGlucoseUnitValue(user.getPreferred_unit()));
            unitPrefA1c.setSummary(getA1CUnitValue(user.getPreferred_unit_a1c()));
            unitPrefWeight.setSummary(getUnitWeight(user.getPreferred_unit_weight()));
            rangePref.setSummary(user.getPreferred_range());

            if (Constants.Units.MG_DL.equals(user.getPreferred_unit())) {
                minRangePref.setSummary(String.valueOf(user.getCustom_range_min()));
                maxRangePref.setSummary(String.valueOf(user.getCustom_range_max()));
            } else {
                minRangePref.setSummary(String.valueOf(GlucosioConverter.glucoseToMmolL(user.getCustom_range_min())));
                maxRangePref.setSummary(String.valueOf(GlucosioConverter.glucoseToMmolL(user.getCustom_range_max())));
            }

            if (!user.getPreferred_range().equals("Custom range")) {
                minRangePref.setEnabled(false);
                maxRangePref.setEnabled(false);
            } else {
                minRangePref.setEnabled(true);
                maxRangePref.setEnabled(true);
            }

            // Get countries list from locale
            /*ArrayList<String> countriesArray = new ArrayList<>();
            Locale[] locales = Locale.getAvailableLocales();

            for (Locale locale : locales) {
                String country = locale.getDisplayCountry();
                if (country.trim().length() > 0 && !countriesArray.contains(country)) {
                    countriesArray.add(country);
                }
            }
            Collections.sort(countriesArray);

            CharSequence[] countries = countriesArray.toArray(new CharSequence[countriesArray.size()]);
            countryPref.setEntryValues(countries);
            countryPref.setEntries(countries);*/

            initLanguagePreference();

            updateToWeb(user);
        }

        private void initLanguagePreference() {
            List<String> valuesLanguages = localeHelper.getLocalesWithTranslation(getResources());
/*
            List<String> displayLanguages = new ArrayList<>(valuesLanguages.size());
            for (String language : valuesLanguages) {
                if (language.length() > 0) {
                    displayLanguages.add(localeHelper.getDisplayLanguage(language));
                }
            }

            languagePref.setEntryValues(getEntryValues(valuesLanguages));
            languagePref.setEntries(getEntryValues(displayLanguages));

            String languageValue = user.getPreferred_language();
            if (languageValue != null) {
                languagePref.setValue(languageValue);
                String displayLanguage = localeHelper.getDisplayLanguage(languageValue);
                languagePref.setSummary(displayLanguage);
            }
*/
            languagePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    String language = (String) newValue;
                    updatedUser.setPreferred_language(language);
                    //languagePref.setSummary(localeHelper.getDisplayLanguage(language));
                    languagePref.setValue(language);

                    updateDB();

                    localeHelper.updateLanguage(getActivity(), language);
                    //getActivity().recreate();
                    showLanguageDialog(true);
                    return true;
                }
            });
        }

        private void showLanguageDialog(final boolean restartRequired) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.pref_language_attention_title))
                    .setMessage(R.string.pref_language_attention)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (restartRequired) {
                                rebootApp();
                            }
                        }
                    })
                    .show();
        }

        private void showExperimentalDialog(final boolean restartRequired) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.preferences_experimental_title))
                    .setMessage(R.string.preferences_experimental)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (restartRequired) {
                                rebootApp();
                            }
                        }
                    })
                    .show();
        }

        private void rebootApp() {
            Intent mStartActivity = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getActivity().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }


        public void updateToWeb(User obj) {
            String server = dB.getAppSettings().IP_ADDRESS;
            final String userApi = "http://" + server + ":8090/webdatabase-deabee/odata/UserResources(" + obj.getId() + ")";
            // perform HTTP PUT request
            try {
                Log.i("UpdateToWeb", userApi);
                Log.i("UpdateToWeb", obj.toString());
                new HTTPAsyncTask(buidJsonObject(obj)).execute(userApi);
            }
            catch (Exception e){
                Log.e("UpdateToWeb", e.getMessage());
                e.printStackTrace();
            }

        }
        public JSONObject buidJsonObject(User obj) throws JSONException {

            JSONObject map = new JSONObject();
            map.accumulate("Id", obj.getId());
            map.accumulate("Name", obj.getName());
            map.accumulate("Preferred_language", obj.getPreferred_language());
            map.accumulate("Fullname", obj.getFullname());
            map.accumulate("Age", obj.getAge());
            map.accumulate("Gender", obj.getGender());
            map.accumulate("InsulinName", obj.getInsulinName());
            map.accumulate("InsulinCompany", obj.getInsulinCompany());
            map.accumulate("D_type", obj.getD_type());
            map.accumulate("Preferred_unit", obj.getPreferred_unit());
            map.accumulate("Preferred_unit_a1c", obj.getPreferred_unit_a1c());
            map.accumulate("Preferred_unit_weight", obj.getPreferred_unit_weight());
            map.accumulate("Preferred_range", obj.getPreferred_range());
            map.accumulate("Custom_range_min", obj.getCustom_range_min());
            map.accumulate("Custom_range_max", obj.getCustom_range_max());
            map.accumulate("UserTypeResourceId", obj.getUserType());

            return map;
        }

        private void setPutRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            Log.i(MainActivity.class.toString(), jsonObject.toString());
            writer.flush();
            writer.close();
            os.close();
        }
        private String httpPost(String myUrl, JSONObject obj) throws IOException, JSONException {
            URL url = new URL(myUrl);

            // 1. create HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // 2. add JSON content to POST request body
            setPutRequestContent(conn, obj);

            // 3. make POST request to the given URL
            //conn.connect();
            String response = sendAndReadString(conn);
            // 4. return response message
            return response;
        }

        public String sendAndReadString(HttpURLConnection conn) throws IOException{
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response=new StringBuilder();
            for(String line;(line=br.readLine())!=null;)response.append(line+"\n");
            return response.toString();
        }


        public class HTTPAsyncTask extends AsyncTask<String, Void, String> {
            private JSONObject obj;
            public HTTPAsyncTask(JSONObject obj){
                this.obj = obj;
            }
            @Override
            protected String doInBackground(String... urls) {
                // params comes from the execute() call: params[0] is the url.
                try {
                    try {
                        httpPost(urls[0], obj);
                        return "OK";
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return "Error!";
                    }
                } catch (IOException e) {
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {

            }
        }
    }
}
