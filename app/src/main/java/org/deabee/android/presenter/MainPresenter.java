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

package org.deabee.android.presenter;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.deabee.android.R;
import org.deabee.android.activity.MainActivity;
import org.deabee.android.db.AppSettings;
import org.deabee.android.db.DatabaseHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainPresenter {

    private DatabaseHandler dB;
    private MainActivity _mainActivity;
    public MainPresenter(MainActivity mainActivity, DatabaseHandler databaseHandler) {
        _mainActivity = mainActivity;
        dB = databaseHandler;
        //Log.i("[userInfo]", dB.getCurrentUser().toString());
        if (dB.getCurrentUser() == null) {
            // if user doesn't exists start hello activity
            mainActivity.startHelloActivity();
        } else {
            // If user already exists, update user's preferred language and recreate MainActivity
            mainActivity.getLocaleHelper().updateLanguage(mainActivity,
                                                          dB.getCurrentUser().getPreferred_language());
        }
    }

    public boolean isdbEmpty() {
        return dB.getGlucoseReadings().size() == 0;
    }

}
