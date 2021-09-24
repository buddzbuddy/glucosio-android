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

package org.deabee.android;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import org.deabee.android.activity.A1cCalculatorActivity;
import org.deabee.android.activity.HelloActivity;
import org.deabee.android.analytics.Analytics;
import org.deabee.android.analytics.GlucosioGoogleAnalytics;
import org.deabee.android.backup.Backup;
import org.deabee.android.backup.GoogleDriveBackup;
import org.deabee.android.db.AppSettings;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.User;
import org.deabee.android.presenter.A1CCalculatorPresenter;
import org.deabee.android.presenter.HelloPresenter;
import org.deabee.android.tools.LocaleHelper;
import org.deabee.android.tools.Preferences;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GlucosioApplication extends Application {

    private static GlucosioApplication sInstance;

    @Nullable
    private Analytics analytics;

    @Nullable
    private LocaleHelper localeHelper;

    @Nullable
    private Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initFont();
        initLanguage();
        //callAsynchronousTask();
        loadActiveServer();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadActiveServer(){
        new AsyncTask<Void, Void, String>(){
            protected String doInBackground(Void[] params) {
                String response="";
                try {
                    String jsonURL = "https://amcrud-69b6c.firebaseio.com/active_server.json";
                    HttpRequest req = new HttpRequest(jsonURL);
                    response = req.prepare(HttpRequest.Method.GET).sendAndReadString();
                } catch (Exception e) {
                    response=e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted(result);
            }
        }.execute();
    }
    public void onTaskCompleted(String response) {
        DatabaseHandler dB = getDBHandler();
        String IP_ADDRESS = response.replace('"', ' ').trim();
        AppSettings appSettings = dB.getAppSettings();
        if(appSettings != null) {
            dB.updateAppSettings(new AppSettings(IP_ADDRESS, 8090));
            Log.i("IP_ADDRESS", "Updated to " + IP_ADDRESS);
        }
        else {
            dB.addAppSettings(new AppSettings(IP_ADDRESS, 8090));
            Log.i("IP_ADDRESS", "Created to " + IP_ADDRESS);
        }
    }

    @VisibleForTesting
    protected void initFont() {
        //TODO: convert of using new introduced class Preferences
        // Get Dyslexia preference and adjust font
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDyslexicModeOn = sharedPref.getBoolean("pref_font_dyslexia", false);

        if (isDyslexicModeOn) {
            setFont("fonts/opendyslexic.otf");
        } else {
            setFont("fonts/lato.ttf");
        }
    }

    @VisibleForTesting
    protected void initLanguage() {
        User user = getDBHandler().getCurrentUser();//.getUser(1);
        if (user != null) {
            checkBadLocale(user);

            String languageTag = user.getPreferred_language();
            if (languageTag != null) {
                getLocaleHelper().updateLanguage(this, languageTag);
            }
        }
    }
    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getLocaleHelper().getDeviceLocale()
        //LocaleManager.setLocale(this);
    }*/
    private void checkBadLocale(User user) {
        Preferences preferences = getPreferences();
        boolean cleanLocaleDone = preferences.isLocaleCleaned();

        if (!cleanLocaleDone) {
            User updatedUser = new User(user);
            updatedUser.setPreferred_language(null);
            //TODO: is it long operation? should we move it to separate thread?
            getDBHandler().updateUser(updatedUser);
            preferences.saveLocaleCleaned();
        }
    }

    private void setFont(String font) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(font)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    @NonNull
    public Backup getBackup() {
        return new GoogleDriveBackup();
    }

    @NonNull
    public Analytics getAnalytics() {
        if (analytics == null) {
            analytics = new GlucosioGoogleAnalytics();
            analytics.init(this);
        }

        return analytics;
    }

    @NonNull
    public DatabaseHandler getDBHandler() {
        return new DatabaseHandler(getApplicationContext());
    }

    @NonNull
    public A1CCalculatorPresenter createA1cCalculatorPresenter(@NonNull final A1cCalculatorActivity activity) {
        return new A1CCalculatorPresenter(activity, getDBHandler());
    }

    @NonNull
    public LocaleHelper getLocaleHelper() {
        if (localeHelper == null) {
            localeHelper = new LocaleHelper();
        }
        return localeHelper;
    }

    @NonNull
    public Preferences getPreferences() {
        if (preferences == null) {
            preferences = new Preferences(this);
        }

        return preferences;
    }

    public static GlucosioApplication getInstance() {
        if (sInstance == null) {
            sInstance = new GlucosioApplication();
        }
        return sInstance;
    }

    @NonNull
    public HelloPresenter createHelloPresenter(@NonNull final HelloActivity activity) {
        return new HelloPresenter(activity, getDBHandler());
    }
}
