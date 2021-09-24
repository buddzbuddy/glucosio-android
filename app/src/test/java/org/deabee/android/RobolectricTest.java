package org.deabee.android;

import org.deabee.android.analytics.Analytics;
import org.deabee.android.backup.Backup;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.presenter.HelloPresenter;
import org.deabee.android.tools.LocaleHelper;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Ignore
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 25)
public abstract class RobolectricTest {
    protected Analytics getAnalytics() {
        return getTestApplication().getAnalytics();
    }

    protected Backup getBackup() {
        return getTestApplication().getBackup();
    }

    private TestGlucosioApplication getTestApplication() {
        return (TestGlucosioApplication) RuntimeEnvironment.application;
    }

    protected DatabaseHandler getDBHandler() {
        return getTestApplication().getDBHandler();
    }

    protected HelloPresenter getHelloPresenter() {
        //noinspection ConstantConditions
        return getTestApplication().createHelloPresenter(null);
    }

    protected LocaleHelper getLocaleHelper() {
        //noinspection ConstantConditions
        return getTestApplication().getLocaleHelper();
    }
}
