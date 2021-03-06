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
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import simonlang.coastdove.lib.EventType;
import simonlang.coastdove.lib.InteractionEventData;
import simonlang.coastdove.lib.ScrollPosition;
import simonlang.coastdove.usagestatistics.usage.sql.AppUsageContract;
import simonlang.coastdove.usagestatistics.utility.Misc;


/**
 * Data collected in any one activity, usually contains several app usage data entries
 * of the types LayoutDataEntry and InteractionDataEntry
 */
public class ActivityData {
    public static ActivityData fromSQLiteDB(SQLiteDatabase db, String appPackageName,
                                            Date timestamp, String activity, int activityID,
                                            int level, long duration) {
        ActivityData result = new ActivityData(appPackageName, timestamp, activity);
        result.duration = duration;
        result.level = level;

        String[] projection = {
                AppUsageContract.DataEntryTable._ID,
                AppUsageContract.DataEntryTable.COLUMN_NAME_TIMESTAMP,
                AppUsageContract.DataEntryTable.COLUMN_NAME_ACTIVITY_ID,
                AppUsageContract.DataEntryTable.COLUMN_NAME_COUNT,
                AppUsageContract.DataEntryTable.COLUMN_NAME_TYPE
        };
        String selection = AppUsageContract.DataEntryTable.COLUMN_NAME_ACTIVITY_ID + "=?";
        String[] selectionArgs = { ""+activityID };
        String sortOrder = AppUsageContract.DataEntryTable.COLUMN_NAME_TIMESTAMP + " ASC";

        Cursor c = db.query(AppUsageContract.DataEntryTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, sortOrder);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            int dataEntryID = c.getInt(0);
            Date entryTimestamp;
            try {
                entryTimestamp = new SimpleDateFormat(Misc.DATE_TIME_FORMAT, Locale.US).parse(c.getString(1));
            } catch (ParseException e) {
                throw new RuntimeException("Cannot parse date: " + c.getString(1));
            }
            int count = c.getInt(3);
            String type = c.getString(4);

            EventType eventType = ActivityDataEntry.entryTypeFromString(type);
            switch (eventType) {
                case CLICK:
                case SCROLLING:
                case LONG_CLICK:
                    result.dataEntries.add(InteractionDataEntry.fromSQLiteDB(db, entryTimestamp, activity,
                            eventType, count, dataEntryID));
                    break;
                case SCREEN_OFF:
                    result.dataEntries.add(ScreenOffEntry.fromSQLiteDB(db, entryTimestamp, activity,
                            count, dataEntryID));
                    break;
                case LAYOUTS:
                    result.dataEntries.add(LayoutDataEntry.fromSQLiteDB(db, entryTimestamp, activity,
                            count, dataEntryID));
                    break;
                case SCROLL_POSITION:
                    Log.d("FROM SQL", "SCROLL POSITION");
                    result.dataEntries.add(ScrollPositionDataEntry.fromSQLiteDB(db, entryTimestamp, activity,
                            count, dataEntryID));
                    break;
            }

            c.moveToNext();
        }
        c.close();

        return result;
    }

    /** Package name of the app */
    private String appPackageName;
    /** Time at which this activity was activated */
    private Date timestamp;
    /** Activity detected */
    private String activity;
    /** List of data entries that were collected during this activity */
    private LinkedList<ActivityDataEntry> dataEntries;
    /** Indicates this activity's level on the stack, above a main activity (those started from a launcher) */
    private int level;
    /** Duration of this activity, in milliseconds */
    private long duration;

    /**
     * Creates a new activity data object
     * @param appPackageName     Package name of the app
     * @param timestamp          Time at which the data were collected
     * @param activity           Activity detected
     */
    public ActivityData(String appPackageName, Date timestamp, String activity) {
        this.appPackageName = appPackageName;
        this.timestamp = timestamp;
        this.activity = activity;
        this.dataEntries = new LinkedList<>();
        this.level = 0;
        start();
    }

    /** Time at which this activity was activated */
    public Date getTimestamp() {
        return timestamp;
    }

    /** Returns the timestamp as a formatted string */
    public String getTimestampString() {
        return new SimpleDateFormat(Misc.DATE_TIME_FORMAT, Locale.US).format(this.timestamp);
    }

    /** Activity detected */
    public String getActivity() {
        return activity;
    }

    /** Activity detected, shortened String (omits everything up to and including the first '/') */
    public String getShortenedActivity() {
        return activity.replaceAll(".*/", "");
    }

    /** List of data entries that were collected during this activity */
    public List<ActivityDataEntry> getDataEntries() {
        return dataEntries;
    }


    /**
     * Writes the contents of this object to an SQLite database
     * @param db       Database to write to
     * @param appID    Primary key of the associated app (AppUsageData)
     */
    public void writeToSQLiteDB(SQLiteDatabase db, long appID) {
        ContentValues values = new ContentValues();
        values.put(AppUsageContract.ActivityTable.COLUMN_NAME_TIMESTAMP, getTimestampString());
        values.put(AppUsageContract.ActivityTable.COLUMN_NAME_APP_ID, appID);
        values.put(AppUsageContract.ActivityTable.COLUMN_NAME_ACTIVITY, this.activity);
        values.put(AppUsageContract.ActivityTable.COLUMN_NAME_LEVEL, this.level);
        values.put(AppUsageContract.ActivityTable.COLUMN_NAME_DURATION, this.duration);

        long rowId = db.insert(AppUsageContract.ActivityTable.TABLE_NAME, null, values);
        if (rowId == -1)
            throw new SQLiteException("Unable to add row to " + AppUsageContract.ActivityTable.TABLE_NAME + ": "
                    + values.toString());

        for (ActivityDataEntry entry : dataEntries)
            entry.writeToSQLiteDB(db, rowId);
    }


    /**
     * Adds a layout data entry
     * @param timestamp          Time at which the data were collected
     * @param activity           Activity detected
     * @param detectedLayouts    Layouts detected
     * @return True if a new data entry was added, false if the previous data entry equals these data
     */
    public boolean addLayoutDataEntry(Date timestamp, String activity, Set<String> detectedLayouts) {
        boolean lastEntryEqual = increasePreviousEntryCountIfEqual(new LayoutDataEntry(null, activity, detectedLayouts));
        if (!lastEntryEqual) {
            ActivityDataEntry entry = new LayoutDataEntry(timestamp, activity, detectedLayouts);
            this.dataEntries.add(entry);
            return true;
        }
        else
            return false;
    }

    /**
     * Adds a screen off entry
     * @param timestamp    Time at which the screen was turned off
     * @param activity     Activity detected
     */
    public void addScreenOffEntry(Date timestamp, String activity) {
        ScreenOffEntry entry = new ScreenOffEntry(timestamp, activity);
        this.dataEntries.add(entry);
    }

    /**
     * Finished a screen off entry (i.e. stops the time measured)
     */
    public void finishScreenOffEntry() {
        ScreenOffEntry lastEntry = (ScreenOffEntry)findLastEntryOfType(ScreenOffEntry.class);
        if (lastEntry != null)
            lastEntry.finish();
        else
            Log.e("ActivityData", "Unable to find ScreenOffEntry. This should not happen and may yield strange results.");
    }

    /**
     * Adds an interaction data entry
     * @param timestamp        Time at which the data were collected
     * @param activity         Activity detected
     * @param detectedInteraction    Interaction detected
     * @return True if a new data entry was added, false if the previous data entry equals these data
     */
    public boolean addInteractionDataEntry(Date timestamp, String activity, Collection<InteractionEventData> detectedInteraction,
                                           EventType type) {
        boolean lastEntryEqual = increasePreviousEntryCountIfEqual(new InteractionDataEntry(null, activity, detectedInteraction,
                type));
        if (!lastEntryEqual) {
            ActivityDataEntry entry = new InteractionDataEntry(timestamp, activity, detectedInteraction, type);
            this.dataEntries.add(entry);
            return true;
        }
        else
            return false;
    }

    public boolean addScrollPositionDataEntry(Date timestamp, String activity, ScrollPosition scrollPosition) {
        ScrollPositionDataEntry last = findLastAdjacentScrollPositionEntry();
        if (last != null && last.getItemCount() == scrollPosition.getItemCount()) {
            last.update(scrollPosition.getFromIndex(), scrollPosition.getToIndex(), scrollPosition.getItemCount());
            return false;
        }
        else {
            ScrollPositionDataEntry entry = new ScrollPositionDataEntry(timestamp, activity, scrollPosition.getFromIndex(),
                    scrollPosition.getToIndex(), scrollPosition.getItemCount());
            this.dataEntries.add(entry);
            return true;
        }
    }

    /**
     * Returns an array with each data entry data converted to a String
     * @return An array of all data entries converted to Strings
     */
    public String[] toStrings(int padding) {
        String[] result = new String[this.dataEntries.size() + 1];
        result[0] = toString(padding);
        int i = 1;
        for (ActivityDataEntry entry : this.dataEntries) {
            result[i++] = entry.toString(padding);
        }
        return result;
    }

    /**
     * Stopwatch-like function to start measuring duration for this activity
     */
    public void start() {
        this.duration = System.currentTimeMillis();
    }

    /**
     * Stopwatch-like function to stop measuring duration for this activity
     */
    public void finish() {
        this.duration = System.currentTimeMillis() - this.duration;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int padding) {
        String timestamp = getTimestampString();
        String paddingString = " ";
        for (int i = 0; i < padding; ++i)
            paddingString += " ";
        return timestamp + paddingString + "Activity: " + getShortenedActivity();
    }

    /** Returns a unique ID for this data entry */
    public long id() {
        return timestamp.getTime();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getDuration() {
        return duration;
    }

    /**
     * If the previous layout entry of the is equal to the given one, that entry's
     * count is increased. If the given entry and the last processed entry are both
     * click entries, and they're equal, the last processed entry's count is increased.
     * @param other    Entry to compare the previous same-type entry with
     * @return True if the entries are equal, false otherwise
     */
    private boolean increasePreviousEntryCountIfEqual(ActivityDataEntry other) {
        ActivityDataEntry previousEntry = null;
        if (other instanceof InteractionDataEntry) {
            ActivityDataEntry last = this.dataEntries.peekLast();
            if (last != null && last instanceof InteractionDataEntry)
                previousEntry = last;
        }
        else if (other instanceof LayoutDataEntry) {
            previousEntry = findLastEntryOfType(LayoutDataEntry.class);
        }

        if (previousEntry != null && previousEntry.equals(other)) {
            previousEntry.increaseCount();
            return true;
        }
        else
            return false;
    }

    /**
     * Returns the last layout entry found, or null if none is found
     */
    private ActivityDataEntry findLastEntryOfType(Class<?> classType) {
        Iterator<ActivityDataEntry> it = this.dataEntries.descendingIterator();
        while (it.hasNext()) {
            ActivityDataEntry entry = it.next();
            if (entry.getClass().equals(classType)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * When scrolling, we only want to get the last position (when scrolling stops),
     * otherwise we get spammed with entries
     */
    private ScrollPositionDataEntry findLastAdjacentScrollPositionEntry() {
        Iterator<ActivityDataEntry> it = this.dataEntries.descendingIterator();
        while (it.hasNext()) {
            ActivityDataEntry entry = it.next();
            EventType type = EventType.valueOf(entry.getType());
            if (type == EventType.CLICK || type == EventType.LONG_CLICK || type == EventType.SCREEN_OFF)
                return null;
            else if (entry instanceof ScrollPositionDataEntry) {
                return (ScrollPositionDataEntry)entry;
            }
        }
        return null;
    }
}
