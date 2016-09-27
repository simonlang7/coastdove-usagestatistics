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

package simonlang.coastdove.usagestatistics.usage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.Date;

import simonlang.coastdove.lib.EventType;
import simonlang.coastdove.usagestatistics.usage.sql.AppUsageContract;

/**
 * Entry for when the scroll position changed (which is a different event from
 * actually scrolling - thanks, Android)
 */
public class ScrollPositionDataEntry extends ActivityDataEntry {

    public static ActivityDataEntry fromSQLiteDB(SQLiteDatabase db, Date timestamp, String activity,
                                                 int count, int dataEntryID) {
        String[] projection = {
                AppUsageContract.ScrollDetailsTable.COLUMN_NAME_DATA_ENTRY_ID,
                AppUsageContract.ScrollDetailsTable.COLUMN_NAME_FROM,
                AppUsageContract.ScrollDetailsTable.COLUMN_NAME_TO,
                AppUsageContract.ScrollDetailsTable.COLUMN_NAME_ITEM_COUNT
        };
        String selection = AppUsageContract.ScrollDetailsTable.COLUMN_NAME_DATA_ENTRY_ID + "=?";
        String[] selectionArgs = { ""+dataEntryID };

        Cursor c = db.query(AppUsageContract.ScrollDetailsTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null);
        int from = 0;
        int to = 0;
        int itemCount = 0;
        if (c.moveToFirst()) {
            from = c.getInt(1);
            to = c.getInt(2);
            itemCount = c.getInt(3);
        }

        c.close();

        ScrollPositionDataEntry dataEntry = new ScrollPositionDataEntry(timestamp, activity,
                from, to, itemCount);
        dataEntry.count = count;
        return dataEntry;
    }

    /** From-position in list */
    private int fromPosition;
    /** To-position in list */
    private int toPosition;
    /** total number of items in list */
    private int itemCount;

    /**
     * Creates a new app usage data entry
     *
     * @param timestamp Time at which the data were collected
     * @param activity  Activity detected
     */
    public ScrollPositionDataEntry(Date timestamp, String activity, int fromPosition,
                                   int toPosition, int itemCount) {
        super(timestamp, activity);
        update(fromPosition, toPosition, itemCount);
    }

    @Override
    public long writeToSQLiteDB(SQLiteDatabase db, long activityID) {
        long dataEntryID = super.writeToSQLiteDB(db, activityID);
        ContentValues values = new ContentValues();
        values.put(AppUsageContract.ScrollDetailsTable.COLUMN_NAME_DATA_ENTRY_ID, dataEntryID);
        values.put(AppUsageContract.ScrollDetailsTable.COLUMN_NAME_FROM, fromPosition);
        values.put(AppUsageContract.ScrollDetailsTable.COLUMN_NAME_TO, toPosition);
        values.put(AppUsageContract.ScrollDetailsTable.COLUMN_NAME_ITEM_COUNT, itemCount);

        long rowId = db.insert(AppUsageContract.ScrollDetailsTable.TABLE_NAME, null, values);
        if (rowId == -1)
            throw new SQLiteException("Unable to add row to " + AppUsageContract.ScrollDetailsTable.TABLE_NAME + ": "
                    + values.toString());
        return dataEntryID;
    }

    @Override
    public String getType() {
        return EventType.SCROLL_POSITION.name();
    }

    @Override
    public String getTypePretty() {
        return EventType.SCROLL_POSITION.toString();
    }

    @Override
    public String getContent() {
        return fromPosition + " to " + toPosition + " of " + itemCount;
    }

    public void update(int fromPosition, int toPosition, int itemCount) {
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.itemCount = itemCount;
    }

    public int getFromPosition() {
        return fromPosition;
    }

    public int getToPosition() {
        return toPosition;
    }

    public int getItemCount() {
        return itemCount;
    }

    public boolean sameItemCount(ScrollPositionDataEntry other) {
        return itemCount == other.itemCount;
    }
}
