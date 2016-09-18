/*  Coast Dove
    Copyright (C) 2016  Simon Lang
    Contact: simon.lang7 at gmail dot com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
import simonlang.coastdove.usagestatistics.utility.FileHelper;

/**
 * Listener to collect app usage statistics using data from CoastDove
 */
public class StatisticsListener extends CoastDoveListenerService {
    /** Session of collected usage data for each app, identified by its package name
     * (starts when the app is opened and ends when it's closed) */
    private transient Map<String, AppUsageData> mCurrentAppUsageData;
    /** Meta information for each app, identified by its package name */
    private transient Map<String, AppMetaInformation> mAppMetaInformationMap;

    @Override
    protected void onServiceBound() {
        mCurrentAppUsageData = new HashMap<>();
        mAppMetaInformationMap = new HashMap<>();
    }

    @Override
    protected void onServiceUnbound() {
    }

    @Override
    protected void onAppEnabled(String appPackageName) {
        // Get meta information from file. If it fails, request it from Coast Dove core.
        if (FileHelper.fileExists(this, FileHelper.Directory.PRIVATE_PACKAGE, appPackageName, FileHelper.APP_META_INFORMATION_FILENAME)) {
            AppMetaInformation appMetaInformation = FileHelper.readAppMetaInformation(this, FileHelper.Directory.PRIVATE_PACKAGE, appPackageName,
                    FileHelper.APP_META_INFORMATION_FILENAME);
            if (appMetaInformation != null) {
                mAppMetaInformationMap.put(appPackageName, appMetaInformation);
                return;
            }
        }

        requestMetaInformation(appPackageName);
    }

    @Override
    protected void onAppDisabled(String appPackageName) {
    }

    @Override
    protected void onMetaInformationDelivered(String appPackageName, AppMetaInformation appMetaInformation) {
        mAppMetaInformationMap.put(appPackageName, appMetaInformation);
        FileHelper.writeAppMetaInformation(this, appMetaInformation, FileHelper.Directory.PRIVATE_PACKAGE, appPackageName,
                FileHelper.APP_META_INFORMATION_FILENAME);
    }

    @Override
    protected void onAppOpened() {
        String appPackageName = getLastAppPackageName();
        mCurrentAppUsageData.put(appPackageName, new AppUsageData(appPackageName));
    }

    @Override
    protected void onAppClosed() {
        // Finalize and save to SQLite
        String appPackageName = getLastAppPackageName();
        AppUsageData appUsageData = mCurrentAppUsageData.get(appPackageName);
        appUsageData.finish();

        AppMetaInformation appMetaInformation;
        if (mAppMetaInformationMap.containsKey(appPackageName))
            appMetaInformation = mAppMetaInformationMap.get(appPackageName);
        else
            appMetaInformation = new AppMetaInformation(appPackageName, new LinkedList<String>());
        AppUsageDataProcessor processor = new AppUsageDataProcessor(appMetaInformation, appUsageData);
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
