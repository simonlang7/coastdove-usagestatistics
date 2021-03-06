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

package simonlang.coastdove.usagestatistics.ui.main;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;

import simonlang.coastdove.usagestatistics.usage.sql.AppUsageContract;
import simonlang.coastdove.usagestatistics.usage.sql.AppUsageDbHelper;

/**
 * Loader for a list of apps to which usage data exists
 */
public class AppListLoader extends AsyncTaskLoader<ArrayList<ApplicationInfo>> {
    public AppListLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<ApplicationInfo> loadInBackground() {
        AppUsageDbHelper dbHelper = new AppUsageDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { AppUsageContract.AppTable.COLUMN_NAME_PACKAGE };
        String groupBy = AppUsageContract.AppTable.COLUMN_NAME_PACKAGE;

        Cursor c = db.query(AppUsageContract.AppTable.TABLE_NAME,
                projection, null, null, groupBy, null, null);
        ArrayList<ApplicationInfo> data = new ArrayList<>(c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String appPackageName = c.getString(0);
            if (appPackageName != null) {
                PackageManager packageManager = getContext().getPackageManager();
                try {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(appPackageName, 0);
                    data.add(appInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AppListLoader", "Unable to find app " + appPackageName + ": " + e.getMessage());
                }
            }
            c.moveToNext();
        }
        c.close();
        db.close();

        return data;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
