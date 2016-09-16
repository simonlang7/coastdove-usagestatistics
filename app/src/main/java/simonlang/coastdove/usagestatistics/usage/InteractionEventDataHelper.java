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

import simonlang.coastdove.usagestatistics.usage.sql.AppUsageContract;
import simonlang.coastdove.lib.InteractionEventData;

/**
 * Helper class for InteractionEventData to provide additional import
 * and export methods
 */
public class InteractionEventDataHelper {
    /**
     * Converts an InteractionEventData object into ContentValues for its SQLite representation
     * @param data           InteractionEventData to convert
     * @param dataEntryID    Primary key of the associated ActivityDataEntry
     * @return ContentValues representing the original object
     */
    public static ContentValues toContentValues(InteractionEventData data, long dataEntryID) {
        ContentValues values = new ContentValues();
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_ANDROID_ID, data.getAndroidID());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_TEXT, data.getText());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_DESCRIPTION, data.getDescription());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_CLASS_NAME, data.getClassName());
        values.put(AppUsageContract.InteractionDetailsTable.COLUMN_NAME_DATA_ENTRY_ID, dataEntryID);
        return values;
    }
}
