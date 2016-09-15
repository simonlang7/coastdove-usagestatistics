package simonlang.coastdove.usagestatistics;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import simonlang.coastdove.lib.AppMetaInformation;
import simonlang.coastdove.lib.CoastDoveListenerService;
import simonlang.coastdove.lib.EventType;
import simonlang.coastdove.lib.InteractionEventData;
import simonlang.coastdove.usagestatistics.usage.AppUsageData;
import simonlang.coastdove.usagestatistics.usage.AppUsageDataProcessor;
import simonlang.coastdove.usagestatistics.usage.NotificationEvent;
import simonlang.coastdove.usagestatistics.usage.sql.AppUsageDbHelper;
import simonlang.coastdove.usagestatistics.usage.sql.SQLiteWriter;

/**
 * Listener to collect app usage statistics using data from CoastDove
 */
public class StatisticsListener extends CoastDoveListenerService {
    /** Session of collected usage data for each app, identified by its package name
     * (starts when the app is opened and ends when it's closed) */
    private transient Map<String, AppUsageData> mCurrentAppUsageData;

    @Override
    protected void onServiceBound() {
        mCurrentAppUsageData = new HashMap<>();
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onAppStarted() {
        String appPackageName = getLastAppPackageName();
        mCurrentAppUsageData.put(appPackageName, new AppUsageData(appPackageName));
    }

    @Override
    protected void onAppClosed() {
        // Finalize and save to SQLite
        String appPackageName = getLastAppPackageName();
        AppUsageData appUsageData = mCurrentAppUsageData.get(appPackageName);
        appUsageData.finish();
        // TODO: Replace app meta information stuff
        AppUsageDataProcessor processor = new AppUsageDataProcessor(new AppMetaInformation(appPackageName, new LinkedList<String>()), appUsageData);
        processor.process();
        new Thread(new SQLiteWriter(getApplicationContext(), appUsageData)).start();

        // Remove from map
        mCurrentAppUsageData.remove(appUsageData);
    }

    @Override
    protected void onActivityDetected(String activity) {
        AppUsageData appUsageData = mCurrentAppUsageData.get(getLastAppPackageName());
        boolean log = appUsageData.addActivityData(activity);
        if (log)
            Log.i("Activity", activity);
    }

    @Override
    protected void onLayoutsDetected(Set<String> layouts) {
        AppUsageData appUsageData = mCurrentAppUsageData.get(getLastAppPackageName());
        boolean shallLog = appUsageData.addLayoutDataEntry(getLastActivity(), layouts);
        if (shallLog)
            Log.i("Recognized layouts", layouts.toString());
    }

    @Override
    protected void onInteractionDetected(Collection<InteractionEventData> interaction, EventType eventType) {
        AppUsageData appUsageData = mCurrentAppUsageData.get(getLastAppPackageName());
        boolean shallLog = appUsageData.addInteractionDataEntry(getLastActivity(), interaction, eventType);
        if (shallLog)
            Log.i("Interaction events", interaction.toString());
    }

    @Override
    protected void onNotificationDetected(String notification) {
        String appPackageName = getLastAppPackageName();
        final NotificationEvent notificationEvent = new NotificationEvent(appPackageName, notification);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppUsageDbHelper helper = new AppUsageDbHelper(getApplicationContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.beginTransactionNonExclusive();
                try {
                    notificationEvent.writeToSQLiteDB(db);
                    db.setTransactionSuccessful();
                } catch (RuntimeException e) {
                    Log.e("AppDetectionData", e.getMessage());
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }
        }).start();
    }

    @Override
    protected void onScreenStateDetected(boolean screenOff) {
        AppUsageData appUsageData = mCurrentAppUsageData.get(getLastAppPackageName());
        if (screenOff)
            appUsageData.addScreenOffEntry();
        else
            appUsageData.finishScreenOffEntry();
    }
}
