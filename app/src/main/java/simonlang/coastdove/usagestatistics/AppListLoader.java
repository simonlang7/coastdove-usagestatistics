package simonlang.coastdove.usagestatistics;

import android.content.Context;
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
public class AppListLoader extends AsyncTaskLoader<ArrayList<String>> {
    public AppListLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<String> loadInBackground() {
        AppUsageDbHelper dbHelper = new AppUsageDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { AppUsageContract.AppTable.COLUMN_NAME_PACKAGE };
        String groupBy = AppUsageContract.AppTable.COLUMN_NAME_PACKAGE;

        Cursor c = db.query(AppUsageContract.AppTable.TABLE_NAME,
                projection, null, null, groupBy, null, null);
        ArrayList<String> data = new ArrayList<>(c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String appPackageName = c.getString(0);
            if (appPackageName != null)
                data.add(appPackageName);
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
