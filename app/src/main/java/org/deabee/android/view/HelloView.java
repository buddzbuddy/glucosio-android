package org.deabee.android.view;

import android.content.Context;

public interface HelloView {
    void displayErrorWrongAge();
    void displayError(String errorText);
    void displayErrorWrongUsername();

    void startMainView();
    void showSimpleProgressDialog();
    void removeSimpleProgressDialog();
}
